package de.george.g3dit.rpc.zmq;

import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.SocketType;
import org.zeromq.ZEvent;
import org.zeromq.ZMQ;
import org.zeromq.ZMonitor.Event;

import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.OneofDescriptor;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.InvalidProtocolBufferException;

import de.george.g3dit.rpc.proto.G3RemoteControlProtos;
import de.george.g3dit.rpc.proto.G3RemoteControlProtos.RequestContainer;
import de.george.g3dit.rpc.proto.G3RemoteControlProtos.ResponseContainer;
import de.george.g3dit.rpc.zmq.ResponseCallback.Status;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import net.jodah.expiringmap.internal.NamedThreadFactory;

public class GothicIpc {
	private static final Logger logger = LoggerFactory.getLogger(GothicIpc.class);

	private static ExecutorService CALLBACK_SERVICE;

	private ExecutorService executor;
	private String address;
	private int port;
	private AtomicInteger requestNumber = new AtomicInteger(0);
	private ConcurrentLinkedQueue<Request> pendingRequests = new ConcurrentLinkedQueue<>();

	public GothicIpc(String address, int port) {
		this.address = address;
		this.port = port;

		synchronized (this) {
			if (CALLBACK_SERVICE == null) {
				CALLBACK_SERVICE = Executors.newCachedThreadPool(new NamedThreadFactory("GothicIpc-Callback-%s"));
			}
		}
	}

	public void start() {
		if (executor != null && !executor.isTerminated()) {
			throw new IllegalStateException("There is already a Ipc worker thread running.");
		}

		pendingRequests.clear();

		executor = Executors.newSingleThreadExecutor();
		executor.submit(new Worker(address, port));
	}

	public void stop() {
		executor.shutdownNow();
	}

	public void stopAndAwaitTermination(long timeout, TimeUnit timeUnit) throws InterruptedException {
		executor.shutdownNow();
		executor.awaitTermination(timeout, timeUnit);
	}

	public int sendRequest(GeneratedMessage request, ResponseCallback responseCallback) {
		return sendRequest(request, responseCallback, null);
	}

	public int sendRequest(GeneratedMessage request, ResponseCallback responseCallback, Object userData) {
		OneofDescriptor requestDescriptors = RequestContainer.getDescriptor().getOneofs().get(0);
		for (int i = 0; i < requestDescriptors.getFieldCount(); i++) {
			FieldDescriptor fieldDescriptor = requestDescriptors.getField(i);
			if (fieldDescriptor.getMessageType() == request.getDescriptorForType()) {
				RequestContainer requestContainer = RequestContainer.newBuilder().setField(fieldDescriptor, request)
						.setRequestNumber(requestNumber.incrementAndGet()).build();
				pendingRequests.add(new Request(requestContainer, responseCallback, userData));
				return requestContainer.getRequestNumber();
			}
		}

		throw new IllegalArgumentException("'" + request.getDescriptorForType().getFullName() + "' is not a valid request.");
	}

	private final class Worker implements Runnable {
		private String address;
		private int port;
		private ZMQ.Context context;
		private ZMQ.Socket requester;

		private Map<Integer, Request> sentRequests = ExpiringMap.builder().expirationPolicy(ExpirationPolicy.ACCESSED)
				.expiration(1500, TimeUnit.MILLISECONDS).expirationListener((rn, rc) -> notifyCallback((Request) rc, Status.Timeout, null))
				.build();

		public Worker(String address, int port) {
			this.address = address;
			this.port = port;
		}

		@Override
		public void run() {
			try {
				context = ZMQ.context(1);
				String monitorAddr = "inproc://monitor";
				ZMQ.Socket monitor = context.socket(SocketType.PAIR);
				monitor.setReceiveTimeOut(0);
				monitor.connect(monitorAddr);

				String gameAddr = "tcp://" + address + ":" + port;
				requester = context.socket(SocketType.DEALER);
				requester.setSendTimeOut(0);
				requester.setReceiveTimeOut(0);
				requester.monitor(monitorAddr, ZMQ.EVENT_CONNECTED | ZMQ.EVENT_DISCONNECTED);
				requester.connect(gameAddr);

				boolean connected = false;
				while (!Thread.currentThread().isInterrupted()) {
					while (true) {
						var monitorEvent = ZEvent.recv(monitor);
						if (monitorEvent == null)
							break;

						if (monitorEvent.getEvent() == Event.DISCONNECTED) {
							connected = false;
							continue;
						}
						try {
							SocketChannel socket = monitorEvent.getValue();
							if (socket.getLocalAddress() == null || socket.getRemoteAddress() == null)
								continue;

							// Depending on the ephemeral port range it might happen that the remote
							// port we try to connect to is assigned as the ephemeral local port.
							// That results in us connecting to ourselves, and then requests we send
							// are immediately delivered to us as responses.
							if (socket.getLocalAddress().equals(socket.getRemoteAddress())) {
								logger.warn("Detected accidental self connect to due remote port being assigned as ephemeral local port.");
								// Reconnect in case we accidentally connected to ourselves.
								requester.disconnect(gameAddr);
								requester.connect(gameAddr);
							} else
								connected = true;
						} catch (Exception e) {
							logger.warn("Error while inspecting connected socket.", e);
						}
					}

					// Nachrichten empfangen
					List<ResponseContainer> responses = getResponses();
					for (ResponseContainer response : responses) {
						Request request = sentRequests.remove(response.getRequestNumber());
						if (request != null) {
							notifyCallback(request, Status.Successful, response);
						} else {
							logger.debug("Received unexpected message with request number: {}", response.getRequestNumber());
						}
					}

					// Nachrichten versenden
					while (pendingRequests.peek() != null) {
						Request request = pendingRequests.poll();
						if (connected && requester.send(request.getContainer().toByteArray())) {
							sentRequests.put(request.getContainer().getRequestNumber(), request);
						} else {
							notifyCallback(request, Status.Timeout, null);
						}
					}

					try {
						TimeUnit.MILLISECONDS.sleep(50);
					} catch (InterruptedException e) {
						// Sleep in Event Loop wurde unterbrochen
					}
				}
			} finally {
				if (requester != null) {
					requester.close();
				}

				if (context != null) {
					context.term();
				}
			}
		}

		private List<ResponseContainer> getResponses() {
			List<ResponseContainer> responses = null;
			byte[] data;
			while ((data = requester.recv()) != null) {
				try {
					ResponseContainer response = G3RemoteControlProtos.ResponseContainer.parseFrom(data);
					if (responses == null) {
						responses = new LinkedList<>();
					}
					responses.add(response);
				} catch (InvalidProtocolBufferException e) {
					logger.warn("Received invalid protocol buffer, skipping it.", e);
				}
			}

			return responses != null ? responses : Collections.emptyList();
		}

		private void notifyCallback(Request request, ResponseCallback.Status status, ResponseContainer response) {
			if (request.getResponseCallback() != null) {
				CALLBACK_SERVICE.submit(() -> request.getResponseCallback().notify(status, response, request.getUserData()));
			}
		}
	}

	private static final class Request {
		private RequestContainer container;
		private ResponseCallback responseCallback;
		private Object userData;

		private Request(RequestContainer container, ResponseCallback responseCallback, Object userData) {
			this.container = container;
			this.responseCallback = responseCallback;
			this.userData = userData;
		}

		public RequestContainer getContainer() {
			return container;
		}

		public ResponseCallback getResponseCallback() {
			return responseCallback;
		}

		public Object getUserData() {
			return userData;
		}
	}
}

package de.george.g3dit.rpc;

import java.util.concurrent.atomic.AtomicInteger;

import com.google.protobuf.GeneratedMessage;

import de.george.g3dit.rpc.zmq.ResponseCallback;

public class MonotonicallyOrderedIpc {
	private AtomicInteger idCounter = new AtomicInteger(0);
	private AtomicInteger lastReceived = new AtomicInteger(0);

	public void sendRequest(GeneratedMessage request, ResponseCallback responseCallback) {
		IpcHelper.getIpc().sendRequest(request, (s, rc, ud) -> {
			int id = (int) ud;
			if (id > lastReceived.getAndUpdate(prev -> Math.max(id, prev))) {
				responseCallback.notify(s, rc, null);
			}
		}, idCounter.incrementAndGet());
	}

	public void sendRequest(GeneratedMessage request, ResponseCallback responseCallback, Object userData) {
		IpcHelper.getIpc().sendRequest(request, (s, rc, ud) -> {
			Object[] data = (Object[]) ud;
			int id = (int) data[0];
			if (id > lastReceived.getAndUpdate(prev -> Math.max(id, prev))) {
				responseCallback.notify(s, rc, data[1]);
			}
		}, new Object[] {idCounter.incrementAndGet(), userData});
	}
}

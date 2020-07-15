package de.george.g3dit;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import javax.swing.SwingUtilities;

import de.george.g3dit.rpc.MonotonicallyOrderedIpc;
import de.george.g3dit.rpc.proto.G3RemoteControlProtos.HearbeatRequest;
import de.george.g3dit.rpc.proto.G3RemoteControlProtos.HearbeatResponse;
import de.george.g3dit.rpc.zmq.ResponseCallback.Status;
import de.george.g3dit.util.event.HolderEventList;

public class IpcMonitor {
	private MonotonicallyOrderedIpc ipcCheck = new MonotonicallyOrderedIpc();
	private ScheduledExecutorService ipcCheckExecutor = Executors.newSingleThreadScheduledExecutor();

	private HolderEventList<IpcMonitor> availabilityListeners = new HolderEventList<>();
	private HolderEventList<IpcMonitor> statusListeners = new HolderEventList<>();
	private boolean available = false;
	private HearbeatResponse.Status status = null;

	public IpcMonitor() {
		ipcCheckExecutor.scheduleAtFixedRate(() -> {
			ipcCheck.sendRequest(HearbeatRequest.getDefaultInstance(), (s, rc, ud) -> {
				SwingUtilities.invokeLater(() -> {
					boolean receivedResponse = s == Status.Successful;

					HearbeatResponse.Status newStatus = receivedResponse ? rc.getHeartbeatResponse().getStatus() : null;
					boolean statusChanged = !Objects.equals(status, newStatus);
					status = newStatus;

					if (receivedResponse) {
						if (!available) {
							available = true;
							availabilityListeners.notify(IpcMonitor.this);
						}
					} else if (available) {
						available = false;
						availabilityListeners.notify(IpcMonitor.this);
					}

					if (statusChanged) {
						statusListeners.notify(IpcMonitor.this);
					}
				});
			});
		}, 0, 250, TimeUnit.MILLISECONDS);
	}

	public boolean isAvailable() {
		return available;
	}

	public Optional<HearbeatResponse.Status> getStatus() {
		return Optional.ofNullable(status);
	}

	/**
	 * @param holder
	 * @param listener
	 * @param available Aufruf wenn sich {@link #isAvailable()}
	 * @param status Aufruf wenn sich {@link #getStatus()}
	 * @param initialCall Einmaliger initialier Aufruf direkt nach der Registrierung
	 */
	public final void addListener(Object holder, Consumer<IpcMonitor> listener, boolean available, boolean status, boolean initialCall) {
		if (available) {
			availabilityListeners.addListener(holder, listener);
		}

		if (status) {
			statusListeners.addListener(holder, listener);
		}

		if (initialCall && (available || status)) {
			listener.accept(this);
		}
	}

	public final void removeListener(Object holder, Consumer<IpcMonitor> listener, boolean available, boolean status) {
		if (available) {
			availabilityListeners.removeListener(holder, listener);
		}

		if (status) {
			statusListeners.removeListener(holder, listener);
		}
	}

	public final void removeListeners(Object holder, boolean available, boolean status) {
		if (available) {
			availabilityListeners.removeListeners(holder);
		}

		if (status) {
			statusListeners.removeListeners(holder);
		}
	}
}

package de.george.g3dit.rpc.zmq;

import de.george.g3dit.rpc.proto.G3RemoteControlProtos.ResponseContainer;

@FunctionalInterface
public interface ResponseCallback {
	public void notify(Status status, ResponseContainer container, Object userData);

	public enum Status {
		Successful,
		Timeout
	}
}

package de.george.g3dit.rpc;

import de.george.g3dit.rpc.zmq.GothicIpc;

public class IpcHelper {
	private static final class Holder {
		public static final GothicIpc IPC;
		static {
			IPC = new GothicIpc("localhost", 5555);
			IPC.start();
			Runtime.getRuntime().addShutdownHook(new Thread(() -> IPC.stop()));
		}
	}

	public static GothicIpc getIpc() {
		return Holder.IPC;
	}
}

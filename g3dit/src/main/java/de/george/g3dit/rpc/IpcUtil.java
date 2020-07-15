package de.george.g3dit.rpc;

import de.george.g3dit.rpc.proto.DTC;
import de.george.g3dit.rpc.proto.G3RemoteControlProtos.GotoRequest;
import de.george.g3utils.structure.GuidUtil;
import de.george.g3utils.structure.bCVector;

public class IpcUtil {
	public static void gotoPosition(bCVector position) {
		gotoPosition(position, false);
	}

	public static void gotoPosition(bCVector position, boolean putToGround) {
		GotoRequest request = GotoRequest.newBuilder().setPosition(DTC.convert(position)).setPutToGround(putToGround).build();
		IpcHelper.getIpc().sendRequest(request, null, null);
	}

	public static void gotoGuid(String guid) {
		GotoRequest request = GotoRequest.newBuilder().setGuid(GuidUtil.hexToGuidText(GuidUtil.parseGuid(guid))).build();
		IpcHelper.getIpc().sendRequest(request, null, null);
	}
}

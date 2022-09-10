package de.george.lrentnode.classes;

import java.util.List;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.io.G3Serializable;

public class eCPhysicsScene_PS extends G3Class {

	public static class eCCollisionPairNotification implements G3Serializable {
		public boolean nxIgnorePair;
		public boolean nxNotifyOnStartTouch;
		public boolean nxNotifyOnEndTouch;
		public boolean nxNotifyOnTouch;
		public boolean nxNotifyOnImpact;
		public boolean nxNotifyOnRoll;
		public boolean nxNotifyOnSlide;

		@Override
		public void read(G3FileReader reader) {
			nxIgnorePair = reader.readBool();
			nxNotifyOnStartTouch = reader.readBool();
			nxNotifyOnEndTouch = reader.readBool();
			nxNotifyOnTouch = reader.readBool();
			nxNotifyOnImpact = reader.readBool();
			nxNotifyOnRoll = reader.readBool();
			nxNotifyOnSlide = reader.readBool();
		}

		@Override
		public void write(G3FileWriter writer) {
			writer.writeBool(nxIgnorePair);
			writer.writeBool(nxNotifyOnStartTouch);
			writer.writeBool(nxNotifyOnEndTouch);
			writer.writeBool(nxNotifyOnTouch);
			writer.writeBool(nxNotifyOnImpact);
			writer.writeBool(nxNotifyOnRoll);
			writer.writeBool(nxNotifyOnSlide);
		}
	};

	private List<eCCollisionPairNotification[]> collisionGroupNotifications;
	private List<boolean[]> groupCollides;

	public eCPhysicsScene_PS(String className, G3FileReader reader) {
		super(className, reader);
	}

	@Override
	protected void readPostClassVersion(G3FileReader reader) {
		collisionGroupNotifications = reader.readPrefixedList(r -> r.readPrefixedArray(eCCollisionPairNotification.class));
		groupCollides = reader.readPrefixedList(G3FileReader::readPrefixedBoolArray);
	}

	@Override
	protected void writePostClassVersion(G3FileWriter writer) {
		writer.writePrefixedList(collisionGroupNotifications, G3FileWriter::writePrefixedArray);
		writer.writePrefixedList(groupCollides, G3FileWriter::writePrefixedBoolArray);
	}
}

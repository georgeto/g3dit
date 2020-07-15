package de.george.lrentnode.classes;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;

public class eCEntityPropertySet extends G3Class {
	private boolean unkFlag;

	public eCEntityPropertySet(String className, G3FileReader reader) {
		super(className, reader);
	}

	@Override
	protected void readPostClassVersion(G3FileReader reader) {
		int version = reader.readUnsignedShort();
		if (version <= 1) {
			unkFlag = true;
		} else {
			unkFlag = reader.readBool();
		}
	}

	@Override
	protected void writePostClassVersion(G3FileWriter writer) {
		writer.writeUnsignedShort(2).writeBool(unkFlag);
	}
}

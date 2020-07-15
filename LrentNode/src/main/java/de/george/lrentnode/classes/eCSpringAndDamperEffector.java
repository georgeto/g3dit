package de.george.lrentnode.classes;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;

public class eCSpringAndDamperEffector extends G3Class {
	public eCSpringAndDamperEffector(String className, G3FileReader reader) {
		super(className, reader);
	}

	@Override
	protected void readPreClassVersion(G3FileReader reader) {
		reader.skip(4); // 3E 00 27 00
	}

	@Override
	protected void writePreClassVersion(G3FileWriter writer) {
		writer.writeUnsignedShort(0x3E).writeUnsignedShort(0x27);
	}

}

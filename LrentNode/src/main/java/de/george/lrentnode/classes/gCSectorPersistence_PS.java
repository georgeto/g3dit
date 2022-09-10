package de.george.lrentnode.classes;

import java.util.List;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.io.G3Serializable;

public class gCSectorPersistence_PS extends G3Class {
	public static class Sector implements G3Serializable {
		public String name;
		public boolean enabled;

		@Override
		public void read(G3FileReader reader) {
			reader.readUnsignedShort();
			name = reader.readEntry();
			enabled = reader.readBool();
		}

		@Override
		public void write(G3FileWriter writer) {
			writer.writeUnsignedShort(1);
			writer.writeEntry(name);
			writer.writeBool(enabled);
		}
	}

	public List<Sector> sectors;

	public gCSectorPersistence_PS(String className, G3FileReader reader) {
		super(className, reader);
	}

	@Override
	protected void readPostClassVersion(G3FileReader reader) {
		sectors = reader.readPrefixedList(Sector.class);
	}

	@Override
	protected void writePostClassVersion(G3FileWriter writer) {
		writer.writePrefixedList(sectors);
	}
}

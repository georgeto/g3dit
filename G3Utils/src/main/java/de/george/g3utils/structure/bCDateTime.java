package de.george.g3utils.structure;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.io.G3Serializable;

public class bCDateTime implements G3Serializable {
	private int lowDateTime;
	private int highDateTime;

	@Override
	public void read(G3FileReader reader) {
		lowDateTime = reader.readInt();
		highDateTime = reader.readInt();
	}

	@Override
	public void write(G3FileWriter writer) {
		writer.writeInt(lowDateTime).writeInt(highDateTime);
	}
}

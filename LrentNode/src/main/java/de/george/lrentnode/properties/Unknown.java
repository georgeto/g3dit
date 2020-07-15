package de.george.lrentnode.properties;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.io.G3Serializable;

public class Unknown implements G3Serializable {
	private String raw;

	@Override
	public void read(G3FileReader reader) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void read(G3FileReader reader, int size) {
		raw = reader.read(size);
	}

	@Override
	public void write(G3FileWriter writer) {
		writer.write(raw);
	}
}

package de.george.lrentnode.properties;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.io.G3Serializable;

public class gUnsignedShort implements G3Serializable {
	private int _unsignedShort;

	public gUnsignedShort() {
		this(0);
	}

	public gUnsignedShort(int _unsignedShort) {
		this._unsignedShort = _unsignedShort;
	}

	@Override
	public void read(G3FileReader reader) {
		_unsignedShort = reader.readUnsignedShort();
	}

	@Override
	public void write(G3FileWriter writer) {
		writer.writeUnsignedShort(_unsignedShort);
	}

	public int getUnsignedShort() {
		return _unsignedShort;
	}

	public void setUnsignedShort(int _unsignedShort) {
		this._unsignedShort = _unsignedShort;
	}

	@Override
	public String toString() {
		return String.valueOf(_unsignedShort);
	}
}

package de.george.lrentnode.properties;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.io.G3Serializable;

public class gShort implements G3Serializable {
	private short _short;

	public gShort() {
		this((short) 0);
	}

	public gShort(short _short) {
		this._short = _short;
	}

	public void writeInternal(G3FileWriter writer) {
		writer.writeShort(_short);
	}

	@Override
	public void read(G3FileReader reader) {
		_short = reader.readShort();
	}

	@Override
	public void write(G3FileWriter writer) {
		writer.writeShort(_short);
	}

	public short getShort() {
		return _short;
	}

	public void setShort(short _short) {
		this._short = _short;
	}

	@Override
	public String toString() {
		return String.valueOf(_short);
	}
}

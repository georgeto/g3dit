package de.george.lrentnode.properties;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.io.G3Serializable;

// TODO: Real Implementation
public class gChar implements G3Serializable {
	private byte _char;

	public gChar() {
		this((byte) 0);
	}

	public gChar(byte _char) {
		this._char = _char;
	}

	@Override
	public void read(G3FileReader reader) {
		_char = reader.readByte();
	}

	@Override
	public void write(G3FileWriter writer) {
		writer.writeByte(_char);
	}

	public byte getChar() {
		return _char;
	}

	public void setChar(byte _char) {
		this._char = _char;
	}

	@Override
	public String toString() {
		return String.valueOf(_char);
	}
}

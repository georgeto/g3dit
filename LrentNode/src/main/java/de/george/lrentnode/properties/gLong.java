package de.george.lrentnode.properties;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.io.G3Serializable;

// TODO: Real Implentation
public class gLong implements G3Serializable {
	private int _long;

	public gLong() {
		this(0);
	}

	public gLong(int _long) {
		this._long = _long;
	}

	@Override
	public void read(G3FileReader reader) {
		_long = reader.readInt();
	}

	@Override
	public void write(G3FileWriter writer) {
		writer.writeInt(_long);
	}

	public int getLong() {
		return _long;
	}

	public void setLong(int _long) {
		this._long = _long;
	}

	@Override
	public String toString() {
		return String.valueOf(_long);
	}
}

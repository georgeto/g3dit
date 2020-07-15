package de.george.lrentnode.properties;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.io.G3Serializable;

public class gInt implements G3Serializable {
	private int _int;

	public gInt() {
		this(0);
	}

	public gInt(int _int) {
		this._int = _int;
	}

	@Override
	public void read(G3FileReader reader) {
		_int = reader.readInt();
	}

	@Override
	public void write(G3FileWriter writer) {
		writer.writeInt(_int);
	}

	public int getInt() {
		return _int;
	}

	public void setInt(int _int) {
		this._int = _int;
	}

	@Override
	public String toString() {
		return String.valueOf(_int);
	}
}

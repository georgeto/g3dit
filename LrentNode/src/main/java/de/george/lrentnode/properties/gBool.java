package de.george.lrentnode.properties;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.io.G3Serializable;

public class gBool implements G3Serializable {
	private boolean _bool;

	public gBool() {
		this(false);
	}

	public gBool(boolean _bool) {
		this._bool = _bool;
	}

	@Override
	public void read(G3FileReader reader) {
		_bool = reader.readBool();
	}

	@Override
	public void write(G3FileWriter writer) {
		writer.writeBool(_bool);
	}

	public boolean isBool() {
		return _bool;
	}

	public void setBool(boolean _bool) {
		this._bool = _bool;
	}

	@Override
	public String toString() {
		return String.valueOf(_bool);
	}
}

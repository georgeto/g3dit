package de.george.lrentnode.properties;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.io.G3Serializable;

public class gFloat implements G3Serializable {
	private float _float;

	public gFloat() {
		this(0.0f);
	}

	public gFloat(float _float) {
		this._float = _float;
	}

	@Override
	public void read(G3FileReader reader) {
		_float = reader.readFloat();
	}

	@Override
	public void write(G3FileWriter writer) {
		writer.writeFloat(_float);
	}

	public float getFloat() {
		return _float;
	}

	public void setFloat(float _float) {
		this._float = _float;
	}

	@Override
	public String toString() {
		return String.valueOf(_float);
	}
}

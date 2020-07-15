package de.george.lrentnode.structures;

import com.fasterxml.jackson.annotation.JsonProperty;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.io.G3Serializable;

public abstract class eSScaleItem<T> implements G3Serializable {
	private float scale;
	private T value;

	public eSScaleItem(T value) {
		this.scale = 0;
		this.value = value;
	}

	public eSScaleItem(float scale, T value) {
		this.scale = scale;
		this.value = value;
	}

	@JsonProperty
	public float getScale() {
		return scale;
	}

	public void setScale(float scale) {
		this.scale = scale;
	}

	@JsonProperty
	public T getValue() {
		return value;
	}

	public void setValue(T value) {
		this.value = value;
	}

	@Override
	public void read(G3FileReader reader) {
		scale = reader.readFloat();
		value = readValue(reader);
	}

	@Override
	public void write(G3FileWriter writer) {
		writer.writeFloat(scale);
		writeValue(writer, value);
	}

	protected abstract T readValue(G3FileReader reader);

	protected abstract void writeValue(G3FileWriter writer, T value);
}

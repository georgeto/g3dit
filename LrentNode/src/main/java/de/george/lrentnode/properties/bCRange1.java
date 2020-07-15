package de.george.lrentnode.properties;

import com.fasterxml.jackson.annotation.JsonProperty;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.io.G3Serializable;

public class bCRange1 implements G3Serializable {
	private float min, max;

	public bCRange1() {
		this(Float.MAX_VALUE, -Float.MAX_VALUE);
	}

	public bCRange1(float min, float max) {
		this.min = min;
		this.max = max;
	}

	@JsonProperty
	public float getMin() {
		return min;
	}

	public void setMin(float min) {
		this.min = min;
	}

	@JsonProperty
	public float getMax() {
		return max;
	}

	public void setMax(float max) {
		this.max = max;
	}

	@Override
	public void read(G3FileReader reader) {
		min = reader.readFloat();
		max = reader.readFloat();
	}

	@Override
	public void write(G3FileWriter writer) {
		writer.writeFloat(min);
		writer.writeFloat(max);
	}

	@Override
	public String toString() {
		return "min=" + min + ", max=" + max;
	}
}

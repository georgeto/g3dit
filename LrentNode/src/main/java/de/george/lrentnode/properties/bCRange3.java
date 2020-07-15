package de.george.lrentnode.properties;

import com.fasterxml.jackson.annotation.JsonProperty;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.io.G3Serializable;
import de.george.g3utils.structure.bCVector;

public class bCRange3 implements G3Serializable {
	private bCVector min;
	private bCVector max;

	public bCRange3() {
		this(bCVector.posInfinity(), bCVector.negInfinity());
	}

	public bCRange3(bCVector min, bCVector max) {
		this.min = min;
		this.max = max;
	}

	@JsonProperty
	public bCVector getMin() {
		return min;
	}

	public void setMin(bCVector min) {
		this.min = min;
	}

	@JsonProperty
	public bCVector getMax() {
		return max;
	}

	public void setMax(bCVector max) {
		this.max = max;
	}

	@Override
	public void read(G3FileReader reader) {
		min = reader.readVector();
		max = reader.readVector();
	}

	@Override
	public void write(G3FileWriter writer) {
		writer.write(min, max);
	}

	@Override
	public String toString() {
		return "min=" + min + ", max=" + max;
	}
}

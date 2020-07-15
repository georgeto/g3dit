package de.george.g3utils.structure;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.io.G3Serializable;

public class bCSphere implements G3Serializable {
	private bCVector position;
	private float radius;

	public bCSphere() {
		invalidate();
	}

	public bCSphere(float radius, bCVector position) {
		this.radius = radius;
		this.position = position;
	}

	public bCVector getPosition() {
		return position;
	}

	public void setPosition(bCVector position) {
		this.position = position;
	}

	public float getRadius() {
		return radius;
	}

	public void setRadius(float radius) {
		this.radius = radius;
	}

	public void invalidate() {
		position = bCVector.nullVector();
		radius = -Float.MAX_VALUE;
	}

	public boolean isValid() {
		return radius != -Float.MAX_VALUE;
	}

	@Override
	public bCSphere clone() {
		return new bCSphere(radius, position);
	}

	@Override
	public void read(G3FileReader reader) {
		radius = reader.readFloat();
		position = reader.readVector();
	}

	@Override
	public void write(G3FileWriter writer) {
		writer.writeFloat(radius);
		writer.writeVector(position);
	}

	@Override
	public String toString() {
		return "position=" + position + ", radius=" + radius;
	}
}

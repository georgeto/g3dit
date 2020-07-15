package de.george.g3utils.structure;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.io.G3Serializable;

public class bCVector4 implements G3Serializable {
	private float x, y, z, w;

	public bCVector4(float x, float y, float z, float w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}

	public bCVector4(float[] vector) {
		x = vector[0];
		y = vector[1];
		z = vector[2];
		w = vector[3];
	}

	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}

	public float getZ() {
		return z;
	}

	public void setZ(float z) {
		this.z = z;
	}

	public float getW() {
		return w;
	}

	public void setW(float w) {
		this.w = w;
	}

	@Override
	public bCVector4 clone() {
		return new bCVector4(x, y, z, w);
	}

	public float[] toArray() {
		return new float[] {x, y, z, w};
	}

	public void setToArray(float[] vector) {
		x = vector[0];
		y = vector[1];
		z = vector[2];
		w = vector[3];
	}

	public boolean isEqual(bCVector4 vec) {
		return x == vec.x && y == vec.y && z == vec.z && w == vec.w;
	}

	public void multiply(float value) {
		x *= value;
		y *= value;
		z *= value;
		w *= value;
	}

	@Override
	public String toString() {
		return String.format("(%f, %f, %f, %f)", x, y, z, w);
	}

	@Override
	public void read(G3FileReader reader) {
		x = reader.readFloat();
		y = reader.readFloat();
		z = reader.readFloat();
		w = reader.readFloat();
	}

	@Override
	public void write(G3FileWriter writer) {
		writer.writeFloat(x).writeFloat(y).writeFloat(z).writeFloat(w);
	}
}

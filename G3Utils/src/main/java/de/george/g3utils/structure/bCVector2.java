package de.george.g3utils.structure;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.io.G3Serializable;

public class bCVector2 implements G3Serializable {
	private float x, y;

	public bCVector2() {
		this(0, 0);
	}

	public bCVector2(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public bCVector2(float[] vector) {
		x = vector[0];
		y = vector[1];
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

	@Override
	public bCVector2 clone() {
		return new bCVector2(x, y);
	}

	public float[] toArray() {
		return new float[] {x, y};
	}

	public void setToArray(float[] vector) {
		x = vector[0];
		y = vector[1];
	}

	public boolean isEqual(bCVector2 vec) {
		return x == vec.x && y == vec.y;
	}

	public bCVector2 translate(float x, float y) {
		this.x += x;
		this.y += y;
		return this;
	}

	public bCVector2 translate(bCVector2 vec) {
		return translate(vec.getX(), vec.getY());
	}

	public bCVector2 invTranslate(float x, float y) {
		return translate(-x, -y);
	}

	public bCVector2 invTranslate(bCVector2 vec) {
		return translate(-vec.getX(), -vec.getY());
	}

	public bCVector2 scale(float factor) {
		x *= factor;
		y *= factor;
		return this;
	}

	public bCVector2 scale(bCVector2 factor) {
		x *= factor.getX();
		y *= factor.getY();
		return this;
	}

	public bCVector2 invScale(float factor) {
		x /= factor;
		y /= factor;
		return this;
	}

	public bCVector2 getTranslated(float x, float y) {
		return new bCVector2(this.x + x, this.y + y);
	}

	public bCVector2 getTranslated(bCVector2 vec) {
		return getTranslated(vec.getX(), vec.getY());
	}

	public bCVector2 getInvTranslated(float x, float y) {
		return getTranslated(-x, -y);
	}

	public bCVector2 getInvTranslated(bCVector2 vec) {
		return getTranslated(-vec.getX(), -vec.getY());
	}

	public bCVector2 getScaled(float factor) {
		return new bCVector2(x * factor, y * factor);
	}

	public bCVector2 getScaled(bCVector2 factor) {
		return new bCVector2(x * factor.x, y * factor.y);
	}

	public bCVector2 getInvScaled(float factor) {
		return new bCVector2(x / factor, y / factor);
	}

	public bCVector2 getInvScaled(bCVector2 factor) {
		return new bCVector2(x / factor.x, y / factor.y);
	}

	public void absolute() {
		x = Math.abs(x);
		y = Math.abs(y);
	}

	public float getMagnitude() {
		return (float) Math.sqrt(x * x + y * y);
	}

	public float getSquareMagnitude() {
		return x * x + y * y;
	}

	public float length() {
		return getMagnitude();
	}

	public bCVector2 normalize() {
		float length = length();
		x /= length;
		y /= length;
		return this;
	}

	public bCVector2 getNormalized() {
		return clone().normalize();
	}

	public bCVector2 perpendicular() {
		float tmp = x;
		x = y;
		y = -tmp;
		return this;
	}

	public bCVector2 getPerpendicular() {
		return clone().perpendicular();
	}

	public float getDotProduct(bCVector2 other) {
		return y * other.y + x * other.x;
	}

	public bCVector to3D(float y) {
		return new bCVector(x, y, this.y);
	}

	@Override
	public String toString() {
		return String.format("(%f, %f)", x, y);
	}

	@Override
	public void read(G3FileReader reader) {
		x = reader.readFloat();
		y = reader.readFloat();
	}

	@Override
	public void write(G3FileWriter writer) {
		writer.writeFloat(x).writeFloat(y);
	}
}

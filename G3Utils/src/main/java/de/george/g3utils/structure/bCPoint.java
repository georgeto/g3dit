package de.george.g3utils.structure;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.io.G3Serializable;

public class bCPoint implements G3Serializable {
	private int x;
	private int y;

	public bCPoint(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	@Override
	public void read(G3FileReader reader) {
		x = reader.readInt();
		y = reader.readInt();
	}

	@Override
	public void write(G3FileWriter writer) {
		writer.writeInt(x);
		writer.writeInt(y);
	}

	@Override
	public bCPoint clone() {
		return new bCPoint(x, y);
	}

	public boolean equals(bCPoint point) {
		return x == point.x && y == point.y;
	}

	public bCPoint translate(bCPoint point) {
		x += point.x;
		y += point.y;
		return this;
	}

	public bCPoint invTranslate(bCPoint point) {
		x -= point.x;
		y -= point.y;
		return this;
	}

	@Override
	public String toString() {
		return "x=" + x + ", y=" + y;
	}
}

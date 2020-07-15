package de.george.g3utils.structure;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.io.G3Serializable;

public class bCRect implements G3Serializable {
	private bCPoint topLeft;
	private bCPoint bottomRight;

	public bCRect() {
		invalidate();
	}

	public bCRect(bCPoint topLeft, bCPoint bottomRight) {
		this.topLeft = topLeft.clone();
		this.bottomRight = bottomRight.clone();
	}

	public bCPoint getTopLeft() {
		return topLeft;
	}

	public void setTopLeft(bCPoint topLeft) {
		this.topLeft = topLeft;
	}

	public bCPoint getBottomRight() {
		return bottomRight;
	}

	public void setBottomRight(bCPoint bottomRight) {
		this.bottomRight = bottomRight;
	}

	@Override
	public void read(G3FileReader reader) {
		topLeft = reader.read(bCPoint.class);
		bottomRight = reader.read(bCPoint.class);
	}

	@Override
	public void write(G3FileWriter writer) {
		topLeft.write(writer);
		bottomRight.write(writer);
	}

	@Override
	public bCRect clone() {
		return new bCRect(topLeft.clone(), bottomRight.clone());
	}

	public int getLeft() {
		return topLeft.getX();
	}

	public int getRight() {
		return bottomRight.getX();
	}

	public int getTop() {
		return topLeft.getY();
	}

	public int getBottom() {
		return bottomRight.getY();
	}

	public int getWidth() {
		return bottomRight.getX() - topLeft.getX();
	}

	public int getHeight() {
		return bottomRight.getY() - topLeft.getY();
	}

	public boolean contains(bCPoint point) {
		return point.getX() >= topLeft.getX() && point.getY() >= topLeft.getY() && point.getX() < bottomRight.getX()
				&& point.getY() < bottomRight.getY();
	}

	public boolean contains(bCRect rect) {
		return rect.topLeft.getX() >= topLeft.getX() && rect.topLeft.getY() >= topLeft.getY()
				&& rect.bottomRight.getX() <= bottomRight.getX() && rect.bottomRight.getY() <= bottomRight.getY();
	}

	public boolean containsInclusive(bCPoint point) {
		return point.getX() >= topLeft.getX() && point.getY() >= topLeft.getY() && point.getX() <= bottomRight.getX()
				&& point.getY() <= bottomRight.getY();
	}

	public boolean containsExclusive(bCPoint point) {
		return point.getX() > topLeft.getX() && point.getY() > topLeft.getY() && point.getX() < bottomRight.getX()
				&& point.getY() < bottomRight.getY();
	}

	public bCRect translate(bCPoint point) {
		topLeft.translate(point);
		bottomRight.translate(point);
		return this;
	}

	public bCRect invTranslate(bCPoint point) {
		topLeft.invTranslate(point);
		bottomRight.invTranslate(point);
		return this;
	}

	public boolean equal(bCRect rect) {
		return rect.topLeft.getX() == topLeft.getX() && rect.topLeft.getY() == topLeft.getY()
				&& rect.bottomRight.getX() == bottomRight.getX() && rect.bottomRight.getY() == bottomRight.getY();
	}

	public bCRect merge(bCRect rect) {
		topLeft.setX(topLeft.getX() < rect.topLeft.getX() ? topLeft.getX() : rect.topLeft.getX());
		topLeft.setY(topLeft.getY() < rect.topLeft.getY() ? topLeft.getY() : rect.topLeft.getY());
		bottomRight.setX(bottomRight.getX() > rect.bottomRight.getX() ? bottomRight.getX() : rect.bottomRight.getX());
		bottomRight.setY(bottomRight.getY() > rect.bottomRight.getY() ? bottomRight.getY() : rect.bottomRight.getY());
		return this;
	}

	public bCRect merge(bCPoint point) {
		topLeft.setX(topLeft.getX() < point.getX() ? topLeft.getX() : point.getX());
		topLeft.setY(topLeft.getY() < point.getY() ? topLeft.getY() : point.getY());
		bottomRight.setX(bottomRight.getX() > point.getX() ? bottomRight.getX() : point.getX());
		bottomRight.setY(bottomRight.getY() > point.getY() ? bottomRight.getY() : point.getY());
		return this;
	}

	public void invalidate() {
		setTopLeft(new bCPoint(Integer.MAX_VALUE, Integer.MAX_VALUE));
		setBottomRight(new bCPoint(Integer.MIN_VALUE, Integer.MIN_VALUE));
	}

	public boolean isValid() {
		return getLeft() <= getRight() && getTop() <= getBottom();
	}

	@Override
	public String toString() {
		return "topLeft=" + topLeft + ", bottomRight=" + bottomRight;
	}
}

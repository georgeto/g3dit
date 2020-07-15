package de.george.g3utils.structure;

import de.george.g3utils.util.FastMath;

public class bCInfDoubleCylinder {
	private float radiusA;
	private bCVector positionA;
	private float radiusB;
	private bCVector positionB;

	public bCInfDoubleCylinder() {
		invalidate();
	}

	public bCInfDoubleCylinder(float radiusA, bCVector positionA, float radiusB, bCVector positionB) {
		this.radiusA = radiusA;
		this.positionA = positionA;
		this.radiusB = radiusB;
		this.positionB = positionB;
	}

	public void invalidate() {
		radiusA = -Float.MAX_VALUE;
		radiusB = -Float.MAX_VALUE;
	}

	// cull with bool is false
	public boolean contains(bCVector point) {
		// Inside cylinder a
		bCVector2 position2dA = positionA.to2D();
		bCVector2 vecA = point.to2D().invTranslate(position2dA);
		if (radiusA * radiusA > vecA.getSquareMagnitude()) {
			return true;
		}

		// Inside cylinder b
		bCVector2 position2dB = positionB.to2D();
		bCVector2 vecB = point.to2D().invTranslate(position2dB);
		if (radiusB * radiusB > vecB.getSquareMagnitude()) {
			return true;
		}

		bCVector2 vecAB = position2dB.getInvTranslated(position2dA);
		bCVector2 ortho = vecAB.getPerpendicular().normalize();
		// DotProdut(Point - PositionA, Ortho)
		float dotA = point.to2D().invTranslate(position2dA).getDotProduct(ortho);

		// Position of point in relation to A and B
		float side;
		if (vecAB.getX() == 0.0) {
			side = (point.getZ() - ortho.getY() * dotA - positionA.getZ()) / vecAB.getY();
		} else {
			side = (point.getX() - ortho.getX() * dotA - positionA.getX()) / vecAB.getX();
		}

		// Interval [0, 1] is between the two circles, partly overlapping with the two inner half
		// circles
		if (side <= 0.0f || side >= 1.0f) {
			return false;
		}

		return side * (radiusB - radiusA) + radiusA >= FastMath.abs(dotA);
	}

	public boolean intersects(bCInfCylinder other) {
		return other.intersects(this);
	}

	public boolean intersects(bCInfDoubleCylinder other) {
		return other.getCylinderA().intersects(this) || other.getCylinderB().intersects(this);
	}

	public boolean intersects(bCVector vector) {
		return contains(vector);
	}

	public float getRadiusA() {
		return radiusA;
	}

	public bCVector getPositionA() {
		return positionA;
	}

	public bCInfCylinder getCylinderA() {
		return new bCInfCylinder(radiusA, positionA);
	}

	public float getRadiusB() {
		return radiusB;
	}

	public bCVector getPositionB() {
		return positionB;
	}

	public bCInfCylinder getCylinderB() {
		return new bCInfCylinder(radiusB, positionB);
	}
}

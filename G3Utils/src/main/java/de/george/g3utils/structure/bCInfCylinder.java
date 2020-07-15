package de.george.g3utils.structure;

public class bCInfCylinder {
	private float radius;
	private bCVector position;

	public bCInfCylinder() {
		invalidate();
	}

	public bCInfCylinder(float radius, bCVector position) {
		this.radius = radius;
		this.position = position;
	}

	public void invalidate() {
		radius = -Float.MAX_VALUE;
	}

	public boolean contains(bCVector point) {
		return radius * radius > point.getInvTranslated(position).to2D().getSquareMagnitude();
	}

	public boolean intersects(bCVector point) {
		return contains(position);
	}

	public boolean intersects(bCVector otherPosition, float otherRadius) {
		bCVector2 vecThisOther = otherPosition.to2D().invTranslate(position.to2D());
		float combinedRadius = radius + otherRadius;
		return combinedRadius * combinedRadius > vecThisOther.getSquareMagnitude();
	}

	public boolean intersects(bCInfCylinder other) {
		return intersects(other.position, other.radius);
	}

	public boolean intersects(bCInfDoubleCylinder other) {
		return intersects(other.getPositionA(), other.getRadiusA()) || intersects(other.getPositionB(), other.getRadiusB());
	}
}

package de.george.g3utils.structure;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.io.G3Serializable;
import de.george.g3utils.util.FastMath;

public class bCBox implements G3Serializable {
	private bCVector min, max;

	public bCBox() {
		invalidate();
	}

	public bCBox(bCVector min, bCVector max) {
		this.min = min.clone();
		this.max = max.clone();
	}

	public bCBox(bCVector center, float radius) {
		setBox(center, radius);
	}

	public bCVector getMin() {
		return min.clone();
	}

	public bCVector getMax() {
		return max.clone();
	}

	public void setMin(bCVector min) {
		this.min = min;
	}

	public void setMax(bCVector max) {
		this.max = max;
	}

	public void setBox(bCVector center, float radius) {
		min = center.getInvTranslated(radius, radius, radius);
		max = center.getTranslated(radius, radius, radius);
	}

	public bCVector getCenter() {
		return min.getTranslated(max).scale(0.5f);
	}

	public bCVector getExtent() {
		return max.getInvTranslated(min).scale(0.5f);
	}

	public bCVector getVertex(int index) {
		switch (index) {
			case 0:
				return new bCVector(max.getX(), max.getY(), max.getZ());
			case 1:
				return new bCVector(max.getX(), min.getY(), max.getZ());
			case 2:
				return new bCVector(min.getX(), min.getY(), max.getZ());
			case 3:
				return new bCVector(min.getX(), max.getY(), max.getZ());
			case 4:
				return new bCVector(max.getX(), max.getY(), min.getZ());
			case 5:
				return new bCVector(max.getX(), min.getY(), min.getZ());
			case 6:
				return new bCVector(min.getX(), min.getY(), min.getZ());
			case 7:
				return new bCVector(min.getX(), max.getY(), min.getZ());
			default:
				return null;
		}
	}

	@Override
	public bCBox clone() {
		return new bCBox(min, max);
	}

	public bCBox merge(bCVector box) {
		min.setX(Math.min(min.getX(), box.getX()));
		min.setY(Math.min(min.getY(), box.getY()));
		min.setZ(Math.min(min.getZ(), box.getZ()));
		max.setX(Math.max(max.getX(), box.getX()));
		max.setY(Math.max(max.getY(), box.getY()));
		max.setZ(Math.max(max.getZ(), box.getZ()));
		return this;
	}

	public bCBox merge(bCBox box) {
		min.setX(Math.min(min.getX(), box.min.getX()));
		min.setY(Math.min(min.getY(), box.min.getY()));
		min.setZ(Math.min(min.getZ(), box.min.getZ()));
		max.setX(Math.max(max.getX(), box.max.getX()));
		max.setY(Math.max(max.getY(), box.max.getY()));
		max.setZ(Math.max(max.getZ(), box.max.getZ()));
		return this;
	}

	public void transform(bCMatrix matrix) {
		// When matrix is excentric we can not simply use its 3x3 rotation and scale equivalent.
		// Therefore
		// our neat optimization will not work.
		if (matrix.isExcentric()) {
			bCBox box = new bCBox();
			for (int i = 0; i < 8; i++) {
				bCVector vertex = getVertex(i).transform(matrix);
				box.merge(vertex);
			}
			setMin(box.getMin());
			setMax(box.getMax());
		} else {
			/*
			 * http://zeuxcg.org/2010/10/17/aabb-from-obb-with-component-wise-abs/
			 * "Transforming Axis-aligned Bounding Boxes" in Graphics Gems, pp. 548-550. (James
			 * Arvo) It also appears in more modern code in the excellent (I highly recommend) book:
			 * "Real-time Collision Detection, pp. 87" (Christer Ericson)
			 */
			bCVector center = getCenter().transform(matrix);

			bCMatrix3 rotation = matrix.getRotation();
			rotation.absolute();
			bCVector extent = getExtent().transform(rotation);

			setMin(center.getInvTranslated(extent));
			setMax(center.getTranslated(extent));
		}
	}

	public bCBox getMerged(bCBox other) {
		bCBox result = clone();
		result.merge(other);
		return result;
	}

	public bCBox getTransformed(bCMatrix matrix) {
		bCBox result = clone();
		result.transform(matrix);
		return result;
	}

	public boolean isEqual(bCBox box) {
		return min.isEqual(box.min) && max.isEqual(box.max);
	}

	public boolean contains(bCBox box) {
		return min.getX() <= box.min.getX() && min.getY() <= box.min.getY() && min.getZ() <= box.min.getZ() && max.getX() >= box.max.getX()
				&& max.getY() >= box.max.getY() && max.getZ() >= box.max.getZ();
	}

	public float sqrDistance(bCBox other) {
		float result = 0;
		for (int i = 0; i < 3; i++) {
			float amin = min.get(i);
			float amax = max.get(i);
			float bmin = other.min.get(i);
			float bmax = other.max.get(i);

			if (amin > bmax) {
				float delta = bmax - amin;
				result += delta * delta;
			} else if (bmin > amax) {
				float delta = amax - bmin;
				result += delta * delta;
			}
		}
		return result;
	}

	public float distance(bCBox other) {
		return FastMath.sqrt(sqrDistance(other));
	}

	public boolean intersects(bCBox bb) {
		return !(min.getX() > bb.max.getX() || min.getY() > bb.max.getY() || min.getZ() > bb.max.getZ() || bb.min.getX() > max.getX()
				|| bb.min.getY() > max.getY() || bb.min.getZ() > max.getZ());
	}

	public void invalidate() {
		min = bCVector.posInfinity();
		max = bCVector.negInfinity();
	}

	public boolean isValid() {
		return min.getX() != Float.MAX_VALUE && min.getY() != Float.MAX_VALUE && min.getZ() != Float.MAX_VALUE
				&& max.getX() != -Float.MAX_VALUE && max.getY() != -Float.MAX_VALUE && max.getZ() != -Float.MAX_VALUE;
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

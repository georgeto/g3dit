package de.george.g3utils.structure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonProperty;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.io.G3Serializable;
import de.george.g3utils.util.Converter;
import one.util.streamex.StreamEx;

public class bCVector implements G3Serializable, Serializable {
	private float x, y, z;

	public bCVector() {
		this(0.0f, 0.0f, 0.0f);
	}

	public bCVector(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public bCVector(double x, double y, double z) {
		this.x = (float) x;
		this.y = (float) y;
		this.z = (float) z;
	}

	public bCVector(float[] aVector) {
		x = aVector[0];
		y = aVector[1];
		z = aVector[2];
	}

	public bCVector(String aVector) {
		x = Converter.HexLittleToFloat(aVector.substring(0, 8));
		y = Converter.HexLittleToFloat(aVector.substring(8, 16));
		z = Converter.HexLittleToFloat(aVector.substring(16, 24));
	}

	public bCVector(bCVector4 vector) {
		x = vector.getX();
		y = vector.getY();
		z = vector.getZ();
	}

	@JsonProperty
	public float getX() {
		return x;
	}

	@JsonProperty
	public float getY() {
		return y;
	}

	@JsonProperty
	public float getZ() {
		return z;
	}

	public bCVector setX(float x) {
		this.x = x;
		return this;
	}

	public bCVector setY(float y) {
		this.y = y;
		return this;
	}

	public bCVector setZ(float z) {
		this.z = z;
		return this;
	}

	public float get(int i) {
		if (i == 0) {
			return x;
		} else if (i == 1) {
			return y;
		} else {
			return z;
		}
	}

	public float[] toArray() {
		return new float[] {x, y, z};
	}

	public void setToArray(float[] vector) {
		x = vector[0];
		y = vector[1];
		z = vector[2];
	}

	public void setTo(bCVector vector) {
		x = vector.x;
		y = vector.y;
		z = vector.z;
	}

	public static bCVector fromString(String string) throws IllegalArgumentException {
		try {
			String[] split = string.replace("//", "").split("/");
			if (split.length == 3) {
				return new bCVector(Float.parseFloat(split[0]), Float.parseFloat(split[1]), Float.parseFloat(split[2]));
			}
		} catch (NumberFormatException e) {
			// Nothing
		}
		throw new IllegalArgumentException("'" + string + "' is not a valid bCVector.");
	}

	@Override
	public String toString() {
		return x + "/" + y + "/" + z + "//";
	}

	public String toMarvinString() {
		return Math.round(x) + "/" + Math.round(y) + "/" + Math.round(z) + "//";
	}

	public String write() {
		return Converter.FloatToHexLittle(x) + Converter.FloatToHexLittle(y) + Converter.FloatToHexLittle(z);
	}

	public bCVector getRelative(bCVector vector) {
		return new bCVector(vector.getX() - getX(), vector.getY() - getY(), vector.getZ() - getZ());
	}

	public bCVector getRelative(double x, double y, double z) {
		return getRelative((float) x, (float) y, (float) z);
	}

	public bCVector getRelative(float x, float y, float z) {
		return new bCVector(x - getX(), y - getY(), z - getZ());
	}

	public List<bCVector> getRelative(List<bCVector> vectors) {
		List<bCVector> relVectors = new ArrayList<>(vectors.size());
		for (bCVector vector : vectors) {
			relVectors.add(this.getRelative(vector));
		}
		return relVectors;
	}

	public bCVector translate(float x, float y, float z) {
		this.x += x;
		this.y += y;
		this.z += z;
		return this;
	}

	public bCVector translate(bCVector vec) {
		return translate(vec.getX(), vec.getY(), vec.getZ());
	}

	public bCVector invTranslate(float x, float y, float z) {
		return translate(-x, -y, -z);
	}

	public bCVector invTranslate(bCVector vec) {
		return translate(-vec.getX(), -vec.getY(), -vec.getZ());
	}

	public bCVector scale(float factor) {
		x *= factor;
		y *= factor;
		z *= factor;
		return this;
	}

	public bCVector scale(bCVector factor) {
		x *= factor.getX();
		y *= factor.getY();
		z *= factor.getZ();
		return this;
	}

	public bCVector invScale(float factor) {
		x /= factor;
		y /= factor;
		z /= factor;
		return this;
	}

	public bCVector invScale(bCVector factor) {
		x /= factor.getX();
		y /= factor.getY();
		z /= factor.getZ();
		return this;
	}

	public bCVector getTranslated(float x, float y, float z) {
		return new bCVector(this.x + x, this.y + y, this.z + z);
	}

	public bCVector getTranslated(bCVector vec) {
		return getTranslated(vec.getX(), vec.getY(), vec.getZ());
	}

	public bCVector getInvTranslated(float x, float y, float z) {
		return getTranslated(-x, -y, -z);
	}

	public bCVector getInvTranslated(bCVector vec) {
		return getTranslated(-vec.getX(), -vec.getY(), -vec.getZ());
	}

	public bCVector getScaled(float factor) {
		return new bCVector(x * factor, y * factor, z * factor);
	}

	public bCVector getScaled(bCVector factor) {
		return new bCVector(x * factor.x, y * factor.y, z * factor.z);
	}

	public bCVector getInvScaled(float factor) {
		return new bCVector(x / factor, y / factor, z / factor);
	}

	public bCVector getInvScaled(bCVector factor) {
		return new bCVector(x / factor.x, y / factor.y, z / factor.z);
	}

	public bCVector transform(bCMatrix matrix) {
		if (matrix.isExcentric()) {
			float excentrixMod = matrix.getXAxis().getW() * x + matrix.getYAxis().getW() * y + matrix.getZAxis().getW() * z
					+ matrix.getTranslation4().getW();
			if (Math.abs(excentrixMod) >= 0.0000099999997f) {
				float x = matrix.getXAxis().getX() * this.x + matrix.getYAxis().getX() * y + matrix.getZAxis().getX() * z
						+ matrix.getTranslation4().getX();
				float y = matrix.getXAxis().getY() * this.x + matrix.getYAxis().getY() * this.y + matrix.getZAxis().getY() * z
						+ matrix.getTranslation4().getY();
				float z = matrix.getXAxis().getZ() * this.x + matrix.getYAxis().getZ() * this.y + matrix.getZAxis().getZ() * this.z
						+ matrix.getTranslation4().getZ();

				float invExcentrixMod = 1f / excentrixMod;
				this.x = x * invExcentrixMod;
				this.y = y * invExcentrixMod;
				this.z = z * invExcentrixMod;
			} else {
				x = 0;
				y = 0;
				z = 0;
			}
		} else {
			float x = matrix.getXAxis().getX() * this.x + matrix.getYAxis().getX() * y + matrix.getZAxis().getX() * z
					+ matrix.getTranslation4().getX();
			float y = matrix.getXAxis().getY() * this.x + matrix.getYAxis().getY() * this.y + matrix.getZAxis().getY() * z
					+ matrix.getTranslation4().getY();
			float z = matrix.getXAxis().getZ() * this.x + matrix.getYAxis().getZ() * this.y + matrix.getZAxis().getZ() * this.z
					+ matrix.getTranslation4().getZ();

			this.x = x;
			this.y = y;
			this.z = z;
		}

		return this;
	}

	public bCVector getTransformed(bCMatrix matrix) {
		return clone().transform(matrix);
	}

	public bCVector transform(bCMatrix3 matrix) {
		float x = matrix.getXAxis().getX() * this.x + matrix.getYAxis().getX() * y + matrix.getZAxis().getX() * z;
		float y = matrix.getXAxis().getY() * this.x + matrix.getYAxis().getY() * this.y + matrix.getZAxis().getY() * z;
		float z = matrix.getXAxis().getZ() * this.x + matrix.getYAxis().getZ() * this.y + matrix.getZAxis().getZ() * this.z;

		this.x = x;
		this.y = y;
		this.z = z;

		return this;
	}

	public bCVector getTransformed(bCMatrix3 matrix) {
		return clone().transform(matrix);
	}

	public bCVector getCrossProduct(bCVector other) {
		return new bCVector(y * other.z - z * other.y, z * other.x - x * other.z, x * other.y - other.x * y);
	}

	public void absolute() {
		x = Math.abs(x);
		y = Math.abs(y);
		z = Math.abs(z);
	}

	public float getMagnitude() {
		return (float) Math.sqrt(x * x + y * y + z * z);
	}

	public float getSquareMagnitude() {
		return x * x + y * y + z * z;
	}

	public float length() {
		return getMagnitude();
	}

	public bCVector normalize() {
		float length = length();
		x /= length;
		y /= length;
		z /= length;
		return this;
	}

	@Override
	public boolean equals(Object raw) {
		if (raw instanceof bCVector vec)
			return vec.getX() == getX() && vec.getY() == getY() && vec.getZ() == getZ();
		else
			return false;
	}

	public boolean simliar(bCVector vec, float epsilon) {
		return Math.abs(vec.getX() - getX()) < epsilon && Math.abs(vec.getY() - getY()) < epsilon
				&& Math.abs(vec.getZ() - getZ()) < epsilon;
	}

	public static bCVector averageVector(bCVector... vecs) {
		bCVector sumVec = nullVector();
		for (bCVector vec : vecs) {
			sumVec.translate(vec);
		}
		if (vecs.length != 0) {
			sumVec.invScale(vecs.length);
		}
		return sumVec;
	}

	public static bCVector averageVector(Iterable<bCVector> vecs) {
		bCVector sumVec = nullVector();
		int count = 0;
		for (bCVector vec : vecs) {
			sumVec.translate(vec);
			count++;
		}

		if (count != 0) {
			sumVec.invScale(count);
		}

		return sumVec;
	}

	public boolean isEqual(bCVector vec) {
		return x == vec.x && y == vec.y && z == vec.z;
	}

	public bCVector apply(Function<? super Float, ? extends Float> mapper) {
		x = mapper.apply(x);
		y = mapper.apply(y);
		z = mapper.apply(z);

		return this;
	}

	public bCVector getApplied(Function<? super Float, ? extends Float> mapper) {
		return clone().apply(mapper);
	}

	@Override
	public bCVector clone() {
		return new bCVector(x, y, z);
	}

	public bCVector2 to2D() {
		return new bCVector2(x, z);
	}

	public static bCVector minimum(Iterable<bCVector> vecs) {
		Iterator<bCVector> iter = vecs.iterator();
		if (!iter.hasNext()) {
			return posInfinity();
		}

		bCVector min = posInfinity();
		while (iter.hasNext()) {
			bCVector vec = iter.next();
			if (vec.x < min.x) {
				min.setX(vec.x);
			}
			if (vec.y < min.y) {
				min.setY(vec.y);
			}
			if (vec.z < min.z) {
				min.setZ(vec.z);
			}
		}
		return min;
	}

	public static bCVector minimum(bCVector... vecs) {
		return minimum(StreamEx.of(vecs));
	}

	public static bCVector maximum(Iterable<bCVector> vecs) {
		Iterator<bCVector> iter = vecs.iterator();
		if (!iter.hasNext()) {
			return negInfinity();
		}

		bCVector max = negInfinity();
		while (iter.hasNext()) {
			bCVector vec = iter.next();
			if (vec.x > max.x) {
				max.setX(vec.x);
			}
			if (vec.y > max.y) {
				max.setY(vec.y);
			}
			if (vec.z > max.z) {
				max.setZ(vec.z);
			}
		}
		return max;
	}

	public static bCVector maximum(bCVector... vecs) {
		return maximum(StreamEx.of(vecs));
	}

	public static bCVector nullVector() {
		return new bCVector(0, 0, 0);
	}

	public static bCVector posInfinity() {
		return new bCVector(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
	}

	public static bCVector negInfinity() {
		return new bCVector(-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE);
	}

	@Override
	public void read(G3FileReader reader) {
		x = reader.readFloat();
		y = reader.readFloat();
		z = reader.readFloat();
	}

	@Override
	public void write(G3FileWriter writer) {
		writer.writeFloat(x).writeFloat(y).writeFloat(z);
	}
}

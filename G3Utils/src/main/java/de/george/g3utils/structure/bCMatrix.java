package de.george.g3utils.structure;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.io.G3Serializable;

public class bCMatrix implements G3Serializable {
	private static final bCMatrix identity = new bCMatrix(new bCVector4(1, 0, 0, 0), new bCVector4(0, 1, 0, 0), new bCVector4(0, 0, 1, 0),
			new bCVector4(0, 0, 0, 1));

	private bCVector4 xAxis;
	private bCVector4 yAxis;
	private bCVector4 zAxis;
	private bCVector4 translation;

	public bCMatrix() {
		setToIdentity();
	}

	public bCMatrix(float[][] matrix) {
		xAxis = new bCVector4(matrix[0]);
		yAxis = new bCVector4(matrix[1]);
		zAxis = new bCVector4(matrix[2]);
		translation = new bCVector4(matrix[3]);
	}

	public bCMatrix(bCMatrix matrix) {
		setToMatrix(matrix);
	}

	public bCMatrix(bCVector4 xAxis, bCVector4 yAxis, bCVector4 zAxis, bCVector4 translation) {
		this.xAxis = xAxis;
		this.yAxis = yAxis;
		this.zAxis = zAxis;
		this.translation = translation;
	}

	public bCMatrix(bCQuaternion q, bCVector t) {
		this.setToRotation(q);
		modifyTranslation(t);
	}

	public bCMatrix(bCEulerAngles a, bCVector t) {
		this.setToRotation(a);
		modifyTranslation(t);
	}

	public bCMatrix(bCEulerAngles a, bCVector s, bCVector t) {
		this.setToRotation(a);
		this.modifyScaling(s);
		modifyTranslation(t);
	}

	public void setToArray(float[][] matrix) {
		xAxis.setToArray(matrix[0]);
		yAxis.setToArray(matrix[1]);
		zAxis.setToArray(matrix[2]);
		translation.setToArray(matrix[3]);
	}

	public float[][] toArray() {
		return new float[][] {xAxis.toArray(), yAxis.toArray(), zAxis.toArray(), translation.toArray()};
	}

	public bCVector4 getXAxis() {
		return xAxis;
	}

	public void setXAxis(bCVector4 xAxis) {
		this.xAxis = xAxis;
	}

	public bCVector4 getYAxis() {
		return yAxis;
	}

	public void setYAxis(bCVector4 yAxis) {
		this.yAxis = yAxis;
	}

	public bCVector4 getZAxis() {
		return zAxis;
	}

	public void setZAxis(bCVector4 zAxis) {
		this.zAxis = zAxis;
	}

	public bCVector4 getTranslation4() {
		return translation;
	}

	public void setTranslation4(bCVector4 translation) {
		this.translation = translation;
	}

	public bCVector getTranslation() {
		return new bCVector(translation);
	}

	public void modifyTranslation(bCVector translation) {
		this.translation.setX(translation.getX());
		this.translation.setY(translation.getY());
		this.translation.setZ(translation.getZ());
	}

	public void modifyRotation(bCQuaternion rotation) {
		float x = rotation.getX();
		float y = rotation.getY();
		float z = rotation.getZ();
		float w = rotation.getW();
		xAxis.setX(1f - (2 * z * z + 2 * y * y));
		xAxis.setY(2 * x * y + 2 * z * w);
		xAxis.setZ(2 * x * z - 2 * y * w);
		yAxis.setX(2 * x * y - 2 * z * w);
		yAxis.setY(1f - (2 * x * x + 2 * z * z));
		yAxis.setZ(2 * y * z + 2 * x * w);
		zAxis.setX(2 * x * z + 2 * y * w);
		zAxis.setY(2 * y * z - 2 * x * w);
		zAxis.setZ(1 - (2 * x * x + 2 * y * y));
	}

	public void modifyRotation(bCEulerAngles angles) {
		float yaw = angles.getYawRad();
		float pitch = angles.getPitchRad();
		float roll = angles.getRollRad();

		xAxis.setX((float) (Math.cos(roll) * Math.cos(yaw) + Math.sin(roll) * Math.sin(pitch) * Math.sin(yaw)));
		xAxis.setY((float) (Math.sin(roll) * Math.cos(pitch)));
		xAxis.setZ((float) (Math.sin(roll) * Math.sin(pitch) * Math.cos(yaw) - Math.cos(roll) * Math.sin(yaw)));
		yAxis.setX((float) (Math.cos(roll) * Math.sin(pitch) * Math.sin(yaw) - Math.sin(roll) * Math.cos(yaw)));
		yAxis.setY((float) (Math.cos(roll) * Math.cos(pitch)));
		yAxis.setZ((float) (Math.sin(roll) * Math.sin(yaw) + Math.cos(roll) * Math.sin(pitch) * Math.cos(yaw)));
		zAxis.setX((float) (Math.cos(pitch) * Math.sin(yaw)));
		zAxis.setY((float) -Math.sin(pitch));
		zAxis.setZ((float) (Math.cos(pitch) * Math.cos(yaw)));
	}

	public void modifyScaling(float scaling) {
		modifyScaling(new bCVector(scaling, scaling, scaling));
	}

	public void modifyScaling(bCVector scaling) {
		bCMatrix pureRotation = getPureRotation();
		pureRotation.modifyTranslation(getTranslation());
		bCMatrix scaled = getIdentity().clone();
		scaled.xAxis.setX(scaling.getX());
		scaled.yAxis.setY(scaling.getY());
		scaled.zAxis.setZ(scaling.getZ());
		setToProduct(pureRotation, scaled);
	}

	public void setToRotation(bCEulerAngles angles) {
		setToIdentity();
		modifyRotation(angles);
	}

	public void setToRotation(bCQuaternion rotation) {
		setToIdentity();
		modifyRotation(rotation);
	}

	public void setToScaling(float scaling) {
		setToIdentity();
		modifyScaling(scaling);
	}

	public void setToScaling(bCVector scaling) {
		setToIdentity();
		modifyScaling(scaling);
	}

	public void setToTranslation(bCVector translation) {
		setToIdentity();
		modifyTranslation(translation);
	}

	public bCVector getPureScaling() {
		float x = (float) Math.sqrt(xAxis.getX() * xAxis.getX() + xAxis.getY() * xAxis.getY() + xAxis.getZ() * xAxis.getZ());
		float y = (float) Math.sqrt(yAxis.getX() * yAxis.getX() + yAxis.getY() * yAxis.getY() + yAxis.getZ() * yAxis.getZ());
		float z = (float) Math.sqrt(zAxis.getX() * zAxis.getX() + zAxis.getY() * zAxis.getY() + zAxis.getZ() * zAxis.getZ());
		return new bCVector(x, y, z);
	}

	public bCMatrix getPureRotation() {
		bCVector scaling = getPureScaling();
		bCMatrix invScaled = getIdentity().clone();
		invScaled.xAxis.setX(1.0f / scaling.getX());
		invScaled.yAxis.setY(1.0f / scaling.getY());
		invScaled.zAxis.setZ(1.0f / scaling.getZ());
		invScaled.setToProduct(this, invScaled);
		return invScaled;
	}

	public void setToProduct(bCMatrix matrix1, bCMatrix matrix2) {
		float[][] result = new float[4][4];
		float[][] mat1 = matrix1.toArray();
		float[][] mat2 = matrix2.toArray();

		for (int r = 0; r < 4; r++) {
			for (int c = 0; c < 4; c++) {
				result[r][c] = mat1[0][c] * mat2[r][0] + mat1[1][c] * mat2[r][1] + mat1[2][c] * mat2[r][2] + mat1[3][c] * mat2[r][3];
			}
		}

		setToArray(result);
	}

	public void setToIdentity() {
		setToMatrix(getIdentity());
	}

	public void setToMatrix(bCMatrix matrix) {
		xAxis = matrix.xAxis.clone();
		yAxis = matrix.yAxis.clone();
		zAxis = matrix.zAxis.clone();
		translation = matrix.translation.clone();
	}

	public bCMatrix3 getRotation() {
		return new bCMatrix3(new bCVector(xAxis), new bCVector(yAxis), new bCVector(zAxis));
	}

	public void multiply(float value) {
		xAxis.multiply(value);
		yAxis.multiply(value);
		zAxis.multiply(value);
		translation.multiply(value);
	}

	public void multiply(bCMatrix mat) {
		setToProduct(this, mat);
	}

	public bCMatrix getProduct(bCMatrix mat) {
		bCMatrix result = new bCMatrix();
		result.setToProduct(this, mat);
		return result;
	}

	public void invert() {
		float[][] result = new float[4][4];
		float[][] m = toArray();

		float fA0 = m[0][0] * m[1][1] - m[0][1] * m[1][0];
		float fA1 = m[0][0] * m[1][2] - m[0][2] * m[1][0];
		float fA2 = m[0][0] * m[1][3] - m[0][3] * m[1][0];
		float fA3 = m[0][1] * m[1][2] - m[0][2] * m[1][1];
		float fA4 = m[0][1] * m[1][3] - m[0][3] * m[1][1];
		float fA5 = m[0][2] * m[1][3] - m[0][3] * m[1][2];
		float fB0 = m[2][0] * m[3][1] - m[2][1] * m[3][0];
		float fB1 = m[2][0] * m[3][2] - m[2][2] * m[3][0];
		float fB2 = m[2][0] * m[3][3] - m[2][3] * m[3][0];
		float fB3 = m[2][1] * m[3][2] - m[2][2] * m[3][1];
		float fB4 = m[2][1] * m[3][3] - m[2][3] * m[3][1];
		float fB5 = m[2][2] * m[3][3] - m[2][3] * m[3][2];
		float fDet = fA0 * fB5 - fA1 * fB4 + fA2 * fB3 + fA3 * fB2 - fA4 * fB1 + fA5 * fB0;

		if (fDet == 0f) {
			throw new ArithmeticException("This matrix cannot be inverted");
		}

		result[0][0] = +m[1][1] * fB5 - m[1][2] * fB4 + m[1][3] * fB3;
		result[1][0] = -m[1][0] * fB5 + m[1][2] * fB2 - m[1][3] * fB1;
		result[2][0] = +m[1][0] * fB4 - m[1][1] * fB2 + m[1][3] * fB0;
		result[3][0] = -m[1][0] * fB3 + m[1][1] * fB1 - m[1][2] * fB0;
		result[0][1] = -m[0][1] * fB5 + m[0][2] * fB4 - m[0][3] * fB3;
		result[1][1] = +m[0][0] * fB5 - m[0][2] * fB2 + m[0][3] * fB1;
		result[2][1] = -m[0][0] * fB4 + m[0][1] * fB2 - m[0][3] * fB0;
		result[3][1] = +m[0][0] * fB3 - m[0][1] * fB1 + m[0][2] * fB0;
		result[0][2] = +m[3][1] * fA5 - m[3][2] * fA4 + m[3][3] * fA3;
		result[1][2] = -m[3][0] * fA5 + m[3][2] * fA2 - m[3][3] * fA1;
		result[2][2] = +m[3][0] * fA4 - m[3][1] * fA2 + m[3][3] * fA0;
		result[3][2] = -m[3][0] * fA3 + m[3][1] * fA1 - m[3][2] * fA0;
		result[0][3] = -m[2][1] * fA5 + m[2][2] * fA4 - m[2][3] * fA3;
		result[1][3] = +m[2][0] * fA5 - m[2][2] * fA2 + m[2][3] * fA1;
		result[2][3] = -m[2][0] * fA4 + m[2][1] * fA2 - m[2][3] * fA0;
		result[3][3] = +m[2][0] * fA3 - m[2][1] * fA1 + m[2][2] * fA0;

		setToArray(result);
		this.multiply(1.0f / fDet);
	}

	public bCMatrix getInverted() {
		bCMatrix result = clone();
		result.invert();
		return result;
	}

	public boolean isEqual(bCMatrix mat) {
		return xAxis.isEqual(mat.xAxis) && yAxis.isEqual(mat.yAxis) && zAxis.isEqual(mat.zAxis) && translation.isEqual(mat.translation);
	}

	@Override
	public bCMatrix clone() {
		return new bCMatrix(xAxis.clone(), yAxis.clone(), zAxis.clone(), translation.clone());
	}

	public static bCMatrix getIdentity() {
		return identity.clone();
	}

	public boolean isExcentric() {
		return xAxis.getW() != 0f || yAxis.getW() != 0f || zAxis.getW() != 0f || translation.getW() != 1f;
	}

	@Override
	public String toString() {
		return String.format("[%s,\n%s,\n%s,\n%s]", xAxis, yAxis, zAxis, translation);
	}

	@Override
	public void read(G3FileReader reader) {
		xAxis = reader.read(bCVector4.class);
		yAxis = reader.read(bCVector4.class);
		zAxis = reader.read(bCVector4.class);
		translation = reader.read(bCVector4.class);
	}

	@Override
	public void write(G3FileWriter writer) {
		writer.write(xAxis, yAxis, zAxis, translation);
	}
}

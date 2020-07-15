package de.george.g3utils.structure;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.io.G3Serializable;

public class bCMatrix3 implements G3Serializable {
	private static final bCMatrix3 identity = new bCMatrix3(new bCVector(1, 0, 0), new bCVector(0, 1, 0), new bCVector(0, 0, 1));

	private bCVector xAxis;
	private bCVector yAxis;
	private bCVector zAxis;

	public bCMatrix3() {
		setToIdentity();
	}

	public bCMatrix3(float[][] matrix) {
		xAxis = new bCVector(matrix[0]);
		yAxis = new bCVector(matrix[1]);
		zAxis = new bCVector(matrix[2]);
	}

	public bCMatrix3(bCMatrix3 matrix) {
		setToMatrix(matrix);
	}

	public bCMatrix3(bCVector xAxis, bCVector yAxis, bCVector zAxis) {
		this.xAxis = xAxis;
		this.yAxis = yAxis;
		this.zAxis = zAxis;
	}

	public bCMatrix3(bCQuaternion angles) {
		setToIdentity();
		this.setToRotation(angles);
	}

	public bCMatrix3(bCEulerAngles angles) {
		setToIdentity();
		this.setToRotation(angles);
	}

	public void setToArray(float[][] matrix) {
		xAxis.setToArray(matrix[0]);
		yAxis.setToArray(matrix[1]);
		zAxis.setToArray(matrix[2]);
	}

	public float[][] toArray() {
		return new float[][] {xAxis.toArray(), yAxis.toArray(), zAxis.toArray()};
	}

	public bCVector getXAxis() {
		return xAxis;
	}

	public void setXAxis(bCVector xAxis) {
		this.xAxis = xAxis;
	}

	public bCVector getYAxis() {
		return yAxis;
	}

	public void setYAxis(bCVector yAxis) {
		this.yAxis = yAxis;
	}

	public bCVector getZAxis() {
		return zAxis;
	}

	public void setZAxis(bCVector zAxis) {
		this.zAxis = zAxis;
	}

	public void scale(bCVector scaling) {
		bCMatrix3 scaled = getIdentity();
		scaled.setToScaling(scaling);
		setToProduct(this, scaled);
	}

	public void absolute() {
		xAxis.absolute();
		yAxis.absolute();
		zAxis.absolute();
	}

	public void setToRotation(bCEulerAngles angles) {
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

	public void setToRotation(bCQuaternion rotation) {
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

	public void setToScaling(bCVector scaling) {
		setToIdentity();
		xAxis.setX(scaling.getX());
		yAxis.setY(scaling.getY());
		zAxis.setZ(scaling.getZ());
	}

	public void setToProduct(bCMatrix3 matrix1, bCMatrix3 matrix2) {
		float[][] result = new float[3][3];
		float[][] mat1 = matrix1.toArray();
		float[][] mat2 = matrix2.toArray();

		for (int r = 0; r < 3; r++) {
			for (int c = 0; c < 3; c++) {
				result[r][c] = mat1[0][c] * mat2[r][0] + mat1[1][c] * mat2[r][1] + mat1[2][c] * mat2[r][2];
			}
		}

		setToArray(result);
	}

	public void setToIdentity() {
		setToMatrix(getIdentity());
	}

	public void setToMatrix(bCMatrix3 matrix) {
		xAxis = matrix.xAxis.clone();
		yAxis = matrix.yAxis.clone();
		zAxis = matrix.zAxis.clone();
	}

	public bCMatrix3 getPureRotation() {
		bCVector scaling = getPureScaling();
		bCMatrix3 invScaled = getIdentity().clone();
		invScaled.xAxis.setX(1.0f / scaling.getX());
		invScaled.yAxis.setY(1.0f / scaling.getY());
		invScaled.zAxis.setZ(1.0f / scaling.getZ());
		invScaled.setToProduct(this, invScaled);
		return invScaled;
	}

	public bCVector getPureScaling() {
		float x = (float) Math.sqrt(xAxis.getX() * xAxis.getX() + xAxis.getY() * xAxis.getY() + xAxis.getZ() * xAxis.getZ());
		float y = (float) Math.sqrt(yAxis.getX() * yAxis.getX() + yAxis.getY() * yAxis.getY() + yAxis.getZ() * yAxis.getZ());
		float z = (float) Math.sqrt(zAxis.getX() * zAxis.getX() + zAxis.getY() * zAxis.getY() + zAxis.getZ() * zAxis.getZ());
		return new bCVector(x, y, z);
	}

	public void multiply(bCMatrix3 mat) {
		setToProduct(this, mat);
	}

	@Override
	public bCMatrix3 clone() {
		return new bCMatrix3(xAxis.clone(), yAxis.clone(), zAxis.clone());
	}

	public static bCMatrix3 getIdentity() {
		return identity.clone();
	}

	@Override
	public void read(G3FileReader reader) {
		xAxis = reader.readVector();
		yAxis = reader.readVector();
		zAxis = reader.readVector();
	}

	@Override
	public void write(G3FileWriter writer) {
		writer.write(xAxis, yAxis, zAxis);
	}

	@Override
	public String toString() {
		return String.format("[%s,\n%s,\n%s]", xAxis, yAxis, zAxis);
	}
}

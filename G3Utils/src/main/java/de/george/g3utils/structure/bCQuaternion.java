package de.george.g3utils.structure;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.io.G3Serializable;

public class bCQuaternion implements G3Serializable {
	private float x, y, z, w;

	public bCQuaternion(float x, float y, float z, float w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}

	public bCQuaternion(float[] quaternion) {
		x = quaternion[0];
		y = quaternion[1];
		z = quaternion[2];
		w = quaternion[3];
	}

	public bCQuaternion(bCMatrix matrix) {
		this.setToMatrix(matrix);
	}

	public bCQuaternion(bCMatrix3 matrix) {
		this.setToMatrix(matrix);
	}

	public bCQuaternion(bCEulerAngles angles) {
		setToAngles(angles);
	}

	public void multiply(bCQuaternion q) {
		setToProduct(this, q);
	}

	public void setToMatrix(bCMatrix matrix) {
		bCMatrix rotMatrix = matrix.getPureRotation();
		setToAxes(new bCVector(rotMatrix.getXAxis()), new bCVector(rotMatrix.getYAxis()), new bCVector(rotMatrix.getZAxis()));
	}

	public void setToMatrix(bCMatrix3 matrix) {
		bCMatrix3 rotMatrix = matrix.getPureRotation();
		setToAxes(rotMatrix.getXAxis(), rotMatrix.getYAxis(), rotMatrix.getZAxis());
	}

	public void setToAxes(bCVector xAxis, bCVector yAxis, bCVector zAxis) {
		float trace = xAxis.getX() + yAxis.getY() + zAxis.getZ() + 1.0f;
		if (trace <= 0.0000099999997d) {
			if (xAxis.getX() <= yAxis.getY() || xAxis.getX() <= zAxis.getZ()) {
				if (yAxis.getY() <= zAxis.getZ()) {
					float f = 2.0f * (float) Math.sqrt(zAxis.getZ() + 1.0f - xAxis.getX() - yAxis.getY());
					x = (zAxis.getX() + xAxis.getZ()) / f;
					y = (zAxis.getY() + yAxis.getZ()) / f;
					z = 0.25f * f;
					w = (yAxis.getX() - xAxis.getY()) / f;
				} else {
					float f = 2.0f * (float) Math.sqrt(yAxis.getY() + 1.0f - xAxis.getX() - zAxis.getZ());
					x = (yAxis.getX() + xAxis.getY()) / f;
					y = 0.25f * f;
					z = (zAxis.getY() + yAxis.getZ()) / f;
					w = (zAxis.getX() - xAxis.getZ()) / f;
				}
			} else {
				float f = 2.0f * (float) Math.sqrt(xAxis.getX() + 1.0f - yAxis.getY() - zAxis.getZ());
				x = 0.25f * f;
				y = (yAxis.getX() + xAxis.getY()) / f;
				z = (zAxis.getX() + xAxis.getZ()) / f;
				w = (zAxis.getY() - yAxis.getZ()) / f;
			}
		} else {
			float f = 0.5f / (float) Math.sqrt(trace);
			x = (yAxis.getZ() - zAxis.getY()) * f;
			y = (zAxis.getX() - xAxis.getZ()) * f;
			z = (xAxis.getY() - yAxis.getX()) * f;
			w = 0.25f / f;
		}
	}

	public void setToAngles(bCEulerAngles eulerAngles) {
		bCQuaternion qPitch = new bCQuaternion((float) Math.sin(eulerAngles.getPitchRad() / 2), 0, 0,
				(float) Math.cos(eulerAngles.getPitchRad() / 2));
		bCQuaternion qYaw = new bCQuaternion(0, (float) Math.sin(eulerAngles.getYawRad() / 2), 0,
				(float) Math.cos(eulerAngles.getYawRad() / 2));
		bCQuaternion qRoll = new bCQuaternion(0, 0, (float) Math.sin(eulerAngles.getRollRad() / 2),
				(float) Math.cos(eulerAngles.getRollRad() / 2));
		setToProduct(qPitch, qRoll);
		setToProduct(qYaw, this);
	}

	public void setToQuaternion(float x, float y, float z, float w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}

	public void setToProduct(bCQuaternion q1, bCQuaternion q2) {
		setToQuaternion(q2.w * q1.x + q1.w * q2.x + q1.y * q2.z - q1.z * q2.y, q1.y * q2.w + q1.w * q2.y + q1.z * q2.x - q2.z * q1.x,
				q2.z * q1.w + q2.w * q1.z + q1.x * q2.y - q1.y * q2.x, q1.w * q2.w - q1.x * q2.x - q1.y * q2.y - q2.z * q1.z);
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public float getZ() {
		return z;
	}

	public float getW() {
		return w;
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

	@Override
	public bCQuaternion clone() {
		return new bCQuaternion(x, y, z, w);
	}

	@Override
	public String toString() {
		return "x=" + x + ", y=" + y + ", z=" + z + ", w=" + w;
	}
}

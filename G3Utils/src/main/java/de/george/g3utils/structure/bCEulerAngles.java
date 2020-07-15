package de.george.g3utils.structure;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.io.G3Serializable;
import de.george.g3utils.util.FastMath;
import de.george.g3utils.util.Misc;

public class bCEulerAngles implements G3Serializable {
	private float yaw, pitch, roll;

	public bCEulerAngles() {
		this(0.0f, 0.0f, 0.0f);
	}

	private bCEulerAngles(float yaw, float pitch, float roll) {
		this.yaw = yaw;
		this.pitch = pitch;
		this.roll = roll;
	}

	public bCEulerAngles(bCMatrix mat) {
		setEulerAngles(mat);
	}

	public bCEulerAngles(bCMatrix3 mat) {
		setEulerAngles(mat);
	}

	public bCEulerAngles(bCQuaternion q) {
		setEulerAngles(q);
	}

	public float getYawRad() {
		return yaw;
	}

	public void setYawRad(float yaw) {
		this.yaw = yaw;
	}

	public float getPitchRad() {
		return pitch;
	}

	public void setPitchRad(float pitch) {
		this.pitch = pitch;
	}

	public float getRollRad() {
		return roll;
	}

	public void setRollRad(float roll) {
		this.roll = roll;
	}

	public float getYawDeg() {
		return yaw * FastMath.RAD_TO_DEG;
	}

	public void setYawDeg(float yaw) {
		this.yaw = yaw * FastMath.DEG_TO_RAD;
	}

	public float getPitchDeg() {
		return pitch * FastMath.RAD_TO_DEG;
	}

	public void setPitchDeg(float pitch) {
		this.pitch = pitch * FastMath.DEG_TO_RAD;
	}

	public float getRollDeg() {
		return roll * FastMath.RAD_TO_DEG;
	}

	public void setRollDeg(float roll) {
		this.roll = roll * FastMath.DEG_TO_RAD;
	}

	public void setEulerAngles(bCMatrix matrix) {
		bCMatrix rotMat = matrix.getPureRotation();
		pitch = FastMath.asin(-rotMat.getZAxis().getY());
		if (Misc.compareFloat(FastMath.abs(-rotMat.getZAxis().getY()), 1.0f, 0.0000099999997f)) {
			yaw = 0;
			roll = FastMath.atan2(-rotMat.getZAxis().getY() * rotMat.getXAxis().getZ(), rotMat.getXAxis().getX());
		} else {
			// yaw = atan2(cos(pitch) * sin(yaw), cos(pitch) * cos(yaw))
			// roll = atan2(sin(roll) * cos(pitch), cos(roll) * .cos(pitch))
			yaw = FastMath.atan2(rotMat.getZAxis().getX(), rotMat.getZAxis().getZ());
			roll = FastMath.atan2(rotMat.getXAxis().getY(), rotMat.getYAxis().getY());
		}
	}

	public void setEulerAngles(bCMatrix3 matrix) {
		bCMatrix3 rotMat = matrix.getPureRotation();
		pitch = FastMath.asin(-rotMat.getZAxis().getY());
		if (Misc.compareFloat(FastMath.abs(-rotMat.getZAxis().getY()), 1.0f, 0.0000099999997f)) {
			yaw = 0;
			roll = FastMath.atan2(-rotMat.getZAxis().getY() * rotMat.getXAxis().getZ(), rotMat.getXAxis().getX());
		} else {
			// yaw = atan2(cos(pitch) * sin(yaw), cos(pitch) * cos(yaw))
			// roll = atan2(sin(roll) * cos(pitch), cos(roll) * .cos(pitch))
			yaw = FastMath.atan2(rotMat.getZAxis().getX(), rotMat.getZAxis().getZ());
			roll = FastMath.atan2(rotMat.getXAxis().getY(), rotMat.getYAxis().getY());
		}
	}

	public void setEulerAngles(bCQuaternion q) {
		setEulerAngles(new bCMatrix3(q));
	}

	@Override
	public void read(G3FileReader reader) {
		yaw = reader.readFloat();
		pitch = reader.readFloat();
		roll = reader.readFloat();
	}

	@Override
	public void write(G3FileWriter writer) {
		writer.writeFloat(yaw).writeFloat(pitch).writeFloat(roll);
	}

	@Override
	public bCEulerAngles clone() {
		return new bCEulerAngles(yaw, pitch, roll);
	}

	@Override
	public String toString() {
		return "yaw=" + getYawDeg() + ", pitch=" + getPitchDeg() + ", roll=" + getRollDeg();
	}

	public static bCEulerAngles fromDegree(float yaw, float pitch, float roll) {
		return new bCEulerAngles(yaw * FastMath.DEG_TO_RAD, pitch * FastMath.DEG_TO_RAD, roll * FastMath.DEG_TO_RAD);
	}

	public static bCEulerAngles fromRadian(float yaw, float pitch, float roll) {
		return new bCEulerAngles(yaw, pitch, roll);
	}
}

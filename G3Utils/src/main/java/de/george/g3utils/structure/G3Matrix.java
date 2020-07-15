package de.george.g3utils.structure;

import java.io.Serializable;
import java.util.List;

import de.george.g3utils.util.Converter;
import de.george.g3utils.util.Misc;

public class G3Matrix implements Serializable {
	private float[][] matrix = new float[3][3];

	public G3Matrix(double pitch, double yaw, double roll) {
		matrix = anglesToMatrix(pitch, yaw, roll, 1);
	}

	public G3Matrix(double pitch, double yaw, double roll, double scale) {
		matrix = anglesToMatrix(pitch, yaw, roll, scale);
	}

	public G3Matrix(float[][] matrix) {
		this.matrix = matrix;
	}

	/**
	 * Liest eine Matrix ein
	 *
	 * @param hex
	 * @param type
	 *            <li>0: 4x4 Matrix, wie in den Entity/Template Headers
	 *            <li>1: 3x3 Matrix, wie in der Orientation einiger CollisionShapes
	 */
	public G3Matrix(String hex, int type) {
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				int offset = i * (type == 0 ? 32 : 24) + j * 8;
				matrix[i][j] = Converter.HexLittleToFloat(hex.substring(offset, offset + 8));
			}
		}
	}

	private static float[][] anglesToMatrix(double pitchD, double yawD, double rollD, double scale) {
		double pitch = Math.toRadians(pitchD);
		double yaw = Math.toRadians(yawD);
		double roll = Math.toRadians(rollD);

		float[][] matrix = new float[3][3];
		// 1: Zeile; 2: Spalte
		matrix[0][0] = (float) ((Math.cos(roll) * Math.cos(yaw) + Math.sin(roll) * Math.sin(pitch) * Math.sin(yaw)) * scale);
		matrix[0][1] = (float) (Math.sin(roll) * Math.cos(pitch) * scale);
		matrix[0][2] = (float) ((Math.sin(roll) * Math.sin(pitch) * Math.cos(yaw) - Math.cos(roll) * Math.sin(yaw)) * scale);
		matrix[1][0] = (float) ((Math.cos(roll) * Math.sin(pitch) * Math.sin(yaw) - Math.sin(roll) * Math.cos(yaw)) * scale);
		matrix[1][1] = (float) (Math.cos(roll) * Math.cos(pitch) * scale);
		matrix[1][2] = (float) ((Math.sin(roll) * Math.sin(yaw) + Math.cos(roll) * Math.sin(pitch) * Math.cos(yaw)) * scale);
		matrix[2][0] = (float) (Math.cos(pitch) * Math.sin(yaw) * scale);
		matrix[2][1] = (float) (-Math.sin(pitch) * scale);
		matrix[2][2] = (float) (Math.cos(pitch) * Math.cos(yaw) * scale);

		/*
		 * for (double[] temp : matrix) { System.out.println(temp[0] + ", " + temp[1] + ", " +
		 * temp[2]); }
		 */

		return matrix;
	}

	public double[] getAnglesRadian() {
		double[] angles = new double[3];
		float[][] unMatrix = new float[3][3];
		double scale = getScale();
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				unMatrix[i][j] = (float) (matrix[i][j] != 0 ? matrix[i][j] / scale : matrix[i][j]);
			}
		}
		// if(Math.abs(matrix[2][1]) != 1) {
		double p1 = Math.asin(-unMatrix[2][1]);
		double p2 = Math.PI - p1;
		double y1 = Math.atan2(unMatrix[2][0] / Math.cos(p1), unMatrix[2][2] / Math.cos(p1));
		double y2 = Math.atan2(unMatrix[2][0] / Math.cos(p2), unMatrix[2][2] / Math.cos(p2));
		double r1 = Math.atan2(unMatrix[0][1] / Math.cos(p1), unMatrix[1][1] / Math.cos(p1));
		double r2 = Math.atan2(unMatrix[0][1] / Math.cos(p2), unMatrix[1][1] / Math.cos(p2));

		double abs1 = Math.abs(p1) + Math.abs(y1) + Math.abs(r1);
		double abs2 = Math.abs(p2) + Math.abs(y2) + Math.abs(r2);
		if (abs1 <= abs2) {
			angles[0] = p1;
			angles[1] = y1;
			angles[2] = r1;
		} else {
			angles[0] = p2;
			angles[1] = y2;
			angles[2] = r2;
		}
		// }
		return angles;
	}

	public double[] getAngles() {
		double[] anglesRadian = getAnglesRadian();
		double[] angles = new double[3];
		angles[0] = Math.toDegrees(anglesRadian[0]);
		angles[1] = Math.toDegrees(anglesRadian[1]);
		;
		angles[2] = Math.toDegrees(anglesRadian[2]);
		;
		return angles;
	}

	public double getPitch() {
		return getAngles()[0];
	}

	public float getPitchF() {
		return Misc.round((float) getPitch(), 5);
	}

	public double getPitchRadian() {
		return getAnglesRadian()[0];
	}

	public double getYaw() {
		return getAngles()[1];
	}

	public float getYawF() {
		return Misc.round((float) getYaw(), 5);
	}

	public double getYawRadian() {
		return getAnglesRadian()[1];
	}

	public double getRoll() {
		return getAngles()[2];
	}

	public float getRollF() {
		return Misc.round((float) getRoll(), 5);
	}

	public double getRollRadian() {
		return getAnglesRadian()[2];
	}

	public float getScale() {
		return Misc.round((float) Math.pow(determinant(), 1D / 3), 6);
	}

	public float getScaleF() {
		return Misc.round(getScale(), 5);
	}

	public float[][] getMatrix() {
		return matrix.clone();
	}

	public float[][] getMatrixFloat() {
		float[][] floatMatrix = new float[3][3];
		for (int x = 0; x < 3; x++) {
			for (int y = 0; y < 3; y++) {
				floatMatrix[x][y] = matrix[x][y];
			}
		}
		return floatMatrix;
	}

	public bCVector transformVector(bCVector vector) {
		return new bCVector(transformVector(vector.toArray()));
	}

	public float[] transformVector(float[] vector) {
		float[] newVector = {0, 0, 0};

		for (int c = 0; c < 3; c++) {
			for (int l = 0; l < 3; l++) {
				newVector[c] += matrix[l][c] * vector[l];
			}
		}

		return newVector;
	}

	public float[] transformLocalPoints(float[] minPoint, float[] maxPoint) {
		float[] newPoints = {0, 0, 0, 0, 0, 0};

		for (int c = 0; c < 3; c++) {
			for (int l = 0; l < 3; l++) {
				newPoints[c] += matrix[l][c] * (matrix[l][c] < 0 ? maxPoint[l] : minPoint[l]);
			}
		}

		for (int c = 0; c < 3; c++) {
			for (int l = 0; l < 3; l++) {
				newPoints[c + 3] += matrix[l][c] * (matrix[l][c] < 0 ? minPoint[l] : maxPoint[l]);
			}
		}

		return newPoints;
	}

	public double determinant() {
		return determinant(matrix);
	}

	private double determinant(float[][] mat) {
		double result = 0;

		if (mat.length == 1) {
			result = mat[0][0];
			return result;
		}

		if (mat.length == 2) {
			result = mat[0][0] * mat[1][1] - mat[0][1] * mat[1][0];
			return result;
		}

		for (int i = 0; i < mat[0].length; i++) {
			float temp[][] = new float[mat.length - 1][mat[0].length - 1];

			for (int j = 1; j < mat.length; j++) {
				System.arraycopy(mat[j], 0, temp[j - 1], 0, i);
				System.arraycopy(mat[j], i + 1, temp[j - 1], i, mat[0].length - i - 1);
			}

			result += mat[0][i] * Math.pow(-1, i) * determinant(temp);
		}

		return result;

	}

	public static double capRotationAngle(double angle) {
		if (angle > 180) {
			return angle - 360;
		} else if (angle < -180) {
			return 360 + angle;
		}
		return angle;
	}

	private enum GenerationMode {
		Archive,
		Memory,
		NodeHeader,
		Template
	}

	public String generateNodeHeaderData(bCVector pos, bCVector minRange, bCVector maxRange) {
		// aMinusVektor+' '+aPlusVektor+' '+aWorldVektor+' '+
		// lwExtendedToHexLESpaced((abs(minarangeX*scale)+abs(maxarangeX*scale))/2)+' '+
		// lwExtendedToHexLESpaced((abs(minarangeY*scale)+abs(maxarangeY*scale))/2)+' '+
		// lwExtendedToHexLESpaced((abs(minarangeZ*scale)+abs(maxarangeZ*scale))/2)+aNodeTemp;//copy(hexview1.Lines[0],1,38)+copy(hexview1.Lines[1],1,38)+copy(hexview1.Lines[2],1,37);
		return generatePositionData(pos, minRange, maxRange, GenerationMode.NodeHeader);
	}

	private String generatePositionData(bCVector pos, bCVector minRange, bCVector maxRange, GenerationMode mode) {
		// Neue Daten berechnen
		float[] ranges = transformLocalPoints(minRange.toArray(), maxRange.toArray());
		float[] minWorldRoot = {pos.getX() + ranges[0], pos.getY() + ranges[1], pos.getZ() + ranges[2]};
		float[] maxWorldRoot = {pos.getX() + ranges[3], pos.getY() + ranges[4], pos.getZ() + ranges[5]};
		float[] worldVector = {(minWorldRoot[0] + maxWorldRoot[0]) / 2, (minWorldRoot[1] + maxWorldRoot[1]) / 2,
				(minWorldRoot[2] + maxWorldRoot[2]) / 2};
		float radius = (float) (Math.sqrt(
				Math.pow(-ranges[0] + ranges[3], 2) + Math.pow(-ranges[1] + ranges[4], 2) + Math.pow(-ranges[2] + ranges[5], 2)) / 2);

		// Schreibe Daten in die Datei
		float[][] tmpMatrix = getMatrixFloat();
		String[] hexMatrix = {Converter.FloatArrayToHexLittle(tmpMatrix[0]), Converter.FloatArrayToHexLittle(tmpMatrix[1]),
				Converter.FloatArrayToHexLittle(tmpMatrix[2])};
		String hexPosition = Converter.FloatArrayToHexLittle(pos.toArray());
		String hexWorldRoots = Converter.FloatArrayToHexLittle(minWorldRoot) + Converter.FloatArrayToHexLittle(maxWorldRoot);
		String hexRanges = Converter.FloatArrayToHexLittle(minRange.toArray()) + Converter.FloatArrayToHexLittle(maxRange.toArray());
		String hexWorldVector = Converter.FloatArrayToHexLittle(worldVector);
		String hexRadius = Converter.FloatToHexLittle(radius);
		if (mode == GenerationMode.Archive) {
			return hexMatrix[0] + "00000000" + hexMatrix[1] + "00000000" + hexMatrix[2] + "00000000" + hexPosition + "0000803F"
					+ hexMatrix[0] + "00000000" + hexMatrix[1] + "00000000" + hexMatrix[2] + "00000000" + hexPosition + "0000803F"
					+ hexWorldRoots + hexRanges + hexWorldRoots + hexRadius + hexWorldVector + hexRadius + hexWorldVector;
		} else if (mode == GenerationMode.Memory) {
			return hexMatrix[0] + "00000000" + hexMatrix[1] + "00000000" + hexMatrix[2] + "00000000" + hexPosition + "0000803F"
					+ hexMatrix[0] + "00000000" + hexMatrix[1] + "00000000" + hexMatrix[2] + "00000000" + hexPosition + "0000803F"
					+ hexWorldRoots + hexRadius + hexWorldVector + hexWorldRoots + hexRadius + hexWorldVector + hexRanges;
		} else if (mode == GenerationMode.NodeHeader) {
			double scale = getScale();
			float[] middle = {(float) ((Math.abs(minRange.getX() * scale) + Math.abs(maxRange.getX() * scale)) / 2),
					(float) ((Math.abs(minRange.getY() * scale) + Math.abs(maxRange.getY() * scale)) / 2),
					(float) ((Math.abs(minRange.getZ() * scale) + Math.abs(maxRange.getZ() * scale)) / 2)};
			return hexWorldRoots + hexWorldVector + Converter.FloatArrayToHexLittle(middle) + hexMatrix[0] + hexMatrix[1] + hexMatrix[2];
		} else if (mode == GenerationMode.Template) {
			return hexMatrix[0] + "00000000" + hexMatrix[1] + "00000000" + hexMatrix[2] + "00000000" + hexPosition + "0000803F"
					+ hexMatrix[0] + "00000000" + hexMatrix[1] + "00000000" + hexMatrix[2] + "00000000" + hexPosition + "0000803F"
					+ hexWorldRoots + hexRanges + hexWorldRoots;
		}
		return null;

	}

	public String generatePositionDataArchive(bCVector pos, bCVector minRange, bCVector maxRange) {
		return generatePositionData(pos, minRange, maxRange, GenerationMode.Archive);
	}

	public String generatePositionDataMemory(bCVector pos, bCVector minRange, bCVector maxRange) {
		return generatePositionData(pos, minRange, maxRange, GenerationMode.Memory);
	}

	public String generatePositionDataTemplate(bCVector pos, bCVector minRange, bCVector maxRange) {
		return generatePositionData(pos, minRange, maxRange, GenerationMode.Template);
	}

	public static bCVector getMinRange(List<bCVector> points, List<Float> pointRadius) {
		bCVector minRange = new bCVector(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
		for (int i = 0; i < points.size(); i++) {
			bCVector point = points.get(i);
			float radius = pointRadius.get(i);
			if (point.getX() - radius < minRange.getX()) {
				minRange.setX(point.getX() - radius);
			}
			if (point.getY() - radius < minRange.getY()) {
				minRange.setY(point.getY() - radius);
			}
			if (point.getZ() - radius < minRange.getZ()) {
				minRange.setZ(point.getZ() - radius);
			}
		}
		return new bCVector(minRange.getX() < 0 ? minRange.getX() : 0, minRange.getY() < 0 ? minRange.getY() : 0,
				minRange.getZ() < 0 ? minRange.getZ() : 0);
	}

	public static bCVector getMaxRange(List<bCVector> points, List<Float> pointRadius) {
		bCVector maxRange = new bCVector(-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE);
		for (int i = 0; i < points.size(); i++) {
			bCVector point = points.get(i);
			float radius = pointRadius.get(i);
			if (point.getX() + radius > maxRange.getX()) {
				maxRange.setX(point.getX() + radius);
			}
			if (point.getY() + radius > maxRange.getY()) {
				maxRange.setY(point.getY() + radius);
			}
			if (point.getZ() + radius > maxRange.getZ()) {
				maxRange.setZ(point.getZ() + radius);
			}
		}
		return new bCVector(maxRange.getX() > 0 ? maxRange.getX() : 0, maxRange.getY() > 0 ? maxRange.getY() : 0,
				maxRange.getZ() > 0 ? maxRange.getZ() : 0);
	}

	public G3Matrix transpose() {
		float[][] transMatrix = new float[3][3];
		for (int i = 0; i < transMatrix.length; i++) {
			for (int j = 0; j < transMatrix.length; j++) {
				transMatrix[i][j] = matrix[j][i];
			}
		}
		return new G3Matrix(transMatrix);
	}

	@Override
	public String toString() {
		return "pitch: " + getPitchF() + " / yaw: " + getYawF() + " / roll: " + getRollF() + " / scale:" + getScaleF();
	}

	public bCVector getXAxis() {
		return new bCVector(matrix[0]);
	}

	public bCVector getYAxis() {
		return new bCVector(matrix[1]);
	}

	public bCVector getZAxis() {
		return new bCVector(matrix[2]);
	}

	@Override
	protected G3Matrix clone() {
		return new G3Matrix(matrix);
	}

}

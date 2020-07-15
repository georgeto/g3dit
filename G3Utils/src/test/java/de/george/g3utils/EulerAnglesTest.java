package de.george.g3utils;

import static de.george.hamcrest.MoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.function.BiFunction;

import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import de.george.g3utils.structure.G3Matrix;
import de.george.g3utils.structure.bCEulerAngles;
import de.george.g3utils.structure.bCMatrix;
import de.george.g3utils.structure.bCMatrix3;
import de.george.g3utils.structure.bCQuaternion;
import de.george.g3utils.structure.bCVector;
import de.george.g3utils.util.Misc;

@RunWith(Theories.class)
public class EulerAnglesTest {
	@DataPoint
	public static final bCEulerAngles ANGLES_ZERO = bCEulerAngles.fromDegree(0, 0, 0);
	@DataPoint
	public static final bCEulerAngles ANGLES_MARGIN1 = bCEulerAngles.fromDegree(179, 0, 0);
	@DataPoint
	public static final bCEulerAngles ANGLES_MARGIN2 = bCEulerAngles.fromDegree(-179, 0, 0);
	@DataPoint
	public static final bCEulerAngles ANGLES_MARGIN3 = bCEulerAngles.fromDegree(0, 89, 0);
	@DataPoint
	public static final bCEulerAngles ANGLES_MARGIN4 = bCEulerAngles.fromDegree(0, -89, 0);
	@DataPoint
	public static final bCEulerAngles ANGLES_MARGIN5 = bCEulerAngles.fromDegree(0, 0, 179);
	@DataPoint
	public static final bCEulerAngles ANGLES_MARGIN6 = bCEulerAngles.fromDegree(0, 0, -179);
	@DataPoint
	public static final bCEulerAngles ANGLES1 = bCEulerAngles.fromDegree(95, -80, 150);
	@DataPoint
	public static final bCEulerAngles ANGLES1_INV = bCEulerAngles.fromDegree(-5, -80, -60);
	@DataPoint
	public static final bCEulerAngles ANGLES2 = bCEulerAngles.fromDegree(45, 5, -50);

	@DataPoint
	public static final float SCALE_1 = 0.123f;
	@DataPoint
	public static final float SCALE_2 = 0.3f;
	@DataPoint
	public static final float SCALE_3 = 0.5f;
	@DataPoint
	public static final float SCALE_4 = 0.76f;
	@DataPoint
	public static final float SCALE_5 = 1.0f;
	@DataPoint
	public static final float SCALE_6 = 1.323f;
	@DataPoint
	public static final float SCALE_7 = 1.75f;
	@DataPoint
	public static final float SCALE_8 = 2.323f;

	public static BiFunction<bCEulerAngles, bCEulerAngles, Boolean> COMPARE_ANGLES = (a1, a2) -> true;

	@Theory
	public void test(bCEulerAngles angles, float scaleX, float scaleY, float scaleZ) {
		bCMatrix matrix = new bCMatrix(angles, bCVector.nullVector());
		matrix.modifyScaling(new bCVector(scaleX, scaleY, scaleZ));
		assertThat(new bCEulerAngles(matrix), equalTo(angles, EulerAnglesTest::compareAnglesEpsilon));
		assertThat(new bCEulerAngles(new bCQuaternion(matrix)), equalTo(angles, EulerAnglesTest::compareAnglesEpsilon));
	}

	@Theory
	public void test3x3(bCEulerAngles angles, float scaleX, float scaleY, float scaleZ) {
		bCMatrix3 matrix = new bCMatrix3(angles);
		matrix.scale(new bCVector(scaleX, scaleY, scaleZ));
		assertThat(new bCEulerAngles(matrix), equalTo(angles, EulerAnglesTest::compareAnglesEpsilon));
		assertThat(new bCEulerAngles(new bCQuaternion(matrix)), equalTo(angles, EulerAnglesTest::compareAnglesEpsilon));
	}

	@Theory
	public void testQuaternion(bCEulerAngles angles) {
		bCQuaternion quaternion = new bCQuaternion(angles);
		assertThat(new bCEulerAngles(quaternion), equalTo(angles, EulerAnglesTest::compareAnglesEpsilon));
	}

	@Theory
	public void testMe(bCEulerAngles angles) {
		G3Matrix matrix = new G3Matrix(angles.getPitchDeg(), angles.getYawDeg(), angles.getRollDeg());
		assertThat(bCEulerAngles.fromDegree(matrix.getYawF(), matrix.getPitchF(), matrix.getRollF()),
				equalTo(angles, EulerAnglesTest::compareAnglesEpsilon));
	}

	public static boolean compareAngles(bCEulerAngles a1, bCEulerAngles a2) {
		return a1.getYawRad() == a2.getYawRad() && a2.getPitchRad() == a2.getPitchRad() && a1.getRollRad() == a2.getRollRad();
	}

	public static boolean compareAnglesEpsilon(bCEulerAngles a1, bCEulerAngles a2) {
		return Misc.compareFloat(a1.getYawRad(), a2.getYawRad(), 0.0001f) && Misc.compareFloat(a1.getPitchRad(), a2.getPitchRad(), 0.0001f)
				&& Misc.compareFloat(a1.getRollRad(), a2.getRollRad(), 0.0001f);
	}
}

package de.george.g3utils;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.google.common.primitives.Floats;

import de.george.g3utils.structure.bCEulerAngles;
import de.george.g3utils.structure.bCMatrix;
import de.george.g3utils.structure.bCVector;

public class MatrixInvertTest {
	@Test
	public void testInvertIndentity() {
		assertTrue(bCMatrix.getIdentity().isEqual(bCMatrix.getIdentity().getInverted()));
	}

	@Test
	public void testInvert() {
		bCMatrix matrix = new bCMatrix(bCEulerAngles.fromDegree(91, -145, 30), new bCVector(7500, -54654, 23112));
		bCMatrix invMatrix = matrix.getInverted();
		assertArrayEquals(Floats.concat(bCMatrix.getIdentity().toArray()), Floats.concat(matrix.getProduct(invMatrix).toArray()), 0.01f);
		assertArrayEquals(Floats.concat(bCMatrix.getIdentity().toArray()), Floats.concat(invMatrix.getProduct(matrix).toArray()), 0.01f);

		bCVector vec = new bCVector(123, 321, -1540);
		bCVector vec2 = vec.getTransformed(matrix).getTransformed(invMatrix);
		bCVector vec3 = vec.getTransformed(invMatrix).getTransformed(matrix);
		assertArrayEquals(vec.toArray(), vec2.toArray(), 0.01f);
		assertArrayEquals(vec.toArray(), vec3.toArray(), 0.01f);
	}

}

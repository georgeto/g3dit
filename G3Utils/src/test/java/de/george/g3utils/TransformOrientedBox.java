package de.george.g3utils;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

import com.google.common.primitives.Floats;

import de.george.g3utils.structure.bCEulerAngles;
import de.george.g3utils.structure.bCMatrix;
import de.george.g3utils.structure.bCMatrix3;
import de.george.g3utils.structure.bCOrientedBox;
import de.george.g3utils.structure.bCVector;

public class TransformOrientedBox {
	@Test
	public void testTransform() {
		bCMatrix matrix = new bCMatrix(bCEulerAngles.fromDegree(91, -145, 30), new bCVector(7500, -54654, 23112));
		bCMatrix invMatrix = matrix.getInverted();

		bCOrientedBox box = new bCOrientedBox(new bCVector(1232, -2354, 212), new bCVector(542, 132, 45),
				new bCMatrix3(bCEulerAngles.fromDegree(25, 95, -157)));
		bCOrientedBox box2 = box.getTransformed(matrix).getTransformed(invMatrix);
		bCOrientedBox box3 = box.getTransformed(invMatrix).getTransformed(matrix);
		assertOrientedBoxEqual(box, box2);
		assertOrientedBoxEqual(box, box3);
	}

	public void assertOrientedBoxEqual(bCOrientedBox excpected, bCOrientedBox actual) {
		assertArrayEquals(excpected.getCenter().toArray(), actual.getCenter().toArray(), 0.01f);
		assertArrayEquals(excpected.getExtent().toArray(), actual.getExtent().toArray(), 0.01f);
		assertArrayEquals(Floats.concat(excpected.getOrientation().toArray()), Floats.concat(actual.getOrientation().toArray()), 0.01f);
	}
}

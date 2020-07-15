package de.george.lrentnode.util;

import de.george.g3utils.structure.bCBox;
import de.george.g3utils.structure.bCVector;

public interface AABBTreePrimitive {
	bCBox getBounds();

	bCVector getReferencePoint();
}

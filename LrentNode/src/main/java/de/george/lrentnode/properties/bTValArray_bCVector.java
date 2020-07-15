package de.george.lrentnode.properties;

import de.george.g3utils.structure.bCVector;

public class bTValArray_bCVector extends bTArray<bCVector> {

	@Override
	public Class<bCVector> getEntryType() {
		return bCVector.class;
	}

}

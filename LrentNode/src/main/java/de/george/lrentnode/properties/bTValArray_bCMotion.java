package de.george.lrentnode.properties;

import de.george.g3utils.structure.bCMotion;

public class bTValArray_bCMotion extends bTArray<bCMotion> {

	@Override
	public Class<bCMotion> getEntryType() {
		return bCMotion.class;
	}

}

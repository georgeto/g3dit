package de.george.lrentnode.properties;

import java.util.List;

import de.george.g3utils.io.G3Serializable;

public class bTObjArray_Generic<T extends G3Serializable> extends bTArray<T> {
	private final Class<T> entryType;

	public bTObjArray_Generic(Class<T> entryType) {
		this.entryType = entryType;
	}

	public bTObjArray_Generic(Class<T> entryType, List<T> entries) {
		this.entryType = entryType;
		setEntries(entries);
	}

	@Override
	public Class<T> getEntryType() {
		return entryType;
	}
}

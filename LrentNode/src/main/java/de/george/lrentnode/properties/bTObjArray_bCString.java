package de.george.lrentnode.properties;

import java.util.List;

public class bTObjArray_bCString extends bTArray<bCString> {

	@Override
	public Class<bCString> getEntryType() {
		return bCString.class;
	}

	public List<String> getNativeEntries() {
		return getEntries(s -> s.getString());
	}

	public void setNativeEntries(List<String> entries) {
		setEntries(entries, s -> new bCString(s));
	}

}

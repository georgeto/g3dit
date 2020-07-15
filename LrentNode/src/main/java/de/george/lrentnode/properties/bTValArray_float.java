package de.george.lrentnode.properties;

import java.util.List;

public class bTValArray_float extends bTArray<gFloat> {

	@Override
	public Class<gFloat> getEntryType() {
		return gFloat.class;
	}

	public List<Float> getNativeEntries() {
		return getEntries(f -> f.getFloat());
	}

	public void setNativeEntries(List<Float> entries) {
		setEntries(entries, f -> new gFloat(f));
	}
}

package de.george.lrentnode.properties;

import java.util.List;

public class bTObjArray_eCEntityProxy extends bTArray<eCEntityProxy> {

	@Override
	public Class<eCEntityProxy> getEntryType() {
		return eCEntityProxy.class;
	}

	public List<String> getNativeEntries() {
		return getEntries(s -> s.getGuid());
	}

	public void setNativeEntries(List<String> entries) {
		setEntries(entries, s -> new eCEntityProxy(s));
	}
}

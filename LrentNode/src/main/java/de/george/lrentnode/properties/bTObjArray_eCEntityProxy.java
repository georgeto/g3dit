package de.george.lrentnode.properties;

import java.util.List;

public class bTObjArray_eCEntityProxy extends bTArray<eCEntityProxy> {

	@Override
	public Class<eCEntityProxy> getEntryType() {
		return eCEntityProxy.class;
	}

	public List<String> getNativeEntries() {
		return getEntries(eCEntityProxy::getGuid);
	}

	public void setNativeEntries(List<String> entries) {
		setEntries(entries, eCEntityProxy::new);
	}
}

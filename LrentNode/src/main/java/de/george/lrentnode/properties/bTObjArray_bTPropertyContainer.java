package de.george.lrentnode.properties;

import java.util.List;

import de.george.lrentnode.enums.G3Enums.G3Enum;

public class bTObjArray_bTPropertyContainer<T extends G3Enum> extends bTArray<bTPropertyContainer<T>> {

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public Class<bTPropertyContainer<T>> getEntryType() {
		return (Class) bTPropertyContainer.class;
	}

	public List<Integer> getNativeEntries() {
		return getEntries(bTPropertyContainer::getEnumValue);
	}

	public void setNativeEntries(List<Integer> entries) {
		setEntries(entries, bTPropertyContainer::new);
	}
}

package de.george.lrentnode.properties.compare;

import de.george.g3utils.io.G3Serializable;
import de.george.lrentnode.util.PropertyUtil;

public class BinaryPropertyComparator<T extends G3Serializable> implements PropertyComparator<T> {
	@Override
	public boolean equals(T o1, T o2) {
		return PropertyUtil.compareSerializable(o1, o2);
	}
}

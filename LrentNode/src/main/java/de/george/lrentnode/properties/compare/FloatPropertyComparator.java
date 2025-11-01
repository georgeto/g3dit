package de.george.lrentnode.properties.compare;

import de.george.g3utils.io.G3Serializable;

public interface FloatPropertyComparator<T extends G3Serializable> extends PropertyComparator<T> {
	public boolean similiar(T o1, T o2);
}

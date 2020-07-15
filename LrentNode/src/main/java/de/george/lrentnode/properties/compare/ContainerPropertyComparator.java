package de.george.lrentnode.properties.compare;

import de.george.g3utils.io.G3Serializable;

public interface ContainerPropertyComparator<T extends G3Serializable, V> extends PropertyComparator<T> {
	public boolean contains(T haystack, V pattern);
}

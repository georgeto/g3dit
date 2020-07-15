package de.george.lrentnode.properties.compare;

import java.util.Comparator;

import de.george.g3utils.io.G3Serializable;

public interface ComparablePropertyComparator<T extends G3Serializable> extends PropertyComparator<T>, Comparator<T> {
	@Override
	default public boolean equals(T o1, T o2) {
		return compare(o1, o2) == 0;
	}
}

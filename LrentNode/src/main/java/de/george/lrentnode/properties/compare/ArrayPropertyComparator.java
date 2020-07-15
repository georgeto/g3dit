package de.george.lrentnode.properties.compare;

import de.george.g3utils.io.G3Serializable;
import de.george.lrentnode.properties.bTArray;

public class ArrayPropertyComparator<V extends G3Serializable, T extends bTArray<V>> implements ContainerPropertyComparator<T, V> {
	private final PropertyComparator<V> valueComparator;

	public ArrayPropertyComparator(PropertyComparator<V> valueComparator) {
		this.valueComparator = valueComparator;
	}

	@Override
	public boolean equals(T o1, T o2) {
		if (o1.getEntries().size() != o2.getEntries().size()) {
			return false;
		}

		for (int i = 0; i < o1.getEntries().size(); i++) {
			if (!valueComparator.equals(o1.getEntries().get(i), o2.getEntries().get(i))) {
				return false;
			}
		}

		return true;
	}

	@Override
	public boolean contains(T haystack, V pattern) {
		for (V value : haystack.getEntries()) {
			if (valueComparator.equals(value, pattern)) {
				return true;
			}
		}

		return false;
	}
}

package de.george.lrentnode.properties.compare;

import java.util.Comparator;
import java.util.function.Function;

import de.george.g3utils.io.G3Serializable;

public class TransformingPropertyComparator<T extends G3Serializable, V> implements ComparablePropertyComparator<T> {
	protected final Function<T, V> valueExtractor;
	private final Comparator<V> valueComparator;

	public TransformingPropertyComparator(Function<T, V> valueExtractor, Comparator<V> valueComparator) {
		this.valueExtractor = valueExtractor;
		this.valueComparator = valueComparator;
	}

	@Override
	public int compare(T o1, T o2) {
		return valueComparator.compare(valueExtractor.apply(o1), valueExtractor.apply(o2));
	}
}

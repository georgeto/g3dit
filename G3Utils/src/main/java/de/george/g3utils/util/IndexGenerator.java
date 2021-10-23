package de.george.g3utils.util;

import com.google.common.base.Function;

public class IndexGenerator<E> implements Function<E, Integer> {
	private int index = 0;

	@Override
	public Integer apply(E e) {
		return index++;
	}
}

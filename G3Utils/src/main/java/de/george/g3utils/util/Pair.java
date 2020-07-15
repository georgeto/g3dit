package de.george.g3utils.util;

public class Pair<K, V> {

	private final K el0;
	private final V el1;

	public static <K, V> Pair<K, V> of(K el0, V el1) {
		return new Pair<>(el0, el1);
	}

	public Pair(K el0, V el1) {
		this.el0 = el0;
		this.el1 = el1;
	}

	public K el0() {
		return el0;
	}

	public V el1() {
		return el1;
	}

	public boolean isNotNull() {
		return el0 != null && el1 != null;
	}

	@Override
	public String toString() {
		return "el0=" + el0 + ", el1=" + el1;
	}
}

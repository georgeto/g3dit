package de.george.g3utils.util;

/**
 * Make a final one of these to hold non-final things in.
 */
public class Holder<T> {
	private T held = null;

	public Holder() {}

	public Holder(T it) {
		held = it;
	}

	public void hold(T it) {
		held = it;
	}

	public T held() {
		return held;
	}

	@Override
	public String toString() {
		return String.valueOf(held);
	}
}

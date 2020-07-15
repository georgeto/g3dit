package de.george.g3dit.gui.components.search;

public interface SearchFilter<T> {
	boolean matches(T value);

	boolean isValid();
}

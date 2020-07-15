package de.george.g3dit.settings;

/**
 * All methods are thread safe.
 */
public interface OptionStore {
	<T> void put(Option<T> option, T value);

	<T> T get(Option<T> option);

	<T> void remove(Option<T> option);

	boolean save();

}

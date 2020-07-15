package de.george.g3dit.settings;

public interface MigratableOptionStore extends OptionStore {
	<T> void put(String optionName, T value);

	<T> T get(String optionName);

	void remove(String optionName);
}

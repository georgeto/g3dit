package de.george.g3dit.settings;

public enum DefaultValueOptionStore implements OptionStore {
	INSTANCE;

	@Override
	public <T> void put(Option<T> option, T value) {}

	@Override
	public <T> T get(Option<T> option) {
		return option.getDefaultValue();
	}

	@Override
	public <T> void remove(Option<T> option) {}

	@Override
	public boolean save() {
		return true;
	}

}

package de.george.g3dit.settings;

public abstract class AbstractOption<T> implements Option<T> {

	protected final T defaultValue;
	protected String name;
	protected String displayName;

	public AbstractOption(T defaultValue, String name, String displayName) {
		this.defaultValue = defaultValue;
		this.name = name;
		this.displayName = displayName;
	}

	@Override
	public T getDefaultValue() {
		return defaultValue;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}

}

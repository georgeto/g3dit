package de.george.g3dit.settings;

import java.awt.Window;

public interface Option<T> {
	public OptionHandler<T> createOptionHandler(Window parent);

	public T getDefaultValue();

	public String getName();

	public String getDisplayName();
}

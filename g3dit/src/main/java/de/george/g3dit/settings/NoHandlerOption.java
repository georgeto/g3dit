package de.george.g3dit.settings;

import java.awt.Window;

public class NoHandlerOption<T> extends AbstractOption<T> {
	public NoHandlerOption(T defaultValue, String name, String displayName) {
		super(defaultValue, name, displayName);
	}

	@Override
	public OptionHandler<T> createOptionHandler(Window parent) {
		throw new UnsupportedOperationException();
	}

}

package de.george.g3dit.settings;

import java.awt.Window;
import java.util.function.Function;

public class LambdaOption<T> extends AbstractOption<T> {
	private Function<Window, OptionHandler<T>> optionHandlerCreator;

	public LambdaOption(T defaultValue, Function<Window, OptionHandler<T>> optionHandlerCreator, String name, String displayName) {
		super(defaultValue, name, displayName);
		this.optionHandlerCreator = optionHandlerCreator;
	}

	@Override
	public OptionHandler<T> createOptionHandler(Window parent) {
		return optionHandlerCreator.apply(parent);
	}
}

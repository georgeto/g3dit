package de.george.g3dit.settings;

import java.awt.Window;
import java.util.Optional;

public abstract class TypedOptionHandler<T> extends AbstractOptionHandler<T> {

	public TypedOptionHandler(Window parent) {
		super(parent);
	}

	@Override
	public void load(OptionStore optionStore, Option<T> option) {
		load(optionStore.get(option));
	}

	@Override
	public void save(OptionStore optionStore, Option<T> option) {
		Optional<T> value = save();
		if (value.isPresent()) {
			optionStore.put(option, value.get());
		} else {
			optionStore.remove(option);
		}

	}

	protected abstract void load(T value);

	protected abstract Optional<T> save();
}

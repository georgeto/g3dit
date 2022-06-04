package de.george.g3dit.gui.edit.handler;

import de.george.g3dit.gui.edit.adapter.PropertyAdapter;
import de.george.g3utils.io.G3Serializable;
import de.george.lrentnode.archive.G3ClassContainer;

public abstract class TypedPropertyHandler<T extends G3Serializable> extends AbstractPropertyHandler<T> {

	@Override
	public void load(G3ClassContainer container, PropertyAdapter<T> property) {
		load(property.getValue(container));
	}

	@Override
	public void save(G3ClassContainer container, PropertyAdapter<T> property) {
		property.setValue(container, save());
	}

	// TODO: Write into exisitng properties instead...
	protected abstract void load(T value);

	protected abstract T save();
}

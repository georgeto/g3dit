package de.george.g3dit.gui.edit.handler;

import de.george.g3dit.gui.edit.adapter.PropertyAdapter;
import de.george.g3utils.io.G3Serializable;
import de.george.lrentnode.archive.eCEntity;

public abstract class TypedPropertyHandler<T extends G3Serializable> extends AbstractPropertyHandler<T> {

	@Override
	public void load(eCEntity entity, PropertyAdapter<T> property) {
		load(property.getValue(entity));
	}

	@Override
	public void save(eCEntity entity, PropertyAdapter<T> property) {
		property.setValue(entity, save());
	}

	// TODO: Write into exisitng properties instead...
	protected abstract void load(T value);

	protected abstract T save();
}

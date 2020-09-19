package de.george.g3dit.gui.edit.adapter;

import de.george.g3utils.io.G3Serializable;
import de.george.lrentnode.archive.eCEntity;

public interface PropertyAdapter<T extends G3Serializable> {
	public T getValue(eCEntity entity);

	public void setValue(eCEntity entity, T newValue);

	public Class<T> getDataType();

	public String getDataTypeName();

	public T getDefaultValue();
}

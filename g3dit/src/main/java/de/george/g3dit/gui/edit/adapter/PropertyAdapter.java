package de.george.g3dit.gui.edit.adapter;

import de.george.g3utils.io.G3Serializable;
import de.george.lrentnode.archive.G3ClassContainer;

public interface PropertyAdapter<T extends G3Serializable> {
	public T getValue(G3ClassContainer container);

	public void setValue(G3ClassContainer container, T newValue);

	public Class<T> getDataType();

	public String getDataTypeName();

	public T getDefaultValue();
}

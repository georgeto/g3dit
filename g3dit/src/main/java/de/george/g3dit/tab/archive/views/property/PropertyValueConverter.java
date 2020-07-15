package de.george.g3dit.tab.archive.views.property;

import de.george.g3utils.io.G3Serializable;

public interface PropertyValueConverter<T extends G3Serializable, V> {
	public V convertTo(T source) throws Exception;

	public T convertFrom(T old, V value) throws Exception;

	public Class<T> getPropertyType();

	public Class<V> getValueType();
}

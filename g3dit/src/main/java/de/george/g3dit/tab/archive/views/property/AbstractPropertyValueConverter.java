package de.george.g3dit.tab.archive.views.property;

import de.george.g3utils.io.G3Serializable;

public abstract class AbstractPropertyValueConverter<T extends G3Serializable, V> implements PropertyValueConverter<T, V> {
	private Class<T> propertyType;
	private Class<V> valueType;

	protected AbstractPropertyValueConverter(Class<T> propertyType, Class<V> valueType) {
		this.propertyType = propertyType;
		this.valueType = valueType;
	}

	@Override
	public Class<T> getPropertyType() {
		return propertyType;
	}

	@Override
	public Class<V> getValueType() {
		return valueType;
	}
}

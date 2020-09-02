package de.george.g3dit.tab.archive.views.property;

import java.lang.reflect.Method;

import com.l2fprod.common.beans.BeanUtils;

import de.george.g3utils.io.G3Serializable;

public class BeanPropertyValueConverter<T extends G3Serializable, V> extends AbstractPropertyValueConverter<T, V> {
	private String accessorName;

	private BeanPropertyValueConverter(Class<T> propertyType, Class<V> valueType, String accessorName) {
		super(propertyType, valueType);
		this.accessorName = accessorName;
	}

	@Override
	@SuppressWarnings("unchecked")
	public V convertTo(T source) {
		try {
			Method method = BeanUtils.getReadMethod(getPropertyType(), accessorName);
			if (method != null) {
				return (V) method.invoke(source);
			} else {
				throw new RuntimeException(String.format("Could not find a read method for the accessor name '%s'.", accessorName));
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public T convertFrom(T old, V value) {
		try {
			Method method = BeanUtils.getWriteMethod(getPropertyType(), accessorName, getValueType());
			if (method != null) {
				method.invoke(old, value);
				return old;
			} else {
				throw new RuntimeException(String.format("Could not find a write method for the accessor name '%s'.", accessorName));
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static <T extends G3Serializable, V> BeanPropertyValueConverter<T, V> with(Class<T> propertyType, Class<V> valueType,
			String accessorName) {
		return new BeanPropertyValueConverter<>(propertyType, valueType, accessorName);
	}
}

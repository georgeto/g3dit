package de.george.g3dit.tab.archive.views.property;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.l2fprod.common.beans.BeanUtils;

import de.george.g3utils.io.G3Serializable;
import de.george.g3utils.util.BiConsumerWithException;
import de.george.g3utils.util.FunctionWithException;
import de.george.g3utils.util.Misc;

public class DictPropertyValueConverter<T extends G3Serializable> extends AbstractPropertyValueConverter<T, String> {
	private Map<String, PropertyField> fields;

	protected DictPropertyValueConverter(Class<T> propertyType, Map<String, PropertyField> fields) {
		super(propertyType, String.class);
		this.fields = fields;
	}

	@Override
	public String convertTo(T source) {
		return fields.values().stream().map(f -> f.accessorName + "=" + f.toString.apply(f.read.apply(source)))
				.collect(Collectors.joining(", "));
	}

	@Override
	public T convertFrom(T old, String value) {
		for (Entry<String, String> entry : Misc.stringValueListToMap(value, ",", "=").entrySet()) {
			PropertyField field = fields.get(entry.getKey().toLowerCase());
			if (field != null) {
				field.write.accept(old, field.fromString.apply(entry.getValue()));
			}
		}
		return old;
	}

	private static class PropertyField {
		private final String accessorName;
		private final FunctionWithException<Object, Object> read;
		private final BiConsumerWithException<Object, Object> write;
		private final Function<Object, String> toString;
		private final Function<String, Object> fromString;

		@SuppressWarnings("unchecked")
		public <P> PropertyField(String accessorName, Method read, Method write, Function<P, String> toString,
				Function<String, P> fromString) {
			this.accessorName = accessorName;
			this.read = read::invoke;
			this.write = write::invoke;
			this.toString = (Function<Object, String>) toString;
			this.fromString = (Function<String, Object>) fromString;
		}
	}

	public static class Builder<T extends G3Serializable> {
		private Class<T> propertyType;
		private Map<String, PropertyField> fields = new LinkedHashMap<>();

		public Builder(Class<T> propertyType) {
			this.propertyType = propertyType;
		}

		public <P> Builder<T> with(String accessorName, Class<P> valueType, Function<P, String> toString, Function<String, P> fromString) {
			Method read = BeanUtils.getReadMethod(propertyType, accessorName);
			Method write = BeanUtils.getWriteMethod(propertyType, accessorName, valueType);
			fields.put(accessorName.toLowerCase(), new PropertyField(accessorName, read, write, toString, fromString));
			return this;
		}

		public DictPropertyValueConverter<T> build() {
			return new DictPropertyValueConverter<>(propertyType, fields);
		}
	}

	public static <T extends G3Serializable> Builder<T> builder(Class<T> propertyType) {
		return new Builder<>(propertyType);
	}
}

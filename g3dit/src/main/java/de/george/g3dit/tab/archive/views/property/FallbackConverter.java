package de.george.g3dit.tab.archive.views.property;

import de.george.g3dit.tab.archive.views.property.FallbackWrapper.Converter;
import de.george.g3utils.io.G3Serializable;

public class FallbackConverter<T extends G3Serializable> implements PropertyValueConverter<T, FallbackWrapper> {
	private PropertyValueConverter primary;
	private PropertyValueConverter fallback;

	public FallbackConverter(PropertyValueConverter<T, ?> primary, PropertyValueConverter<T, ?> fallback) {
		this.primary = primary;
		this.fallback = fallback;
	}

	@Override
	public FallbackWrapper convertTo(T source) throws Exception {
		try {
			return new FallbackWrapper(Converter.Primary, primary.convertTo(source), primary.getValueType());
		} catch (Exception e) {
			try {
				return new FallbackWrapper(Converter.Fallback, fallback.convertTo(source), fallback.getValueType());
			} catch (Exception ex) {
				throw e;
			}
		}
	}

	@Override
	public T convertFrom(T old, FallbackWrapper value) throws Exception {
		if (value.converter == Converter.Primary)
			return (T) primary.convertFrom(old, value.value);
		else
			return (T) fallback.convertFrom(old, value.value);

	}

	@Override
	public Class<T> getPropertyType() {
		return primary.getPropertyType();
	}

	@Override
	public Class<FallbackWrapper> getValueType() {
		return FallbackWrapper.class;
	}
}

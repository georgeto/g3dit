package de.george.g3dit.tab.archive.views.property;

import java.util.Objects;

public class FallbackWrapper {
	public enum Converter {
		Primary,
		Fallback
	}

	public final Converter converter;
	public Object value;
	public final Class<?> valueType;

	public FallbackWrapper(Converter converter, Object value, Class<?> valueType) {
		this.converter = converter;
		this.value = value;
		this.valueType = valueType;
	}

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof FallbackWrapper castOther)) {
			return false;
		}
		return Objects.equals(converter, castOther.converter) && Objects.equals(value, castOther.value)
				&& Objects.equals(valueType, castOther.valueType);
	}

	@Override
	public int hashCode() {
		return Objects.hash(converter, value);
	}
}

package de.george.g3dit.tab.archive.views.property;

import java.util.Objects;

import de.george.lrentnode.enums.G3Enums;
import de.george.lrentnode.enums.G3Enums.G3Enum;

public class G3EnumWrapper {
	private int enumValue;
	private Class<? extends G3Enum> enumClass;

	public G3EnumWrapper(int enumValue, Class<? extends G3Enum> enumClass) {
		this.enumValue = enumValue;
		this.enumClass = enumClass;
	}

	public int getEnumValue() {
		return enumValue;
	}

	public Class<? extends G3Enum> getEnumClass() {
		return enumClass;
	}

	@Override
	public String toString() {
		String asString = G3Enums.asString(getEnumClass(), getEnumValue(), false);

		if (asString == null) {
			return "Unbekannt: " + getEnumValue();
		}

		return asString;
	}

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof G3EnumWrapper)) {
			return false;
		}
		G3EnumWrapper castOther = (G3EnumWrapper) other;
		return Objects.equals(enumValue, castOther.enumValue) && Objects.equals(enumClass, castOther.enumClass);
	}

	@Override
	public int hashCode() {
		return Objects.hash(enumValue, enumClass);
	}
}

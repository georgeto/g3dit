package de.george.g3dit.tab.archive.views.property;

import java.util.Objects;

import com.teamunify.i18n.I;

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
			return I.trcf("Unknown enum value", "Unknown: {0}", getEnumValue());
		}

		return asString;
	}

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof G3EnumWrapper castOther)) {
			return false;
		}
		return Objects.equals(enumValue, castOther.enumValue) && Objects.equals(enumClass, castOther.enumClass);
	}

	@Override
	public int hashCode() {
		return Objects.hash(enumValue, enumClass);
	}
}

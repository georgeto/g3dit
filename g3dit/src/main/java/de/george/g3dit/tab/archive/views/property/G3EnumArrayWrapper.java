package de.george.g3dit.tab.archive.views.property;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import de.george.lrentnode.enums.G3Enums;
import de.george.lrentnode.enums.G3Enums.G3Enum;

public class G3EnumArrayWrapper {
	private List<Integer> enumValues;
	private Class<? extends G3Enum> enumClass;

	public G3EnumArrayWrapper(List<Integer> enumValues, Class<? extends G3Enum> enumClass) {
		this.enumValues = enumValues;
		this.enumClass = enumClass;
	}

	public List<Integer> getEnumValues() {
		return enumValues;
	}

	public Class<? extends G3Enum> getEnumClass() {
		return enumClass;
	}

	@Override
	public String toString() {
		return enumValues.stream().map(enumValue -> Optional.ofNullable(G3Enums.asString(getEnumClass(), enumValue, false))
				.orElseGet(() -> "Unbekannt: " + enumValue)).collect(Collectors.joining(", ", "[", "]"));
	}

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof G3EnumArrayWrapper castOther)) {
			return false;
		}
		return Objects.equals(enumValues, castOther.enumValues) && Objects.equals(enumClass, castOther.enumClass);
	}

	@Override
	public int hashCode() {
		return Objects.hash(enumValues, enumClass);
	}
}

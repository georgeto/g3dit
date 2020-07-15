package de.george.g3dit.tab.archive.views.property;

import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.base.Splitter;

import de.george.g3utils.io.G3Serializable;
import de.george.lrentnode.properties.bTArray;

public final class ArrayPropertyValueConverter<E extends G3Serializable, T extends bTArray<E>>
		extends AbstractPropertyValueConverter<T, String> {
	private Function<E, String> to;
	private Function<String, E> from;

	private ArrayPropertyValueConverter(Class<T> propertyType, Function<E, String> to, Function<String, E> from) {
		super(propertyType, String.class);
		this.to = to;
		this.from = from;
	}

	@Override
	public String convertTo(T source) {
		return source.getEntries().stream().map(e -> to.apply(e)).collect(Collectors.joining(", ", "[", "]"));
	}

	@Override
	public T convertFrom(T old, String value) {
		value = value.trim();
		if (value.matches("^\\[\\s*\\]$")) {
			old.clear();
		} else if (value.matches("^\\[.*\\]$")) {
			old.setEntries(Splitter.on(",")
					.splitToList(value.replaceFirst("^\\s*\\[\\s*", "").replaceFirst("\\s*\\]\\s*$", "").replaceAll("\\s*,\\s*", ","))
					.stream().map(s -> from.apply(s)).collect(Collectors.toList()));
		}
		return old;

	}

	public static <E extends G3Serializable, T extends bTArray<E>> ArrayPropertyValueConverter<E, T> of(Class<T> propertyType,
			Function<E, String> to, Function<String, E> from) {
		return new ArrayPropertyValueConverter<>(propertyType, to, from);
	}
}

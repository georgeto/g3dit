package de.george.g3dit.tab.archive.views.property;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import de.george.g3utils.io.G3Serializable;

public final class LambdaConverter<T extends G3Serializable, V> extends AbstractPropertyValueConverter<T, V> {
	private Function<T, V> to;
	private BiFunction<T, V, T> from;

	private LambdaConverter(Class<T> propertyType, Class<V> valueType, Function<T, V> to, BiFunction<T, V, T> from) {
		super(propertyType, valueType);
		this.to = to;
		this.from = from;
	}

	@Override
	public V convertTo(T source) {
		return to.apply(source);
	}

	@Override
	public T convertFrom(T old, V value) {
		return from.apply(old, value);
	}

	public static <T extends G3Serializable, V> LambdaConverter<T, V> ofInplace(Class<T> propertyType, Class<V> valueType,
			Function<T, V> to, BiConsumer<T, V> from) {
		return new LambdaConverter<>(propertyType, valueType, to, (old, value) -> {
			from.accept(old, value);
			return old;
		});
	}

	public static <T extends G3Serializable, V> LambdaConverter<T, V> of(Class<T> propertyType, Class<V> valueType, Function<T, V> to,
			BiFunction<T, V, T> from) {
		return new LambdaConverter<>(propertyType, valueType, to, from);
	}

	public static <T extends G3Serializable, V> LambdaConverter<T, V> of(Class<T> propertyType, Class<V> valueType, Function<T, V> to,
			Function<V, T> from) {
		return new LambdaConverter<>(propertyType, valueType, to, (old, value) -> from.apply(value));
	}
}

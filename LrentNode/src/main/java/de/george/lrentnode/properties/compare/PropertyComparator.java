package de.george.lrentnode.properties.compare;

import java.util.Set;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;

import de.george.g3utils.io.G3Serializable;

public interface PropertyComparator<T extends G3Serializable> {
	public boolean equals(T o1, T o2);

	/**
	 * @param <V> Type of the value to compare against, must be identical to {@code T} excpect for
	 *            {@link CompareOperation#Contains}, {@link CompareOperation#Regex} and
	 *            {@link CompareOperation#RegexIgnoreCase}.
	 * @param operation Must be one of the operations returned by {@link getSupportedCompareOperations}.
	 * @param object
	 * @param value
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public default <V> boolean satisfies(CompareOperation operation, T object, V value) {
		return switch (operation) {
			case Equals -> equals(object, (T) value);
			case NotEquals -> !equals(object, (T) value);
			case Greater -> ((ComparablePropertyComparator<T>) this).compare(object, (T) value) > 0;
			case GreaterThanEquals -> ((ComparablePropertyComparator<T>) this).compare(object, (T) value) >= 0;
			case Less -> ((ComparablePropertyComparator<T>) this).compare(object, (T) value) < 0;
			case LessThanEquals -> ((ComparablePropertyComparator<T>) this).compare(object, (T) value) <= 0;
			case EqualsIgnoreCase -> ((TextPropertyComparator<T>) this).equalsIgnoreCase(object, (T) value);
			case NotEqualsIgnoreCase -> !((TextPropertyComparator<T>) this).equalsIgnoreCase(object, (T) value);
			case Contains -> ((TextPropertyComparator<T>) this).contains(object, (T) value);
			case ContainsIgnoreCase -> ((TextPropertyComparator<T>) this).containsIgnoreCase(object, (T) value);
			case StartsWith -> ((TextPropertyComparator<T>) this).startsWith(object, (T) value);
			case StartsWithIgnoreCase -> ((TextPropertyComparator<T>) this).startsWithIgnoreCase(object, (T) value);
			case EndsWith -> ((TextPropertyComparator<T>) this).endsWith(object, (T) value);
			case EndsWithIgnoreCase -> ((TextPropertyComparator<T>) this).endsWithIgnoreCase(object, (T) value);
			case Regex, RegexIgnoreCase -> ((TextPropertyComparator<T>) this).regex(object, (Pattern) value);
			case ContainsElement -> ((ContainerPropertyComparator<T, V>) this).contains(object, value);
			case Similar -> ((FloatPropertyComparator<T>) this).similiar(object, (T) value);
			case NotSimilar -> !((FloatPropertyComparator<T>) this).similiar(object, (T) value);
			default -> throw new IllegalStateException();
		};
	}

	public default Set<CompareOperation> getSupportedCompareOperations() {
		Builder<CompareOperation> builder = ImmutableSet.builder();
		builder.add(CompareOperation.Equals, CompareOperation.NotEquals);

		if (this instanceof TextPropertyComparator<?>) {
			builder.add(CompareOperation.EqualsIgnoreCase, CompareOperation.NotEqualsIgnoreCase, CompareOperation.Contains,
					CompareOperation.ContainsIgnoreCase, CompareOperation.StartsWith, CompareOperation.StartsWithIgnoreCase,
					CompareOperation.EndsWith, CompareOperation.EndsWithIgnoreCase, CompareOperation.Regex,
					CompareOperation.RegexIgnoreCase);
		}

		if (this instanceof ComparablePropertyComparator<?>) {
			builder.add(CompareOperation.GreaterThanEquals, CompareOperation.LessThanEquals, CompareOperation.Greater,
					CompareOperation.Less);
		}

		if (this instanceof ContainerPropertyComparator<?, ?>) {
			builder.add(CompareOperation.ContainsElement);
		}

		if (this instanceof FloatPropertyComparator<?>) {
			builder.add(CompareOperation.Similar, CompareOperation.NotSimilar);
		}

		return builder.build();
	}
}

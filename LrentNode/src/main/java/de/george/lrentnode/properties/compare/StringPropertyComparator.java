package de.george.lrentnode.properties.compare;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Pattern;

import de.george.g3utils.io.G3Serializable;

public class StringPropertyComparator<T extends G3Serializable> extends TransformingPropertyComparator<T, String>
		implements TextPropertyComparator<T>, ComparablePropertyComparator<T> {

	public StringPropertyComparator(Function<T, String> valueExtractor) {
		super(valueExtractor, String::compareTo);
	}

	private boolean applyOperator(T haystack, T pattern, BiFunction<String, String, Boolean> operator) {
		return operator.apply(valueExtractor.apply(haystack), valueExtractor.apply(pattern));
	}

	private boolean applyOperatorIgnoreCase(T haystack, T pattern, BiFunction<String, String, Boolean> operator) {
		return operator.apply(valueExtractor.apply(haystack).toLowerCase(), valueExtractor.apply(pattern).toLowerCase());
	}

	@Override
	public boolean equals(T haystack, T pattern) {
		return applyOperator(haystack, pattern, String::equals);
	}

	@Override
	public boolean equalsIgnoreCase(T haystack, T pattern) {
		return applyOperator(haystack, pattern, String::equalsIgnoreCase);
	}

	@Override
	public boolean contains(T haystack, T pattern) {
		return applyOperator(haystack, pattern, String::contains);
	}

	@Override
	public boolean containsIgnoreCase(T haystack, T pattern) {
		return applyOperatorIgnoreCase(haystack, pattern, String::contains);
	}

	@Override
	public boolean startsWith(T haystack, T pattern) {
		return applyOperator(haystack, pattern, String::startsWith);
	}

	@Override
	public boolean startsWithIgnoreCase(T haystack, T pattern) {
		return applyOperatorIgnoreCase(haystack, pattern, String::startsWith);
	}

	@Override
	public boolean endsWith(T haystack, T pattern) {
		return applyOperator(haystack, pattern, String::endsWith);
	}

	@Override
	public boolean endsWithIgnoreCase(T haystack, T pattern) {
		return applyOperatorIgnoreCase(haystack, pattern, String::endsWith);
	}

	@Override
	public boolean regex(T haystack, Pattern pattern) {
		return pattern.matcher(valueExtractor.apply(haystack)).find();
	}
}

package de.george.hamcrest;

import java.util.function.BiPredicate;

import org.hamcrest.Matcher;

public abstract class MoreMatchers {
	public static <T> Matcher<T> equalTo(final T value, BiPredicate<T, T> predicate) {
		return new IsEqual<>(value, predicate);
	}
}

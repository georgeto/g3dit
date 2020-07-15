/*
 * Copyright (c) 2000-2006 hamcrest.org
 */
package de.george.hamcrest;

import java.util.function.BiPredicate;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;

/**
 * Is the value equal to another value, as tested by the {@link java.lang.Object#equals}
 * invokedMethod?
 */
public class IsEqual<T> extends BaseMatcher<T> {
	private final T expectedValue;
	private final BiPredicate<T, T> predicate;

	public IsEqual(T expectedValue, BiPredicate<T, T> predicate) {
		this.expectedValue = expectedValue;
		this.predicate = predicate;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean matches(Object actualValue) {
		return predicate.test((T) actualValue, expectedValue);
	}

	@Override
	public void describeTo(Description description) {
		description.appendValue(expectedValue);
	}

	@Factory
	public static <T> Matcher<T> equalTo(T operand, BiPredicate<T, T> predicate) {
		return new IsEqual<>(operand, predicate);
	}
}

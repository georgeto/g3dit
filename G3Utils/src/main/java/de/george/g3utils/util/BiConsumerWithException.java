package de.george.g3utils.util;

import java.util.function.Consumer;

/**
 * Represents an operation that accepts two input arguments and returns no result. This is the
 * two-arity specialization of {@link Consumer}. Unlike most other functional interfaces,
 * {@code BiConsumer} is expected to operate via side-effects.
 * <p>
 * This is a <a href="package-summary.html">functional interface</a> whose functional method is
 * {@link #accept(Object, Object)}.
 *
 * @param <T> the type of the first argument to the operation
 * @param <U> the type of the second argument to the operation
 * @see Consumer
 * @since 1.8
 */
@FunctionalInterface
public interface BiConsumerWithException<T, U> {

	default void accept(T t, U u) throws RuntimeException {
		try {
			acceptWithException(t, u);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Performs this operation on the given arguments.
	 *
	 * @param t the first input argument
	 * @param u the second input argument
	 */
	void acceptWithException(T t, U u) throws Exception;
}

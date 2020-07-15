package de.george.g3utils.util;

/**
 * Represents a function that accepts one argument and produces a result and can throw an exception.
 * <p>
 * This is a <a href="package-summary.html">functional interface</a> whose functional method is
 * {@link #apply(Object)}.
 *
 * @param <T> the type of the input to the function
 * @param <R> the type of the result of the function
 * @since 1.8
 */
@FunctionalInterface
public interface FunctionWithException<T, R> {
	default R apply(T t) throws RuntimeException {
		try {
			return applyWithException(t);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Applies this function to the given argument.
	 *
	 * @param t the function argument
	 * @return the function result
	 */
	R applyWithException(T t) throws Exception;
}

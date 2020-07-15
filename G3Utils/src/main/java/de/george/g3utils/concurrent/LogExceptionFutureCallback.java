package de.george.g3utils.concurrent;

import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.FutureCallback;

@FunctionalInterface
public interface LogExceptionFutureCallback<V> extends FutureCallback<V> {
	@Override
	default void onFailure(Throwable t) {
		LoggerFactory.getLogger(getClass()).warn("Execution failed.", t);
	}

}

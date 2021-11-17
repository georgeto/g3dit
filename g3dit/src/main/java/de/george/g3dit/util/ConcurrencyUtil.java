package de.george.g3dit.util;

import java.math.RoundingMode;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.math.IntMath;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

public final class ConcurrencyUtil {
	private static final Logger logger = LoggerFactory.getLogger(ConcurrencyUtil.class);

	private ConcurrencyUtil() {}

	public static ListenableFuture<Void> executeAndInvokeLater(Runnable task, FutureCallback<Void> callback,
			ListeningExecutorService executorService) {
		return executeAndInvokeLater(Executors.<Void>callable(task, null), callback, executorService);
	}

	public static <V> ListenableFuture<V> executeAndInvokeLater(Callable<V> task, FutureCallback<V> callback,
			ListeningExecutorService executorService) {
		ListenableFuture<V> future = executorService.submit(task);
		Futures.addCallback(future, new FutureCallback<V>() {
			@Override
			public void onSuccess(V result) {
				SwingUtilities.invokeLater(() -> callback.onSuccess(result));
			}

			@Override
			public void onFailure(Throwable t) {
				SwingUtilities.invokeLater(() -> callback.onFailure(t));
			}
		}, MoreExecutors.directExecutor());
		return future;
	}

	public static <V> ListenableFuture<V> executeAndInvokeLaterOnSuccess(Callable<V> task, Consumer<V> callback,
			ListeningExecutorService executorService) {
		ListenableFuture<V> future = executorService.submit(task);
		Futures.addCallback(future, new FutureCallback<V>() {
			@Override
			public void onSuccess(V result) {
				SwingUtilities.invokeLater(() -> callback.accept(result));
			}

			@Override
			public void onFailure(Throwable t) {
				logger.warn("Execution of '{}' failed with callback '{}' failed.", task.getClass(), callback.getClass(), t);
			}
		}, MoreExecutors.directExecutor());
		return future;
	}

	private static final int NUMBER_OF_PROCESSORS = Runtime.getRuntime().availableProcessors();
	private static final ExecutorService MULTI_EXECUTOR = Executors.newFixedThreadPool(NUMBER_OF_PROCESSORS);

	public static interface Awaitable {
		/**
		 * @see {@link CountDownLatch#await()}
		 */
		public void await();

		/**
		 * @see {@link CountDownLatch#await(long, TimeUnit)}
		 */
		public boolean await(long timeout, TimeUnit unit);
	}

	public static interface ValueAwaitable<T> {
		public T get();
	}

	private static class CountDownLatchAwaitable implements Awaitable {
		private final CountDownLatch latch;

		public CountDownLatchAwaitable(CountDownLatch latch) {
			this.latch = latch;
		}

		@Override
		public void await() {
			try {
				latch.await();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public boolean await(long timeout, TimeUnit unit) {
			try {
				return latch.await(timeout, unit);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private static class CountDownLatchValueAwaitable<T> extends CountDownLatchAwaitable implements ValueAwaitable<T> {
		public Supplier<T> value;

		public CountDownLatchValueAwaitable(CountDownLatch latch, Supplier<T> value) {
			super(latch);
			this.value = value;
		}

		@Override
		public T get() {
			await();
			return value.get();
		}

	}

	public static final <T> Awaitable processInPartitions(Consumer<T> processor, List<T> items) {
		return processInPartitions(processor, items, 1);
	}

	public static final <T> Awaitable processInPartitions(Consumer<T> processor, List<T> items, int partitionFactor) {
		int itemCount = items.size();
		int partSize = IntMath.divide(itemCount, NUMBER_OF_PROCESSORS * partitionFactor, RoundingMode.CEILING);
		int partCount = IntMath.divide(itemCount, partSize, RoundingMode.CEILING);
		CountDownLatch latch = new CountDownLatch(partCount);
		for (int p = 0; p < partCount; p++) {
			int partBegin = p * partSize;
			MULTI_EXECUTOR.submit(() -> {
				try {
					for (int i = partBegin; i < partBegin + partSize && i < itemCount; i++) {
						processor.accept(items.get(i));
					}
				} catch (Exception e) {
					logger.warn("Execution of '{}' failed failed.", processor, e);
				} finally {
					latch.countDown();
				}
			});
		}

		return new CountDownLatchAwaitable(latch);
	}

	public static final <T, V> ValueAwaitable<V> processInPartitionsAndGet(Function<T, V> processor, List<T> items, int partitionFactor) {
		int itemCount = items.size();
		int partSize = IntMath.divide(itemCount, NUMBER_OF_PROCESSORS * partitionFactor, RoundingMode.CEILING);
		int partCount = IntMath.divide(itemCount, partSize, RoundingMode.CEILING);

		AtomicReference<V> result = new AtomicReference<>(null);
		CountDownLatch latch = new CountDownLatch(partCount);
		for (int p = 0; p < partCount; p++) {
			int partBegin = p * partSize;
			MULTI_EXECUTOR.submit(() -> {
				try {
					for (int i = partBegin; i < partBegin + partSize && i < itemCount && result.get() == null; i++) {
						V value = processor.apply(items.get(i));
						if (value != null) {
							result.set(value);
							break;
						}
					}
				} catch (Exception e) {
					logger.warn("Execution of '{}' failed failed.", processor, e);
				} finally {
					latch.countDown();
				}
			});
		}
		return new CountDownLatchValueAwaitable<>(latch, result::get);
	}

	public static final <T> Awaitable processInListPartitions(Consumer<List<T>> processor, List<T> items) {
		return processInListPartitions(processor, items, 1);
	}

	public static final <T> Awaitable processInListPartitions(Consumer<List<T>> processor, List<T> items, int partitionFactor) {
		int itemCount = items.size();
		int partSize = IntMath.divide(itemCount, NUMBER_OF_PROCESSORS * partitionFactor, RoundingMode.CEILING);

		List<List<T>> partitions = Lists.partition(items, partSize);
		CountDownLatch latch = new CountDownLatch(partitions.size());
		for (List<T> partition : partitions) {
			MULTI_EXECUTOR.submit(() -> {
				try {
					processor.accept(partition);
				} catch (Exception e) {
					logger.warn("Execution of '{}' failed failed.", processor, e);
				} finally {
					latch.countDown();
				}
			});
		}
		return new CountDownLatchAwaitable(latch);
	}
}

package de.george.g3utils.io;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.george.g3utils.util.ReflectionUtils;

public class ByteBufferCleaner {
	private static final Logger logger = LoggerFactory.getLogger(ByteBufferCleaner.class);

	// Initialization-on-demand holder idiom
	private static class Holder {
		static final Consumer<MappedByteBuffer> CLEANER = ByteBufferCleaner.constructCleaner();
	}

	public static void clean(ByteBuffer buffer) {
		if (Holder.CLEANER != null && buffer instanceof MappedByteBuffer) {
			Holder.CLEANER.accept((MappedByteBuffer) buffer);
		}
	}

	private static Consumer<MappedByteBuffer> constructCleaner() {
		try {
			final Method getCleaner = Class.forName("sun.nio.ch.DirectBuffer").getMethod("cleaner");
			final Method clean = Class.forName("sun.misc.Cleaner").getMethod("clean");
			return buffer -> {
				var cleaner = ReflectionUtils.invoke(getCleaner, buffer);
				ReflectionUtils.invoke(clean, cleaner);
			};
		} catch (ReflectiveOperationException e) {
			try {
				Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
				Field unsafeField = unsafeClass.getDeclaredField("theUnsafe");
				unsafeField.setAccessible(true);
				final Object unsafe = unsafeField.get(null);
				Method invokeCleaner = unsafeClass.getMethod("invokeCleaner", ByteBuffer.class);
				return buffer -> ReflectionUtils.invoke(invokeCleaner, unsafe, buffer);
			} catch (ReflectiveOperationException ne) {
				logger.warn("Failed to find byte buffer cleaner interface.");
				return null;
			}

		}
	}
}

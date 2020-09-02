package de.george.g3dit.cache;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.george.g3dit.EditorContext;

public class CacheManager {
	private static final Logger logger = LoggerFactory.getLogger(CacheManager.class);

	public static final String CACHE_FOLDER = "cache";

	private EditorContext ctx;

	public CacheManager(EditorContext ctx) {
		this.ctx = ctx;
		File cacheDir = new File(CACHE_FOLDER);
		if (!cacheDir.exists()) {
			cacheDir.mkdir();
		}
	}

	@SuppressWarnings("rawtypes")
	private final Map<Class, AbstractCache> caches = new HashMap<>();

	public <T extends AbstractCache<T>> void registerCache(Class<T> tClass, T t) {
		caches.put(tClass, t);
	}

	@SuppressWarnings("unchecked")
	public <T extends AbstractCache<T>> T getCache(Class<T> tClass) {
		return (T) caches.get(tClass);
	}

	public <T extends AbstractCache<T>> void createCache(Class<T> tClass) {
		try {
			T cache = getCache(tClass);
			cache.create();
			if (cache.isValid()) {
				cache.save(new File(cache.getSavePath()));
			}
			ctx.runGC();
		} catch (Exception e) {
			logger.warn("Failed to create cache {}.", tClass.getSimpleName(), e);
		}
	}

	public void save() {
		for (AbstractCache<?> cache : caches.values()) {
			try {
				if (cache.isChanged()) {
					cache.save(new File(cache.getSavePath()));
				}
			} catch (IOException e) {
				logger.warn("Failed to save cache {}.", cache.getClass().getSimpleName(), e);
			}
		}
	}
}

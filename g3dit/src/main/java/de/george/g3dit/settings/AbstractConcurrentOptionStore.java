package de.george.g3dit.settings;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class AbstractConcurrentOptionStore implements MigratableOptionStore {
	protected final ConcurrentMap<String, Object> options = new ConcurrentHashMap<>();

	@Override
	public <T> void put(Option<T> option, T value) {
		options.put(option.getName(), value);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T get(Option<T> option) {
		return (T) options.getOrDefault(option.getName(), option.getDefaultValue());
	}

	@Override
	public <T> void remove(Option<T> option) {
		options.remove(option.getName());
	}

	@Override
	public <T> void put(String optionName, T value) {
		options.put(optionName, value);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T get(String optionName) {
		return (T) options.get(optionName);
	}

	@Override
	public void remove(String optionName) {
		options.remove(optionName);
	}

	public Map<String, Object> getOptions() {
		return Collections.unmodifiableMap(options);
	}
}

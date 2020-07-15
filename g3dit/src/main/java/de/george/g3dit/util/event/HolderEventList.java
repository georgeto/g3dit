package de.george.g3dit.util.event;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import com.google.common.collect.MapMaker;

public class HolderEventList<T> {
	private Map<Object, Set<Consumer<T>>> listeners = new MapMaker().weakKeys().makeMap();

	public void addListener(Object holder, Consumer<T> listener) {
		if (!listeners.containsKey(holder)) {
			listeners.put(holder, new HashSet<>());
		}
		listeners.get(holder).add(listener);
	}

	public void removeListener(Object holder, Consumer<T> listener) {
		if (listeners.containsKey(holder)) {
			Set<Consumer<T>> set = listeners.get(holder);
			set.remove(listener);
			if (set.isEmpty()) {
				listeners.remove(holder);
			}
		}
	}

	public void removeListeners(Object holder) {
		listeners.remove(holder);
	}

	public void notify(T param) {
		listeners.values().forEach(s -> s.forEach(l -> l.accept(param)));
	}
}

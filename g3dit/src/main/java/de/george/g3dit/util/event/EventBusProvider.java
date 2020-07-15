package de.george.g3dit.util.event;

import com.google.common.eventbus.EventBus;

public class EventBusProvider implements IEventBusProvider {

	private EventBus eventBus;

	public EventBusProvider() {
		eventBus = new EventBus();
	}

	public EventBusProvider(String busIdentifier) {
		eventBus = new EventBus(busIdentifier);
	}

	@Override
	public EventBus eventBus() {
		return eventBus;
	}

}

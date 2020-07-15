package de.george.g3dit.gui.components.tab;

import de.george.g3dit.util.event.CancelableEvent;

public class TabCloseEvent<T> extends CancelableEvent {
	private T tab;

	public TabCloseEvent(T tab) {
		this.tab = tab;
	}

	public T getTab() {
		return tab;
	}
}

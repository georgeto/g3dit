package de.george.g3dit.gui.components.tab;

public class TabRemovedEvent<T> {
	private T tab;

	public TabRemovedEvent(T tab) {
		this.tab = tab;
	}

	public T getTab() {
		return tab;
	}
}

package de.george.g3dit.gui.components.tab;

import java.util.Optional;

public class TabSelectedEvent<T> {
	private Optional<T> tab;
	private Optional<T> previousTab;

	public TabSelectedEvent(Optional<T> previousTab, Optional<T> tab) {
		this.previousTab = previousTab;
		this.tab = tab;
	}

	public Optional<T> getTab() {
		return tab;
	}

	public Optional<T> getPreviousTab() {
		return previousTab;
	}
}

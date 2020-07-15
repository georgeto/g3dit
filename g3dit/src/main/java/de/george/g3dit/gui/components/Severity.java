package de.george.g3dit.gui.components;

import de.george.g3dit.util.Icons;

public enum Severity {
	Info(Icons.Signal.INFO),
	Warn(Icons.Signal.WARN),
	Error(Icons.Signal.ERROR);

	private final String icon;

	private Severity(String icon) {
		this.icon = icon;
	}

	public String getIcon() {
		return icon;
	}
}

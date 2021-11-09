package de.george.g3dit.gui.components;

import com.teamunify.i18n.I;

import de.george.g3dit.util.Icons;

public enum Severity {
	Info(Icons.Signal.INFO, I.tr("Info")),
	Warn(Icons.Signal.WARN, I.tr("Warn")),
	Error(Icons.Signal.ERROR, I.tr("Error"));

	private final String icon;
	private final String displayName;

	private Severity(String icon, String displayName) {
		this.icon = icon;
		this.displayName = displayName;
	}

	public String getIcon() {
		return icon;
	}

	public String getDisplayName() {
		return displayName;
	}
}

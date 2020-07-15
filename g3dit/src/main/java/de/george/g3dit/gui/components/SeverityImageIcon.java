package de.george.g3dit.gui.components;

import javax.swing.ImageIcon;

import de.george.g3dit.util.Icons;

public class SeverityImageIcon extends ImageIcon implements Comparable<SeverityImageIcon> {
	private final Severity severity;

	public SeverityImageIcon(Severity severity) {
		this(severity, severity.name());
	}

	public SeverityImageIcon(Severity severity, String message) {
		super(Icons.getImageIcon(severity.getIcon()).getImage(), message);
		this.severity = severity;
	}

	@Override
	public int compareTo(SeverityImageIcon o) {
		int result = severity.compareTo(o.severity);
		if (result == 0) {
			result = getDescription().compareTo(o.getDescription());
		}
		return result;
	}

	public Severity getSeverity() {
		return severity;
	}
}

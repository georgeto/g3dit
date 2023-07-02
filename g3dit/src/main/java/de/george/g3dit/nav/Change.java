package de.george.g3dit.nav;

import java.nio.file.Path;
import java.util.List;

import de.george.g3dit.gui.components.Severity;

public interface Change {
	String getGuid();

	Path getFile();

	Severity getSeverity();

	String getMessage();

	String getDetails();

	boolean isFixed();

	boolean canBeFixed();

	void setFixed(boolean fixed);

	void fix();

	void showInEditor();

	void showOnMap();

	void teleport();

	List<Change> dependsOn();
}

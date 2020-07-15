package de.george.g3dit.nav;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.george.g3dit.gui.components.Severity;

public abstract class BaseChange implements Change {
	private final String guid;
	private Severity severity;
	private final String message;
	private String details;
	private boolean fixed;
	private List<Change> dependsOn;

	public BaseChange(String guid, Severity severity, String message, String details) {
		this.guid = guid;
		this.severity = severity;
		this.message = message;
		this.details = details;
	}

	@Override
	public String getGuid() {
		return guid;
	}

	@Override
	public String getMessage() {
		return message;
	}

	@Override
	public Severity getSeverity() {
		return severity;
	}

	@Override
	public String getDetails() {
		return details;
	}

	@Override
	public final boolean canBeFixed() {
		return fixable() && dependsOn().stream().allMatch(Change::canBeFixed);
	}

	protected boolean fixable() {
		return true;
	}

	@Override
	public boolean isFixed() {
		return fixed;
	}

	@Override
	public void setFixed(boolean fixed) {
		if (canBeFixed() && fixed && !isFixed()) {
			fix();
		}
	}

	@Override
	public void fix() {
		if (canBeFixed()) {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public void showInEditor() {}

	@Override
	public void showOnMap() {}

	@Override
	public void teleport() {}

	public void markFixed() {
		fixed = true;
	}

	@Override
	public List<Change> dependsOn() {
		return dependsOn != null ? dependsOn : Collections.emptyList();
	}

	public BaseChange dependsOn(Change change) {
		if (change != null) {
			if (dependsOn == null) {
				dependsOn = new ArrayList<>(2);
			}
			dependsOn.add(change);
		}
		return this;
	}
}

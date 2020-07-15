package de.george.g3dit.check.problem;

import de.george.g3dit.EditorContext;

public abstract class AbstractBaseProblem implements Problem {
	private Problem parent;
	private Severity severity;
	private String message;
	private String details;

	public AbstractBaseProblem(String message) {
		this(message, null);
	}

	public AbstractBaseProblem(String message, String details) {
		this.message = message;
		this.details = details;
	}

	public void setParent(Problem parent) {
		this.parent = parent;
	}

	@Override
	public Problem getParent() {
		return parent;
	}

	@Override
	public Severity getSeverity() {
		return severity;
	}

	public void setSeverity(Severity severity) {
		this.severity = severity;
	}

	@Override
	public String getMessage() {
		return message;
	}

	@Override
	public String getDetails() {
		return details;
	}

	@Override
	public boolean canNavigate() {
		return false;
	}

	@Override
	public boolean navigate(EditorContext context) {
		return false;
	}

	@Override
	public boolean canFix() {
		return false;
	}

	@Override
	public boolean fix(EditorContext context) {
		return false;
	}
}

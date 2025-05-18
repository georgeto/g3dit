package de.george.g3dit.check.problem;

import de.george.g3dit.EditorContext;
import de.george.g3dit.gui.components.Severity;

public abstract class AbstractBaseProblem implements Problem {
	private Problem parent;
	private Severity severity;
	private String message;
	private String details;
	private Fixer fixer;
	private boolean fixed;

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

	public void setFixer(Fixer fixer) {
		this.fixer = fixer;
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
	public boolean isFixed() {
		return fixed;
	}

	@Override
	public boolean canBeFixed() {
		return fixer != null;
	}

	@Override
	public void fix(EditorContext context) {
		if (fixer != null && !fixed)
			fixed = fixer.fix();
	}
}

package de.george.g3dit.check.problem;

import de.george.g3dit.EditorContext;

public interface Problem {
	Problem getParent();

	Severity getSeverity();

	String getMessage();

	String getDetails();

	Category getCategory();

	boolean canNavigate();

	boolean navigate(EditorContext context);

	boolean canFix();

	boolean fix(EditorContext context);
}

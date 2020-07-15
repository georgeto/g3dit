package de.george.g3dit.check.problem;

import de.george.g3dit.EditorContext;

public class TemplateProblem extends AbstractBaseProblem {
	public TemplateProblem(String message) {
		super(message);
	}

	public TemplateProblem(String message, String details) {
		super(message, details);
	}

	@Override
	public Category getCategory() {
		return Category.Template;
	}

	@Override
	public boolean canNavigate() {
		return true;
	}

	@Override
	public boolean navigate(EditorContext context) {
		return getParent().navigate(context);
	}
}

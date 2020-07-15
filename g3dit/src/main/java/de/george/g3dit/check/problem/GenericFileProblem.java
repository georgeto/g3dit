package de.george.g3dit.check.problem;

import de.george.g3dit.EditorContext;

public class GenericFileProblem extends AbstractBaseProblem {
	public GenericFileProblem(String message) {
		super(message);
	}

	public GenericFileProblem(String message, String details) {
		super(message, details);
	}

	@Override
	public Category getCategory() {
		return Category.Misc;
	}

	@Override
	public boolean canNavigate() {
		return getParent() != null && getParent().canNavigate();
	}

	@Override
	public boolean navigate(EditorContext context) {
		return getParent().navigate(context);
	}
}

package de.george.g3dit.check.problem;

import de.george.g3dit.EditorContext;

public class EntityProblem extends AbstractBaseProblem {
	public EntityProblem(String message) {
		super(message);
	}

	public EntityProblem(String message, String details) {
		super(message, details);
	}

	@Override
	public Category getCategory() {
		return Category.Entity;
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

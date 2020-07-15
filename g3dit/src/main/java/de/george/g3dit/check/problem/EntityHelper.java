package de.george.g3dit.check.problem;

import de.george.g3dit.EditorContext;
import de.george.g3dit.check.EntityDescriptor;

public class EntityHelper implements Problem {
	private final EntityDescriptor descriptor;
	private final FileHelper parent;

	public EntityHelper(EntityDescriptor descriptor, FileHelper parent) {
		this.descriptor = descriptor;
		this.parent = parent;
	}

	public EntityDescriptor getDescriptor() {
		return descriptor;
	}

	@Override
	public FileHelper getParent() {
		return parent;
	}

	@Override
	public Severity getSeverity() {
		return null;
	}

	@Override
	public String getMessage() {
		return descriptor.getDisplayName();
	}

	@Override
	public String getDetails() {
		return String.format("Name: %s\nGuid: %s\nIndex: %d", descriptor.getDisplayName(), descriptor.getGuid(), descriptor.getIndex());
	}

	@Override
	public Category getCategory() {
		return Category.Helper;
	}

	@Override
	public boolean canNavigate() {
		return true;
	}

	@Override
	public boolean navigate(EditorContext context) {
		return context.getEditor().openEntity(descriptor);
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

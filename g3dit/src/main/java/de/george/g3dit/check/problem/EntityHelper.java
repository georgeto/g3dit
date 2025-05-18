package de.george.g3dit.check.problem;

import static j2html.TagCreator.dd;
import static j2html.TagCreator.dl;
import static j2html.TagCreator.dt;

import com.teamunify.i18n.I;

import de.george.g3dit.EditorContext;
import de.george.g3dit.check.EntityDescriptor;
import de.george.g3dit.gui.components.Severity;

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
		return dl(dt(I.tr("Name")), dd(descriptor.getDisplayName()), dt(I.tr("Guid")), dd(descriptor.getGuid()), dt(I.tr("Index")),
				dd(String.valueOf(descriptor.getIndex()))).render();
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
	public boolean isFixed() {
		return false;
	}

	@Override
	public boolean canBeFixed() {
		return false;
	}

	@Override
	public void fix(EditorContext context) {}
}

package de.george.g3dit.check.problem;

import de.george.g3dit.EditorContext;
import de.george.g3dit.check.FileDescriptor;

public class FileHelper implements Problem {
	private final FileDescriptor descriptor;

	public FileHelper(FileDescriptor descriptor) {
		this.descriptor = descriptor;
	}

	public FileDescriptor getDescriptor() {
		return descriptor;
	}

	@Override
	public FileHelper getParent() {
		return null;
	}

	@Override
	public Severity getSeverity() {
		return null;
	}

	@Override
	public String getMessage() {
		return descriptor.getPath().getName();
	}

	@Override
	public String getDetails() {
		return String.format("Pfad: %s", descriptor.getPath().getAbsolutePath());
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
		if (context.getEditor().openOrSelectFile(descriptor.getPath())) {
			return true;
		}

		return context.getFileManager().explorePath(descriptor.getPath());
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

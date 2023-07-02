package de.george.g3dit.check.problem;

import static j2html.TagCreator.dd;
import static j2html.TagCreator.dl;
import static j2html.TagCreator.dt;

import java.util.function.Function;

import com.teamunify.i18n.I;

import de.george.g3dit.EditorContext;
import de.george.g3dit.check.FileDescriptor;
import de.george.g3dit.gui.components.Severity;
import de.george.g3utils.util.FilesEx;

public class FileHelper implements Problem {
	private final FileDescriptor descriptor;
	private final Function<Problem, String> pathResolver;

	public FileHelper(FileDescriptor descriptor, Function<Problem, String> pathResolver) {
		this.descriptor = descriptor;
		this.pathResolver = pathResolver;
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
		return FilesEx.getFileName(descriptor.getPath());
	}

	@Override
	public String getDetails() {
		return dl(dt(I.tr("Name")), dd(FilesEx.getFileName(descriptor.getPath())), dt(I.tr("Path")), dd(pathResolver.apply(this)))
				.render();
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

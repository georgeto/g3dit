package de.george.g3dit.check.checks;

import java.io.File;
import java.util.List;
import java.util.function.Supplier;

import de.george.g3dit.check.Check;
import de.george.g3dit.check.EntityDescriptor;
import de.george.g3dit.check.FileDescriptor;
import de.george.g3dit.check.problem.EntityProblem;
import de.george.g3dit.check.problem.ProblemConsumer;
import de.george.g3dit.check.problem.Severity;
import de.george.g3dit.check.problem.TemplateProblem;
import de.george.g3utils.util.Holder;
import de.george.lrentnode.archive.ArchiveFile;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.template.TemplateEntity;
import de.george.lrentnode.template.TemplateFile;

public abstract class AbstractEntityCheck implements Check {
	private final String name;
	private final String description;
	private final int templatePasses;
	private final int archivePasses;

	public AbstractEntityCheck(String name, String description, int templatePasses, int archivePasses) {
		this.name = name;
		this.description = description;
		this.templatePasses = templatePasses;
		this.archivePasses = archivePasses;
	}

	@Override
	public String getTitle() {
		return name;
	}

	@Override
	public String getDescription() {
		return description;
	}

	protected boolean onProcessArchive(ArchiveFile archiveFile, File dataFile, int pass) {
		return true;
	}

	public static enum EntityPassStatus {
		Next,
		ArchiveDone,
		PassDone
	}

	@Override
	public PassStatus processArchive(ArchiveFile archiveFile, File dataFile, int pass, ProblemConsumer problemConsumer) {
		if (!onProcessArchive(archiveFile, dataFile, pass)) {
			return PassStatus.Next;
		}

		FileDescriptor fileDescriptor = new FileDescriptor(dataFile, archiveFile.getArchiveType());

		Holder<Integer> entityPositon = new Holder<>(0);
		Holder<EntityDescriptor> entityDescriptor = new Holder<>();
		for (eCEntity entity : archiveFile) {
			entityDescriptor.hold(null);
			Supplier<EntityDescriptor> getDescriptor = () -> {
				if (entityDescriptor.held() == null) {
					entityDescriptor.hold(new EntityDescriptor(entity, entityPositon.held(), fileDescriptor));
				}
				return entityDescriptor.held();
			};

			EntityPassStatus status = processEntity(archiveFile, dataFile, entity, entityPositon.held(), pass, getDescriptor,
					(severity, message, details) -> postEntityProblem(problemConsumer, getDescriptor.get(), severity, message, details));
			entityPositon.hold(entityPositon.held() + 1);

			if (status == EntityPassStatus.ArchiveDone) {
				return PassStatus.Next;
			}

			if (status == EntityPassStatus.PassDone) {
				return PassStatus.Done;
			}
		}
		return PassStatus.Next;
	}

	protected abstract EntityPassStatus processEntity(ArchiveFile archiveFile, File dataFile, eCEntity entity, int entityPosition,
			int pass, Supplier<EntityDescriptor> descriptor, StringProblemConsumer problemConsumer);

	@Override
	public PassStatus processTemplate(TemplateFile tple, File dataFile, int pass, ProblemConsumer problemConsumer) {
		TemplateEntity entity = tple.getReferenceHeader();
		if (entity != null) {
			FileDescriptor descriptor = new FileDescriptor(dataFile, FileDescriptor.FileType.Template);
			return processTemplateEntity(tple, dataFile, entity, pass, descriptor,
					(severity, message, details) -> postTemplateProblem(problemConsumer, descriptor, severity, message, details));
		}
		return PassStatus.Next;
	}

	protected PassStatus processTemplateEntity(TemplateFile tple, File dataFile, eCEntity entity, int pass, FileDescriptor descriptor,
			StringProblemConsumer problemConsumer) {
		return PassStatus.Done;
	}

	@Override
	public void reportProblems(ProblemConsumer problemConsumer) {}

	@Override
	public int getTemplatePasses() {
		return templatePasses;
	}

	@Override
	public int getArchivePasses() {
		return archivePasses;
	}

	@Override
	public void reset() {}

	protected void postEntityProblem(ProblemConsumer problemConsumer, EntityDescriptor descriptor, Severity severity, String message,
			String details) {
		EntityProblem problem = new EntityProblem(message, details);
		problem.setSeverity(severity);
		problem.setParent(problemConsumer.getEntityHelper(descriptor));
		problemConsumer.post(problem);
	}

	protected void postTemplateProblem(ProblemConsumer problemConsumer, FileDescriptor descriptor, Severity severity, String message,
			String details) {
		TemplateProblem problem = new TemplateProblem(message, details);
		problem.setSeverity(severity);
		problem.setParent(problemConsumer.getFileHelper(descriptor));
		problemConsumer.post(problem);
	}

	protected interface StringProblemConsumer {
		void post(Severity severity, String message, String details);

		default void info(String message) {
			info(message, null);
		}

		default void info(String message, String details) {
			post(Severity.Info, message, details);
		}

		default void warning(String message) {
			warning(message, null);
		}

		default void warning(String message, String details) {
			post(Severity.Warning, message, details);
		}

		default void fatal(String message) {
			fatal(message, null);
		}

		default void fatal(String message, String details) {
			post(Severity.Fatal, message, details);
		}

		default void postIfDetailsNotEmpy(Severity severity, String message, List<String> details) {
			if (details != null && !details.isEmpty()) {
				post(severity, message, String.join("<br>", details));
			}
		}
	}
}

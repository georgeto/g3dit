package de.george.g3dit.check;

import java.io.File;

import de.george.g3dit.check.problem.ProblemConsumer;
import de.george.lrentnode.archive.ArchiveFile;
import de.george.lrentnode.template.TemplateFile;

public interface EntityCheck {
	String getTitle();

	String getDescription();

	public static enum PassStatus {
		Next,
		Done
	}

	/**
	 * Template-Pass wird vor Archive-Pass ausgeführt
	 *
	 * @param tple
	 * @param dataFile
	 * @param pass
	 * @param problemConsumer
	 * @return
	 */
	PassStatus processTemplate(TemplateFile tple, File dataFile, int pass, ProblemConsumer problemConsumer);

	/**
	 * Archive-Pass wird nach Template-Pass ausgeführt
	 *
	 * @param archiveFile
	 * @param dataFile
	 * @param pass
	 * @param problemConsumer
	 * @return
	 */
	PassStatus processArchive(ArchiveFile archiveFile, File dataFile, int pass, ProblemConsumer problemConsumer);

	void reportProblems(ProblemConsumer problemConsumer);

	int getTemplatePasses();

	int getArchivePasses();

	void reset();
}

package de.george.g3dit.check;

import java.io.File;

import de.george.g3dit.check.problem.ProblemConsumer;
import de.george.lrentnode.archive.ArchiveFile;
import de.george.lrentnode.template.TemplateFile;

/**
 * Implementations must not assume that the they are executed on the Swing Event Dispatch Thread.
 */
public interface Check {
	String getTitle();

	String getDescription();

	public static enum PassStatus {
		Next,
		Done
	}

	/**
	 * Template pass gets executed before archive pass
	 *
	 * @param tple
	 * @param dataFile
	 * @param pass
	 * @param problemConsumer
	 * @return
	 */
	PassStatus processTemplate(TemplateFile tple, File dataFile, int pass, ProblemConsumer problemConsumer);

	/**
	 * Archive pass gets executed after template pass
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

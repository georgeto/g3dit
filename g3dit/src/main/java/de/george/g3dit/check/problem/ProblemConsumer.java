package de.george.g3dit.check.problem;

import de.george.g3dit.check.EntityDescriptor;
import de.george.g3dit.check.FileDescriptor;

public interface ProblemConsumer {
	void post(Problem problem);

	EntityHelper getEntityHelper(EntityDescriptor entity);

	FileHelper getFileHelper(FileDescriptor file);

	default void info(AbstractBaseProblem problem) {
		problem.setSeverity(Severity.Info);
		post(problem);
	}

	default void warning(AbstractBaseProblem problem) {
		problem.setSeverity(Severity.Warning);
		post(problem);
	}

	default void fatal(AbstractBaseProblem problem) {
		problem.setSeverity(Severity.Fatal);
		post(problem);
	}
}

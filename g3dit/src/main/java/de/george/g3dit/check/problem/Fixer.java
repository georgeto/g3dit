package de.george.g3dit.check.problem;

@FunctionalInterface
public interface Fixer {

	/**
	 * Apply the fix.
	 *
	 * @return Whether the fix was successfully applied.
	 */
	boolean fix();
}

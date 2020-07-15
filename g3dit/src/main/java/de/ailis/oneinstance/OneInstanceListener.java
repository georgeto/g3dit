/*
 * Copyright (C) 2012 Klaus Reimer <k@ailis.de> See LICENSE.txt for licensing information.
 */

package de.ailis.oneinstance;

import java.io.File;
import java.io.PrintWriter;
import java.util.function.Consumer;

/**
 * Interface for listeners which are informed about a new instance which wants to start.
 *
 * @author Klaus Reimer (k@ailis.de)
 */
public interface OneInstanceListener {
	public enum InstanceAction {
		Allow,
		Exit,
		Wait
	}

	/**
	 * Called when a new application instance was created.
	 *
	 * @param workingDir The current working directory of the client. Needed if relative pathnames
	 *            are specified on the command line because the server may currently be in a
	 *            different directory than the client.
	 * @param args The command line arguments which was given to the new application instance.
	 * @param writer
	 * @param exitCode
	 * @return True if new application instance is allowed to start, false if not.
	 */
	InstanceAction newInstanceCreated(File workingDir, String[] args, PrintWriter writer, Consumer<Integer> exitCode);
}

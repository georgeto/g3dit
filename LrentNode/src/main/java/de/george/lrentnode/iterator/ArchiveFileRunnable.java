package de.george.lrentnode.iterator;

import de.george.lrentnode.archive.ArchiveFile;

public abstract class ArchiveFileRunnable implements Runnable {

	protected ArchiveFile file;

	public ArchiveFileRunnable(ArchiveFile file) {
		this.file = file;
	}

}

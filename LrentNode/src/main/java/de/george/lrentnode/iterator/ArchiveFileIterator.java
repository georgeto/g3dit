package de.george.lrentnode.iterator;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.george.lrentnode.archive.ArchiveFile;
import de.george.lrentnode.util.FileUtil;

public class ArchiveFileIterator implements Iterator<ArchiveFile> {
	private static final Logger logger = LoggerFactory.getLogger(ArchiveFileIterator.class);

	private List<Path> files;
	private int position;
	private ArchiveFile nextFile;
	private Path nextDataFile;

	private boolean skipPropertySets;

	public ArchiveFileIterator(List<Path> files) {
		this(files, false);
	}

	public ArchiveFileIterator(List<Path> files, boolean skipPropertySets) {
		this.files = files;
		this.skipPropertySets = skipPropertySets;
		position = 0;
	}

	@Override
	public boolean hasNext() {
		while (position < files.size()) {
			Path file = files.get(position++);
			if (Files.isRegularFile(file)) {
				ArchiveFile aFile = null;
				try {
					aFile = FileUtil.openArchive(file, false, skipPropertySets);
				} catch (Exception e) {
					logger.warn("Error while opening archive({}): {}", file.toAbsolutePath(), e.getMessage());
				}
				if (aFile != null) {
					nextFile = aFile;
					nextDataFile = file;
					return true;
				}
			}
		}
		nextFile = null;
		nextDataFile = null;
		return false;
	}

	@Override
	public ArchiveFile next() {
		return nextFile;
	}

	public Path nextFile() {
		return nextDataFile;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

}

package de.george.lrentnode.iterator;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.george.lrentnode.archive.ArchiveFile;
import de.george.lrentnode.util.FileUtil;

public class ArchiveFileIterator implements Iterator<ArchiveFile> {
	private static final Logger logger = LoggerFactory.getLogger(ArchiveFileIterator.class);

	private List<File> files;
	private int position;
	private ArchiveFile nextFile;
	private File nextDataFile;

	private boolean skipPropertySets;

	public ArchiveFileIterator(List<File> files) {
		this(files, false);
	}

	public ArchiveFileIterator(List<File> files, boolean skipPropertySets) {
		this.files = files;
		this.skipPropertySets = skipPropertySets;
		position = 0;
	}

	@Override
	public boolean hasNext() {
		while (position < files.size()) {
			File file = files.get(position++);
			if (file.isFile()) {
				ArchiveFile aFile = null;
				try {
					aFile = FileUtil.openArchive(file, false, skipPropertySets);
				} catch (Exception e) {
					logger.warn("Fehler beim Öffnen von Archiv({}): {}", file.getAbsolutePath(), e.getMessage());
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

	public File nextFile() {
		return nextDataFile;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

}

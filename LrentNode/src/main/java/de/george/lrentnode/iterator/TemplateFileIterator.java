package de.george.lrentnode.iterator;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.george.g3utils.util.FilesEx;
import de.george.g3utils.util.IOUtils;
import de.george.lrentnode.template.TemplateFile;
import de.george.lrentnode.util.FileUtil;

public class TemplateFileIterator implements Iterator<TemplateFile> {
	private static final Logger logger = LoggerFactory.getLogger(TemplateFileIterator.class);

	private List<Path> files;
	private int position;
	private TemplateFile nextTple;
	private Path nextFile;

	public TemplateFileIterator(List<Path> files) {
		this.files = files;
		position = 0;
	}

	@Override
	public boolean hasNext() {
		while (position < files.size()) {
			Path file = files.get(position++);
			if (Files.isRegularFile(file) && IOUtils.isValidTemplateFile(FilesEx.getFileName(file))) {
				TemplateFile tple = null;
				try {
					tple = FileUtil.openTemplate(file);
				} catch (Exception e) {
					logger.warn("Error while opening template({}): {}", file.toAbsolutePath(), e.getMessage());
				}
				if (tple != null) {
					nextTple = tple;
					nextFile = file;
					return true;
				}
			}
		}
		nextTple = null;
		nextFile = null;
		return false;
	}

	@Override
	public TemplateFile next() {
		return nextTple;
	}

	public Path nextFile() {
		return nextFile;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

}

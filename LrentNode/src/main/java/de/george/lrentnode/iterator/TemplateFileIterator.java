package de.george.lrentnode.iterator;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.george.lrentnode.template.TemplateFile;
import de.george.lrentnode.util.FileUtil;

public class TemplateFileIterator implements Iterator<TemplateFile> {
	private static final Logger logger = LoggerFactory.getLogger(TemplateFileIterator.class);

	private List<File> files;
	private int position;
	private TemplateFile nextTple;
	private File nextFile;

	public TemplateFileIterator(List<File> files) {
		this.files = files;
		position = 0;
	}

	@Override
	public boolean hasNext() {
		while (position < files.size()) {
			File file = files.get(position++);
			if (file.isFile() && file.getName().endsWith(".tple") && !file.getName().startsWith("_deleted_")) {
				TemplateFile tple = null;
				try {
					tple = FileUtil.openTemplate(file);
				} catch (Exception e) {
					logger.warn("Fehler beim Öffnen von Template({}): {}", file.getAbsolutePath(), e.getMessage());
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

	public File nextFile() {
		return nextFile;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

}

package de.george.g3utils.io;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import de.george.g3utils.util.IOUtils;

public class RecursiveFileLocator implements FileLocator {
	private File root;
	private String lastRootPath;
	private Map<String, String> fileTable = new HashMap<>();

	private boolean refreshOnCacheMiss;

	public RecursiveFileLocator(boolean refreshOnCacheMiss) {
		this.refreshOnCacheMiss = refreshOnCacheMiss;
	}

	public RecursiveFileLocator(String rootPath, boolean refreshOnCacheMiss) throws IOException {
		setRootPath(rootPath);
		this.refreshOnCacheMiss = refreshOnCacheMiss;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.george.g3utils.io.FileLocator#setRootPath(java.lang.String)
	 */
	@Override
	public void setRootPath(String rootPath) throws IOException {
		Objects.requireNonNull(rootPath);

		if (lastRootPath != null && lastRootPath.equals(rootPath)) {
			return;
		}

		root = new File(rootPath).getCanonicalFile();
		if (!root.exists()) {
			throw new IllegalArgumentException("Given root path \"" + root + "\" does not exist");
		}

		if (!root.isDirectory()) {
			throw new IllegalArgumentException("Given root path \"" + root + "\" is not a directory");
		}

		lastRootPath = rootPath;

		fileTable.clear();
		List<File> files = IOUtils.listFiles(rootPath, (f) -> true);
		for (File file : files) {
			fileTable.put(file.getName().toLowerCase(), file.getCanonicalPath());
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.george.g3utils.io.FileLocator#locate(java.lang.String)
	 */
	@Override
	public Optional<File> locate(String name) {
		File file = new File(root, name);
		if (!file.exists()) {
			String filePath = fileTable.get(name.toLowerCase());
			if (filePath != null) {
				file = new File(filePath);
			}
		}

		if (refreshOnCacheMiss && !file.exists()) {
			return IOUtils.findFirstFile(root.getAbsolutePath(), (ftf) -> ftf.getName().equalsIgnoreCase(name));
		}

		if (file.exists() && file.isFile()) {
			return Optional.of(file);
		} else {
			return Optional.empty();
		}
	}

}

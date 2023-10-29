package de.george.g3utils.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import de.george.g3utils.util.FilesEx;
import de.george.g3utils.util.IOUtils;

public class RecursiveFileLocator implements FileLocator {
	private Path root;
	private Path lastRootPath;
	private Map<String, Path> fileTable = new HashMap<>();

	private boolean refreshOnCacheMiss;

	public RecursiveFileLocator(boolean refreshOnCacheMiss) {
		this.refreshOnCacheMiss = refreshOnCacheMiss;
	}

	public RecursiveFileLocator(Path rootPath, boolean refreshOnCacheMiss) throws IOException {
		setRootPath(rootPath);
		this.refreshOnCacheMiss = refreshOnCacheMiss;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.george.g3utils.io.FileLocator#setRootPath(java.lang.String)
	 */
	@Override
	public void setRootPath(Path rootPath) throws IOException {
		Objects.requireNonNull(rootPath);

		if (lastRootPath != null && lastRootPath.equals(rootPath)) {
			return;
		}

		if (!Files.exists(rootPath)) {
			throw new IllegalArgumentException("Given root path \"" + rootPath + "\" does not exist");
		}

		if (!Files.isDirectory(rootPath)) {
			throw new IllegalArgumentException("Given root path \"" + rootPath + "\" is not a directory");
		}

		root = rootPath.toRealPath();
		lastRootPath = rootPath;

		fileTable.clear();
		List<Path> files = IOUtils.listFiles(root, (f) -> true);
		for (Path file : files) {
			fileTable.put(FilesEx.getFileNameLowerCase(file), file);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.george.g3utils.io.FileLocator#locate(java.lang.String)
	 */
	@Override
	public Optional<Path> locate(String name) {
		Path file = root.resolve(name);
		if (file == null || !Files.exists(file)) {
			file = fileTable.get(name.toLowerCase());
		}

		if (refreshOnCacheMiss && (file == null || !Files.exists(file))) {
			return IOUtils.findFirstFile(root, (ftf) -> FilesEx.getFileName(ftf).equalsIgnoreCase(name));
		}

		return Optional.ofNullable(file).filter(Files::isRegularFile);
	}

}

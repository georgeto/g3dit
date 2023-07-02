package de.george.g3utils.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import de.george.g3utils.util.FilesEx;
import de.george.g3utils.util.IOUtils;

public class RecursiveFileLocator implements FileLocator {
	private Path root;
	private String lastRootPath;
	private Map<String, Path> fileTable = new HashMap<>();

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

		Path newRoot = Paths.get(rootPath);
		if (!Files.exists(newRoot)) {
			throw new IllegalArgumentException("Given root path \"" + newRoot + "\" does not exist");
		}

		if (!Files.isDirectory(newRoot)) {
			throw new IllegalArgumentException("Given root path \"" + newRoot + "\" is not a directory");
		}

		root = newRoot.toRealPath();
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
		if (!Files.exists(file)) {
			file = fileTable.get(name.toLowerCase());
		}

		if (refreshOnCacheMiss && !Files.exists(file)) {
			return IOUtils.findFirstFile(root, (ftf) -> ftf.getName().equalsIgnoreCase(name));
		}

		if (Files.isRegularFile(file)) {
			return Optional.of(file);
		} else {
			return Optional.empty();
		}
	}

}

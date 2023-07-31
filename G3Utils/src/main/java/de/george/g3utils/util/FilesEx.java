package de.george.g3utils.util;

import java.nio.file.Path;
import java.util.Optional;

public final class FilesEx {

	private FilesEx() {}

	public static boolean isEmpty(Path path) {
		return !path.isAbsolute() && path.getNameCount() == 1 && path.getName(0).toString().isEmpty();
	}

	public static boolean isNotEmpty(Path path) {
		return !isEmpty(path);
	}

	public static Optional<Path> notEmpty(Path path) {
		return Optional.of(path).filter(FilesEx::isNotEmpty);
	}

	public static String getFileName(Path file) {
		return file.getFileName().toString();
	}

	public static String getFileNameLowerCase(Path file) {
		return getFileName(file).toLowerCase();
	}

	public static String getAbsolutePath(Path file) {
		return file.toAbsolutePath().toString();
	}

	public static String getFileExtension(String fileName) {
		int dotIndex = fileName.lastIndexOf('.');
		return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
	}

	public static String stripExtension(String fileName) {

		// Handle null case specially.
		if (fileName == null) {
			return null;
		}

		// Get position of last '.'.
		int pos = fileName.lastIndexOf(".");

		// If there wasn't any '.' just return the string as is.
		if (pos == -1) {
			return fileName;
		}

		// Otherwise return the string, up to the dot.
		return fileName.substring(0, pos);
	}

	public static String changeExtension(String str, String newExt) {
		return stripExtension(str) + "." + newExt;
	}

	public static Path changeExtension(Path file, String extension) {
		return file.resolveSibling(changeExtension(getFileName(file), extension));
	}

	public static String getFileExtension(Path file) {
		return getFileExtension(getFileName(file));
	}

	public static boolean hasFileExtension(Path file, String extension) {
		return extension.equals(getFileExtension(file));
	}
}

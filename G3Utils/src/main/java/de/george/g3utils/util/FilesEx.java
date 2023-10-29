package de.george.g3utils.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;

import com.google.common.io.ByteStreams;

public final class FilesEx {
	private static final int BUFFER_SIZE = 8192;

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

	public static boolean sameContent(Path file1, Path file2) throws IOException {
		if (Files.isSameFile(file1, file2))
			return true;

		if (Files.size(file1) != Files.size(file2))
			return false;

		try (InputStream in1 = Files.newInputStream(file1); InputStream in2 = Files.newInputStream(file2);) {
			byte[] buffer1 = new byte[BUFFER_SIZE];
			byte[] buffer2 = new byte[BUFFER_SIZE];
			while (true) {
				int nRead1 = ByteStreams.read(in1, buffer1, 0, BUFFER_SIZE);
				int nRead2 = ByteStreams.read(in2, buffer2, 0, BUFFER_SIZE);
				if (nRead1 != nRead2 || !Arrays.equals(buffer1, buffer2))
					return false;

				if (nRead1 < BUFFER_SIZE)
					// Reached end of file without finding mismatch.
					return true;
			}
		}
	}
}

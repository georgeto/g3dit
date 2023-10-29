package de.george.g3utils.util;

import java.nio.file.Path;

public interface PathFilter {
	boolean accept(Path path);

	static PathFilter withExt(String extension) {
		return f -> FilesEx.hasFileExtension(f, extension);
	}
}

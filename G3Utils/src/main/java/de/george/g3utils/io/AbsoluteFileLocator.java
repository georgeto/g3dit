package de.george.g3utils.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class AbsoluteFileLocator implements FileLocator {
	private static final AbsoluteFileLocator INSTANCE = new AbsoluteFileLocator();

	public static AbsoluteFileLocator instance() {
		return INSTANCE;
	}

	private AbsoluteFileLocator() {}

	@Override
	public void setRootPath(Path rootPath) throws IOException {}

	@Override
	public Optional<Path> locate(String name) {
		return Optional.of(Paths.get(name)).filter(Path::isAbsolute).filter(Files::isRegularFile);
	}
}

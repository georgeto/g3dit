package de.george.g3utils.io;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class AbsoluteFileLocator implements FileLocator {
	private static final AbsoluteFileLocator INSTANCE = new AbsoluteFileLocator();

	public static AbsoluteFileLocator instance() {
		return INSTANCE;
	}

	private AbsoluteFileLocator() {}

	@Override
	public void setRootPath(String rootPath) throws IOException {}

	@Override
	public Optional<File> locate(String name) {
		return Optional.of(new File(name)).filter(File::isAbsolute).filter(File::isFile);
	}
}

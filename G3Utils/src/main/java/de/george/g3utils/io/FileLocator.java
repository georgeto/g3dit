package de.george.g3utils.io;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

public interface FileLocator {

	void setRootPath(String rootPath) throws IOException;

	Optional<Path> locate(String name);

}

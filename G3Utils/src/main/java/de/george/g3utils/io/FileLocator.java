package de.george.g3utils.io;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public interface FileLocator {

	void setRootPath(String rootPath) throws IOException;

	Optional<File> locate(String name);

}

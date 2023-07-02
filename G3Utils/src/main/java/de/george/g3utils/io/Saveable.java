package de.george.g3utils.io;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;

public interface Saveable {
	public void save(Path file) throws IOException;

	public void save(OutputStream out) throws IOException;
}

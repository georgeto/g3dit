package de.george.g3utils.io;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

public interface Saveable {
	public void save(File file) throws IOException;

	public void save(OutputStream out) throws IOException;
}

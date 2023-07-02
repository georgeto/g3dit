package de.george.g3dit.cache;

import java.nio.file.Files;
import java.nio.file.Path;

import javax.swing.SwingWorker;

import de.george.g3utils.util.IOUtils;

public class LoadObjectWorker extends SwingWorker<Object, Void> {
	protected Path file;

	public LoadObjectWorker(Path file) {
		this.file = file;
	}

	@Override
	protected Object doInBackground() throws Exception {
		if (Files.exists(file)) {
			return IOUtils.loadObjectFromFile(file);
		}
		return null;
	}

}

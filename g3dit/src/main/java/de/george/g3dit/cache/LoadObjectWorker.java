package de.george.g3dit.cache;

import java.io.File;

import javax.swing.SwingWorker;

import de.george.g3utils.util.IOUtils;

public class LoadObjectWorker extends SwingWorker<Object, Void> {
	protected File file;

	public LoadObjectWorker(File file) {
		this.file = file;
	}

	@Override
	protected Object doInBackground() throws Exception {
		if (file.exists()) {
			return IOUtils.loadObjectFromFile(file);
		}
		return null;
	}

}

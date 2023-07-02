package de.george.g3dit.util;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.teamunify.i18n.I;

public abstract class AbstractFileWorker<T, V> extends SwingWorker<T, V> {
	private static final Logger logger = LoggerFactory.getLogger(AbstractFileWorker.class);

	private Callable<List<Path>> fileProvider;
	private List<Path> openFiles;
	protected int fileCount = -1;
	protected AtomicInteger filesDone = new AtomicInteger(0);
	protected boolean progressBarSwitched = false;
	private String startMessage;
	protected String progessMessage;
	protected String doneMessage;
	private JProgressBar progressBar;
	protected Supplier<String> doneMessageSupplier;
	private Runnable doneCallback;

	public AbstractFileWorker(Callable<List<Path>> fileProvider, String startMessage, String progessMessage, String doneMessage) {
		this(fileProvider, null, startMessage, progessMessage, doneMessage);
	}

	public AbstractFileWorker(Callable<List<Path>> fileProvider, List<Path> openFiles, String startMessage, String progessMessage,
			String doneMessage) {
		this.fileProvider = fileProvider;
		this.openFiles = openFiles;
		this.startMessage = startMessage;
		this.progessMessage = progessMessage;
		this.doneMessage = doneMessage;

	}

	protected void setProgressBar(JProgressBar progressBar) {
		this.progressBar = progressBar;
		progressBar.setString(startMessage);
		progressBar.setIndeterminate(true);
	}

	protected List<Path> getFiles() throws Exception {
		List<Path> files = fileProvider.call();
		if (openFiles != null) {
			files.removeAll(openFiles);
		}
		fileCount = files.size();
		return files;
	}

	@Override
	protected void process(List<V> results) {
		if (isCancelled()) {
			return;
		}

		if (!progressBarSwitched) {
			progressBar.setIndeterminate(false);
			progressBar.setMaximum(fileCount);
			progressBar.setStringPainted(true);
			progressBarSwitched = true;
		}
		progressBar.setValue(filesDone.get());
		progressBar.setString(I.format(progessMessage, filesDone.get(), fileCount));
	}

	@Override
	protected void done() {
		progressBar.setIndeterminate(false);
		progressBar.setValue(0);
		progressBar.setString(doneMessageSupplier != null ? doneMessageSupplier.get() : doneMessage);

		if (doneCallback != null) {
			doneCallback.run();
		}

		try {
			get();
		} catch (InterruptedException | ExecutionException e) {
			logger.warn("Error while processing files.", e);
		} catch (CancellationException e) {
			// Ignore
		}
	}

	public void setDoneCallback(Runnable doneCallback) {
		this.doneCallback = doneCallback;
	}
}

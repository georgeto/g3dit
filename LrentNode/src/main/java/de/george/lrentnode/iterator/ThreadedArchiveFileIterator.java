package de.george.lrentnode.iterator;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadedArchiveFileIterator extends ArchiveFileIterator {

	private ExecutorService executor;

	public ThreadedArchiveFileIterator(List<Path> files) {
		super(files);
		executor = Executors.newFixedThreadPool(1);// Runtime.getRuntime().availableProcessors());
	}

	public void runNext(ArchiveFileRunnable runnable) {
		executor.submit(runnable);
	}

	public ExecutorService executor() {
		return executor;
	}
}

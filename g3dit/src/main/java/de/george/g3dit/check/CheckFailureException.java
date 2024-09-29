package de.george.g3dit.check;

public class CheckFailureException extends RuntimeException {
	public final FileDescriptor file;
	public final Exception exception;

	public CheckFailureException(FileDescriptor file, Exception exception) {
		this.file = file;
		this.exception = exception;
	}
}

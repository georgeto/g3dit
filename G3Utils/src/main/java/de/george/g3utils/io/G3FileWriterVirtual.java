package de.george.g3utils.io;

public class G3FileWriterVirtual extends G3FileWriter {

	public G3FileWriterVirtual() {}

	public G3FileWriterVirtual(int initialSize) {
		super(initialSize);
	}

	public G3FileWriterVirtual(byte[] data) {
		super(data);
	}

	public G3FileWriterVirtual(String hex, int size) {
		super(hex, size);
	}

	public G3FileWriterVirtual(String hex) {
		super(hex);
	}

	public G3FileWriterVirtual(StringBuffer hex) {
		super(hex);
	}

	@Override
	public G3FileWriter writeEntry(String entry) {
		writeUnsignedShort(entry.length());
		writeString(entry);
		return this;
	}
}

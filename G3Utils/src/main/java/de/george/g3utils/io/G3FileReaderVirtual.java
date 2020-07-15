package de.george.g3utils.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class G3FileReaderVirtual extends G3FileReader {
	public G3FileReaderVirtual(byte[] bytes) {
		super(bytes);
	}

	public G3FileReaderVirtual(ByteBuffer buffer) {
		super(buffer);
	}

	public G3FileReaderVirtual(File file) throws IOException {
		super(file);
	}

	public G3FileReaderVirtual(FileChannel fileChannel) throws IOException {
		super(fileChannel);
	}

	public G3FileReaderVirtual(InputStream is) throws IOException {
		super(is);
	}

	public G3FileReaderVirtual(String hex) {
		super(hex);
	}

	@Override
	public String readEntry() {
		return readString(readShort());
	}

}

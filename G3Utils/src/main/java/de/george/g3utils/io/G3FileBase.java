package de.george.g3utils.io;

import java.nio.ByteBuffer;

public class G3FileBase {
	protected ByteBuffer buffer;

	public int getPos() {
		return buffer.position();
	}

	public int position() {
		return buffer.position();
	}

	public void seek(int pos) {
		buffer.position(pos);
	}

	public void skip(int length) {
		buffer.position(buffer.position() + length);
	}

	public ByteBuffer getBuffer() {
		return buffer;
	}
}

package de.george.g3utils.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.Collection;
import java.util.function.BiConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.george.g3utils.structure.bCBox;
import de.george.g3utils.structure.bCVector;
import de.george.g3utils.util.Converter;
import de.george.g3utils.util.Misc;

public abstract class G3FileWriter extends G3FileBase {
	private static final Logger logger = LoggerFactory.getLogger(G3FileWriter.class);

	private static int INITIAL_BUFFER_SIZE = 1000000;

	private byte[] tmpData;

	public G3FileWriter() {
		buffer = ByteBuffer.allocate(INITIAL_BUFFER_SIZE);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
	}

	public G3FileWriter(int initialSize) {
		buffer = ByteBuffer.allocate(initialSize);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
	}

	public G3FileWriter(byte[] data) {
		this();
		write(data);
	}

	public G3FileWriter(String hex) {
		this();
		write(Misc.asByte(hex));
	}

	public G3FileWriter(String hex, int size) {
		this();
		write(Misc.asByte(hex));
	}

	public G3FileWriter(StringBuffer hex) {
		this();
		write(Misc.asByte(hex.toString()));
	}

	public G3FileWriter write(byte[] data) {
		ensureCapacity(buffer.position() + data.length);
		buffer.put(data);
		return this;
	}

	public G3FileWriter write(String data) {
		write(Misc.asByte(data));
		return this;
	}

	public G3FileWriter writeBool(boolean bool) {

		writeByte((byte) (bool ? 0x01 : 0x00));
		return this;
	}

	public G3FileWriter writeByte(byte data) {
		ensureCapacity(buffer.position() + 1);
		buffer.put(data);
		return this;
	}

	public G3FileWriter writeChar(char data) {
		return writeByte((byte) data);
	}

	public G3FileWriter writeShort(short data) {
		ensureCapacity(buffer.position() + 2);
		buffer.putShort(data);
		return this;
	}

	public G3FileWriter writeInt(int offset, int data) {
		ensureCapacity(offset + 4);
		buffer.putInt(offset, data);
		return this;
	}

	public G3FileWriter writeInt(int data) {
		ensureCapacity(buffer.position() + 4);
		buffer.putInt(data);
		return this;
	}

	public G3FileWriter writeFloat(float data) {
		ensureCapacity(buffer.position() + 4);
		buffer.putFloat(data);
		return this;
	}

	public G3FileWriter writeUnsignedByte(int data) {
		writeByte((byte) data);
		return this;
	}

	public G3FileWriter writeUnsignedShort(int data) {
		writeShort((short) data);
		return this;
	}

	public G3FileWriter writeUnsignedInt(long data) {
		writeInt((int) data);
		return this;
	}

	public G3FileWriter writeFloatArray(float... data) {
		for (float flot : data) {
			writeFloat(flot);
		}
		return this;
	}

	public G3FileWriter writeFloatArray2D(float[][] data) {
		for (float[] row : data) {
			writeFloatArray(row);
		}
		return this;
	}

	public G3FileWriter writeFloatArray(Collection<Float> data) {
		for (float value : data) {
			writeFloat(value);
		}
		return this;
	}

	public G3FileWriter writeIntArray(int... data) {
		for (int value : data) {
			writeInt(value);
		}
		return this;
	}

	public G3FileWriter writeIntArray(Collection<Integer> data) {
		for (int value : data) {
			writeInt(value);
		}
		return this;
	}

	public G3FileWriter writeVector(bCVector vector) {
		writeFloat(vector.getX());
		writeFloat(vector.getY());
		writeFloat(vector.getZ());
		return this;
	}

	public G3FileWriter writeBox(bCBox box) {
		writeVector(box.getMin());
		writeVector(box.getMax());
		return this;
	}

	public G3FileWriter writeString(String string) {
		write(Converter.stringToByteArray(string));
		return this;
	}

	public G3FileWriter write(G3Serializable object) {
		object.write(this);
		return this;
	}

	public G3FileWriter write(G3Serializable... objects) {
		for (G3Serializable object : objects) {
			object.write(this);
		}
		return this;
	}

	public G3FileWriter write(Iterable<? extends G3Serializable> objects) {
		for (G3Serializable object : objects) {
			object.write(this);
		}
		return this;
	}

	public <T> G3FileWriter write(Collection<T> objects, BiConsumer<G3FileWriter, T> writer) {
		for (T object : objects) {
			writer.accept(this, object);
		}
		return this;
	}

	public G3FileWriter writeListPrefix() {
		// bTArray writes a useless byte during serialization.
		writeUnsignedByte(1);
		return this;
	}

	public G3FileWriter writeList(Collection<? extends G3Serializable> objects) {
		writeInt(objects.size());
		return write(objects);
	}

	public <T> G3FileWriter writeList(Collection<? extends T> objects, BiConsumer<G3FileWriter, T> writer) {
		writeInt(objects.size());
		for (T object : objects) {
			writer.accept(this, object);
		}
		return this;
	}

	public G3FileWriter writePrefixedList(Collection<? extends G3Serializable> objects) {
		return writeListPrefix().writeList(objects);
	}

	public <T> G3FileWriter writePrefixedList(Collection<? extends T> objects, BiConsumer<G3FileWriter, T> writer) {
		return writeListPrefix().writeList(objects, writer);
	}

	public <T extends G3Serializable> G3FileWriter writeArray(T[] objects) {
		writeInt(objects.length);
		return write(objects);
	}

	public <T> G3FileWriter writeArray(T[] objects, BiConsumer<G3FileWriter, T> writer) {
		writeInt(objects.length);
		for (T object : objects) {
			writer.accept(this, object);
		}
		return this;
	}

	public <T extends G3Serializable> G3FileWriter writePrefixedArray(T[] objects) {
		return writeListPrefix().writeArray(objects);
	}

	public <T> G3FileWriter writePrefixedArray(T[] objects, BiConsumer<G3FileWriter, T> writer) {
		return writeListPrefix().writeArray(objects, writer);
	}

	public abstract G3FileWriter writeEntry(String entry);

	public G3FileWriter startInsert(int pos) {
		tmpData = new byte[getSize() - pos];
		buffer.position(pos);
		buffer.get(tmpData);
		buffer.position(pos);
		return this;
	}

	public G3FileWriter finishInsert() {
		ensureCapacity(buffer.position() + tmpData.length);
		buffer.put(tmpData);
		tmpData = null;
		return this;
	}

	public G3FileWriter replace(String data, int pos) {
		byte[] bdata = Misc.asByte(data);
		int savePos = buffer.position();
		buffer.put(bdata, pos, bdata.length);
		buffer.position(savePos);
		return this;
	}

	public G3FileWriter replaceInt(int data, int pos) {
		int savePos = buffer.position();
		buffer.putInt(pos, data);
		buffer.position(savePos);
		return this;
	}

	public byte[] getData() {
		byte[] data = new byte[buffer.position()];
		int posSave = buffer.position();
		buffer.position(0);
		buffer.get(data, 0, data.length);
		buffer.position(posSave);
		return data;
	}

	public int getSize() {
		return buffer.position();
	}

	public void save(File file) throws IOException {
		try (FileOutputStream out = new FileOutputStream(file)) {
			buffer.flip();
			out.getChannel().write(buffer);
		}
	}

	public void save(OutputStream out) throws IOException {
		WritableByteChannel channel = Channels.newChannel(out);
		buffer.flip();
		channel.write(buffer);
	}

	/**
	 * Increases the capacity if necessary to ensure that it can hold at least the number of
	 * elements specified by the minimum capacity argument.
	 *
	 * @param minCapacity the desired minimum capacity
	 * @throws OutOfMemoryError if {@code minCapacity < 0}. This is interpreted as a request for the
	 *             unsatisfiably large capacity
	 *             {@code (long) Integer.MAX_VALUE + (minCapacity - Integer.MAX_VALUE)}.
	 */
	private void ensureCapacity(int minCapacity) {
		// overflow-conscious code
		if (minCapacity - buffer.capacity() > 0) {
			grow(minCapacity);
		}
	}

	/**
	 * The maximum size of array to allocate. Some VMs reserve some header words in an array.
	 * Attempts to allocate larger arrays may result in OutOfMemoryError: Requested array size
	 * exceeds VM limit
	 */
	private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

	/**
	 * Increases the capacity to ensure that it can hold at least the number of elements specified
	 * by the minimum capacity argument.
	 *
	 * @param minCapacity the desired minimum capacity
	 */
	private void grow(int minCapacity) {
		// overflow-conscious code
		int oldCapacity = buffer.capacity();
		int newCapacity = oldCapacity << 1;
		if (newCapacity - minCapacity < 0) {
			newCapacity = minCapacity;
		}
		if (newCapacity - MAX_ARRAY_SIZE > 0) {
			newCapacity = hugeCapacity(minCapacity);
		}

		ByteBuffer tmpBuffer = ByteBuffer.allocate(newCapacity);
		tmpBuffer.order(buffer.order());
		buffer.limit(buffer.position());
		buffer.rewind();
		tmpBuffer.put(buffer);
		buffer = tmpBuffer;
		logger.debug("Increasing buffer from {} bytes to {} bytes.", oldCapacity, newCapacity);
	}

	private static int hugeCapacity(int minCapacity) {
		if (minCapacity < 0) {
			throw new OutOfMemoryError();
		}
		return minCapacity > MAX_ARRAY_SIZE ? Integer.MAX_VALUE : MAX_ARRAY_SIZE;
	}

}

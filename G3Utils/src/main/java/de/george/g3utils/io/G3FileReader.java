package de.george.g3utils.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.teamunify.i18n.I;

import de.george.g3utils.structure.bCBox;
import de.george.g3utils.structure.bCVector;
import de.george.g3utils.util.Converter;
import de.george.g3utils.util.FilesEx;
import de.george.g3utils.util.Misc;

public abstract class G3FileReader extends G3FileBase implements AutoCloseable {
	private static final Logger logger = LoggerFactory.getLogger(G3FileReader.class);
	private static final Objenesis objenesis = new ObjenesisStd(true);

	private static final String NOT_A_FILE = I.tr("<source is not a file>");
	protected String fileName = NOT_A_FILE;

	public G3FileReader(String hex) {
		this(Misc.asByte(hex));
	}

	public G3FileReader(byte[] bytes) {
		buffer = ByteBuffer.wrap(bytes);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
	}

	public G3FileReader(ByteBuffer buffer) {
		this.buffer = buffer;
		buffer.order(ByteOrder.LITTLE_ENDIAN);
	}

	public G3FileReader(Path file) throws IOException {
		fileName = FilesEx.getFileName(file);
		try (FileChannel channel = FileChannel.open(file, StandardOpenOption.READ)) {
			fromChannel(channel);
		}
	}

	public G3FileReader(InputStream is) throws IOException {
		fromInputStream(is);
	}

	public G3FileReader(FileChannel fileChannel) throws IOException {
		fromChannel(fileChannel);
	}

	protected void fromChannel(FileChannel channel) throws IOException {
		buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
		buffer.order(ByteOrder.LITTLE_ENDIAN);
	}

	protected void fromInputStream(InputStream is) throws IOException {
		try {
			ByteArrayOutputStream tmpBuffer = new ByteArrayOutputStream();
			int nRead;
			byte[] data = new byte[2048];
			while ((nRead = is.read(data, 0, data.length)) != -1) {
				tmpBuffer.write(data, 0, nRead);
			}
			tmpBuffer.flush();

			buffer = ByteBuffer.wrap(tmpBuffer.toByteArray());
			buffer.order(ByteOrder.LITTLE_ENDIAN);
		} catch (IOException e) {
			throw e;
		}
	}

	public String read(int length) {
		try {
			byte[] bytes = new byte[length];
			buffer.get(bytes);
			return Misc.asHex(bytes);
		} catch (Exception e) {
			warn(logger, "Error during read(): " + e.getMessage());
			throw e;
		}
	}

	public byte[] readByteArray(int length) {
		byte[] bytes = new byte[length];
		buffer.get(bytes);
		return bytes;
	}

	public String readString(int length) {
		return Converter.byteArrayToString(readByteArray(length));
	}

	public String readSilent(int length) {
		return readSilent(0, length);
	}

	public String readSilent(int off, int length) {
		return Misc.asHex(readSilentByteArray(off, length));
	}

	public byte[] readSilentByteArray(int length) {
		return readSilentByteArray(0, length);
	}

	public byte[] readSilentByteArray(int off, int length) {
		try {
			byte[] bytes = new byte[length];
			int savePos = buffer.position();
			buffer.position(savePos + off);
			buffer.get(bytes);
			buffer.position(savePos);
			return bytes;
		} catch (Exception e) {
			warn(logger, "Error during readSilentByteArray(): " + e.getMessage());
			throw e;
		}
	}

	public String readGUID() {
		return read(20);
	}

	public boolean readBool() {
		return (buffer.get() & 0xFF) == 1;
	}

	public byte readByte() {
		return buffer.get();
	}

	public char readChar() {
		return (char) readByte();
	}

	public short readShort() {
		return buffer.getShort();
	}

	public int readInt() {
		return buffer.getInt();
	}

	public int readUnsignedByte() {
		return Byte.toUnsignedInt(buffer.get());
	}

	public int readUnsignedShort() {
		return Short.toUnsignedInt(buffer.getShort());
	}

	public long readUnsignedInt() {
		return Integer.toUnsignedLong(buffer.getInt());
	}

	public long readLong() {
		return buffer.getLong();
	}

	public float readFloat() {
		return buffer.getFloat();
	}

	public int[] readIntArray(int entries) {
		int[] result = new int[entries];
		for (int i = 0; i < entries; i++) {
			result[i] = buffer.getInt();
		}
		return result;
	}

	public float[] readFloatArray(int entries) {
		float[] result = new float[entries];
		for (int i = 0; i < entries; i++) {
			result[i] = buffer.getFloat();
		}
		return result;
	}

	public bCVector readVector() {
		return new bCVector(buffer.getFloat(), buffer.getFloat(), buffer.getFloat());
	}

	public bCBox readBox() {
		return new bCBox(readVector(), readVector());
	}

	public abstract String readEntry();

	public int getSize() {
		return buffer.capacity();
	}

	public int getRemainingSize() {
		return buffer.capacity() - buffer.position();
	}

	public String getData() {
		return Misc.asHex(buffer.array());
	}

	@Override
	public ByteBuffer getBuffer() {
		return buffer;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * Hängt an {@code message} die aktuelle Position und Namen der Datei an, und loggt diese über
	 * {@code logger}.
	 *
	 * @param logger
	 * @param message
	 * @param params
	 */
	public void warn(Logger logger, String message, Object... params) {
		logger.warn(message.concat(" ({}, {})"), Misc.concat(params, getPos(), getFileName()));
	}

	/**
	 * Hängt an {@code message} die aktuelle Position und Namen der Datei an, und loggt diese über
	 * {@code logger}.
	 *
	 * @param logger
	 * @param message
	 * @param params
	 */
	public void info(Logger logger, String message, Object... params) {
		logger.info(message.concat(" ({}, {})"), Misc.concat(params, getPos(), getFileName()));
	}

	/**
	 * Hängt an {@code message} die aktuelle Position und Namen der Datei an, und loggt diese über
	 * {@code logger}.
	 *
	 * @param logger
	 * @param message
	 * @param params
	 */
	public void debug(Logger logger, String message, Object... params) {
		logger.debug(message.concat(" ({}, {})"), Misc.concat(params, getPos(), getFileName()));
	}

	/**
	 * Hängt an {@code message} die aktuelle Position und Namen der Datei an, und loggt diese über
	 * {@code logger}.
	 *
	 * @param logger
	 * @param message
	 * @param params
	 */
	public void error(Logger logger, String message, Object... params) {
		logger.error(message.concat(" ({}, {})"), Misc.concat(params, getPos(), getFileName()));
	}

	/**
	 * Hängt an {@code message} die aktuelle Position und Namen der Datei an, und loggt diese über
	 * {@code logger}.
	 *
	 * @param logger
	 * @param message
	 * @param params
	 */
	public void trace(Logger logger, String message, Object... params) {
		logger.trace(message.concat(" ({}, {})"), Misc.concat(params, getPos(), getFileName()));
	}

	@Override
	public String toString() {
		int pos = getPos();
		int left = Math.max(0, pos - 10) - pos;
		int right = Math.min(getSize(), getPos() + 10) - pos;
		return String.format("@%08X: %s [%s] %s", pos, left != 0 ? readSilent(left, -left) : "", right != 0 ? readSilent(0, 1) : "",
				right > 1 ? readSilent(1, right - 1) : "");
	}

	@Override
	public void close() throws IOException {
		// Ensure that buffer is unmapped, because on Windows otherwise the file is locked until the
		// JVM exits or the ByteBuffer is garbage collected.
		ByteBufferCleaner.clean(buffer);
	}

	public <T extends G3Serializable> T read(Class<T> type) {
		T instance = objenesis.getInstantiatorOf(type).newInstance();
		instance.read(this);
		return instance;
	}

	public <T extends G3Serializable> T read(Class<T> type, int size) {
		T instance = objenesis.getInstantiatorOf(type).newInstance();
		instance.read(this, size);
		return instance;
	}

	public <T extends G3Serializable> T read(Supplier<T> instanceSupplier) {
		T instance = instanceSupplier.get();
		instance.read(this);
		return instance;
	}

	public <T extends G3Serializable> void readList(Class<T> type, Consumer<T> consumer) {
		readList(type, consumer, readInt());
	}

	public <T extends G3Serializable> List<T> readList(Class<T> type) {
		return readList(type, readInt());
	}

	public <T> void readList(Function<G3FileReader, T> extractor, Consumer<T> consumer) {
		readList(extractor, consumer, readInt());
	}

	public <T> List<T> readList(Function<G3FileReader, T> extractor) {
		return readList(extractor, readInt());
	}

	public <T> List<T> readList(Function<G3FileReader, ?> extractor, Class<T> castTo) {
		return readList(extractor, castTo, readInt());
	}

	public <T extends G3Serializable> void readList(Class<T> type, Consumer<T> consumer, int count) {
		for (int i = 0; i < count; i++) {
			consumer.accept(read(type));
		}
	}

	public <T extends G3Serializable> List<T> readList(Class<T> type, int count) {
		ArrayList<T> result = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			result.add(read(type));
		}
		return result;
	}

	public <T> void readList(Function<G3FileReader, T> extractor, Consumer<T> consumer, int count) {
		for (int i = 0; i < count; i++) {
			consumer.accept(extractor.apply(this));
		}
	}

	public <T> List<T> readList(Function<G3FileReader, T> extractor, int count) {
		ArrayList<T> result = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			result.add(extractor.apply(this));
		}
		return result;
	}

	public <T> List<T> readList(Function<G3FileReader, ?> extractor, Class<T> castTo, int count) {
		ArrayList<T> result = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			result.add(castTo.cast(extractor.apply(this)));
		}
		return result;
	}

	public void skipListPrefix() {
		// bTArray writes a useless byte during serialization.
		skip(1);
	}

	public <T extends G3Serializable> void readPrefixedList(Class<T> type, Consumer<T> consumer) {
		skipListPrefix();
		readList(type, consumer, readInt());
	}

	public <T extends G3Serializable> List<T> readPrefixedList(Class<T> type) {
		skipListPrefix();
		return readList(type, readInt());
	}

	public <T> void readPrefixedList(Function<G3FileReader, T> extractor, Consumer<T> consumer) {
		skipListPrefix();
		readList(extractor, consumer, readInt());
	}

	public <T> List<T> readPrefixedList(Function<G3FileReader, T> extractor) {
		skipListPrefix();
		return readList(extractor, readInt());
	}

	public <T> List<T> readPrefixedList(Function<G3FileReader, ?> extractor, Class<T> castTo) {
		skipListPrefix();
		return readList(extractor, castTo, readInt());
	}

	public <T extends G3Serializable> void readArray(Class<T> type, Consumer<T> consumer) {
		readArray(type, consumer, readInt());
	}

	public <T extends G3Serializable> T[] readArray(Class<T> type) {
		return readArray(type, readInt());
	}

	public <T> void readArray(Function<G3FileReader, T> extractor, Consumer<T> consumer) {
		readArray(extractor, consumer, readInt());
	}

	public <T> T[] readArray(Function<G3FileReader, ?> extractor, Class<T> castTo) {
		return readArray(extractor, castTo, readInt());
	}

	public <T extends G3Serializable> void readArray(Class<T> type, Consumer<T> consumer, int count) {
		for (int i = 0; i < count; i++) {
			consumer.accept(read(type));
		}
	}

	public <T extends G3Serializable> T[] readArray(Class<T> type, int count) {
		@SuppressWarnings("unchecked")
		T[] result = (T[]) Array.newInstance(type, count);
		for (int i = 0; i < count; i++) {
			result[i] = read(type);
		}
		return result;
	}

	public <T> void readArray(Function<G3FileReader, T> extractor, Consumer<T> consumer, int count) {
		for (int i = 0; i < count; i++) {
			consumer.accept(extractor.apply(this));
		}
	}

	public <T> T[] readArray(Function<G3FileReader, ?> extractor, Class<T> castTo, int count) {
		@SuppressWarnings("unchecked")
		T[] result = (T[]) Array.newInstance(castTo, count);
		for (int i = 0; i < count; i++) {
			result[i] = castTo.cast(extractor.apply(this));
		}
		return result;
	}

	public boolean[] readBoolArray() {
		boolean[] result = new boolean[readInt()];
		for (int i = 0; i < result.length; i++) {
			result[i] = readBool();
		}
		return result;
	}

	public <T extends G3Serializable> void readPrefixedArray(Class<T> type, Consumer<T> consumer) {
		skipListPrefix();
		readArray(type, consumer, readInt());
	}

	public <T extends G3Serializable> T[] readPrefixedArray(Class<T> type) {
		skipListPrefix();
		return readArray(type, readInt());
	}

	public <T> void readPrefixedArray(Function<G3FileReader, T> extractor, Consumer<T> consumer) {
		skipListPrefix();
		readArray(extractor, consumer, readInt());
	}

	public <T> T[] readPrefixedArray(Function<G3FileReader, ?> extractor, Class<T> castTo) {
		skipListPrefix();
		return readArray(extractor, castTo, readInt());
	}

	public boolean[] readPrefixedBoolArray() {
		skipListPrefix();
		return readBoolArray();
	}

	public <V extends G3Serializable> Map<String, V> readStringMap(Class<V> value) {
		return readMap(G3FileReader::readEntry, r -> r.read(value));
	}

	public <K extends G3Serializable, V extends G3Serializable> Map<K, V> readMap(Class<K> key, Class<V> value) {
		return readMap(r -> r.read(key), r -> r.read(value));
	}

	public <K, V extends G3Serializable> Map<K, V> readMap(Function<G3FileReader, K> key, Class<V> value) {
		return readMap(key, r -> r.read(value));
	}

	public <K, V> Map<K, V> readMap(Function<G3FileReader, K> key, Function<G3FileReader, V> value) {
		int count = readInt();
		Map<K, V> result = new LinkedHashMap<>();
		for (int i = 0; i < count; i++) {
			result.put(key.apply(this), value.apply(this));
		}
		return result;
	}

	public <V extends G3Serializable> Map<String, V> readStringMapPrefixed(Class<V> value) {
		skipListPrefix();
		return readStringMap(value);
	}

	public <K extends G3Serializable, V extends G3Serializable> Map<K, V> readMapPrefixed(Class<K> key, Class<V> value) {
		skipListPrefix();
		return readMap(key, value);
	}

	public <K, V extends G3Serializable> Map<K, V> readMapPrefixed(Function<G3FileReader, K> key, Class<V> value) {
		skipListPrefix();
		return readMap(key, value);
	}

	public <K, V> Map<K, V> readMapPrefixed(Function<G3FileReader, K> key, Function<G3FileReader, V> value) {
		skipListPrefix();
		return readMap(key, value);
	}
}

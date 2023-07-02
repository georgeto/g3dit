package de.george.lrentnode.effect;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileReaderEx;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.io.G3FileWriterEx;
import de.george.g3utils.io.G3Serializable;
import de.george.g3utils.io.GenomeFile;

public class gCSVMManager extends GenomeFile {
	public static class SVoice implements G3Serializable {
		public List<String> entries;

		@Override
		public void read(G3FileReader reader) {
			if (reader.readUnsignedShort() != 1) {
				throw new IllegalStateException("Version != 1 of SVoice is not supported.");
			}

			entries = reader.readPrefixedList(G3FileReader::readEntry);
		}

		@Override
		public void write(G3FileWriter writer) {
			writer.writePrefixedList(entries, G3FileWriter::writeEntry);
		}
	}

	public static class SBlock implements G3Serializable {
		public static class Entry implements G3Serializable {
			public String id;
			public String description;

			@Override
			public void read(G3FileReader reader) {
				if (reader.readUnsignedShort() != 1) {
					throw new IllegalStateException("Version != 1 of SBlock::Entry not supported.");
				}
				id = reader.readEntry();
				description = new String(reader.readByteArray(reader.readUnsignedShort()), StandardCharsets.UTF_16LE);
			}

			@Override
			public void write(G3FileWriter writer) {
				writer.writeUnsignedShort(1);
				writer.writeEntry(id);
				writer.writeUnsignedShort(description.length() * 2).write(description.getBytes(StandardCharsets.UTF_16LE));
			}
		}

		public List<Entry> entries;

		@Override
		public void read(G3FileReader reader) {
			if (reader.readUnsignedShort() != 2) {
				throw new IllegalStateException("Version != 1 of SBlock not supported.");
			}

			entries = reader.readPrefixedList(Entry.class);
		}

		@Override
		public void write(G3FileWriter writer) {
			writer.writePrefixedList(entries);
		}
	}

	public Map<String, SVoice> voiceList;
	public Map<String, SBlock> blockList;

	public gCSVMManager(Path file) throws IOException {
		try (G3FileReaderEx reader = new G3FileReaderEx(file)) {
			read(reader);
		}
	}

	public gCSVMManager(G3FileReaderEx reader) throws IOException {
		read(reader);
	}

	@Override
	protected void readInternal(G3FileReaderEx reader) throws IOException {
		if (reader.readUnsignedShort() != 1) {
			throw new IOException("Version != 1 of SVMManager is not supported.");
		}

		voiceList = reader.readStringMapPrefixed(SVoice.class);
		blockList = reader.readStringMapPrefixed(SBlock.class);
	}

	@Override
	protected void writeInternal(G3FileWriterEx writer) throws IOException {
		throw new UnsupportedOperationException();
	}
}

package de.george.lrentnode.archive;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileReaderEx;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.io.G3FileWriterEx;
import de.george.g3utils.io.GenomeFile;

public class SecDat extends GenomeFile {
	private List<String> nodeFiles = new ArrayList<>();
	private List<String> lrentdatFiles = new ArrayList<>();

	public SecDat() {}

	public SecDat(G3FileReaderEx reader) throws IOException {
		read(reader);
	}

	public SecDat addNodeFile(String nodeFile) {
		nodeFiles.add(nodeFile);
		return this;
	}

	public SecDat addLrentdatFile(String lrentdatFile) {
		lrentdatFiles.add(lrentdatFile);
		return this;
	}

	public List<String> getNodeFiles() {
		return nodeFiles;
	}

	public List<String> getLrentdatFiles() {
		return lrentdatFiles;
	}

	@Override
	protected void readInternal(G3FileReaderEx reader) throws IOException {
		// Skip version
		reader.skip(2);

		int lrendatCount = reader.readInt();
		int nodeCount = reader.readInt();
		reader.readList(G3FileReader::readEntry, this::addLrentdatFile, lrendatCount);
		reader.readList(G3FileReader::readEntry, this::addNodeFile, nodeCount);
	}

	@Override
	protected void writeInternal(G3FileWriterEx writer) throws IOException {
		writer.writeUnsignedShort(0x1B);
		writer.writeInt(lrentdatFiles.size()).writeInt(nodeFiles.size());
		writer.write(lrentdatFiles, G3FileWriter::writeEntry);
		writer.write(nodeFiles, G3FileWriter::writeEntry);
	}
}

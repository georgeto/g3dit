package de.george.lrentnode.archive.node;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.george.g3utils.io.G3FileReaderEx;
import de.george.g3utils.io.G3FileWriterEx;
import de.george.lrentnode.archive.ArchiveEntity;
import de.george.lrentnode.archive.ArchiveFile;
import de.george.lrentnode.archive.eCEntity;

public class NodeFile extends ArchiveFile {
	private static final Logger logger = LoggerFactory.getLogger(NodeFile.class);

	public NodeFile(G3FileReaderEx reader, boolean verifyEntityGraph, boolean skipPropertySets) throws IOException {
		super(verifyEntityGraph, skipPropertySets);
		read(reader);
	}

	@Override
	public ArchiveType getArchiveType() {
		return ArchiveType.Node;
	}

	@Override
	protected void readInternal(G3FileReaderEx reader) throws IOException {
		if (reader.readShort() != 83) {
			reader.warn(logger, "Node unbekannte Version.");
		}

		super.readInternal(reader);
	}

	@Override
	protected ArchiveEntity readEntity(G3FileReaderEx reader) throws IOException {
		boolean hasCreator = reader.readBool();
		boolean disablePatchWithTemplate = reader.readBool();
		NodeEntity entity = new NodeEntity(false);
		entity.read(reader, skipPropertySets);
		if (hasCreator && !disablePatchWithTemplate) {
			reader.readGUID(); // bCPropertyID
		}
		return entity;
	}

	@Override
	public void writeInternal(G3FileWriterEx writer) {
		writer.writeUnsignedShort(83);
		super.writeInternal(writer);
	}

	@Override
	protected void writeEntity(G3FileWriterEx writer, eCEntity entity) {
		writer.writeBool(entity.hasCreator());
		writer.writeBool(false); // DisablePatchWithTemplate, always false
		entity.write(writer);
		if (entity.hasCreator()) {
			writer.write(entity.getCreator()); // bCPropertyID
		}
	}
}

package de.george.lrentnode.archive.lrentdat;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.george.g3utils.io.G3FileReaderEx;
import de.george.g3utils.io.G3FileWriterEx;
import de.george.g3utils.structure.bCBox;
import de.george.lrentnode.archive.ArchiveEntity;
import de.george.lrentnode.archive.ArchiveFile;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.classes.eCEntityDynamicContext;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.util.ClassUtil;

public class LrentdatFile extends ArchiveFile {
	private static final Logger logger = LoggerFactory.getLogger(LrentdatFile.class);

	private eCEntityDynamicContext context;

	public LrentdatFile(G3FileReaderEx reader, boolean verifyEntityGraph, boolean skipPropertySets) throws IOException {
		super(verifyEntityGraph, skipPropertySets);
		read(reader);
	}

	@Override
	public ArchiveType getArchiveType() {
		return ArchiveType.Lrentdat;
	}

	@Override
	protected void readInternal(G3FileReaderEx reader) throws IOException {
		if (!reader.read(8).equals("47454E4F4D45444C")) {
			throw new IOException("'" + reader.getFileName() + "' ist keine gültige .lrentdat Datei.");
		}

		if (reader.readShort() != 83) {
			reader.warn(logger, "Lrentdat unbekannte Version.");
		}

		context = (eCEntityDynamicContext) ClassUtil.readSubClass(reader);

		super.readInternal(reader);
	}

	@Override
	protected ArchiveEntity readEntity(G3FileReaderEx reader) throws IOException {
		LrentdatEntity entity = new LrentdatEntity(false);
		entity.read(reader, skipPropertySets);
		return entity;
	}

	@Override
	public void writeInternal(G3FileWriterEx writer) {
		writer.write("47454E4F4D45444C5300");
		updateContentBox();
		ClassUtil.writeSubClass(writer, context);

		super.writeInternal(writer);
	}

	@Override
	protected void writeEntity(G3FileWriterEx writer, eCEntity entity) {
		entity.write(writer);
	}

	@Override
	protected void writeInternalAfterDeadbeef(G3FileWriterEx writer, int deadbeef) {
		writer.replaceInt(deadbeef - 43, 39);
	}

	public void updateContentBox() {
		context.setPropertyData(CD.eCContextBase.ContextBox, graph != null ? graph.getWorldTreeBoundary() : new bCBox());
	}
}

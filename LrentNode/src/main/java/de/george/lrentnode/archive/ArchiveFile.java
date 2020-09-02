package de.george.lrentnode.archive;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableMap;

import de.george.g3utils.io.G3FileReaderEx;
import de.george.g3utils.io.G3FileWriterEx;
import de.george.g3utils.util.IndexGenerator;

public abstract class ArchiveFile extends AbstractEntityFile<eCEntity> {

	public enum ArchiveType {
		Lrentdat,
		Node
	}

	private boolean verifyEntityGraph;
	private List<eCEntity> orphanEntities;

	protected boolean skipPropertySets;

	public ArchiveFile(boolean verifyEntityGraph, boolean skipPropertySets) {
		this.verifyEntityGraph = verifyEntityGraph;
		this.skipPropertySets = skipPropertySets;
	}

	public abstract ArchiveType getArchiveType();

	public boolean isLrentdat() {
		return getArchiveType() == ArchiveType.Lrentdat;
	}

	protected abstract ArchiveEntity readEntity(G3FileReaderEx reader) throws IOException;

	@Override
	protected void readInternal(G3FileReaderEx reader) throws IOException {
		// Entities einlesen
		List<ArchiveEntity> entities = new ArrayList<>();
		int entityNumber = reader.readInt();
		for (int i = 0; i < entityNumber; i++) {
			ArchiveEntity entity = readEntity(reader);
			entity.setFile(this);
			entities.add(entity);
		}

		// SubEntityDefinition einlesen
		int parentIndex = 0;
		while ((parentIndex = reader.readInt()) != -1) {
			int childIndex = reader.readInt();
			if (childIndex < 0 || childIndex >= entities.size()) {
				throw new IOException(reader.getFileName() + ": SubEntityDefinition contains invalid entity index " + childIndex
						+ " at address " + (reader.getPos() - 4) + ".");
			}
			// Attach entity
			entities.get(parentIndex).attachChild(entities.get(childIndex));
		}

		setGraph(entities.get(0));

		if (verifyEntityGraph) {
			orphanEntities = entities.stream().filter(e -> e != graph && !e.hasParent()).collect(Collectors.toList());
		}
	}

	protected abstract void writeEntity(G3FileWriterEx writer, eCEntity entity);

	@Override
	protected void writeInternal(G3FileWriterEx writer) {
		// Entities schreiben
		ImmutableMap<eCEntity, Integer> entities = getEntities().toMap(new IndexGenerator<>());
		writer.writeInt(entities.size());
		for (eCEntity entity : entities.keySet()) {
			writeEntity(writer, entity);
		}

		// SubEntityDefinition schreiben
		for (eCEntity entity : entities.keySet()) {
			for (eCEntity child : entity.getChilds()) {
				writer.writeInt(entities.get(entity)).writeInt(entities.get(child));
			}
		}

		writer.writeInt(-1).writeInt(-1);
	}

	public List<eCEntity> pullOrphanEntities() {
		List<eCEntity> result = orphanEntities != null ? orphanEntities : Collections.emptyList();
		// Referenzen freigeben
		orphanEntities = null;
		return result;
	}
}

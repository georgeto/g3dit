package de.george.lrentnode.archive;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;

import de.george.g3utils.io.G3FileReaderEx;
import de.george.g3utils.io.G3FileWriterEx;

public abstract class ArchiveFile extends AbstractEntityFile<eCEntity> {

	public enum ArchiveType {
		Lrentdat,
		Node
	};

	private boolean verifyEntityGraph;
	private List<eCEntity> orphanEntities;

	protected boolean skipPropertySets;

	public ArchiveFile(boolean verifyEntityGraph, boolean skipPropertySets) {
		this.verifyEntityGraph = verifyEntityGraph;
		this.skipPropertySets = skipPropertySets;
	}

	public abstract ArchiveType getArchiveType();

	// @foff
	/**
	 * Fügt {@link entity} an der letzte Position ein
	 */
	/*public void addEntity(ArchiveEntity entity) {
		entities.add(entity);
	}*/

	/**
	 * Fügt {@link entity} an der vorletzte Position ein, praktisch für die .nodes im SysDyn Verzeichnis
	 */
	/*public void addEntityBeforeLast(ArchiveEntity entity) {
		entities.add(entities.size() - 1, entity);
	}*/

	/*public void addEntities(List<ArchiveEntity> entities2) {
		this.entities.addAll(entities2);
	}*/

	/**
	 * RootEntity wird als Master für {@link entity} in der SubEntityDefinition benutzt
	 * @param entity
	 */
	/*public void addEntityMapping(ArchiveEntity entity) {
		addEntityMapping(entities.get(0), entity);
	}*/

	/**
	 * {@link master} wird als Master für {@link entity} in der SubEntityDefinition benutzt
	 * @param master
	 *            Bla
	 * @param entity
	 */
	/*public void addEntityMapping(ArchiveEntity master, ArchiveEntity entity) {
		//int rootP = getEntityPosition(root);
		//int entityP = getEntityPosition(entity);
		if (!entityMapping.containsKey(master))
			entityMapping.put(master, new LinkedHashSet<ArchiveEntity>());
		entityMapping.get(master).add(entity);
	}*/

	/*public void removeEntityMapping(ArchiveEntity master, ArchiveEntity entity) {
		Set<ArchiveEntity> mapping = getDirectSubEntities(master);
		if (mapping.contains(entity))
			mapping.remove(entity);
	}*/

	/*public LinkedHashMap<ArchiveEntity, LinkedHashSet<ArchiveEntity>> getEntityMapping() {
		return entityMapping;
	}*/

	/*public ArchiveEntity getMasterEntity(ArchiveEntity entity) {
		for (Map.Entry<ArchiveEntity, LinkedHashSet<ArchiveEntity>> entry : entityMapping.entrySet()) {
			if (isDirectSubEntityOf(entry.getKey(), entity))
				return entry.getKey();
		}
		return null;
	}

	public boolean isMasterEntity(ArchiveEntity master) {
		return entityMapping.get(master) != null ? true : false;
	}

	public boolean isSubEntity(ArchiveEntity subEntity, boolean ignoreRoot) {
		for (Map.Entry<ArchiveEntity, LinkedHashSet<ArchiveEntity>> entry : entityMapping.entrySet()) {
			if (ignoreRoot && getEntity(0).equals(entry.getKey())) //RootEntity zählt nicht
				continue;
			for (ArchiveEntity entity : entry.getValue())
				if (entity.equals(subEntity))
					return true;
		}
		return false;
	}

	public boolean isDirectSubEntityOf(ArchiveEntity master, ArchiveEntity subEntity) {
		Set<ArchiveEntity> mapping = getDirectSubEntities(master);
		return mapping.contains(subEntity);
	}

	public boolean isSubEntityOf(ArchiveEntity master, ArchiveEntity subEntity) {
		Set<ArchiveEntity> mapping = getAllSubEntities(master);
		return mapping.contains(subEntity);
	}

	public Set<ArchiveEntity> getDirectSubEntities(ArchiveEntity master) {
		Set<ArchiveEntity> subEntities = entityMapping.get(master);
		if (subEntities == null)
			return Collections.emptySet();
		return subEntities;
	}

	public Set<ArchiveEntity> getAllSubEntities(ArchiveEntity master) {
		Set<ArchiveEntity> subEntities = entityMapping.get(master);
		if (subEntities == null)
			return Collections.emptySet();

		Set<ArchiveEntity> allSubEntities = new LinkedHashSet<ArchiveEntity>(subEntities);
		for (ArchiveEntity subEntity : subEntities)
			allSubEntities.addAll(getAllSubEntities(subEntity));
		return allSubEntities;
	}*/
	// @fon

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
				throw new IOException(reader.getFileName() + ": SubEntityDefinition enthält ungültigen Entity-Index " + childIndex
						+ " an Adresse " + (reader.getPos() - 4) + ".");
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
		ImmutableList<eCEntity> entities = getEntities().toList();
		writer.writeInt(entities.size());
		for (eCEntity entity : entities) {
			writeEntity(writer, entity);
		}

		// SubEntityDefinition schreiben
		for (eCEntity entity : entities) {
			for (eCEntity child : entity.getChilds()) {
				writer.writeInt(entities.indexOf(entity)).writeInt(entities.indexOf(child));
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

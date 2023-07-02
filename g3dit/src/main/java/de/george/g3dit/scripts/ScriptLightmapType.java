package de.george.g3dit.scripts;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import com.teamunify.i18n.I;

import ca.odell.glazedlists.impl.sort.ComparableComparator;
import de.george.g3utils.structure.bCVector;
import de.george.g3utils.util.FilesEx;
import de.george.lrentnode.archive.ArchiveFile;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.classes.G3Class;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.classes.desc.CD.eCVisualMeshBase_PS;
import de.george.lrentnode.enums.G3Enums;
import de.george.lrentnode.enums.G3Enums.eELightmapType;
import de.george.lrentnode.enums.G3Enums.eEStaticLighingType;
import de.george.lrentnode.iterator.ArchiveFileIterator;
import de.george.lrentnode.util.EntityUtil;

public class ScriptLightmapType implements IScript {

	@Override
	public String getTitle() {
		return I.tr("List LightmapType");
	}

	@Override
	public String getDescription() {
		return I.tr("Listing of all entities that are illuminated by lightmap.");
	}

	@Override
	public boolean execute(IScriptEnvironment env) {
		ListMultimap<String, EntityEntry> entities = LinkedListMultimap.create();

		ArchiveFileIterator worldFilesIterator = env.getFileManager().worldFilesIterator();
		while (worldFilesIterator.hasNext()) {
			ArchiveFile aFile = worldFilesIterator.next();
			for (eCEntity entity : aFile.getEntities()) {
				G3Class mesh = EntityUtil.getStaticMeshClass(entity);
				if (mesh != null) {
					if (mesh.property(CD.eCVisualMeshBase_PS.StaticLightingType)
							.getEnumValue() == eEStaticLighingType.eEStaticLighingType_Lightmap) {
						entities.put(mesh.property(CD.eCVisualMeshBase_PS.ResourceFileName).getString(),
								new EntityEntry(mesh.property(eCVisualMeshBase_PS.LightmapType).getEnumValue(),
										mesh.property(eCVisualMeshBase_PS.ResourceFileName).getString(), entity.getGuid(),
										entity.getWorldPosition(), FilesEx.getFileName(worldFilesIterator.nextFile())));
						// enviroment.log(G3Enums.asString(eELightmapType.class,
						// mesh.property(CD.eCVisualMeshBase_PS.LightmapType).enumValue) + " | " +
						// entity.toString() + " | "
						// + entity.getGuid() + " | " + worldFilesIterator.nextFile().getName());
					}
				}
			}
		}

		SortedSet<String> typeSet = new TreeSet<>(entities.keySet());

		for (String entityType : typeSet) {
			List<EntityEntry> entries = entities.get(entityType);
			entries.sort(new ComparableComparator());

			if (entries.stream().anyMatch(p -> p.getLightmapType() == eELightmapType.eELightmapType_PerVertex)
					&& entries.stream().anyMatch(p -> p.getLightmapType() == eELightmapType.eELightmapType_Mixed)) {
				env.log("\n" + entityType + "(" + entries.size() + ") = ...");
				entries.forEach(e -> env.log(G3Enums.asString(eELightmapType.class, e.getLightmapType()) + " | " + e.getEntityName()
						+ " | " + e.getEntityPosition().toMarvinString() + " | " + e.getEntityGuid() + " | " + e.getFileName()));
			}
		}

		for (String entityType : typeSet) {
			List<EntityEntry> entries = entities.get(entityType);
			entries.sort(new ComparableComparator());

			if (!(entries.stream().anyMatch(p -> p.getLightmapType() == eELightmapType.eELightmapType_PerVertex)
					&& entries.stream().anyMatch(p -> p.getLightmapType() == eELightmapType.eELightmapType_Mixed))) {
				env.log("\n" + entityType + "(" + entries.size() + ") = "
						+ G3Enums.asString(eELightmapType.class, entries.get(0).getLightmapType()));
			}
		}

		return true;
	}

	private static class EntityEntry implements Comparable<EntityEntry> {
		private int lightmapType;
		private String entityName;
		private String entityGuid;
		private bCVector entityPosition;
		private String fileName;

		public EntityEntry(int lightmapType, String entityName, String entityGuid, bCVector entityPosition, String fileName) {
			this.lightmapType = lightmapType;
			this.entityName = entityName;
			this.entityGuid = entityGuid;
			this.entityPosition = entityPosition;
			this.fileName = fileName;
		}

		public int getLightmapType() {
			return lightmapType;
		}

		public String getEntityName() {
			return entityName;
		}

		public String getEntityGuid() {
			return entityGuid;
		}

		public String getFileName() {
			return fileName;
		}

		public bCVector getEntityPosition() {
			return entityPosition;
		}

		@Override
		public int compareTo(EntityEntry o) {
			int result = getFileName().compareTo(o.getFileName());
			if (result == 0) {
				result = getEntityGuid().compareTo(o.getEntityGuid());
			}
			return result;
		}
	}
}

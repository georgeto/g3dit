package de.george.g3dit.scripts;

import java.util.HashMap;
import java.util.Map;

import de.george.g3dit.settings.EditorOptions;
import de.george.g3dit.util.FileManager;
import de.george.g3utils.util.IOUtils;
import de.george.lrentnode.archive.ArchiveFile;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.classes.eCVegetation_PS;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.enums.G3Enums.gESpecies;
import de.george.lrentnode.iterator.ArchiveFileIterator;
import de.george.lrentnode.properties.bTPropertyContainer;
import de.george.lrentnode.util.NPCUtil;

public class ScriptGenerateStatistics implements IScript {

	@Override
	public String getTitle() {
		return "Änderungsstatistiken generieren";
	}

	@Override
	public String getDescription() {
		return "Generiert Stastiken zu den vorgenommenen Veränderungen.";
	}

	@Override
	public boolean execute(IScriptEnvironment env) {
		ArchiveFileIterator secWorldFilesIterator = new ArchiveFileIterator(IOUtils.listFiles(
				env.getEditorContext().getOptionStore().get(EditorOptions.Path.SECONDARY_DATA_FOLDER) + FileManager.RP_PROJECTS_COMPILED,
				IOUtils.archiveFileFilter));

		Map<String, String> originalNPCs = new HashMap<>();
		Map<String, String> originalMonsters = new HashMap<>();
		Map<String, String> originalObjects = new HashMap<>();
		int originalVegetationObjectCount = 0;

		while (secWorldFilesIterator.hasNext()) {
			ArchiveFile archiveFile = secWorldFilesIterator.next();

			for (eCEntity entity : archiveFile.getEntities().filter(ScriptGenerateStatistics::isIncluded)) {
				if (isNPC(entity)) {
					originalNPCs.put(entity.getGuid(), entity.toString());
				} else if (isMonster(entity)) {
					originalMonsters.put(entity.getGuid(), entity.toString());
				} else {
					if (entity.hasClass(CD.eCVegetation_PS.class)) {
						eCVegetation_PS vegetation = entity.getClass(CD.eCVegetation_PS.class);
						originalVegetationObjectCount += vegetation.getGrid().getEntryCount();
					}

					originalObjects.put(entity.getGuid(), entity.toString());
				}
			}
		}

		int originalNpcsCount = originalNPCs.size();
		int originalMonstersCount = originalMonsters.size();
		int originalObjectsCount = originalObjects.size();

		Map<String, String> newNPCs = new HashMap<>();
		Map<String, String> newMonsters = new HashMap<>();
		Map<String, String> newObjects = new HashMap<>();
		int vegetationObjectCount = 0;

		ArchiveFileIterator worldFilesIterator = env.getFileManager().worldFilesIterator();
		while (worldFilesIterator.hasNext()) {
			ArchiveFile archiveFile = worldFilesIterator.next();

			for (eCEntity entity : archiveFile.getEntities().filter(ScriptGenerateStatistics::isIncluded)) {
				if (isNPC(entity)) {
					if (originalNPCs.remove(entity.getGuid()) == null) {
						newNPCs.put(entity.getGuid(), entity.toString());
					}
				} else if (isMonster(entity)) {
					if (originalMonsters.remove(entity.getGuid()) == null) {
						newMonsters.put(entity.getGuid(), entity.toString());
					}
				} else {
					if (entity.hasClass(CD.eCVegetation_PS.class)) {
						eCVegetation_PS vegetation = entity.getClass(CD.eCVegetation_PS.class);
						vegetationObjectCount += vegetation.getGrid().getEntryCount();
					}

					if (originalObjects.remove(entity.getGuid()) == null) {
						newObjects.put(entity.getGuid(), entity.toString());
					}
				}
			}
		}

		logDiff(env, "NPCs", originalNpcsCount, newNPCs.size(), originalNPCs.size());
		logDiff(env, "Monster", originalMonstersCount, newMonsters.size(), originalMonsters.size());
		logDiff(env, "Objekte", originalObjectsCount, newObjects.size(), originalObjects.size());
		env.log("Vegetationsobjekte: %d (%+d)", vegetationObjectCount, vegetationObjectCount - originalVegetationObjectCount);

		return true;
	}

	private static void logDiff(IScriptEnvironment env, String name, int original, int created, int deleted) {
		env.log("%s: %d (Gothic3) + %d (neu) - %d (entfernt) = %d", name, original, created, deleted, original + created + deleted);
	}

	private static boolean isIncluded(eCEntity entity) {
		if (entity.getName().equalsIgnoreCase("Root") || entity.getName().equalsIgnoreCase("RootEntity")) {
			return false;
		}
		if (entity.hasParent() && NPCUtil.isNPC(entity.getParent())) {
			return false;
		}
		return true;
	}

	private static boolean isMonster(eCEntity entity) {
		return NPCUtil.isNPC(entity);
	}

	private static boolean isNPC(eCEntity entity) {
		if (!NPCUtil.isNPC(entity)) {
			return false;
		}

		bTPropertyContainer<gESpecies> species = entity.getClass(CD.gCNPC_PS.class).property(CD.gCNPC_PS.Species);
		return species.getEnumValue() == gESpecies.gESpecies_Human || species.getEnumValue() == gESpecies.gESpecies_Orc;
	}
}

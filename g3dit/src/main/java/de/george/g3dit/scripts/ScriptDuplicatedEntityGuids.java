package de.george.g3dit.scripts;

import com.google.common.collect.SortedSetMultimap;
import com.google.common.collect.TreeMultimap;

import de.george.lrentnode.archive.ArchiveFile;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.iterator.ArchiveFileIterator;

public class ScriptDuplicatedEntityGuids implements IScript {

	@Override
	public String getTitle() {
		return "Uneindeutige Entity-Guids ermitteln";
	}

	@Override
	public String getDescription() {
		return "Überprüft alle Entities nach mehrfach vorkommenden Guids.";
	}

	@Override
	public boolean execute(IScriptEnvironment env) {
		ArchiveFileIterator worldFilesIterator = env.getFileManager().worldFilesIterator();
		SortedSetMultimap<String, String> entityGuidMap = TreeMultimap.create();

		while (worldFilesIterator.hasNext()) {
			ArchiveFile archiveFile = worldFilesIterator.next();

			int entityPosition = 0;
			for (eCEntity entity : archiveFile.getEntities()) {
				String entityIdentifier = worldFilesIterator.nextFile().getName() + "#" + entity.toString() + "(" + entityPosition + ")";
				entityGuidMap.put(entity.getGuid(), entityIdentifier);
				entityPosition++;
			}
		}

		entityGuidMap.asMap().forEach((key, value) -> {
			if (value.size() > 1) {
				env.log("Mehrfach vorkommende Entity-Guid " + key + ": \n  " + String.join("\n  ", value) + "\n");
			}
		});

		return true;
	}
}

package de.george.g3dit.scripts;

import java.util.stream.Collectors;

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

		entityGuidMap.asMap().entrySet().forEach(e -> {
			if (e.getValue().size() > 1) {
				env.log("Mehrfach vorkommende Entity-Guid " + e.getKey() + ": \n  "
						+ e.getValue().stream().collect(Collectors.joining("\n  ")) + "\n");
			}
		});

		return true;
	}
}

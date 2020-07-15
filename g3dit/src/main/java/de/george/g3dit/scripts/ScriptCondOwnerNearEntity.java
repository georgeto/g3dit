package de.george.g3dit.scripts;

import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import de.george.lrentnode.archive.ArchiveFile;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.iterator.ArchiveFileIterator;

public class ScriptCondOwnerNearEntity implements IScript {

	@Override
	public String getTitle() {
		return "ScriptCondOwnerNearEntity";
	}

	@Override
	public String getDescription() {
		return "ScriptCondOwnerNearEntity";
	}

	@Override
	public boolean execute(IScriptEnvironment env) {
		Set<String> npcs = new HashSet<>();
		ArchiveFileIterator worldFilesIterator = env.getFileManager().worldFilesIterator();
		while (worldFilesIterator.hasNext()) {
			ArchiveFile archiveFile = worldFilesIterator.next();
			for (eCEntity entity : archiveFile) {
				if (entity.hasClass(CD.gCNPC_PS.class)) {
					npcs.add(entity.getName());
				}
			}
		}

		for (File file : env.getFileManager().listFiles("Infos/G3_World_01/", f -> f.getName().endsWith(".info"))) {
			Properties properties = new Properties();
			try {
				properties.load(new FileReader(file));
				String ownerNearEntity = properties.getProperty("CondOwnerNearEntity");
				if (ownerNearEntity != null && !ownerNearEntity.isEmpty() && npcs.contains(ownerNearEntity)) {
					env.log(file.getName() + ": " + ownerNearEntity);
					Desktop.getDesktop().open(file);
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		/*
		 * ArchiveFileIterator worldFilesIterator = env.getFileManager().worldFilesIterator();
		 * while(worldFilesIterator.hasNext()) { ArchiveFile archiveFile =
		 * worldFilesIterator.next();
		 *
		 * }
		 */

		return true;
	}
}

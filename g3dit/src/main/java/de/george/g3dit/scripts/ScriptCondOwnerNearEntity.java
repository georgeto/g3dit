package de.george.g3dit.scripts;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import com.teamunify.i18n.I;

import de.george.g3utils.util.PathFilter;
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

		for (Path file : env.getFileManager().listFiles("Infos/G3_World_01/", PathFilter.withExt("info"))) {
			Properties properties = new Properties();
			try {
				properties.load(Files.newBufferedReader(file));
				String ownerNearEntity = properties.getProperty("CondOwnerNearEntity");
				if (ownerNearEntity != null && !ownerNearEntity.isEmpty() && npcs.contains(ownerNearEntity)) {
					env.log(file.getFileName() + ": " + ownerNearEntity);
					Desktop.getDesktop().open(file.toFile());
				}
			} catch (IOException e) {
				env.log(I.trf("Failed to open info {0}: {1}", file.getFileName(), e.getMessage()));
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

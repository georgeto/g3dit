package de.george.g3dit.scripts;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.george.g3dit.util.FileDialogWrapper;
import de.george.g3dit.util.FileManager;
import de.george.g3dit.util.StringtableHelper;
import de.george.lrentnode.archive.ArchiveFile;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.classes.eCVegetation_Mesh;
import de.george.lrentnode.classes.eCVegetation_PS;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.iterator.ArchiveFileIterator;

public class ScriptPurgeUnusedVegMeshes implements IScript {
	private static final Logger logger = LoggerFactory.getLogger(ScriptPurgeUnusedVegMeshes.class);

	@Override
	public String getTitle() {
		return "Nicht verwendete Vegetationsmeshes löschen";
	}

	@Override
	public String getDescription() {
		return "Löscht alle nicht verwendeten Vegetationsmeshes aus den Weltdaten.";
	}

	@Override
	public boolean execute(IScriptEnvironment env) {
		File saveDir = FileDialogWrapper.chooseDirectory("Speicherpfad auswählen", env.getParentWindow());
		if (saveDir == null) {
			return false;
		}

		int totalRemoved = 0;
		ArchiveFileIterator worldFilesIterator = env.getFileManager().worldFilesIterator();
		while (worldFilesIterator.hasNext()) {
			int preFileTotalRemoved = totalRemoved;
			ArchiveFile aFile = worldFilesIterator.next();
			File file = worldFilesIterator.nextFile();
			for (eCEntity entity : aFile.getEntities()) {
				if (entity.hasClass(CD.eCVegetation_PS.class)) {
					eCVegetation_PS veg = entity.getClass(CD.eCVegetation_PS.class);
					List<eCVegetation_Mesh> unusedMeshes = veg.getMeshClasses().stream()
							.filter(m -> veg.getMeshUseCount(m.getMeshID()) == 0).collect(Collectors.toList());
					unusedMeshes.forEach(m -> veg.removeMeshClass(m));

					if (!unusedMeshes.isEmpty()) {
						env.log("Entferne " + unusedMeshes.size() + " unbenutzte Meshes in " + file.getName() + "#" + entity.getGuid());
						totalRemoved += unusedMeshes.size();
					}
				}
			}

			// Es wurden Meshes gelöscht
			if (totalRemoved > preFileTotalRemoved) {
				FileManager fileManager = env.getFileManager();
				Optional<String> relativePath = fileManager.getRelativePath(file);
				if (!relativePath.isPresent()) {
					env.log("Relativer Pfad von " + file.getAbsolutePath()
							+ " konnte nicht ermittelt werden, übernehmen der Änderungen nicht möglich.");
					continue;
				}
				try {
					File out = new File(saveDir, relativePath.get());
					out.getParentFile().mkdirs();
					StringtableHelper.clearStringtableSafe(aFile.getEntities().toList(), aFile.getStringtable(), true,
							env.getParentWindow());
					aFile.save(out);
				} catch (IOException e) {
					env.log("Speichern von " + file.getAbsolutePath() + " fehlgeschlagen: " + e.getMessage());
					logger.warn("Speichern fehlgeschlagen", e);
				}
			}
		}
		env.log("Es wurden insgesamt " + totalRemoved + " unbenutzte Meshes entfernt.");
		return true;
	}
}

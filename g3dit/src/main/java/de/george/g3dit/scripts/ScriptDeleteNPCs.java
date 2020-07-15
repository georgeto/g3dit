package de.george.g3dit.scripts;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Splitter;

import de.george.g3dit.settings.LambdaOption;
import de.george.g3dit.settings.Option;
import de.george.g3dit.settings.OptionPanel;
import de.george.g3dit.settings.TextFieldOptionHandler;
import de.george.g3dit.util.FileDialogWrapper;
import de.george.g3dit.util.FileManager;
import de.george.lrentnode.archive.ArchiveFile;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.iterator.ArchiveFileIterator;

public class ScriptDeleteNPCs implements IScript {
	private static final Logger logger = LoggerFactory.getLogger(ScriptDeleteNPCs.class);

	private static final Option<String> WHITELIST = new LambdaOption<>("",
			(parent) -> new TextFieldOptionHandler(parent, "Whitelist", "List von NPC-Namen getrennt mit Kommas"),
			"ScriptDeleteNPCs.WHITELIST", "Whitelist");

	@Override
	public String getTitle() {
		return "NPCs löschen";
	}

	@Override
	public String getDescription() {
		return "Löscht alle NPCs aus den Weltdaten.";
	}

	@Override
	public boolean execute(IScriptEnvironment env) {
		File saveDir = FileDialogWrapper.chooseDirectory("Speicherpfad auswählen", env.getParentWindow());
		if (saveDir == null) {
			return false;
		}

		Set<String> whitelist = Splitter.on(',').omitEmptyStrings().trimResults().splitToStream(env.getOption(WHITELIST))
				.collect(Collectors.toSet());

		int totalRemoved = 0;
		ArchiveFileIterator worldFilesIterator = env.getFileManager().worldFilesIterator();
		while (worldFilesIterator.hasNext()) {
			ArchiveFile aFile = worldFilesIterator.next();
			File file = worldFilesIterator.nextFile();
			List<eCEntity> toBeDeleted = new ArrayList<>();
			for (eCEntity entity : aFile.getEntities()) {
				if (entity.hasClass(CD.gCNPC_PS.class) && !entity.getName().equals("PC_Hero") && !whitelist.contains(entity.getName())) {
					toBeDeleted.add(entity);
				}
			}

			// Es wurden Meshes gelöscht
			if (!toBeDeleted.isEmpty()) {
				toBeDeleted.forEach(e -> e.removeFromParent(false));
				totalRemoved += toBeDeleted.size();
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
					aFile.save(out);
				} catch (IOException e) {
					env.log("Speichern von " + file.getAbsolutePath() + " fehlgeschlagen: " + e.getMessage());
					logger.warn("Speichern fehlgeschlagen", e);
				}
			}
		}
		env.log("Es wurden insgesamt " + totalRemoved + " NPCs entfernt.");
		return true;
	}

	@Override
	public void installOptions(OptionPanel optionPanel) {
		optionPanel.addOption(WHITELIST);
	}

}

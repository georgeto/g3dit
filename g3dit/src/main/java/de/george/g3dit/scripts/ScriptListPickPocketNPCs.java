package de.george.g3dit.scripts;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import de.george.g3dit.util.FileDialogWrapper;
import de.george.g3utils.util.IOUtils;
import de.george.lrentnode.archive.ArchiveFile;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.classes.gCInventory_PS;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.classes.desc.PropertyDescriptor;
import de.george.lrentnode.iterator.ArchiveFileIterator;
import de.george.lrentnode.properties.bCString;
import de.george.lrentnode.util.PropertyUtil;

public class ScriptListPickPocketNPCs implements IScript {

	@Override
	public String getTitle() {
		return "Beklaubare NPCs auflisten";
	}

	@Override
	public String getDescription() {
		return "Erstellt eine Liste aller beklaubaren NPCs (dazu zählen auch Truhen) und deren PickPocket TreasureSets.";
	}

	@Override
	public boolean execute(IScriptEnvironment env) {
		File saveFile = FileDialogWrapper.saveFile("Auflistung speichern unter...", env.getParentWindow(), FileDialogWrapper.CSV_FILTER);
		if (saveFile == null) {
			return false;
		}

		List<String> output = new ArrayList<>();

		output.add("Name;Guid;1.TreasureSet;2.TreasureSet;3.TreasureSet;4.TreasureSet;5.TreasureSet");

		ArchiveFileIterator worldFilesIterator = env.getFileManager().worldFilesIterator();
		while (worldFilesIterator.hasNext()) {
			ArchiveFile aFile = worldFilesIterator.next();
			for (eCEntity entity : aFile.getEntities()) {
				if (!entity.hasClass(CD.gCInventory_PS.class)) {
					continue;
				}

				gCInventory_PS inv = entity.getClass(CD.gCInventory_PS.class);

				List<String> treasureSets = new ArrayList<>();
				for (PropertyDescriptor<bCString> tsProp : PropertyUtil.GetTreasureSetProperties()) {
					String treasureSet = inv.property(tsProp).getString();
					if (treasureSet.startsWith("TS_PickPocket")) {
						treasureSets.add(treasureSet);
					}
				}

				if (!treasureSets.isEmpty()) {
					String line = entity.getName() + ";" + entity.getGuid() + ";" + String.join(";", treasureSets);
					for (int i = treasureSets.size(); i < 5; i++) {
						line += ";";
					}
					output.add(line);
				}
			}
		}

		try {
			IOUtils.writeTextFile(output, saveFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			env.log("Datei konnte nicht gespeichert werden:" + e.getMessage());
		}
		return true;
	}

}

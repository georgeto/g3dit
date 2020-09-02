package de.george.g3dit.scripts;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.teamunify.i18n.I;

import de.george.g3dit.util.FileDialogWrapper;
import de.george.g3utils.util.IOUtils;
import de.george.lrentnode.archive.ArchiveFile;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.classes.G3Class;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.enums.G3Enums.gESpecies;
import de.george.lrentnode.iterator.ArchiveFileIterator;
import de.george.lrentnode.properties.bTPropertyContainer;

public class ScriptListVoices implements IScript {

	@Override
	public String getTitle() {
		return I.tr("NPC-Stimmen auflisten");
	}

	@Override
	public String getDescription() {
		return I.tr("Erstellt eine Liste, die zu jedem NPC dessen Stimme enthält.");
	}

	@Override
	public boolean execute(IScriptEnvironment env) {
		File saveFile = FileDialogWrapper.saveFile(I.tr("NPC-Stimmen Auflistung speichern unter..."), env.getParentWindow(),
				FileDialogWrapper.CSV_FILTER);
		if (saveFile == null) {
			return false;
		}

		List<String> output = new ArrayList<>();
		List<String> specialOutput = new ArrayList<>();

		ArchiveFileIterator worldFilesIterator = env.getFileManager().worldFilesIterator();
		while (worldFilesIterator.hasNext()) {
			ArchiveFile aFile = worldFilesIterator.next();
			for (eCEntity entity : aFile.getEntities()) {
				if (entity.hasClass(CD.gCNPC_PS.class)) {
					G3Class npc = entity.getClass(CD.gCNPC_PS.class);

					String voice = npc.property(CD.gCNPC_PS.Voice).getString();
					bTPropertyContainer<?> species = npc.property(CD.gCNPC_PS.Species);
					if (species.getEnumValue() != gESpecies.gESpecies_Human && species.getEnumValue() != gESpecies.gESpecies_Orc) {
						if (!voice.isEmpty()) {
							specialOutput.add("!!! " + entity.getName() + " !!!;" + entity.getGuid() + ";" + voice);
						}
					} else {
						output.add(entity.getName() + ";" + entity.getGuid() + ";" + voice);
					}
				}
			}
		}

		output.sort(String::compareTo);
		specialOutput.sort(String::compareTo);
		output.addAll(specialOutput);
		output.add(0, "Name;Guid;Voice");

		try {
			IOUtils.writeTextFile(output, saveFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			env.log(I.trf("Datei konnte nicht gespeichert werden: {0}", e.getMessage()));
			return false;
		}
		return true;
	}
}

package de.george.g3dit.scripts;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;

import com.teamunify.i18n.I;

import de.george.g3dit.cache.Caches;
import de.george.g3dit.cache.TemplateCache;
import de.george.g3dit.cache.TemplateCache.TemplateCacheEntry;
import de.george.g3dit.util.FileDialogWrapper;
import de.george.g3utils.util.IOUtils;
import de.george.g3utils.util.Pair;
import de.george.lrentnode.archive.ArchiveFile;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.classes.G3Class;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.iterator.ArchiveFileIterator;
import de.george.lrentnode.properties.bCString;
import de.george.lrentnode.properties.bTObjArray_bCString;
import de.george.lrentnode.properties.bTObjArray_eCEntityProxy;
import de.george.lrentnode.properties.eCEntityProxy;

public class ScriptListTeachers implements IScript {

	@Override
	public String getTitle() {
		return I.tr("Lehrer auflisten");
	}

	@Override
	public String getDescription() {
		return I.tr("Erstellt eine Liste aller Lehrer mit den Perks und Attributen die sie lehren.");
	}

	@Override
	public boolean execute(IScriptEnvironment env) {
		File saveFile = FileDialogWrapper.saveFile(I.tr("Lehrer-Auflistung speichern unter..."), env.getParentWindow());
		if (saveFile == null) {
			return false;
		}

		SortedMap<String, Pair<List<String>, List<String>>> pairs = new TreeMap<>();

		TemplateCache tpleCache = Caches.template(env.getEditorContext());

		ArchiveFileIterator worldFilesIterator = env.getFileManager().worldFilesIterator();
		while (worldFilesIterator.hasNext()) {
			ArchiveFile aFile = worldFilesIterator.next();
			for (eCEntity entity : aFile.getEntities()) {
				if (entity.hasClass(CD.gCNPC_PS.class)) {
					G3Class npc = entity.getClass(CD.gCNPC_PS.class);

					bTObjArray_bCString teachAttribs = npc.property(CD.gCNPC_PS.TeachAttribs);
					String name = entity.getName();

					var pair = pairs.computeIfAbsent(name, k -> Pair.of(new ArrayList<>(), new ArrayList<>()));
					pair.el0().addAll(teachAttribs.getEntries(bCString::getString));

					bTObjArray_eCEntityProxy teachSkills = npc.property(CD.gCNPC_PS.TeachSkills);

					for (eCEntityProxy skillProxy : teachSkills.getEntries()) {
						Optional<TemplateCacheEntry> lookupGuid = tpleCache.getEntryByGuid(skillProxy.getGuid());
						if (lookupGuid.isPresent()) {
							pair.el1().add(lookupGuid.get().getName());
						} else {
							pair.el1().add(skillProxy.getGuid());
						}
					}

					if (pair.el0().size() == 0 && pair.el1().size() == 0) {
						pairs.remove(name);
					}
				}
			}
		}

		List<String> output = new ArrayList<>();
		for (Entry<String, Pair<List<String>, List<String>>> entry : pairs.entrySet()) {
			List<String> attributes = entry.getValue().el0();
			Collections.sort(attributes);
			List<String> skills = entry.getValue().el1();
			Collections.sort(skills);

			output.add(entry.getKey());
			if (attributes.size() > 0) {
				output.add("\t" + I.tr("Attribute") + ":");
				attributes.forEach(attr -> output.add("\t\t" + attr));
			}

			if (skills.size() > 0) {
				output.add("\t" + I.tr("Skills") + ":");
				skills.forEach(skill -> output.add("\t\t" + skill));
			}

			output.add("\n----------------------------\n");
		}

		try {
			IOUtils.writeTextFile(output, saveFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			env.log(I.trf("Datei konnte nicht gespeichert werden: {0}", e.getMessage()));
			return false;
		}
		return true;
	}
}

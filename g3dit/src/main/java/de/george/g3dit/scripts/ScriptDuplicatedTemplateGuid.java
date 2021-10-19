package de.george.g3dit.scripts;

import com.google.common.collect.SortedSetMultimap;
import com.google.common.collect.TreeMultimap;

import de.george.lrentnode.iterator.TemplateFileIterator;
import de.george.lrentnode.template.TemplateFile;

public class ScriptDuplicatedTemplateGuid implements IScript {

	@Override
	public String getTitle() {
		return "Uneindeutige Template-Guids ermitteln";
	}

	@Override
	public String getDescription() {
		return "Überprüft alle Templates nach mehrfach vorkommenden Guids.";
	}

	@Override
	public boolean execute(IScriptEnvironment env) {
		TemplateFileIterator tpleFilesIterator = env.getFileManager().templateFilesIterator();
		SortedSetMultimap<String, String> itemGuidMap = TreeMultimap.create();
		SortedSetMultimap<String, String> refGuidMap = TreeMultimap.create();

		while (tpleFilesIterator.hasNext()) {
			TemplateFile tple = tpleFilesIterator.next();
			itemGuidMap.put(tple.getItemHeader().getGuid(), tple.getFileName());
			refGuidMap.put(tple.getReferenceHeader().getGuid(), tple.getFileName());
		}

		itemGuidMap.asMap().forEach((key, value) -> {
			if (value.size() > 1) {
				env.log("Mehrfach vorkommende Item-Guid " + key + ": \n  " + String.join("\n  ", value) + "\n");
			}
		});

		refGuidMap.asMap().forEach((key, value) -> {
			if (value.size() > 1) {
				env.log("Mehrfach vorkommende Reference-Guid " + key + ": \n  " + String.join("\n  ", value) + "\n");
			}
		});

		return true;
	}
}

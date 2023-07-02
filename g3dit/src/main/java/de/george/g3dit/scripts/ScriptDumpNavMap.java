package de.george.g3dit.scripts;

import java.nio.file.Path;

import com.teamunify.i18n.I;

import de.george.g3dit.util.FileDialogWrapper;
import de.george.g3utils.io.G3FileReaderEx;
import de.george.g3utils.util.FilesEx;
import de.george.navmap.sections.NavMap;

public class ScriptDumpNavMap implements IScript {

	@Override
	public String getTitle() {
		return "NavigationMap2Text";
	}

	@Override
	public String getDescription() {
		return I.tr("Converts the NavigationMap into a text file.");
	}

	@Override
	public boolean execute(IScriptEnvironment env) {
		Path loadFile = FileDialogWrapper.openFile(I.tr("Open NavigationMap..."), env.getParentWindow(), FileDialogWrapper.XNAV_FILTER);
		if (loadFile == null) {
			return false;
		}

		Path saveFile = FileDialogWrapper.saveFile(I.tr("Save NavigationMap in text form"),
				FilesEx.changeExtension(FilesEx.getFileName(loadFile), "txt"), env.getParentWindow(), FileDialogWrapper.TXT_FILTER);
		if (saveFile == null) {
			return false;
		}

		try (G3FileReaderEx reader = new G3FileReaderEx(loadFile)) {
			NavMap navMap = new NavMap(reader);
			navMap.saveText(saveFile);
		} catch (Exception e) {
			env.log(e.getMessage());
			return false;
		}

		return true;
	}
}

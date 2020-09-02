package de.george.g3dit.scripts;

import java.io.File;

import com.teamunify.i18n.I;

import de.george.g3dit.util.FileDialogWrapper;
import de.george.g3utils.io.G3FileReaderEx;
import de.george.g3utils.util.IOUtils;
import de.george.navmap.sections.NavMap;

public class ScriptDumpNavMap implements IScript {

	@Override
	public String getTitle() {
		return "NavigationMap2Text";
	}

	@Override
	public String getDescription() {
		return I.tr("Wandelt die NavigationMap in eine Textdatei um.");
	}

	@Override
	public boolean execute(IScriptEnvironment env) {
		File loadFile = FileDialogWrapper.openFile(I.tr("NavigationMap öffnen..."), env.getParentWindow(), FileDialogWrapper.XNAV_FILTER);
		if (loadFile == null) {
			return false;
		}

		File saveFile = FileDialogWrapper.saveFile(I.tr("NavigationMap in Textform speichern"),
				IOUtils.changeExtension(loadFile.getName(), "txt"), env.getParentWindow(), FileDialogWrapper.TXT_FILTER);
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

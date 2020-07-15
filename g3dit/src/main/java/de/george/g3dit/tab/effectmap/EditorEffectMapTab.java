package de.george.g3dit.tab.effectmap;

import java.awt.Component;
import java.io.File;
import java.io.IOException;

import javax.swing.Icon;

import com.ezware.dialog.task.TaskDialogs;

import de.george.g3dit.EditorContext;
import de.george.g3dit.settings.EditorOptions;
import de.george.g3dit.tab.EditorAbstractFileTab;
import de.george.g3dit.util.FileDialogWrapper;
import de.george.g3dit.util.Icons;
import de.george.g3utils.io.Saveable;
import de.george.lrentnode.effect.gCEffectMap;
import net.tomahawk.ExtensionsFilter;

public class EditorEffectMapTab extends EditorAbstractFileTab {
	private EffectMapContentPane contentPane;

	private gCEffectMap currentFile;

	public EditorEffectMapTab(EditorContext ctx) {
		super(ctx, EditorTabType.EffectMap);
		contentPane = new EffectMapContentPane(this);
		contentPane.initGUI();
	}

	@Override
	public Icon getTabIcon() {
		return Icons.getImageIcon(Icons.Document.LETTER_S);
	}

	/**
	 * Liefert die aktuell geöffnete Datei zurück
	 *
	 * @return aktuelle geöffnete Datei
	 */
	public gCEffectMap getCurrentEffectMap() {
		return currentFile;
	}

	/**
	 * @param inSecdat geladene Secdat
	 * @param file File Objekt der Datei
	 */
	public void setCurrentFile(gCEffectMap effectMap, File file) {
		currentFile = null;
		setFileChanged(false);

		if (effectMap != null) {
			setDataFile(file);
			currentFile = effectMap;
			contentPane.loadValues();
			contentPane.setVisible(true);
		} else {
			contentPane.setVisible(false);
		}

		eventBus().post(new StateChangedEvent(this));
	}

	@Override
	public boolean openFile(File file) {
		try {
			gCEffectMap effectMap = new gCEffectMap(file);
			setCurrentFile(effectMap, file);
			return true;
		} catch (IOException e) {
			TaskDialogs.showException(e);
		}
		return false;
	}

	@Override
	protected Saveable getSaveable() {
		contentPane.saveValues();

		if (getOptionStore().get(EditorOptions.Misc.CLEAN_STRINGTABLE)) {
			getCurrentEffectMap().getStringtable().clear();
		}

		return getCurrentEffectMap();

	}

	@Override
	public Component getTabContent() {
		return contentPane;
	}

	@Override
	public boolean onClose(boolean appExit) {
		if (super.onClose(appExit)) {
			contentPane.onClose();
			setCurrentFile(null, null);
			return true;
		}
		return false;
	}

	@Override
	public String getDefaultFileExtension() {
		return "efm";
	}

	@Override
	public ExtensionsFilter getFileFilter() {
		return FileDialogWrapper.EFFECT_MAP_FILTER;
	}
}

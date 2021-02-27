package de.george.g3dit.tab.secdat;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

import javax.swing.Icon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ezware.dialog.task.TaskDialogs;

import de.george.g3dit.EditorContext;
import de.george.g3dit.tab.EditorAbstractFileTab;
import de.george.g3dit.util.FileDialogWrapper;
import de.george.g3dit.util.Icons;
import de.george.g3utils.io.G3FileReaderEx;
import de.george.g3utils.io.Saveable;
import de.george.g3utils.util.IOUtils;
import de.george.lrentnode.archive.SecDat;
import de.george.lrentnode.util.FileUtil;
import net.tomahawk.ExtensionsFilter;

public class EditorSecdatTab extends EditorAbstractFileTab {
	private static final Logger logger = LoggerFactory.getLogger(EditorSecdatTab.class);

	private SecdatContentPane contentPane;

	private SecDat currentFile;

	public EditorSecdatTab(EditorContext ctx) {
		super(ctx, EditorTabType.Secdat);
		contentPane = new SecdatContentPane(this);
		contentPane.initGUI();
		ctx.eventBus().register(this);
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
	public SecDat getCurrentSecdat() {
		return currentFile;
	}

	/**
	 * @param inSecdat geladene Secdat
	 * @param file File Objekt der Datei
	 */
	public void setCurrentFile(SecDat inSecdat, File file) {
		currentFile = null;
		setFileChanged(false);

		if (inSecdat != null) {
			setDataFile(file);
			currentFile = inSecdat;
			contentPane.loadValues();
			contentPane.setVisible(true);
		} else {
			contentPane.setVisible(false);
		}

		eventBus().post(new StateChangedEvent(this));
	}

	@Override
	public boolean openFile(File file) {
		try (G3FileReaderEx reader = new G3FileReaderEx(file)) {
			SecDat secdat = FileUtil.openSecdat(reader);
			setCurrentFile(secdat, file);
			updateChecksum(reader.getBuffer());
			return true;
		} catch (IOException e) {
			TaskDialogs.showException(e);
		}
		return false;
	}

	@Override
	protected Saveable getSaveable() {
		contentPane.saveValues();
		return getCurrentSecdat();

	}

	@Override
	public boolean saveFile(Optional<File> file) {
		boolean result = super.saveFile(file);
		if (result && file.isPresent()) {
			processSector();
		}

		return result;
	}

	@Override
	public Component getTabContent() {
		return contentPane;
	}

	@Override
	public boolean onClose(boolean appExit) {
		if (super.onClose(appExit)) {
			contentPane.onClose();
			ctx.eventBus().unregister(this);
			setCurrentFile(null, null);
			return true;
		}
		return false;
	}

	@Override
	public String getDefaultFileExtension() {
		return "secdat";
	}

	@Override
	public ExtensionsFilter getFileFilter() {
		return FileDialogWrapper.SECDAT_FILTER;
	}

	public void processSector() {
		File dataFile = getDataFile().get();
		File sector = new File(IOUtils.changeExtension(dataFile.getAbsolutePath(), "sec"));
		if (!sector.exists() && !ctx.getFileManager().moveFromPrimaryToSecondary(sector).filter(f -> f.exists()).isPresent()) {
			boolean result = TaskDialogs.ask(ctx.getParentWindow(), ".sec erstellen",
					"Für '" + dataFile.getName() + "' konnte keine zugehörige .sec gefunden werden.\nSoll eine .sec erstellt werden?");
			if (result) {
				try {
					FileUtil.createSec(sector);
				} catch (IOException e) {
					logger.warn("Fehler beim Erstellen des Sektors für {}: ", dataFile.getName(), e);
				}
			}
		}
	}
}

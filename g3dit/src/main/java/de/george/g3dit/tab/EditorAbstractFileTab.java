package de.george.g3dit.tab;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

import javax.swing.SwingWorker;

import com.ezware.dialog.task.TaskDialogs;

import de.george.g3dit.Editor;
import de.george.g3dit.EditorContext;
import de.george.g3dit.gui.dialogs.ProgressDialog;
import de.george.g3dit.settings.EditorOptions;
import de.george.g3dit.util.Dialogs;
import de.george.g3dit.util.FileChangeMonitor;
import de.george.g3dit.util.FileDialogWrapper;
import de.george.g3dit.util.SettingsHelper;
import de.george.g3utils.io.Saveable;
import net.tomahawk.ExtensionsFilter;

public abstract class EditorAbstractFileTab extends EditorTab implements FileChangeMonitor {

	private File dataFile;
	private boolean fileChanged;

	public EditorAbstractFileTab(EditorContext ctx, EditorTabType type) {
		super(ctx, type);
	}

	@Override
	public String getTitle() {
		if (getDataFile().isPresent()) {
			return getDataFile().get().getName();
		}
		return "<Nicht gespeichert>";
	}

	@Override
	public String getTabTitle() {
		if (getDataFile().isPresent()) {
			return (isFileChanged() ? "*" : "") + getDataFile().get().getName();
		}
		return "<Nicht gespeichert>";
	}

	@Override
	public String getEditorTitle() {
		if (getDataFile().isPresent()) {
			String filePath = SettingsHelper.applyAliasMap(SettingsHelper.getDataFolderAlias(ctx.getOptionStore()),
					getDataFile().get().getAbsolutePath());
			return (isFileChanged() ? "*" : "") + filePath + " - " + Editor.EDITOR_TITLE;
		} else {
			return (isFileChanged() ? "*" : "") + Editor.EDITOR_TITLE;
		}
	}

	public Optional<File> getDataFile() {
		return Optional.ofNullable(dataFile);
	}

	public void setDataFile(File dataFile) {
		this.dataFile = dataFile;
		eventBus().post(new StateChangedEvent(this));
	}

	@Override
	public boolean isFileChanged() {
		return fileChanged;
	}

	public void setFileChanged(boolean fileChanged) {
		if (this.fileChanged != fileChanged) {
			this.fileChanged = fileChanged;
			eventBus().post(new StateChangedEvent(this));
		}
	}

	/**
	 * Markiert die Datei als verändert.
	 */
	@Override
	public void fileChanged() {
		setFileChanged(true);
	}

	/**
	 * Öffnet die Datei <code>file</code>, Fehlerbehandlung in der Methode.
	 *
	 * @param file
	 * @return false, wenn beim Öffnen ein Fehler aufgetreten ist
	 */
	public abstract boolean openFile(File file);

	/**
	 * Wendet alle Änderungen an und liefert das Saveable Objekt zurück
	 *
	 * @return Saveable Objekt
	 */
	protected abstract Saveable getSaveable();

	/**
	 * Speichert die aktuelle geöffnete Datei.
	 *
	 * @return true, wenn Speichern erfolgreich
	 */
	public boolean saveFile() {
		return saveFile(getDataFile());
	}

	/**
	 * Speichert die aktuelle geöffnete Datei, unter dem angegeben Pfad.
	 *
	 * @param file Pfad des Speicherortes
	 * @return true, wenn Speichern erfolgreich
	 */
	public boolean saveFile(Optional<File> file) {
		if (!file.isPresent()) {
			return showSaveFileAsDialog();
		}
		Saveable saveable = getSaveable();
		if (saveable != null) {
			file = getFileManager().confirmSaveInSecondary(file.get());
			if (!file.isPresent()) {
				return false;
			}

			SaveFileWorker worker = new SaveFileWorker(file.get(), saveable);
			worker.execute();
			worker.getProgressDialog().setVisible(true);

			if (!worker.isFileSaved()) {
				return false;
			}

			setDataFile(file.get());
			return true;

		} else {
			TaskDialogs.error(ctx.getParentWindow(), "Speichern fehlgeschlagen", "Momentan ist keine Datei geöffnet.");
			return false;
		}

	}

	public abstract String getDefaultFileExtension();

	public abstract ExtensionsFilter getFileFilter();

	/**
	 * Speichert die aktuelle geöffnete Datei
	 *
	 * @param file Pfad des Speicherortes
	 * @return true, wenn Speichern erfolgreich
	 */
	private boolean showSaveFileAsDialog() {
		Optional<File> dataFile = getDataFile();
		// Datei die im sekunden Data-Verzeichnis liegt automatisch ins primäre verschieben, wenn
		// Elternverzeichnis dort existiert
		if (dataFile.isPresent()) {
			if (ctx.getFileManager().isInSecondaryDataFolder(dataFile.get())) {
				Optional<File> movedDataFile = ctx.getFileManager().moveFromSecondaryToPrimary(dataFile.get());
				if (movedDataFile.isPresent() && movedDataFile.get().getParentFile().exists()) {
					dataFile = movedDataFile;
				}
			}
		}

		String absolutePath = dataFile.map(File::getAbsolutePath).orElse(null);
		File file = FileDialogWrapper.saveFile("Speichern unter", absolutePath, getDefaultFileExtension(), ctx.getParentWindow(),
				getFileFilter());
		if (file != null) {
			if (!this.saveFile(Optional.ofNullable(file))) {
				return false;
			}
			ctx.getEditor().getMainMenu().addRecentFile(file.getAbsolutePath());
			return true;
		} else {
			TaskDialogs.inform(ctx.getParentWindow(), "Speichern fehlgeschlagen", "Es wurde kein Speicherort ausgewählt.");
			return false;
		}
	}

	/**
	 * Fragt ob Änderungen an der aktuellen Datei gespeichert werden sollen
	 *
	 * @param message Nachricht die angezeigt wird
	 * @return false, wenn Abbrechenoption gewählt wurde bzw. die Datei nicht verändert wurde
	 */
	private boolean askSaveChanges(String message) {
		if (isFileChanged()) {
			switch (Dialogs.askSaveChanges(ctx.getParentWindow(), String.format(message, getTitle()))) {
				case Yes:
					showSaveFileAsDialog();
					return true;
				case No:
					return true;
				case Cancel:
					return false;
			}
		}
		return true;
	}

	@Override
	public boolean onClose(boolean appExit) {
		String message = appExit ? "Änderungen an '%s' vor dem Beenden speichern?" : "Änderungen an '%s' Speichern?";
		return askSaveChanges(message);
	}

	@Override
	public void onSave() {
		saveFile();
	}

	@Override
	public void onSaveAs() {
		showSaveFileAsDialog();
	}

	private class SaveFileWorker extends SwingWorker<Void, Void> {

		private ProgressDialog progDlg;
		private File file;
		private Saveable fileToSave;
		private boolean fileSaved = false;

		private Path sourcePath;
		private Path destPath;
		private boolean makeBackup;

		public SaveFileWorker(File file, Saveable fileToSave) {
			progDlg = new ProgressDialog(ctx.getParentWindow(), "Speichere Datei...", file.getName(), false);
			progDlg.setLocationRelativeTo(ctx.getParentWindow());
			progDlg.getProgressBar().setIndeterminate(true);
			this.file = file;
			sourcePath = file.toPath();
			destPath = new File(file.getParent(), file.getName() + ".bak").toPath();
			makeBackup = ctx.getOptionStore().get(EditorOptions.Misc.MAKE_BACKUP);
			this.fileToSave = fileToSave;
		}

		public ProgressDialog getProgressDialog() {
			return progDlg;
		}

		public boolean isFileSaved() {
			return fileSaved;
		}

		@Override
		protected Void doInBackground() throws Exception {
			if (makeBackup && Files.exists(sourcePath) && !Files.isDirectory(sourcePath)) {
				Files.move(sourcePath, destPath, StandardCopyOption.REPLACE_EXISTING);
			}
			file.getParentFile().mkdirs();
			fileToSave.save(file);
			return null;
		}

		@Override
		protected void done() {
			try {
				get();
				setFileChanged(false);
				fileSaved = true;
				progDlg.dispose();
			} catch (Exception e) {
				try {
					Files.move(destPath, sourcePath, StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException e1) {
					// Ignore...
				}
				progDlg.dispose();
				TaskDialogs.showException(e);
			}
		}
	}
}

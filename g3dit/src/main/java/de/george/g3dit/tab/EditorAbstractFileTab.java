package de.george.g3dit.tab;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.zip.CRC32;

import javax.swing.SwingWorker;

import com.ezware.dialog.task.TaskDialogs;
import com.teamunify.i18n.I;

import de.george.g3dit.Editor;
import de.george.g3dit.EditorContext;
import de.george.g3dit.gui.dialogs.ProgressDialog;
import de.george.g3dit.settings.EditorOptions;
import de.george.g3dit.util.Dialogs;
import de.george.g3dit.util.FileChangeMonitor;
import de.george.g3dit.util.FileDialogWrapper;
import de.george.g3dit.util.SettingsHelper;
import de.george.g3utils.io.Crc32OutputStream;
import de.george.g3utils.io.Saveable;
import de.george.g3utils.io.TeeOutputStream;
import de.george.g3utils.util.FilesEx;
import net.tomahawk.ExtensionsFilter;

public abstract class EditorAbstractFileTab extends EditorTab implements FileChangeMonitor {
	private static final long NO_CHECKSUM = Long.MAX_VALUE;

	private static Dialogs.Answer ANSWER_ALL = null;

	private Path dataFile;
	private boolean fileChanged;
	private long checksum = NO_CHECKSUM;

	public EditorAbstractFileTab(EditorContext ctx, EditorTabType type) {
		super(ctx, type);
	}

	@Override
	public String getTitle() {
		if (getDataFile().isPresent()) {
			return FilesEx.getFileName(getDataFile().get());
		}
		return I.tr("<Not saved>");
	}

	@Override
	public String getTabTitle() {
		if (getDataFile().isPresent()) {
			return (isFileChanged() ? "*" : "") + FilesEx.getFileName(getDataFile().get());
		}
		return I.tr("<Not saved>");
	}

	@Override
	public String getEditorTitle() {
		if (getDataFile().isPresent()) {
			String filePath = SettingsHelper.applyAliasMap(SettingsHelper.getDataFolderAlias(ctx.getOptionStore()),
					FilesEx.getAbsolutePath(getDataFile().get()));
			return (isFileChanged() ? "*" : "") + filePath + " - " + Editor.EDITOR_TITLE;
		} else {
			return (isFileChanged() ? "*" : "") + Editor.EDITOR_TITLE;
		}
	}

	public Optional<Path> getDataFile() {
		return Optional.ofNullable(dataFile);
	}

	public void setDataFile(Path dataFile) {
		this.dataFile = dataFile;
		eventBus().post(new StateChangedEvent(this));
	}

	@Override
	public boolean isFileChanged() {
		return fileChanged;
	}

	public boolean isFileChangedReliable() {
		return false;
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

	protected void updateChecksum(ByteBuffer data) {
		if (ctx.getOptionStore().get(EditorOptions.Misc.IMPROVE_CHANGE_DETECTION)) {
			int position = data.position();
			data.position(0);
			CRC32 crc32 = new CRC32();
			crc32.update(data);
			checksum = crc32.getValue();
			data.position(position);
		} else {
			checksum = NO_CHECKSUM;
		}
	}

	protected Optional<Boolean> validateChecksum() {
		if (checksum == NO_CHECKSUM || !ctx.getOptionStore().get(EditorOptions.Misc.IMPROVE_CHANGE_DETECTION)) {
			return Optional.empty();
		}

		try {
			Crc32OutputStream crc32 = new Crc32OutputStream();
			getSaveable().save(crc32);
			return Optional.of(checksum == crc32.getValue());
		} catch (Exception e) {
			return Optional.of(true);
		}
	}

	/**
	 * Öffnet die Datei <code>file</code>, Fehlerbehandlung in der Methode.
	 *
	 * @param file
	 * @return false, wenn beim Öffnen ein Fehler aufgetreten ist
	 */
	public abstract boolean openFile(Path file);

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
	public boolean saveFile(Optional<Path> file) {
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
			TaskDialogs.error(ctx.getParentWindow(), I.tr("Saving failed"), I.tr("Currently no file is open."));
			return false;
		}

	}

	public abstract String getDefaultFileExtension();

	public abstract ExtensionsFilter getFileFilter();

	/**
	 * Speichert die aktuelle geöffnete Datei
	 *
	 * @return true, wenn Speichern erfolgreich
	 */
	private boolean showSaveFileAsDialog() {
		Optional<Path> dataFile = getDataFile();
		// Datei die im sekunden Data-Verzeichnis liegt automatisch ins primäre verschieben, wenn
		// Elternverzeichnis dort existiert
		if (dataFile.isPresent()) {
			if (ctx.getFileManager().isInSecondaryDataFolder(dataFile.get())) {
				Optional<Path> movedDataFile = ctx.getFileManager().moveFromSecondaryToPrimary(dataFile.get());
				if (movedDataFile.isPresent() && Files.exists(movedDataFile.get().getParent())) {
					dataFile = movedDataFile;
				}
			}
		}

		String absolutePath = dataFile.map(FilesEx::getAbsolutePath).orElse(null);
		Path file = FileDialogWrapper.saveFile(I.tr("Save as"), absolutePath, getDefaultFileExtension(), ctx.getParentWindow(),
				getFileFilter());
		if (file != null) {
			if (!this.saveFile(Optional.of(file))) {
				return false;
			}
			ctx.getEditor().getMainMenu().addRecentFile(FilesEx.getAbsolutePath(file));
			return true;
		} else {
			TaskDialogs.inform(ctx.getParentWindow(), I.tr("Saving failed"), I.tr("No save location has been selected."));
			return false;
		}
	}

	/**
	 * Fragt ob Änderungen an der aktuellen Datei gespeichert werden sollen
	 *
	 * @param message Nachricht die angezeigt wird
	 * @return false, wenn Abbrechenoption gewählt wurde bzw. die Datei nicht verändert wurde
	 */
	private boolean askSaveChanges(String message, boolean all) {
		Optional<Boolean> checksum = validateChecksum();
		boolean changed = checksum.isPresent() ? !checksum.get() : isFileChanged();
		if (changed) {
			Dialogs.Answer answer = ANSWER_ALL;
			if (!all || answer == null)
				answer = Dialogs.askSaveChanges(ctx.getParentWindow(), I.format(message, getTitle()), all);
			switch (answer) {
				case Yes:
					showSaveFileAsDialog();
					return true;
				case No:
					return true;
				case Cancel:
					return false;
				case AllNo:
					ANSWER_ALL = Dialogs.Answer.AllNo;
					return true;
				case AllYes:
					ANSWER_ALL = Dialogs.Answer.AllYes;
					saveFile();
					return true;
			}
		}
		return true;
	}

	@Override
	public boolean onClose(boolean appExit) {
		String message = appExit ? I.tr("Save changes to ''{0}'' before exiting?") : I.tr("Save changes to ''{0}''?");
		return askSaveChanges(message, appExit);
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
		private Saveable fileToSave;
		private boolean fileSaved = false;

		private Path file;
		private Path bakFile;
		private boolean makeBackup;

		public SaveFileWorker(Path file, Saveable fileToSave) {
			progDlg = new ProgressDialog(ctx.getParentWindow(), I.tr("Saving file..."), FilesEx.getFileName(file), false);
			progDlg.setLocationRelativeTo(ctx.getParentWindow());
			progDlg.getProgressBar().setIndeterminate(true);
			this.file = file;
			bakFile = file.resolveSibling(FilesEx.getFileName(file) + ".bak");
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
			if (makeBackup && Files.exists(file) && !Files.isDirectory(file)) {
				Files.move(file, bakFile, StandardCopyOption.REPLACE_EXISTING);
			}
			Files.createDirectories(file.getParent());
			try (OutputStream out = Files.newOutputStream(file)) {
				if (ctx.getOptionStore().get(EditorOptions.Misc.IMPROVE_CHANGE_DETECTION)) {
					Crc32OutputStream crc32 = new Crc32OutputStream();
					fileToSave.save(new TeeOutputStream(out, crc32));
					checksum = crc32.getValue();
				} else {
					fileToSave.save(out);
				}
			}
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
					Files.move(bakFile, file, StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException e1) {
					// Ignore...
				}
				progDlg.dispose();
				TaskDialogs.showException(e);
			}
		}
	}
}

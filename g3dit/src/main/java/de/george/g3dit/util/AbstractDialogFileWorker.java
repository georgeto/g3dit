package de.george.g3dit.util;

import java.awt.Window;
import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;

import javax.swing.JProgressBar;

import com.teamunify.i18n.I;

import de.george.g3dit.gui.dialogs.ProgressDialog;

public abstract class AbstractDialogFileWorker<T> extends AbstractFileWorker<T, Integer> {
	protected ProgressDialog progDlg;
	protected String statusFormat;

	protected AbstractDialogFileWorker(Callable<List<File>> fileProvider, List<File> openFiles, String dialogTitle, Window parent) {
		super(fileProvider, openFiles, "", I.tr("{0, number}/{1, number} Dateien verarbeitet"), I.tr("Verarbeitung abgeschlossen"));

		progDlg = new ProgressDialog(parent, dialogTitle, I.tr("Ermittele zu bearbeitende Dateien..."), true);
		progDlg.setLocationRelativeTo(parent);
		progDlg.setCancelListener(() -> cancel(false));

		JProgressBar progressBar = progDlg.getProgressBar();
		progressBar.setIndeterminate(true);

		setProgressBar(progressBar);
	}

	public ProgressDialog getProgressDialog() {
		return progDlg;
	}

	@Override
	protected void process(List<Integer> chunks) {
		super.process(chunks);
		progDlg.setStatusMessage(I.format(statusFormat, chunks.get(chunks.size() - 1)));
	}

	public void executeAndShowDialog() {
		execute();
		getProgressDialog().setVisible(true);
	}
}

package de.george.g3dit.settings;

import java.awt.Window;
import java.nio.file.Path;
import java.util.Optional;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.teamunify.i18n.I;

import de.george.g3dit.util.FileDialogWrapper;
import de.george.g3utils.gui.SwingUtils;
import de.george.g3utils.util.FilesEx;
import net.miginfocom.swing.MigLayout;

public class FolderPathOptionHandler extends TitledOptionHandler<String> {
	private JTextField tfPath;
	private String chooseFolderDialogTitle;

	public FolderPathOptionHandler(Window parent, String title, String chooseFolderDialogTitle) {
		super(parent, title);
		this.chooseFolderDialogTitle = chooseFolderDialogTitle;
	}

	@Override
	protected void load(String value) {
		tfPath.setText(value);
	}

	@Override
	protected Optional<String> save() {
		String path = tfPath.getText();
		if (path.isEmpty()) {
			return Optional.empty();
		} else {
			return Optional.of(path);
		}
	}

	@Override
	protected void addValueComponent(JPanel content) {
		tfPath = SwingUtils.createUndoTF();
		content.add(tfPath, "grow");

		JButton btnPath = new JButton(I.tr("..."));
		btnPath.addActionListener(e -> {
			Path file = FileDialogWrapper.chooseDirectory(chooseFolderDialogTitle, getParent());
			if (file != null) {
				tfPath.setText(FilesEx.getAbsolutePath(file));
			}
		});
		content.add(btnPath, "");
	}

	@Override
	protected MigLayout getLayoutManager() {
		return new MigLayout("ins 0, fillx", "[grow]5px push[]");
	}
}

package de.george.g3dit.settings;

import java.awt.Window;
import java.io.File;
import java.util.Optional;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import de.george.g3dit.util.FileDialogWrapper;
import de.george.g3utils.gui.SwingUtils;
import net.miginfocom.swing.MigLayout;
import net.tomahawk.ExtensionsFilter;

public class FilePathOptionHandler extends TitledOptionHandler<String> {
	private JTextField tfPath;
	private String openFileDialogTitle;
	private ExtensionsFilter filter;
	private String tooltip;

	public FilePathOptionHandler(Window parent, String title, String openFileDialogTitle, ExtensionsFilter filter) {
		this(parent, title, null, openFileDialogTitle, filter);
	}

	public FilePathOptionHandler(Window parent, String title, String tooltip, String openFileDialogTitle, ExtensionsFilter filter) {
		super(parent, title);
		this.tooltip = tooltip;
		this.openFileDialogTitle = openFileDialogTitle;
		this.filter = filter;
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

		if (tooltip != null) {
			tfPath.setToolTipText(tooltip);
		}

		JButton btnPath = new JButton("...");
		btnPath.addActionListener(e -> {
			File file = FileDialogWrapper.openFile(openFileDialogTitle, getParent(), filter);
			if (file != null) {
				tfPath.setText(file.getAbsolutePath());
			}
		});
		content.add(btnPath, "");
	}

	@Override
	protected MigLayout getLayoutManager() {
		return new MigLayout("ins 0, fillx", "[grow]5px push[]");
	}
}

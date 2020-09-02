package de.george.g3dit.settings;

import java.awt.Window;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.teamunify.i18n.I;

import de.george.g3dit.util.FileDialogWrapper;
import de.george.g3utils.gui.SwingUtils;
import de.george.g3utils.util.IOUtils;
import net.miginfocom.swing.MigLayout;

public class AliasFolderPathOptionHandler extends AbstractOptionHandler<String> {
	private Option<String> aliasOption;
	private JTextField tfPath, tfPathAlias;
	private String title;
	private String chooseFolderDialogTitle;

	public AliasFolderPathOptionHandler(Window parent, Option<String> aliasOption, String title, String chooseFolderDialogTitle) {
		super(parent);
		this.aliasOption = aliasOption;
		this.title = title;
		this.chooseFolderDialogTitle = chooseFolderDialogTitle;
	}

	@Override
	protected JPanel initContent() {
		JPanel content = super.initContent();

		// Titel
		content.add(new JLabel(title + " (" + I.tr("Alias") + ":"), "split 3");
		tfPathAlias = SwingUtils.createNoBorderUndoTF();
		content.add(tfPathAlias, "grow, width 50:50:150, height 15!");
		content.add(new JLabel(")"), "wrap");

		// Pfad
		tfPath = SwingUtils.createUndoTF();
		content.add(tfPath, "grow");

		JButton btnPath = new JButton(I.tr("..."));
		btnPath.addActionListener(e -> {
			File file = FileDialogWrapper.chooseDirectory(chooseFolderDialogTitle, getParent());
			if (file != null) {
				tfPath.setText(file.getAbsolutePath());
			}
		});
		content.add(btnPath, "");
		return content;
	}

	@Override
	public void load(OptionStore optionStore, Option<String> option) {
		tfPath.setText(optionStore.get(option));
		tfPathAlias.setText(optionStore.get(aliasOption));
	}

	@Override
	public void save(OptionStore optionStore, Option<String> option) {
		String path = tfPath.getText();
		if (path.isEmpty()) {
			optionStore.remove(option);
		} else {
			optionStore.put(option, IOUtils.ensureTrailingSlash(path));
		}

		optionStore.put(aliasOption, tfPathAlias.getText());
	}

	@Override
	protected MigLayout getLayoutManager() {
		return new MigLayout("ins 0, fillx", "[grow]5px push[]");
	}
}

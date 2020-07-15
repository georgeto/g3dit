package de.george.g3dit.gui.renderer;

import java.awt.Component;
import java.io.File;
import java.util.Optional;
import java.util.function.Supplier;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

import de.george.g3dit.settings.OptionStore;
import de.george.g3dit.util.SettingsHelper;

public class FileListCellRenderer extends DefaultListCellRenderer {
	private final Supplier<OptionStore> optionStore;

	public FileListCellRenderer(Supplier<OptionStore> optionStore) {
		this.optionStore = optionStore;
	}

	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

		File file = (File) value;
		if (file != null) {
			String fileName = file.getName();
			String filePath = file.getAbsolutePath();
			Optional<String> alias = SettingsHelper.getAlias(optionStore.get(), filePath);
			if (alias.isPresent()) {
				fileName = alias.get() + " " + fileName;
				filePath = SettingsHelper.applyAlias(optionStore.get(), filePath);
			}

			label.setText(fileName);
			label.setToolTipText(filePath);
		} else {
			label.setText(" ");
		}

		return label;
	}
}

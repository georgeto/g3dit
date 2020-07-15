package de.george.g3dit.gui.table.renderer;

import java.awt.Component;
import java.io.File;
import java.util.Optional;
import java.util.function.Supplier;

import javax.swing.JLabel;
import javax.swing.JTable;

import org.jdesktop.swingx.renderer.DefaultTableRenderer;

import de.george.g3dit.settings.OptionStore;
import de.george.g3dit.util.SettingsHelper;

public class FileTableCellRenderer extends DefaultTableRenderer {
	private final Supplier<OptionStore> optionStore;

	public FileTableCellRenderer(Supplier<OptionStore> optionStore) {
		this.optionStore = optionStore;
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

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
		}

		return label;
	}
}

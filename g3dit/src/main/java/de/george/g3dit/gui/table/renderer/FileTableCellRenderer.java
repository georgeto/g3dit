package de.george.g3dit.gui.table.renderer;

import java.awt.Component;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Supplier;

import javax.swing.JLabel;
import javax.swing.JTable;

import org.jdesktop.swingx.renderer.DefaultTableRenderer;

import de.george.g3dit.settings.OptionStore;
import de.george.g3dit.util.PathAliases;
import de.george.g3utils.util.FilesEx;

public class FileTableCellRenderer extends DefaultTableRenderer {
	private final Supplier<OptionStore> optionStore;

	public FileTableCellRenderer(Supplier<OptionStore> optionStore) {
		this.optionStore = optionStore;
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		Path file = (Path) value;
		if (file != null) {
			var aliases = PathAliases.from(optionStore.get());
			String fileName = FilesEx.getFileName(file);
			String filePath = aliases.apply(file);
			Optional<String> alias = aliases.getAlias(file);
			if (alias.isPresent())
				fileName = alias.get() + " " + fileName;

			label.setText(fileName);
			label.setToolTipText(filePath);
		}

		return label;
	}
}

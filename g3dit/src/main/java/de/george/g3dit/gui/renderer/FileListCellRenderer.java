package de.george.g3dit.gui.renderer;

import java.awt.Component;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Supplier;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

import de.george.g3dit.settings.OptionStore;
import de.george.g3dit.util.PathAliases;
import de.george.g3utils.util.FilesEx;

public class FileListCellRenderer extends DefaultListCellRenderer {
	private final Supplier<OptionStore> optionStore;

	public FileListCellRenderer(Supplier<OptionStore> optionStore) {
		this.optionStore = optionStore;
	}

	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

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
		} else {
			label.setText(" ");
		}

		return label;
	}
}

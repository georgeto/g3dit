package de.george.g3dit.gui.table.renderer;

import java.awt.Component;
import java.util.Objects;
import java.util.function.Function;

import javax.swing.JLabel;
import javax.swing.JTable;

import org.jdesktop.swingx.renderer.DefaultTableRenderer;

public class FunctionalTableCellRenderer<T> extends DefaultTableRenderer {
	private Function<T, String> textExtractor;
	private Function<T, String> toolTipTextExtractor;

	public FunctionalTableCellRenderer(Function<T, String> textExtractor) {
		this(textExtractor, null);
	}

	public FunctionalTableCellRenderer(Function<T, String> textExtractor, Function<T, String> toolTipTextExtractor) {
		this.textExtractor = Objects.requireNonNull(textExtractor);
		this.toolTipTextExtractor = toolTipTextExtractor;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		JLabel comp = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		comp.setText(value != null ? textExtractor.apply((T) value) : "");

		if (value != null && toolTipTextExtractor != null) {
			comp.setToolTipText(toolTipTextExtractor.apply((T) value));
		}

		return comp;
	}

}

package de.george.g3dit.gui.renderer;

import java.awt.Component;
import java.util.Objects;
import java.util.function.Function;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

public class FunctionalListCellRenderer<T> extends DefaultListCellRenderer {
	private Function<T, String> textExtractor;
	private Function<T, String> toolTipTextExtractor;

	public FunctionalListCellRenderer(Function<T, String> textExtractor) {
		this(textExtractor, null);
	}

	public FunctionalListCellRenderer(Function<T, String> textExtractor, Function<T, String> toolTipTextExtractor) {
		this.textExtractor = Objects.requireNonNull(textExtractor);
		this.toolTipTextExtractor = toolTipTextExtractor;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

		setText(value != null ? textExtractor.apply((T) value) : "");

		if (value != null && toolTipTextExtractor != null) {
			setToolTipText(toolTipTextExtractor.apply((T) value));
		}

		return this;
	}

}

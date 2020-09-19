package de.george.g3dit.gui.editor;

import java.awt.Component;
import java.util.function.Function;

import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;

public class LambdaConvertEditor<T> extends DefaultCellEditor {
	private final Function<T, String> to;
	private final Function<String, T> from;

	public LambdaConvertEditor(Function<T, String> to, Function<String, T> from) {
		super(new JTextField());
		this.to = to;
		this.from = from;
	}

	@Override
	public Object getCellEditorValue() {
		try {
			return from.apply(super.getCellEditorValue().toString());
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {
		return super.getTreeCellEditorComponent(tree, value != null ? to.apply((T) value) : null, isSelected, expanded, leaf, row);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		return super.getTableCellEditorComponent(table, value != null ? to.apply((T) value) : null, isSelected, row, column);
	}

}

package de.george.g3dit.tab.archive.views.property;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;

import javax.swing.table.TableCellEditor;

public class PropertyEditorAdapter implements PropertyEditor {
	private TableCellEditor editor;

	private Object value;

	public PropertyEditorAdapter(TableCellEditor editor) {
		this.editor = editor;
	}

	@Override
	public void setValue(Object value) {
		this.value = value;
	}

	@Override
	public Object getValue() {
		editor.stopCellEditing();
		value = editor.getCellEditorValue();
		return value;
	}

	@Override
	public boolean isPaintable() {
		return false;
	}

	@Override
	public void paintValue(Graphics gfx, Rectangle box) {}

	@Override
	public String getJavaInitializationString() {
		return null;
	}

	@Override
	public String getAsText() {
		return null;
	}

	@Override
	public void setAsText(String text) throws IllegalArgumentException {}

	@Override
	public String[] getTags() {
		return null;
	}

	@Override
	public Component getCustomEditor() {
		return editor.getTableCellEditorComponent(null, value, true, 0, 0);
	}

	@Override
	public boolean supportsCustomEditor() {
		return true;
	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener) {}
}

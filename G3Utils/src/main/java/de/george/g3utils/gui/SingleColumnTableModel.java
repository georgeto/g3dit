package de.george.g3utils.gui;

public class SingleColumnTableModel<T> extends ListTableModel<T> {
	private boolean editable;

	public SingleColumnTableModel(String columnName, boolean editable) {
		super(columnName);
		this.editable = editable;
	}

	@Override
	public Object getValueAt(T entry, int col) {
		return entry;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void setValueAt(Object value, int row, int col) {
		entries.set(row, (T) value);
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return editable;
	}

}

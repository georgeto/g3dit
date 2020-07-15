package de.george.g3utils.gui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import com.google.common.collect.ImmutableSortedSet;

public abstract class ListTableModel<T> extends AbstractTableModel {
	private String[] columnNames;
	protected List<T> entries = new ArrayList<>();

	public ListTableModel(String... columnNames) {
		this.columnNames = columnNames;
	}

	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public int getRowCount() {
		return entries.size();
	}

	@Override
	public String getColumnName(int column) {
		return columnNames[column];
	}

	@Override
	public Object getValueAt(int row, int col) {
		return getValueAt(entries.get(row), col);
	}

	public abstract Object getValueAt(T entry, int col);

	@Override
	public void setValueAt(Object value, int row, int col) {
		setValueAt(value, entries.get(row), col);
	}

	public void setValueAt(Object value, T entry, int col) {}

	public List<T> getEntries() {
		return entries;
	}

	public void setEntries(List<T> entries) {
		this.entries = new ArrayList<>(entries);
		fireTableDataChanged();
	}

	public void clearEntries() {
		if (!entries.isEmpty()) {
			entries.clear();
			fireTableDataChanged();
		}
	}

	public void addEntry(T entry) {
		entries.add(entry);
		int insertedRow = entries.size() - 1;
		fireTableRowsInserted(insertedRow, insertedRow);
	}

	public T getEntry(int row) {
		return entries.get(row);
	}

	public void removeEntry(int row) {
		entries.remove(row);
		fireTableRowsDeleted(row, row);
	}

	public void removeEntry(T entry) {
		removeEntry(entries.indexOf(entry));
	}

	public boolean removeEntries(Iterable<Integer> rows) {
		ImmutableSortedSet<Integer> sortedRows = ImmutableSortedSet.<Integer>reverseOrder().addAll(rows).build();
		for (int row : sortedRows) {
			entries.remove(row);
			fireTableRowsDeleted(row, row);
		}
		return !sortedRows.isEmpty();
	}
}

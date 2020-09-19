package de.george.g3dit.gui.table;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.search.SearchFactory;
import org.jdesktop.swingx.table.ColumnFactory;
import org.jdesktop.swingx.table.TableColumnExt;

import com.google.common.collect.ImmutableSortedSet;
import com.google.common.primitives.Booleans;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.gui.AbstractTableComparatorChooser;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.gui.WritableTableFormat;
import ca.odell.glazedlists.swing.AdvancedTableModel;
import ca.odell.glazedlists.swing.GlazedListsSwing;
import ca.odell.glazedlists.swing.TableComparatorChooser;
import de.george.g3utils.gui.SwingUtils;
import one.util.streamex.StreamEx;

public abstract class TableUtil {
	public static int getSelectedRow(JTable table) {
		int selectedRow = table.getSelectedRow();
		if (selectedRow != -1) {
			selectedRow = table.convertRowIndexToModel(selectedRow);
		}
		return selectedRow < table.getModel().getRowCount() ? selectedRow : -1;
	}

	public static void withSelectedRow(JTable table, Consumer<Integer> rowConsumer) {
		int row = getSelectedRow(table);
		if (row != -1) {
			rowConsumer.accept(row);
		}
	}

	public static void withSelectedRowOrInvalid(JTable table, Consumer<Integer> rowConsumer) {
		int row = getSelectedRow(table);
		// Sometimes the table selection model isn't updated before ListSelectionListener is called.
		if (row >= table.getRowCount()) {
			row = -1;
		}
		rowConsumer.accept(row);
	}

	public static StreamEx<Integer> getSelectedRows(JTable table) {
		return StreamEx.of(Arrays.stream(table.getSelectedRows()).mapToObj(r -> table.convertRowIndexToModel(r))
				.filter(r -> r < table.getModel().getRowCount()));
	}

	public static boolean removeRows(DefaultTableModel model, List<Integer> rows) {
		for (int row : ImmutableSortedSet.<Integer>reverseOrder().addAll(rows).build()) {
			model.removeRow(row);
		}
		return !rows.isEmpty();
	}

	public static Runnable enableOnEqual(JTable table, JComponent comp, int value) {
		return enableOnEqual(table, comp, value, () -> true);
	}

	public static Runnable enableOnLesserEqual(JTable table, JComponent comp, int max) {
		return enableOnLesserEqual(table, comp, max, () -> true);
	}

	public static Runnable enableOnGreaterEqual(JTable table, JComponent comp, int min) {
		return enableOnGreaterEqual(table, comp, min, () -> true);
	}

	public static Runnable enableOnEqual(JTable table, JComponent comp, int value, Supplier<Boolean> condition) {
		comp.setEnabled(false);
		return addTableSelectionListener(table, () -> comp.setEnabled(table.getSelectedRowCount() == value && condition.get()));
	}

	public static Runnable enableOnLesserEqual(JTable table, JComponent comp, int max, Supplier<Boolean> condition) {
		comp.setEnabled(false);
		return addTableSelectionListener(table, () -> comp.setEnabled(table.getSelectedRowCount() <= max && condition.get()));
	}

	public static Runnable enableOnGreaterEqual(JTable table, JComponent comp, int min, Supplier<Boolean> condition) {
		comp.setEnabled(false);
		return addTableSelectionListener(table, () -> comp.setEnabled(table.getSelectedRowCount() >= min && condition.get()));
	}

	public static Runnable enableOnEqual(JTable table, Action action, int value) {
		return enableOnEqual(table, action, value, () -> true);
	}

	public static Runnable enableOnLesserEqual(JTable table, Action action, int max) {
		return enableOnLesserEqual(table, action, max, () -> true);
	}

	public static Runnable enableOnGreaterEqual(JTable table, Action action, int min) {
		return enableOnGreaterEqual(table, action, min, () -> true);
	}

	public static Runnable enableOnEqual(JTable table, Action action, int value, Supplier<Boolean> condition) {
		action.setEnabled(false);
		return addTableSelectionListener(table, () -> action.setEnabled(table.getSelectedRowCount() == value && condition.get()));
	}

	public static Runnable enableOnLesserEqual(JTable table, Action action, int max, Supplier<Boolean> condition) {
		action.setEnabled(false);
		return addTableSelectionListener(table, () -> action.setEnabled(table.getSelectedRowCount() <= max && condition.get()));
	}

	public static Runnable enableOnGreaterEqual(JTable table, Action action, int min, Supplier<Boolean> condition) {
		action.setEnabled(false);
		return addTableSelectionListener(table, () -> action.setEnabled(table.getSelectedRowCount() >= min && condition.get()));
	}

	private static Runnable addTableSelectionListener(JTable table, Runnable listener) {
		table.getSelectionModel().addListSelectionListener(x -> listener.run());
		return listener;
	}

	public static void disableSearch(JXTable table) {
		table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).remove(SearchFactory.getInstance().getSearchAccelerator());
	}

	public static MouseListener createDoubleClickListener(Consumer<Integer> modelIndexConsumer) {
		return new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent me) {
				JTable table = (JTable) me.getSource();
				if (me.getClickCount() == 2) {
					int row = table.rowAtPoint(me.getPoint());
					if (row != -1) {
						modelIndexConsumer.accept(table.convertRowIndexToModel(row));
					}
				}
			}
		};
	}

	public static void stopEditing(JTable table) {
		if (table.isEditing()) {
			table.getCellEditor().stopCellEditing();
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> TableFormat<T> tableFormat(Class<T> baseClass, TableColumnDef... tableColumns) {
		WritableTableFormat<T> tableFormat = (WritableTableFormat<T>) GlazedLists.tableFormat(baseClass,
				Arrays.stream(tableColumns).map(TableColumnDef::getFieldName).toArray(String[]::new),
				Arrays.stream(tableColumns).map(TableColumnDef::getDisplayName).toArray(String[]::new),
				Booleans.toArray(Arrays.stream(tableColumns).map(TableColumnDef::isEditable).collect(Collectors.toList())));

		return new TransformingBeanTableFormat<>(tableFormat,
				Arrays.stream(tableColumns).map(TableColumnDef::getCellValueTransformer).toArray(BiFunction[]::new));
	}

	private static final class TableColumnDefColumnFactory extends ColumnFactory {
		private final TableColumnDef[] columns;

		public TableColumnDefColumnFactory(TableColumnDef[] columns) {
			this.columns = columns;
		}

		@Override
		public void configureTableColumn(TableModel model, TableColumnExt columnExt) {
			super.configureTableColumn(model, columnExt);

			if (columnExt.getModelIndex() < columns.length) {
				TableColumnDef columnDef = columns[columnExt.getModelIndex()];
				columnExt.setEditable(columnDef.isEditable());

				if (columnDef.hasCellRenderer()) {
					columnExt.setCellRenderer(columnDef.getCellRenderer());
				}

				if (columnDef.hasCellEditor()) {
					columnExt.setCellEditor(columnDef.getCellEditor());
				}

				if (columnDef.hasComparator()) {
					columnExt.setComparator(columnDef.getComparator());
				}
			}
		}

		@Override
		public void configureColumnWidths(JXTable table, TableColumnExt columnExt) {
			if (columnExt.getModelIndex() < columns.length) {
				TableColumnDef columnDef = columns[columnExt.getModelIndex()];
				if (columnDef.hasPreferredSize()) {
					columnExt.setPreferredWidth(columnDef.getPreferredSize());
					return;
				}
				if (columnDef.hasPreferredSizeExample()) {
					columnExt.setPrototypeValue(columnDef.getPreferredSizeExample());
				}
				if (columnDef.hasMaxSize()) {
					columnExt.setMaxWidth(columnDef.getMaxSize());
				}
			}

			// Calculates the column width from its prototype value, or sets a sane default.
			super.configureColumnWidths(table, columnExt);
		}

	}

	public static ColumnFactory columnFactory(TableColumnDef... tableColumns) {
		return new TableColumnDefColumnFactory(tableColumns);
	}

	public static class SortableEventTable<T> {
		public final SortedList<T> sortedSource;
		public final TableFormat<T> tableFormat;
		public final AdvancedTableModel<T> tableModel;
		public final JXTable table;

		public SortableEventTable(SortedList<T> sortedSource, TableFormat<T> tableFormat, AdvancedTableModel<T> tableModel,
				JXTable table) {
			this.sortedSource = sortedSource;
			this.tableFormat = tableFormat;
			this.tableModel = tableModel;
			this.table = table;
		}

		public T getRowAt(int index) {
			return tableModel.getElementAt(index);
		}

		public Optional<T> getSelectedRow() {
			return Optional.of(TableUtil.getSelectedRow(table)).filter(r -> r >= 0).map(tableModel::getElementAt);
		}

		public StreamEx<T> getSelectedRows() {
			return TableUtil.getSelectedRows(table).map(tableModel::getElementAt);
		}

		public void addSelectionListener(ListSelectionListener l) {
			table.getSelectionModel().addListSelectionListener(l);
		}

		public void addModelListener(TableModelListener l) {
			tableModel.addTableModelListener(l);
		}
	}

	public static <T, V extends T> SortableEventTable<T> createTable(EventList<T> source, Class<V> sourceClass,
			TableColumnDef... tableColumns) {
		return createSortableTable(source, sourceClass, false, tableColumns);
	}

	public static <T, V extends T> SortableEventTable<T> createSortableTable(EventList<T> source, Class<V> sourceClass,
			TableColumnDef... tableColumns) {
		return createSortableTable(source, sourceClass, true, tableColumns);
	}

	private static <T, V extends T> SortableEventTable<T> createSortableTable(EventList<T> source, Class<V> sourceClass, boolean sortable,
			TableColumnDef[] tableColumns) {
		@SuppressWarnings("unchecked")
		TableFormat<T> tableFormat = (TableFormat<T>) TableUtil.tableFormat(sourceClass, tableColumns);

		// Use chained SortedList for TableComparatorChooser, so that the default sort order is
		// always applied as a baseline.
		SortedList<T> sortedSource = new SortedList<>(source, null);
		AdvancedTableModel<T> tableModel = GlazedListsSwing.eventTableModelWithThreadProxyList(sortedSource, tableFormat);

		JXTable table = new JXTable();
		SwingUtils.monospaceFont(table);
		table.setAutoCreateRowSorter(false);
		table.setSortable(false);
		table.setRowSorter(null);
		table.setColumnControlVisible(true);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		table.setColumnFactory(TableUtil.columnFactory(tableColumns));
		table.setModel(tableModel);
		if (sortable) {
			TableComparatorChooser.install(table, sortedSource, AbstractTableComparatorChooser.MULTIPLE_COLUMN_KEYBOARD, tableFormat);
		}

		// Prevent visual artifacts, when the number of table entries changes.
		tableModel.addTableModelListener(l -> table.repaint());

		return new SortableEventTable<>(sortedSource, tableFormat, tableModel, table);
	}
}

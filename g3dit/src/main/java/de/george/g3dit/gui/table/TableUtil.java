package de.george.g3dit.gui.table;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyEditor;
import java.util.Arrays;
import java.util.Collection;
import java.util.EventObject;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.search.SearchFactory;
import org.jdesktop.swingx.table.ColumnFactory;
import org.jdesktop.swingx.table.TableColumnExt;

import com.google.common.collect.ImmutableSortedSet;
import com.google.common.primitives.Booleans;
import com.l2fprod.common.propertysheet.CellEditorAdapter;
import com.l2fprod.common.propertysheet.DefaultProperty;
import com.l2fprod.common.propertysheet.PropertyEditorRegistry;
import com.l2fprod.common.propertysheet.PropertyRendererRegistry;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.gui.AbstractTableComparatorChooser;
import ca.odell.glazedlists.gui.AdvancedTableFormat;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.impl.beans.BeanTableFormat;
import ca.odell.glazedlists.swing.AdvancedTableModel;
import ca.odell.glazedlists.swing.GlazedListsSwing;
import ca.odell.glazedlists.swing.TableComparatorChooser;
import de.george.g3dit.EditorContext;
import de.george.g3dit.gui.components.TableModificationControl;
import de.george.g3dit.util.FileChangeMonitor;
import de.george.g3dit.util.PropertySheetUtil;
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
		return StreamEx.of(Arrays.stream(table.getSelectedRows()).mapToObj(table::convertRowIndexToModel)
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
		BeanTableFormat<T> tableFormat = (BeanTableFormat<T>) GlazedLists.tableFormat(baseClass,
				Arrays.stream(tableColumns).map(TableColumnDef::getFieldName).toArray(String[]::new),
				Arrays.stream(tableColumns).map(TableColumnDef::getDisplayName).toArray(String[]::new),
				Booleans.toArray(Arrays.stream(tableColumns).map(TableColumnDef::isEditable).collect(Collectors.toList())));

		return new TransformingBeanTableFormat<>(tableFormat,
				Arrays.stream(tableColumns).map(TableColumnDef::getCellValueTransformer).toArray(BiFunction[]::new));
	}

	public static TableCellRenderer monospaceTableCellRenderer(TableCellRenderer renderer) {
		if (renderer == null)
			return null;

		return (JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) -> {
			Component comp = renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			SwingUtils.monospaceFont(comp);
			return comp;
		};
	}

	private static class MonospaceTableCellEditor implements TableCellEditor {
		private final TableCellEditor editor;

		public MonospaceTableCellEditor(TableCellEditor editor) {
			this.editor = editor;
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			Component comp = editor.getTableCellEditorComponent(table, value, isSelected, row, column);
			SwingUtils.monospaceFont(comp);
			return comp;
		}

		@Override
		public Object getCellEditorValue() {
			return editor.getCellEditorValue();
		}

		@Override
		public boolean isCellEditable(EventObject event) {
			return editor.isCellEditable(event);
		}

		@Override
		public boolean shouldSelectCell(EventObject event) {
			return editor.shouldSelectCell(event);
		}

		@Override
		public boolean stopCellEditing() {
			return editor.stopCellEditing();
		}

		@Override
		public void cancelCellEditing() {
			editor.cancelCellEditing();
		}

		@Override
		public void addCellEditorListener(CellEditorListener listener) {
			editor.addCellEditorListener(listener);
		}

		@Override
		public void removeCellEditorListener(CellEditorListener listener) {
			editor.removeCellEditorListener(listener);
		}
	}

	public static TableCellEditor monospaceTableCellEditor(TableCellEditor editor) {
		if (editor != null)
			return new MonospaceTableCellEditor(editor);
		return null;
	}

	private static final class TableColumnDefColumnFactory extends ColumnFactory {
		private final TableColumnDef[] columns;

		private PropertyRendererRegistry rendererRegistry;
		private PropertyEditorRegistry editorRegistry;

		public TableColumnDefColumnFactory(EditorContext ctx, TableColumnDef[] columns) {
			this.columns = columns;
			this.rendererRegistry = new PropertyRendererRegistry();
			this.editorRegistry = new PropertyEditorRegistry();
			PropertySheetUtil.registerDefaultRenderers(rendererRegistry, ctx);
			PropertySheetUtil.registerDefaultEditors(editorRegistry, ctx);
		}

		@Override
		public TableColumnExt createAndConfigureTableColumn(TableModel model, int modelIndex) {
			return super.createAndConfigureTableColumn(model, modelIndex);
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

				if (model instanceof AdvancedTableModel<?> advancedModel) {
					if (advancedModel.getTableFormat() instanceof AdvancedTableFormat<?> tableFormat) {
						Class<?> columnClass = tableFormat.getColumnClass(columnExt.getModelIndex());

						if (!columnDef.hasCellRenderer() && columnClass != null) {
							TableCellRenderer renderer = rendererRegistry.createTableCellRenderer(columnClass);
							// TODO: Proxied TableCellRenderer that implements the stuff from
							// TypedProperty getValue().
							if (renderer != null)
								columnExt.setCellRenderer(monospaceTableCellRenderer(renderer));
						}

						if (!columnDef.hasCellEditor() && columnClass != null) {
							// TypedProperty property = new
							// TypedProperty(columnDef.getDisplayName(), columnClass);
							DefaultProperty property = new DefaultProperty();
							property.setType(columnClass);
							PropertyEditor editor = editorRegistry.createPropertyEditor(property);
							if (editor != null)
								// TODO: Proxied TableCellEditor that implements the stuff from
								// TypedProperty setValue().
								columnExt.setCellEditor(monospaceTableCellEditor(new CellEditorAdapter(editor)));
						}
					}
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

	public static ColumnFactory columnFactory(EditorContext ctx, TableColumnDef... tableColumns) {
		return new TableColumnDefColumnFactory(ctx, tableColumns);
	}

	public static class SortableEventTable<T> {
		private final SortedList<T> sortedSource;
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

		public TableModificationControl<T> createModificationControl(FileChangeMonitor changeMonitor, Supplier<T> entrySupplier) {
			return new TableModificationControl<>(changeMonitor, table, sortedSource, entrySupplier);
		}

		public TableModificationControl<T> createModificationControl(FileChangeMonitor changeMonitor, Supplier<T> entrySupplier,
				Function<T, Optional<T>> cloneEntry, boolean movable) {
			return new TableModificationControl<>(changeMonitor, table, sortedSource, entrySupplier, cloneEntry, movable);
		}

		public boolean isEditing() {
			return table.isEditing();
		}

		public void stopEditing() {
			TableUtil.stopEditing(table);
		}

		public boolean isEmpty() {
			return sortedSource.isEmpty();
		}

		public List<T> getEntries() {
			return GlazedLists.readOnlyList(sortedSource);
		}

		public <R> List<R> getEntries(Function<? super T, ? extends R> mapper) {
			return sortedSource.stream().map(mapper).collect(Collectors.toList());
		}

		public void setEntries(Collection<T> entries) {
			sortedSource.clear();
			sortedSource.addAll(entries);
		}

		public <R> void setEntries(Collection<R> entries, Function<? super R, ? extends T> mapper) {
			sortedSource.clear();
			entries.stream().map(mapper).forEach(sortedSource::add);
		}

		public void clearEntries() {
			sortedSource.clear();
		}
	}

	public static <T, V extends T> SortableEventTable<T> createTable(EditorContext ctx, EventList<T> source, Class<V> sourceClass,
			TableColumnDef... tableColumns) {
		return createSortableTable(ctx, source, sourceClass, false, tableColumns);
	}

	public static <T, V extends T> SortableEventTable<T> createSortableTable(EditorContext ctx, EventList<T> source, Class<V> sourceClass,
			TableColumnDef... tableColumns) {
		return createSortableTable(ctx, source, sourceClass, true, tableColumns);
	}

	private static <T, V extends T> SortableEventTable<T> createSortableTable(EditorContext ctx, EventList<T> source, Class<V> sourceClass,
			boolean sortable, TableColumnDef[] tableColumns) {
		@SuppressWarnings("unchecked")
		TableFormat<T> tableFormat = (TableFormat<T>) TableUtil.tableFormat(sourceClass, tableColumns);

		// Use chained SortedList for TableComparatorChooser, so that the default sort order is
		// always applied as a baseline.
		SortedList<T> sortedSource = new SortedList<>(source, null);
		AdvancedTableModel<T> tableModel = GlazedListsSwing.eventTableModelWithThreadProxyList(sortedSource, tableFormat);

		JXTable table = new JXTable();
		// TODO: Configure per column?! Maybe by overriding getCellRender/getCellEditor of
		// TableColumnExt in createAndConfigureTableColumn... Then we could also move the property
		// editor lookup handling into that.
		SwingUtils.monospaceFont(table);
		table.setAutoCreateRowSorter(false);
		table.setSortable(false);
		table.setRowSorter(null);
		table.setColumnControlVisible(true);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		table.setColumnFactory(TableUtil.columnFactory(ctx, tableColumns));
		table.setModel(tableModel);
		if (sortable) {
			TableComparatorChooser.install(table, sortedSource, AbstractTableComparatorChooser.MULTIPLE_COLUMN_KEYBOARD, tableFormat);
		}

		// Prevent visual artifacts, when the number of table entries changes.
		tableModel.addTableModelListener(l -> table.repaint());

		return new SortableEventTable<>(sortedSource, tableFormat, tableModel, table);
	}
}

package de.george.g3dit.gui.table;

import java.util.Comparator;
import java.util.function.BiFunction;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class TableColumnDef {
	private final String fieldName;
	private final String displayName;
	private final int preferredSize;
	private final int maxSize;
	private final Object preferredSizeExample;
	private final boolean editable;
	private final BiFunction<? super Object, ? super Object, ?> cellValueTransformer;
	private final TableCellRenderer cellRenderer;
	private final TableCellEditor cellEditor;
	private final Comparator<?> comparator;

	private TableColumnDef(String fieldName, String displayName, int preferredSize, Object preferredSizeExample, int maxSize,
			boolean editable, BiFunction<? super Object, ? super Object, ?> cellValueTransformer, TableCellRenderer cellRenderer,
			TableCellEditor cellEditor, Comparator<?> comparator) {
		this.fieldName = fieldName;
		this.displayName = displayName;
		this.preferredSize = preferredSize;
		this.preferredSizeExample = preferredSizeExample;
		this.maxSize = maxSize;
		this.editable = editable;
		this.cellValueTransformer = cellValueTransformer;
		this.cellRenderer = cellRenderer;
		this.cellEditor = cellEditor;
		this.comparator = comparator;
	}

	public String getFieldName() {
		return fieldName;
	}

	public String getDisplayName() {
		return displayName;
	}

	public boolean hasPreferredSize() {
		return preferredSize != -1;
	}

	public int getPreferredSize() {
		return preferredSize;
	}

	public boolean hasPreferredSizeExample() {
		return preferredSizeExample != null;
	}

	public Object getPreferredSizeExample() {
		return preferredSizeExample;
	}

	public boolean hasMaxSize() {
		return maxSize != -1;
	}

	public int getMaxSize() {
		return maxSize;
	}

	public boolean isEditable() {
		return editable;
	}

	public BiFunction<? super Object, ? super Object, ?> getCellValueTransformer() {
		return cellValueTransformer;
	}

	public boolean hasCellRenderer() {
		return cellRenderer != null;
	}

	public TableCellRenderer getCellRenderer() {
		return cellRenderer;
	}

	public boolean hasCellEditor() {
		return cellEditor != null;
	}

	public TableCellEditor getCellEditor() {
		return cellEditor;
	}

	public boolean hasComparator() {
		return comparator != null;
	}

	public Comparator<?> getComparator() {
		return comparator;
	}

	public static final Builder withName(String fieldName) {
		return new Builder().fieldName(fieldName);
	}

	public static final class Builder {
		private String fieldName;
		private String displayName;
		private int preferredSize = -1;
		private Object preferredSizeExample;
		private int maxSize = -1;
		private boolean editable = false;
		private BiFunction<? super Object, ? super Object, ?> cellValueTransformer;
		private TableCellRenderer cellRenderer;
		private TableCellEditor cellEditor;
		private Comparator<?> comparator;

		private Builder() {}

		/**
		 * @param fieldName Name of the bean field.
		 */
		public Builder fieldName(String fieldName) {
			this.fieldName = fieldName;
			return this;
		}

		/**
		 * @param displayName Name under which the column is presented (default {@link #fieldName}.
		 */
		public Builder displayName(String displayName) {
			this.displayName = displayName;
			return this;
		}

		/**
		 * @param preferredSize Preferred size of the column. Mutually exclusive with
		 *            {@link #sizeExample(String)}.
		 */
		public Builder size(int preferredSize) {
			this.preferredSize = preferredSize;
			preferredSizeExample = null;
			return this;
		}

		/**
		 * @param preferredSizeExample Text from which the preferred size of the column is derived
		 *            Mutually exclusive with {@link #size(int)}.
		 */
		public Builder sizeExample(Object preferredSizeExample) {
			this.preferredSizeExample = preferredSizeExample;
			preferredSize = -1;
			return this;
		}

		/**
		 * {@link Builder#sizeExample(String)} with {@code displayName} as argument.
		 */
		public Builder sizeFromName() {
			return sizeExample(displayName != null ? displayName : fieldName);
		}

		public Builder maxSize(int maxSize) {
			this.maxSize = maxSize;
			return this;
		}

		/**
		 * @param editable Whether column should be editable (default {@code false}).
		 */
		public Builder editable(boolean editable) {
			this.editable = editable;
			return this;
		}

		/**
		 * @param cellValueTransformer Transforms values before they are passed to the
		 *            {@link #cellRenderer(TableCellRenderer)}.
		 */
		@SuppressWarnings("unchecked")
		public <T, V> Builder cellValueTransformer(BiFunction<T, V, ? extends Object> cellValueTransformer) {
			this.cellValueTransformer = (BiFunction<? super Object, ? super Object, ? extends Object>) cellValueTransformer;
			return this;
		}

		/**
		 * @param cellRenderer Renderer used to render the cell.
		 */
		public Builder cellRenderer(TableCellRenderer cellRenderer) {
			this.cellRenderer = cellRenderer;
			return this;
		}

		/**
		 * @param cellEditor Editor used to edit the cell.
		 */
		public Builder cellEditor(TableCellEditor cellEditor) {
			this.cellEditor = cellEditor;
			return this;
		}

		/**
		 * @param comparator Comparator used for sorting the column.
		 */
		public Builder comparator(Comparator<?> comparator) {
			this.comparator = comparator;
			return this;
		}

		public TableColumnDef b() {
			return new TableColumnDef(fieldName, displayName != null ? displayName : fieldName, preferredSize, preferredSizeExample,
					maxSize, editable, cellValueTransformer, cellRenderer, cellEditor, comparator);
		}
	}
}

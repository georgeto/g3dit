package de.george.g3dit.gui.table;

import java.util.function.BiFunction;

import ca.odell.glazedlists.gui.WritableTableFormat;

public class TransformingBeanTableFormat<T> implements WritableTableFormat<T> {
	private final WritableTableFormat<T> wrapped;
	private BiFunction<T, ? super Object, ? extends Object>[] transformers;

	public TransformingBeanTableFormat(WritableTableFormat<T> wrapped,
			BiFunction<T, ? super Object, ? extends Object>[] cellValueTransformer) {
		this.wrapped = wrapped;
		this.transformers = cellValueTransformer;
	}

	@Override
	public boolean isEditable(T baseObject, int column) {
		return wrapped.isEditable(baseObject, column);
	}

	@Override
	public T setColumnValue(T baseObject, Object editedValue, int column) {
		return wrapped.setColumnValue(baseObject, editedValue, column);
	}

	@Override
	public int getColumnCount() {
		return wrapped.getColumnCount();
	}

	@Override
	public String getColumnName(int column) {
		return wrapped.getColumnName(column);
	}

	@Override
	public Object getColumnValue(T baseObject, int column) {
		Object value = wrapped.getColumnValue(baseObject, column);
		if (transformers[column] != null) {
			value = transformers[column].apply(baseObject, value);
		}
		return value;
	}
}

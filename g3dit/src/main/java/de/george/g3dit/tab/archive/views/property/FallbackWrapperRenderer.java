package de.george.g3dit.tab.archive.views.property;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import com.l2fprod.common.propertysheet.PropertyRendererRegistry;

public class FallbackWrapperRenderer implements TableCellRenderer {

	private final PropertyRendererRegistry registry;

	public FallbackWrapperRenderer(PropertyRendererRegistry registry) {
		this.registry = registry;
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		FallbackWrapper wrapper = (FallbackWrapper) value;

		Class<?> type = wrapper.valueType;
		TableCellRenderer renderer = null;
		while (type != null) {
			renderer = registry.createTableCellRenderer(type);
			if (renderer != null)
				break;
			type = type.getSuperclass();
		}

		if (renderer == null)
			renderer = table.getDefaultRenderer(Object.class);

		return renderer.getTableCellRendererComponent(table, wrapper.value, isSelected, hasFocus, row, column);
	}
}

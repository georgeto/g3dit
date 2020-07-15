package de.george.g3dit.util;

import java.awt.Color;
import java.lang.reflect.Field;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.table.TableColumn;

import com.l2fprod.common.propertysheet.PropertySheet;
import com.l2fprod.common.propertysheet.PropertySheetPanel;

import de.george.g3dit.tab.archive.views.property.G3EnumArrayPropertyEditor;
import de.george.g3dit.tab.archive.views.property.G3EnumArrayWrapper;
import de.george.g3dit.tab.archive.views.property.G3EnumEditor;
import de.george.g3dit.tab.archive.views.property.G3EnumWrapper;
import de.george.g3dit.tab.archive.views.property.JsonPropertyValueConverter.JsonStringWrapper;
import de.george.g3dit.tab.archive.views.property.JsonStringPropertyEditor;

public class PropertySheetUtil {
	public static PropertySheetPanel createPropertyField() {
		PropertySheetPanel sheet = createBasicPropertySheetPanel();
		sheet.setMode(PropertySheet.VIEW_AS_FLAT_LIST);
		sheet.setDescriptionVisible(false);
		sheet.setToolBarVisible(false);
		sheet.setSorting(false);

		// Hide name column
		TableColumn nameColumn = sheet.getTable().getColumnModel().getColumn(0);
		nameColumn.setMinWidth(0);
		nameColumn.setMaxWidth(0);
		nameColumn.setPreferredWidth(0);
		nameColumn.setResizable(false);
		// sheet.getTable().removeColumn(sheet.getTable().getColumnModel().getColumn(0));
		return sheet;
	}

	public static PropertySheetPanel createPropertySheetPanel() {
		PropertySheetPanel sheet = createBasicPropertySheetPanel();
		sheet.setMode(PropertySheet.VIEW_AS_CATEGORIES);
		sheet.setDescriptionVisible(true);
		try {
			Field declaredField = sheet.getClass().getDeclaredField("tableScroll");
			declaredField.setAccessible(true);
			((JScrollPane) declaredField.get(sheet)).setBorder(BorderFactory.createLineBorder(Color.BLACK));
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}

		return sheet;
	}

	@SuppressWarnings("deprecation")
	private static final PropertySheetPanel createBasicPropertySheetPanel() {
		PropertySheetPanel sheet = new PropertySheetPanel();
		sheet.getEditorRegistry().registerEditor(G3EnumWrapper.class, G3EnumEditor.class);
		sheet.getEditorRegistry().registerEditor(G3EnumArrayWrapper.class, G3EnumArrayPropertyEditor.class);
		sheet.getEditorRegistry().registerEditor(JsonStringWrapper.class, JsonStringPropertyEditor.class);
		return sheet;
	}
}

package de.george.g3dit.util;

import java.awt.Color;
import java.lang.reflect.Field;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.table.TableColumn;

import com.l2fprod.common.propertysheet.PropertyEditorRegistry;
import com.l2fprod.common.propertysheet.PropertyRendererRegistry;
import com.l2fprod.common.propertysheet.PropertySheet;
import com.l2fprod.common.propertysheet.PropertySheetPanel;

import de.george.g3dit.EditorContext;
import de.george.g3dit.gui.table.renderer.NamedGuidTableCellReditor;
import de.george.g3dit.tab.archive.views.property.EntityProxyMarker;
import de.george.g3dit.tab.archive.views.property.G3EnumArrayPropertyEditor;
import de.george.g3dit.tab.archive.views.property.G3EnumArrayWrapper;
import de.george.g3dit.tab.archive.views.property.G3EnumEditor;
import de.george.g3dit.tab.archive.views.property.G3EnumWrapper;
import de.george.g3dit.tab.archive.views.property.JsonPropertyValueConverter.JsonStringWrapper;
import de.george.g3dit.tab.archive.views.property.JsonStringPropertyEditor;
import de.george.g3dit.tab.archive.views.property.PropertyEditorAdapter;
import de.george.g3dit.tab.archive.views.property.TemplateProxyMarker;

public class PropertySheetUtil {
	public static PropertySheetPanel createPropertyField(EditorContext ctx) {
		PropertySheetPanel sheet = createBasicPropertySheetPanel(ctx);
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

	public static PropertySheetPanel createPropertySheetPanel(EditorContext ctx) {
		PropertySheetPanel sheet = createBasicPropertySheetPanel(ctx);
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
	private static PropertySheetPanel createBasicPropertySheetPanel(EditorContext ctx) {
		PropertySheetPanel sheet = new PropertySheetPanel();
		registerDefaultRenderers(sheet.getRendererRegistry(), ctx);
		registerDefaultEditors(sheet.getEditorRegistry(), ctx);
		// Workaround to make category readable with dark theme (original code uses darker() three
		// times, which brings it too close to background color).
		sheet.getTable().setCategoryForeground(sheet.getTable().getPropertyForeground().darker());
		sheet.getTable().setSelectedCategoryForeground(sheet.getTable().getCategoryForeground());
		return sheet;
	}

	public static void registerDefaultRenderers(PropertyRendererRegistry registry, EditorContext ctx) {
		registry.registerRenderer(EntityProxyMarker.class, new NamedGuidTableCellReditor(ctx, false, 70));
		registry.registerRenderer(TemplateProxyMarker.class, new NamedGuidTableCellReditor(ctx, true, 70));
	}

	public static void registerDefaultEditors(PropertyEditorRegistry registry, EditorContext ctx) {
		registry.registerEditor(G3EnumWrapper.class, G3EnumEditor.class);
		registry.registerEditor(G3EnumArrayWrapper.class, G3EnumArrayPropertyEditor.class);
		registry.registerEditor(JsonStringWrapper.class, JsonStringPropertyEditor.class);
		registry.registerEditor(EntityProxyMarker.class, new PropertyEditorAdapter(new NamedGuidTableCellReditor(ctx, false, 70)));
		registry.registerEditor(TemplateProxyMarker.class, new PropertyEditorAdapter(new NamedGuidTableCellReditor(ctx, true, 70)));
	}
}

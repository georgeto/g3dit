package de.george.g3dit.gui.table.renderer;

import java.awt.Component;

import javax.swing.AbstractCellEditor;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.ListCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.TreeCellEditor;

import org.netbeans.validation.api.Validator;
import org.netbeans.validation.api.ui.ValidationGroup;

import de.george.g3dit.EditorContext;
import de.george.g3dit.gui.components.JEntityGuidField;
import de.george.g3dit.gui.components.JSearchNamedGuidField;
import de.george.g3dit.gui.components.JSearchNamedGuidField.Layout;
import de.george.g3dit.gui.components.JTemplateGuidField;
import de.george.g3utils.structure.GuidUtil;

public class NamedGuidTableCellReditor extends AbstractCellEditor
		implements TableCellEditor, TreeCellEditor, TableCellRenderer, ListCellRenderer<String> {

	private final JSearchNamedGuidField tfGuidField;

	public NamedGuidTableCellReditor(EditorContext ctx, boolean template) {
		tfGuidField = template ? new JTemplateGuidField(ctx) : new JEntityGuidField(ctx);
		tfGuidField.setFieldLayout(Layout.Horizontal, 50);
	}

	public NamedGuidTableCellReditor(EditorContext ctx, boolean template, ValidationGroup group, String fieldName,
			Validator... validators) {
		this(ctx, template);
		tfGuidField.initValidation(group, fieldName, validators);
	}

	@Override
	public Object getCellEditorValue() {
		return GuidUtil.parseGuid(tfGuidField.getText());
	}

	@Override
	public Component getListCellRendererComponent(JList<? extends String> list, String value, int index, boolean isSelected,
			boolean cellHasFocus) {
		return getComponent(value);
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		return getComponent((String) value);
	}

	@Override
	public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {
		return getComponent((String) value);
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		return getComponent((String) value);
	}

	private Component getComponent(String value) {
		tfGuidField.setText(value);
		return tfGuidField;
	}
}

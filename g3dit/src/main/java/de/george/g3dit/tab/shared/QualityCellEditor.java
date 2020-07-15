package de.george.g3dit.tab.shared;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractCellEditor;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.table.TableCellEditor;
import javax.swing.tree.TreeCellEditor;

public class QualityCellEditor extends AbstractCellEditor implements TableCellEditor, TreeCellEditor {

	private QualityPanel qualityPanel;
	private QualityPanelPopup popup;
	private JTextField tfValue;

	public QualityCellEditor() {
		qualityPanel = new QualityPanel();
		popup = new QualityPanelPopup();

		tfValue = new JTextField();
		tfValue.setEditable(false);
		tfValue.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				showPopup();
			}
		});
	}

	@Override
	public Integer getCellEditorValue() {
		return qualityPanel.getQuality();
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		qualityPanel.setQuality(getValueAsQuality(value));
		tfValue.setText(QualityPanel.getQualityAsString(qualityPanel.getQuality()));
		return tfValue;
	}

	@Override
	public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {
		qualityPanel.setQuality(getValueAsQuality(value));
		tfValue.setText(QualityPanel.getQualityAsString(qualityPanel.getQuality()));
		return tfValue;
	}

	protected int getValueAsQuality(Object value) {
		return (int) value;
	}

	protected boolean isEmpty(Object value) {
		return value == null || value instanceof String && ((String) value).length() == 0;
	}

	public void showPopup() {
		if (!popup.isVisible()) {
			popup.show(tfValue, 0, tfValue.getHeight());
		}
	}

	protected class QualityPanelPopup extends JPopupMenu {
		public QualityPanelPopup() {
			setLayout(new BorderLayout());
			add(qualityPanel, BorderLayout.CENTER);
		}
	}
}

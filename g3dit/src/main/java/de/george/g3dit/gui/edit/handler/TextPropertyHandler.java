package de.george.g3dit.gui.edit.handler;

import javax.swing.JPanel;

import de.george.g3dit.gui.edit.PropertyPanelDef;
import de.george.g3utils.gui.SwingUtils;
import de.george.g3utils.gui.UndoableTextField;
import de.george.lrentnode.properties.bCString;

public class TextPropertyHandler extends TitledPropertyHandler<bCString> {
	private UndoableTextField tfValue;

	public TextPropertyHandler(PropertyPanelDef def) {
		super(def);
	}

	@Override
	protected void addValueComponent(JPanel content) {
		tfValue = SwingUtils.createUndoTF();
		tfValue.setToolTipText(def.getTooltip());
		tfValue.setEditable(def.isEditable());
		def.apply(tfValue);
		content.add(tfValue, "width 100:100:, grow");
	}

	@Override
	protected void load(bCString value) {
		tfValue.setText(value.getString());
		tfValue.setCaretPosition(0);
	}

	@Override
	protected bCString save() {
		return new bCString(tfValue.getText());
	}
}

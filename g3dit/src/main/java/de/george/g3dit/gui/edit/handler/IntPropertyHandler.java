package de.george.g3dit.gui.edit.handler;

import javax.swing.JPanel;

import de.george.g3dit.gui.edit.PropertyPanelDef;
import de.george.g3utils.gui.SwingUtils;
import de.george.g3utils.gui.UndoableTextField;
import de.george.lrentnode.properties.gInt;

public class IntPropertyHandler extends TitledPropertyHandler<gInt> {
	private UndoableTextField tfValue;

	public IntPropertyHandler(PropertyPanelDef def) {
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
	protected void load(gInt value) {
		tfValue.setText(Integer.toString(value.getInt()));
	}

	@Override
	protected gInt save() {
		return new gInt(Integer.valueOf(tfValue.getText()));
	}
}

package de.george.g3dit.gui.edit.handler;

import javax.swing.JPanel;

import de.george.g3dit.gui.edit.PropertyPanelDef;
import de.george.g3utils.gui.SwingUtils;
import de.george.g3utils.gui.UndoableTextField;
import de.george.lrentnode.properties.gLong;

public class LongPropertyHandler extends TitledPropertyHandler<gLong> {
	private UndoableTextField tfValue;

	public LongPropertyHandler(PropertyPanelDef def) {
		super(def);
	}

	@Override
	protected void addValueComponent(JPanel content) {
		tfValue = SwingUtils.createUndoTF();
		def.apply(tfValue);
		content.add(tfValue, "grow");
	}

	@Override
	protected void load(gLong value) {
		tfValue.setText(Integer.toString(value.getLong()));
	}

	@Override
	protected gLong save() {
		return new gLong(Integer.valueOf(tfValue.getText()));
	}
}

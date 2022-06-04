package de.george.g3dit.gui.edit.handler;

import javax.swing.JPanel;

import de.george.g3dit.gui.components.FloatSpinner;
import de.george.g3dit.gui.edit.PropertyPanelDef;
import de.george.lrentnode.properties.gFloat;

public class FloatPropertyHandler extends TitledPropertyHandler<gFloat> {
	private FloatSpinner spValue;

	public FloatPropertyHandler(PropertyPanelDef def) {
		super(def);
	}

	@Override
	protected void addValueComponent(JPanel content) {
		spValue = new FloatSpinner();
		spValue.setToolTipText(def.getTooltip());
		spValue.setEnabled(def.isEditable());
		def.apply(spValue);
		content.add(spValue, "width 100:100:, grow");
	}

	@Override
	protected void load(gFloat value) {
		spValue.setVal(value.getFloat());
	}

	@Override
	protected gFloat save() {
		return new gFloat(spValue.getVal());
	}
}

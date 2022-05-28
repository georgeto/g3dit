package de.george.g3dit.gui.edit.handler;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import de.george.g3dit.gui.edit.PropertyPanelDef;
import de.george.lrentnode.properties.gBool;

public class BooleanPropertyHandler extends TypedPropertyHandler<gBool> {
	private PropertyPanelDef def;
	private JCheckBox cbBoolean;

	public BooleanPropertyHandler(PropertyPanelDef def) {
		this.def = def;
	}

	@Override
	protected JPanel initContent() {
		JPanel content = super.initContent();
		cbBoolean = new JCheckBox(def.getName());
		cbBoolean.setToolTipText(def.getTooltip());
		cbBoolean.setEnabled(def.isEditable());
		def.apply(cbBoolean);
		content.add(cbBoolean, "");
		return content;
	}

	@Override
	protected void load(gBool value) {
		cbBoolean.setSelected(value.isBool());
	}

	@Override
	protected gBool save() {
		return new gBool(cbBoolean.isSelected());
	}
}

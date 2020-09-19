package de.george.g3dit.gui.edit.handler;

import javax.swing.JPanel;

import de.george.g3dit.gui.edit.PropertyPanelDef;
import de.george.g3utils.gui.JComboBoxExt;
import de.george.lrentnode.properties.bCString;

public class SelectPropertyHandler extends TitledPropertyHandler<bCString> {
	private JComboBoxExt<String> cbValue;

	public SelectPropertyHandler(PropertyPanelDef def) {
		super(def);
	}

	@Override
	protected void addValueComponent(JPanel content) {
		cbValue = new JComboBoxExt<>(def.getValueList());
		def.apply(cbValue);
		content.add(cbValue, "width 100:100:, grow");
	}

	@Override
	protected void load(bCString value) {
		cbValue.setSelectedItem(value.getString());
	}

	@Override
	protected bCString save() {
		return new bCString(cbValue.getSelectedItem());
	}
}

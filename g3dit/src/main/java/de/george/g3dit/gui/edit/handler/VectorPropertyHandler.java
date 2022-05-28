package de.george.g3dit.gui.edit.handler;

import javax.swing.JPanel;

import de.george.g3dit.gui.edit.PropertyPanelDef;
import de.george.g3utils.gui.SwingUtils;
import de.george.g3utils.gui.UndoableTextField;
import de.george.g3utils.structure.bCVector;

public class VectorPropertyHandler extends TitledPropertyHandler<bCVector> {
	private UndoableTextField tfValue;

	public VectorPropertyHandler(PropertyPanelDef def) {
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
	protected void load(bCVector value) {
		tfValue.setText(value.toString());
	}

	@Override
	protected bCVector save() {
		return bCVector.fromString(tfValue.getText());
	}
}

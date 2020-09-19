package de.george.g3dit.gui.edit.handler;

import javax.swing.JComponent;
import javax.swing.JLabel;

import de.george.g3dit.gui.edit.adapter.PropertyAdapter;
import de.george.g3utils.io.G3Serializable;
import de.george.lrentnode.archive.eCEntity;

public class LabelPropertyHandler implements PropertyHandler<G3Serializable> {
	private final JLabel lText;

	public LabelPropertyHandler(String text) {
		lText = new JLabel(text);
	}

	@Override
	public JComponent getContent() {
		return lText;
	}

	@Override
	public void load(eCEntity entity, PropertyAdapter<G3Serializable> property) {}

	@Override
	public void save(eCEntity entity, PropertyAdapter<G3Serializable> property) {}

}

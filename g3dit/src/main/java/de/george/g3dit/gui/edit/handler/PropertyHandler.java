package de.george.g3dit.gui.edit.handler;

import javax.swing.JComponent;

import de.george.g3dit.gui.edit.adapter.PropertyAdapter;
import de.george.g3utils.io.G3Serializable;
import de.george.lrentnode.archive.eCEntity;

public interface PropertyHandler<T extends G3Serializable> {
	public JComponent getContent();

	public void load(eCEntity entity, PropertyAdapter<T> property);

	public void save(eCEntity entity, PropertyAdapter<T> property);
}

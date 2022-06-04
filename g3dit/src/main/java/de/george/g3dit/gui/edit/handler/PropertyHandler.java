package de.george.g3dit.gui.edit.handler;

import javax.swing.JComponent;

import de.george.g3dit.gui.edit.adapter.PropertyAdapter;
import de.george.g3utils.io.G3Serializable;
import de.george.lrentnode.archive.G3ClassContainer;

public interface PropertyHandler<T extends G3Serializable> {
	public JComponent getContent();

	public void load(G3ClassContainer container, PropertyAdapter<T> property);

	public void save(G3ClassContainer container, PropertyAdapter<T> property);
}

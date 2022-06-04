package de.george.g3dit.gui.edit.handler;

import java.util.function.Consumer;

import javax.swing.JComponent;

import de.george.g3dit.gui.edit.adapter.PropertyAdapter;
import de.george.g3utils.io.G3Serializable;
import de.george.lrentnode.archive.G3ClassContainer;

public class LambdaPropertyHandler implements PropertyHandler<G3Serializable> {
	private final JComponent content;
	private final Consumer<G3ClassContainer> load;
	private final Consumer<G3ClassContainer> save;

	public LambdaPropertyHandler(JComponent content, Consumer<G3ClassContainer> load, Consumer<G3ClassContainer> save) {
		this.content = content;
		this.load = load;
		this.save = save;
	}

	@Override
	public void load(G3ClassContainer container, PropertyAdapter<G3Serializable> property) {
		load.accept(container);
	}

	@Override
	public void save(G3ClassContainer container, PropertyAdapter<G3Serializable> property) {
		save.accept(container);
	}

	@Override
	public JComponent getContent() {
		return content;
	}
}

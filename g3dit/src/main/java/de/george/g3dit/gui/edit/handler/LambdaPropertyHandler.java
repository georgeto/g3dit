package de.george.g3dit.gui.edit.handler;

import java.util.function.Consumer;

import javax.swing.JComponent;

import de.george.g3dit.gui.edit.adapter.PropertyAdapter;
import de.george.g3utils.io.G3Serializable;
import de.george.lrentnode.archive.eCEntity;

public class LambdaPropertyHandler implements PropertyHandler<G3Serializable> {
	private final JComponent content;
	private final Consumer<eCEntity> load;
	private final Consumer<eCEntity> save;

	public LambdaPropertyHandler(JComponent content, Consumer<eCEntity> load, Consumer<eCEntity> save) {
		this.content = content;
		this.load = load;
		this.save = save;
	}

	@Override
	public void load(eCEntity entity, PropertyAdapter<G3Serializable> property) {
		load.accept(entity);
	}

	@Override
	public void save(eCEntity entity, PropertyAdapter<G3Serializable> property) {
		save.accept(entity);
	}

	@Override
	public JComponent getContent() {
		return content;
	}
}

package de.george.g3dit.tab.archive.views;

import java.awt.Component;

import de.george.g3dit.entitytree.filter.ITreeExtension;
import de.george.g3dit.tab.archive.EditorArchiveTab;
import de.george.g3dit.tab.shared.SharedPropertyView;
import de.george.lrentnode.archive.eCEntity;

public class PropertyView extends SingleEntityArchiveView {
	private final SharedPropertyView content;

	public PropertyView(EditorArchiveTab ctx) {
		super(ctx);
		content = new SharedPropertyView(ctx);
	}

	@Override
	public Component getContent() {
		return content;
	}

	@Override
	public ITreeExtension getTreeExtension() {
		return new EntityTreeExtension(ctx);
	}

	@Override
	public void cleanUp() {
		content.cleanUp();
	}

	@Override
	public void loadInternal(eCEntity entity) {
		content.load(entity);
	}

	@Override
	public void save(eCEntity entity) {
		content.save(entity);
	}
}

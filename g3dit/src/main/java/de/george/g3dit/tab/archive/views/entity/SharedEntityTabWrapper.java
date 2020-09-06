package de.george.g3dit.tab.archive.views.entity;

import java.awt.Container;

import javax.swing.JComponent;

import de.george.g3dit.EditorContext;
import de.george.g3dit.tab.archive.EditorArchiveTab;
import de.george.g3dit.tab.shared.AbstractSharedTab;
import de.george.lrentnode.archive.eCEntity;

public class SharedEntityTabWrapper extends AbstractEntityTab {
	private AbstractSharedTab sharedTab;

	public SharedEntityTabWrapper(Class<? extends AbstractSharedTab> sharedTab, EditorArchiveTab ctx) {
		super(ctx);
		try {
			this.sharedTab = sharedTab.getConstructor(EditorContext.class, Container.class).newInstance(ctx, this);
		} catch (ReflectiveOperationException | SecurityException e) {
			if (e.getSuppressed().length > 0) {
				throw new RuntimeException(e.getSuppressed()[0]);
			}
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void initComponents() {
		sharedTab.initComponents(validation(), getScrollpane());
	}

	@Override
	public void cleanUp() {
		sharedTab.cleanUp();
	}

	@Override
	public String getTabTitle() {
		return sharedTab.getTabTitle();
	}

	@Override
	public void loadValues(eCEntity entity) {
		sharedTab.loadValues(entity);
	}

	@Override
	public void saveValues(eCEntity entity) {
		sharedTab.saveValues(entity);
	}

	@Override
	public boolean isActive(eCEntity entity) {
		return sharedTab.isActive(entity);
	}

	@Override
	public void registerKeyStrokes(JComponent container) {
		sharedTab.registerKeyStrokes(container);
	}

	@Override
	public void unregisterKeyStrokes(JComponent container) {
		sharedTab.unregisterKeyStrokes(container);
	}
}

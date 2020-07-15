package de.george.g3dit.tab.archive.views.entity;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JComponent;

import de.george.g3dit.gui.components.Keystroker;
import de.george.g3dit.gui.components.ValidationPanelContainer;
import de.george.g3dit.gui.components.tab.ITypedTab;
import de.george.g3dit.tab.archive.EditorArchiveTab;
import de.george.lrentnode.archive.eCEntity;

public abstract class AbstractEntityTab extends ValidationPanelContainer<AbstractEntityTab> implements Keystroker, ITypedTab {
	protected final EditorArchiveTab ctx;

	public AbstractEntityTab(EditorArchiveTab ctx) {
		this.ctx = ctx;
	}

	/**
	 * Wird aufgerufen, wenn die TemplateHeaderView, die diesen EntityTab enthält, geschlossen wird.
	 * In dieser Methode kann der EntityTab Resourcen freigeben.
	 */
	public void cleanUp() {}

	/**
	 * Nach dem Umschalten auf eine Entity aufrufen, um deren Werte ins GUI zu laden.
	 */
	public abstract void loadValues(eCEntity entity);

	/**
	 * Vor dem Umschalten auf eine andere Entity aufrufen, um Änderungen zu speichern.
	 */
	public abstract void saveValues(eCEntity entity);

	public abstract boolean isActive(eCEntity entity);

	@Override
	public void registerKeyStrokes(JComponent container) {

	}

	@Override
	public void unregisterKeyStrokes(JComponent container) {

	}

	@Override
	public Icon getTabIcon() {
		return null;
	}

	@Override
	public Component getTabContent() {
		return getLayeredPane();
	}
}

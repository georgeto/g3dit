package de.george.g3dit.tab.template.views.header;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JComponent;

import de.george.g3dit.gui.components.Keystroker;
import de.george.g3dit.gui.components.ValidationPanelContainer;
import de.george.g3dit.gui.components.tab.ITypedTab;
import de.george.g3dit.tab.template.EditorTemplateTab;
import de.george.lrentnode.template.TemplateFile;

public abstract class AbstractTemplateTab extends ValidationPanelContainer<AbstractTemplateTab> implements ITypedTab, Keystroker {
	protected final EditorTemplateTab ctx;

	public AbstractTemplateTab(EditorTemplateTab ctx) {
		this.ctx = ctx;
	}

	@Override
	public Icon getTabIcon() {
		return null;
	}

	@Override
	public Component getTabContent() {
		return getLayeredPane();
	}

	/**
	 * Wird aufgerufen, wenn die TemplateHeaderView, die diesen EntityTab enthält, geschlossen wird.
	 * In dieser Methode kann der EntityTab Resourcen freigeben.
	 */
	public void cleanUp() {}

	/**
	 * Nach dem Umschalten auf eine Entity aufrufen, um deren Werte ins GUI zu laden.
	 */
	public abstract void loadValues(TemplateFile tple);

	/**
	 * Vor dem Umschalten auf eine andere Entity aufrufen, um Änderungen zu speichern.
	 */
	public abstract void saveValues(TemplateFile tple);

	public abstract boolean isActive(TemplateFile tple);

	@Override
	public void registerKeyStrokes(JComponent container) {

	}

	@Override
	public void unregisterKeyStrokes(JComponent container) {

	}
}

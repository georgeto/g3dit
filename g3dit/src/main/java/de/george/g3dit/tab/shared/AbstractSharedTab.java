package de.george.g3dit.tab.shared;

import java.awt.Container;

import javax.swing.JComponent;
import javax.swing.JScrollPane;

import org.netbeans.validation.api.ui.ValidationGroup;

import de.george.g3dit.EditorContext;
import de.george.g3dit.gui.components.Keystroker;
import de.george.lrentnode.archive.G3ClassContainer;

public abstract class AbstractSharedTab implements Keystroker {
	protected final EditorContext ctx;
	protected final Container container;

	public AbstractSharedTab(EditorContext ctx, Container container) {
		this.ctx = ctx;
		this.container = container;
	}

	/**
	 * Wird aufgerufen, wenn die TabbedPane, die diesen Tab enthält, geschlossen wird. In dieser
	 * Methode kann der Tab Resourcen freigeben.
	 */
	public void cleanUp() {}

	public abstract String getTabTitle();

	public abstract void initComponents(ValidationGroup validation, JScrollPane scrollPane);

	/**
	 * Nach dem Umschalten auf einen anderen {@code G3ClassContainer} aufrufen, um deren Werte ins
	 * GUI zu laden.
	 */
	public abstract void loadValues(G3ClassContainer clazzContainer);

	/**
	 * Vor dem Umschalten auf eine andere {@code G3ClassContainer} aufrufen, um Änderungen zu
	 * speichern.
	 */
	public abstract void saveValues(G3ClassContainer clazzContainer);

	public abstract boolean isActive(G3ClassContainer clazzContainer);

	@Override
	public void registerKeyStrokes(JComponent container) {}

	@Override
	public void unregisterKeyStrokes(JComponent container) {}
}

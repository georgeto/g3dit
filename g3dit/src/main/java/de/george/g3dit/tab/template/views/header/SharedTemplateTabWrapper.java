package de.george.g3dit.tab.template.views.header;

import java.awt.Container;

import javax.swing.JComponent;

import de.george.g3dit.EditorContext;
import de.george.g3dit.tab.shared.AbstractSharedTab;
import de.george.g3dit.tab.template.EditorTemplateTab;
import de.george.lrentnode.template.TemplateFile;

public class SharedTemplateTabWrapper extends AbstractTemplateTab {
	private AbstractSharedTab sharedTab;

	public SharedTemplateTabWrapper(Class<? extends AbstractSharedTab> sharedTab, EditorTemplateTab ctx) {
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
	public void loadValues(TemplateFile tple) {
		sharedTab.loadValues(tple.getReferenceHeader());
	}

	@Override
	public void saveValues(TemplateFile tple) {
		sharedTab.saveValues(tple.getReferenceHeader());
	}

	@Override
	public boolean isActive(TemplateFile tple) {
		return sharedTab.isActive(tple.getReferenceHeader());
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

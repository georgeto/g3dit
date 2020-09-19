package de.george.g3dit.tab.archive.views.entity;

import de.george.g3dit.gui.edit.PropertyPanel;
import de.george.g3dit.tab.archive.EditorArchiveTab;
import de.george.lrentnode.archive.eCEntity;
import net.miginfocom.swing.MigLayout;

public abstract class AbstractPropertyEntityTab extends AbstractEntityTab {
	private PropertyPanel propertyPanel;

	public AbstractPropertyEntityTab(EditorArchiveTab ctx) {
		super(ctx);
	}

	@Override
	public final void initComponents() {
		setLayout(new MigLayout("fillx"));
		propertyPanel = new PropertyPanel(ctx);
		initPropertyPanel(propertyPanel);
		add(propertyPanel.getContent(), "spanx");
	}

	protected abstract void initPropertyPanel(PropertyPanel propertyPanel);

	@Override
	public void loadValues(eCEntity entity) {
		propertyPanel.load(entity);
	}

	@Override
	public void saveValues(eCEntity entity) {
		propertyPanel.save(entity);
	}
}

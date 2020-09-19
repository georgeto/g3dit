package de.george.g3dit.tab.template.views.header;

import de.george.g3dit.gui.edit.PropertyPanel;
import de.george.g3dit.tab.template.EditorTemplateTab;
import de.george.lrentnode.template.TemplateFile;
import net.miginfocom.swing.MigLayout;

public abstract class AbstractPropertyTemplateTab extends AbstractTemplateTab {
	private PropertyPanel propertyPanel;

	public AbstractPropertyTemplateTab(EditorTemplateTab ctx) {
		super(ctx);
	}

	@Override
	public final void initComponents() {
		setLayout(new MigLayout("fillx"));
		propertyPanel = new PropertyPanel(ctx);
		initPropertyPanel(propertyPanel);
		add(propertyPanel.getContent(), "span");
	}

	protected abstract void initPropertyPanel(PropertyPanel propertyPanel);

	@Override
	public void loadValues(TemplateFile tple) {
		propertyPanel.load(tple.getReferenceHeader());
	}

	@Override
	public void saveValues(TemplateFile tple) {
		propertyPanel.save(tple.getReferenceHeader());
	}
}

package de.george.g3dit.gui.edit.handler;

import de.george.g3dit.gui.edit.adapter.PropertyAdapter;
import de.george.g3dit.tab.shared.QualityPanel;
import de.george.lrentnode.archive.G3ClassContainer;
import de.george.lrentnode.properties.gLong;

public class QualityPanelPropertyHandler implements PropertyHandler<gLong> {
	private final QualityPanel qualityPanel = new QualityPanel();

	@Override
	public QualityPanel getContent() {
		return qualityPanel;
	}

	@Override
	public void load(G3ClassContainer container, PropertyAdapter<gLong> property) {
		qualityPanel.setQuality(property.getValue(container).getLong());
	}

	@Override
	public void save(G3ClassContainer container, PropertyAdapter<gLong> property) {
		property.getValue(container).setLong(qualityPanel.getQuality());
	}
}

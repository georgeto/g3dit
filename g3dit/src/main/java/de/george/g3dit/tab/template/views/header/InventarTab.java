package de.george.g3dit.tab.template.views.header;

import de.george.g3dit.tab.shared.SharedInventarTab;
import de.george.g3dit.tab.template.EditorTemplateTab;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.template.TemplateFile;

public class InventarTab extends SharedTemplateTabWrapper {
	public InventarTab(EditorTemplateTab inEditor) {
		super(SharedInventarTab.class, inEditor);
	}

	@Override
	public boolean isActive(TemplateFile tple) {
		return super.isActive(tple) && !tple.getReferenceHeader().hasClass(CD.gCTreasureSet_PS.class);
	}
}

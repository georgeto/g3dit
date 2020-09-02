package de.george.g3dit.tab.archive.views.entity;

import de.george.g3dit.gui.edit.PropertyPanel;
import de.george.g3dit.gui.validation.EntityExistenceValidator;
import de.george.g3dit.tab.archive.EditorArchiveTab;
import de.george.g3utils.validation.GuidValidator;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.classes.desc.CD;

public class AIZoneTab extends AbstractPropertyEntityTab {
	public AIZoneTab(EditorArchiveTab ctx) {
		super(ctx);
	}

	@Override
	public void initPropertyPanel(PropertyPanel propertyPanel) {
		//@foff
		propertyPanel
			.add(CD.gCAIZone_PS.Type).name("ZoneType").horizontalStart()
			.add(CD.gCAIZone_PS.SecurityLevel).horizontal()
			.add(CD.gCAIZone_PS.Owner)
				.validate(validation(), GuidValidator.INSTANCE_ALLOW_EMPTY, new EntityExistenceValidator(validation(), ctx))
			.done();
		//@fon
	}

	@Override
	public String getTabTitle() {
		return "AIZone";
	}

	@Override
	public boolean isActive(eCEntity entity) {
		return entity.hasClass(CD.gCAIZone_PS.class);
	}
}

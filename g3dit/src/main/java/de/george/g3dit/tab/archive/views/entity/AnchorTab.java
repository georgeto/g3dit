package de.george.g3dit.tab.archive.views.entity;

import de.george.g3dit.gui.edit.PropertyPanel;
import de.george.g3dit.gui.validation.EntityExistenceValidator;
import de.george.g3dit.tab.archive.EditorArchiveTab;
import de.george.g3utils.validation.GuidValidator;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.classes.gCAnchor_PS;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.properties.bTObjArray_eCEntityProxy;

public class AnchorTab extends AbstractPropertyEntityTab {
	public AnchorTab(EditorArchiveTab ctx) {
		super(ctx);
	}

	@Override
	public void initPropertyPanel(PropertyPanel propertyPanel) {
		//@foff
		propertyPanel
			.add(CD.gCAnchor_PS.AnchorType)
			.add(CD.gCAnchor_PS.class, gCAnchor_PS::getInteractPoints, gCAnchor_PS::setInteractPoints, bTObjArray_eCEntityProxy::new, bTObjArray_eCEntityProxy.class, "bTObjArray<class eCEntityProxy>")
				.name("InteractPoints")
				.validate(validation(), GuidValidator.INSTANCE, new EntityExistenceValidator(validation(), ctx))
				.grow()
			.done();
		//@fon
	}

	@Override
	public String getTabTitle() {
		return "Anchor";
	}

	@Override
	public boolean isActive(eCEntity entity) {
		return entity.hasClass(CD.gCAnchor_PS.class);
	}
}

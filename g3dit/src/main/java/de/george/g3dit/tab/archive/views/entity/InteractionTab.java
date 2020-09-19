package de.george.g3dit.tab.archive.views.entity;

import de.george.g3dit.gui.edit.PropertyPanel;
import de.george.g3dit.gui.validation.EntityExistenceValidator;
import de.george.g3dit.gui.validation.TemplateExistenceValidator;
import de.george.g3dit.tab.archive.EditorArchiveTab;
import de.george.g3utils.validation.GuidValidator;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.classes.desc.CD;

public class InteractionTab extends AbstractPropertyEntityTab {
	public InteractionTab(EditorArchiveTab ctx) {
		super(ctx);
	}

	@Override
	public void initPropertyPanel(PropertyPanel propertyPanel) {
		//@foff
		propertyPanel
			.add(CD.gCInteraction_PS.FocusPriority).horizontalStart()
			.add(CD.gCInteraction_PS.UseType).horizontal()
			.add(CD.gCInteraction_PS.RoutineExclusive).horizontal()
			.add(CD.gCInteraction_PS.ScriptUseFunc).growx()
			.add(CD.gCInteraction_PS.AnchorPoint)
				.validate(validation(), GuidValidator.INSTANCE_ALLOW_EMPTY, new EntityExistenceValidator(validation(), ctx))
			.add(CD.gCInteraction_PS.Owner)
				.validate(validation(),GuidValidator.INSTANCE_ALLOW_EMPTY, new EntityExistenceValidator(validation(), ctx))
			.add(CD.gCInteraction_PS.Spell)
				.validate(validation(),GuidValidator.INSTANCE_ALLOW_EMPTY, new TemplateExistenceValidator(validation(), ctx))
			.done();
		//@fon
	}

	@Override
	public String getTabTitle() {
		return "Interaction";
	}

	@Override
	public boolean isActive(eCEntity entity) {
		return entity.hasClass(CD.gCInteraction_PS.class);
	}
}

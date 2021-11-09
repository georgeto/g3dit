package de.george.g3dit.tab.archive.views.entity;

import com.teamunify.i18n.I;

import de.george.g3dit.entitytree.filter.GuidEntityFilter.MatchMode;
import de.george.g3dit.gui.components.JSearchNamedGuidField;
import de.george.g3dit.gui.dialogs.EntitySearchDialog;
import de.george.g3dit.gui.edit.PropertyPanel;
import de.george.g3dit.gui.validation.EntityExistenceValidator;
import de.george.g3dit.tab.archive.EditorArchiveTab;
import de.george.g3dit.util.Icons;
import de.george.g3utils.validation.GuidValidator;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.classes.gCParty_PS;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.properties.bTObjArray_eCEntityProxy;

public class PartyTab extends AbstractPropertyEntityTab {
	public PartyTab(EditorArchiveTab ctx) {
		super(ctx);
	}

	@Override
	public void initPropertyPanel(PropertyPanel propertyPanel) {
		//@foff
		propertyPanel
			.add(CD.gCParty_PS.PartyLeaderEntity)
				.name("PartyLeader")
				.<JSearchNamedGuidField>customize(tfPartyLeader -> tfPartyLeader.addMenuItem(I.tr("List all PartyMembers of the PartyLeader"), Icons.getImageIcon(Icons.Misc.GLOBE),
					(c, text) -> EntitySearchDialog.openEntitySearchGuid(c, MatchMode.PartyLeader, text)))
			.add(CD.gCParty_PS.PartyMemberType)
			.add(CD.gCParty_PS.class, gCParty_PS::getMembers, gCParty_PS::setMembers, bTObjArray_eCEntityProxy::new, bTObjArray_eCEntityProxy.class, "bTObjArray<class eCEntityProxy>")
				.name("Members")
				.validate(validation(), GuidValidator.INSTANCE, new EntityExistenceValidator(validation(), ctx))
				.grow()
			.done();
		//@fon
	}

	@Override
	public String getTabTitle() {
		return "Party";
	}

	@Override
	public boolean isActive(eCEntity entity) {
		return entity.hasClass(CD.gCParty_PS.class);
	}
}

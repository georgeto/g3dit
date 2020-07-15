package de.george.g3dit.tab.archive.views.entity;

import java.util.Objects;
import java.util.stream.Collectors;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import de.george.g3dit.entitytree.filter.GuidEntityFilter.MatchMode;
import de.george.g3dit.gui.components.JEntityGuidField;
import de.george.g3dit.gui.components.JEnumComboBox;
import de.george.g3dit.gui.components.JSearchGuidField;
import de.george.g3dit.gui.components.TableModificationControl;
import de.george.g3dit.gui.dialogs.EntitySearchDialog;
import de.george.g3dit.tab.archive.EditorArchiveTab;
import de.george.g3dit.util.Icons;
import de.george.g3dit.util.PropertySync;
import de.george.g3utils.gui.SingleColumnTableModel;
import de.george.g3utils.structure.GuidUtil;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.classes.gCParty_PS;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.enums.G3Enums.gEPartyMemberType;
import net.miginfocom.swing.MigLayout;

public class PartyTab extends AbstractEntityTab {
	private JSearchGuidField tfPartyLeader;
	private JEnumComboBox<gEPartyMemberType> cbPartyMemberType;
	private JTable tableMembers;
	private SingleColumnTableModel<String> modelMembersModel;

	public PartyTab(EditorArchiveTab ctx) {
		super(ctx);

		setLayout(new MigLayout("fillx", "[]"));

		add(new JLabel("PartyLeader"), "wrap");

		tfPartyLeader = new JEntityGuidField(ctx);
		add(tfPartyLeader, "width 100:300:300, spanx 3, gapleft 7, wrap");

		tfPartyLeader.addMenuItem("Alle PartyMember des PartyLeaders auflisten", Icons.getImageIcon(Icons.Misc.GLOBE),
				(c, text) -> EntitySearchDialog.openEntitySearchGuid(c, MatchMode.PartyLeader, text));

		add(new JLabel("PartyMemberType"), "wrap");

		cbPartyMemberType = new JEnumComboBox<>(gEPartyMemberType.class);
		add(cbPartyMemberType, "width 50:100:100, gapleft 7, wrap");

		add(new JLabel("Members"), "wrap");

		modelMembersModel = new SingleColumnTableModel<>("Members", true);
		tableMembers = new JTable(modelMembersModel);
		tableMembers.setTableHeader(null);
		JScrollPane scroll = new JScrollPane(tableMembers);
		add(scroll, "width 150:300:300, sgx tableMembers, gapleft 7, wrap");

		add(new TableModificationControl<>(ctx, tableMembers, modelMembersModel, () -> ""), "gapleft 7, sgx tableMembers");
	}

	@Override
	public String getTabTitle() {
		return "Party";
	}

	@Override
	public boolean isActive(eCEntity entity) {
		return entity.hasClass(CD.gCParty_PS.class);
	}

	@Override
	public void initValidation() {}

	@Override
	public void loadValues(eCEntity entity) {
		gCParty_PS party = entity.getClass(CD.gCParty_PS.class);

		PropertySync sync = PropertySync.wrap(party);
		sync.readEnum(cbPartyMemberType, CD.gCParty_PS.PartyMemberType);
		sync.readGuid(tfPartyLeader, CD.gCParty_PS.PartyLeaderEntity);

		modelMembersModel.setEntries(party.members.getNativeEntries());
	}

	@Override
	public void saveValues(eCEntity entity) {
		gCParty_PS party = entity.getClass(CD.gCParty_PS.class);

		PropertySync sync = PropertySync.wrap(party);
		sync.writeEnum(cbPartyMemberType, CD.gCParty_PS.PartyMemberType);
		sync.writeGuid(tfPartyLeader, CD.gCParty_PS.PartyLeaderEntity);

		if (tableMembers.isEditing()) {
			tableMembers.getCellEditor().stopCellEditing();
		}

		party.members.setNativeEntries(
				modelMembersModel.getEntries().stream().map(GuidUtil::parseGuid).filter(Objects::nonNull).collect(Collectors.toList()));
	}
}

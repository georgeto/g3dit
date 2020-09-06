package de.george.g3dit.tab.archive.views.entity;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import org.netbeans.validation.api.builtin.stringvalidation.StringValidators;

import de.george.g3dit.config.StringListConfigFile;
import de.george.g3dit.entitytree.filter.GuidEntityFilter.MatchMode;
import de.george.g3dit.gui.components.JEntityGuidField;
import de.george.g3dit.gui.components.JEnumComboBox;
import de.george.g3dit.gui.components.JSearchGuidField;
import de.george.g3dit.gui.dialogs.EntitySearchDialog;
import de.george.g3dit.gui.validation.EntityExistenceValidator;
import de.george.g3dit.tab.archive.EditorArchiveTab;
import de.george.g3dit.util.Icons;
import de.george.g3utils.gui.SwingUtils;
import de.george.g3utils.structure.GuidUtil;
import de.george.g3utils.validation.EmtpyWarnValidator;
import de.george.g3utils.validation.GuidValidator;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.classes.G3Class;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.enums.G3Enums.gEClass;
import de.george.lrentnode.enums.G3Enums.gEGender;
import de.george.lrentnode.enums.G3Enums.gEGuardStatus;
import de.george.lrentnode.enums.G3Enums.gENPCType;
import de.george.lrentnode.enums.G3Enums.gEPoliticalAlignment;
import de.george.lrentnode.enums.G3Enums.gESlot;
import de.george.lrentnode.enums.G3Enums.gESpecies;
import de.george.lrentnode.util.NPCUtil;
import net.miginfocom.swing.MigLayout;

public class NPCTab extends AbstractEntityTab {
	private final StringListConfigFile voices;

	private JTextField tfLevel, tfLevelMax;
	private JComboBox<String> cbVoice;
	private JEnumComboBox<gEGender> cbGender;
	private JEnumComboBox<gESpecies> cbSpecies;
	private JEnumComboBox<gEPoliticalAlignment> cbPol;
	private JEnumComboBox<gEClass> cbClass;
	private JEnumComboBox<gENPCType> cbType;
	private JCheckBox cbTrade;

	private JTabbedPane slotTabs;

	private JEnumComboBox<gEGuardStatus> cbGuardStatus;

	private JSearchGuidField tfGuardPoint, tfEnclave;

	public NPCTab(EditorArchiveTab ctx) {
		super(ctx);
		voices = ctx.getFileManager().getConfigFile("Voices.list", StringListConfigFile.class);
	}

	@Override
	public void initComponents() {
		setLayout(new MigLayout("fillx", "[]20px[]20px[]push"));

		JLabel lblCommon = SwingUtils.createBoldLabel("Eigenschaften");
		add(lblCommon, "wrap");

		JLabel lblVoice = new JLabel("Stimme");
		cbVoice = new JComboBox<>(voices.getContent().toArray(new String[0]));
		cbVoice.setEditable(true);
		cbVoice.setName("Stimme");
		addValidators(cbVoice, EmtpyWarnValidator.INSTANCE);

		JLabel lblLevel = new JLabel("Level");
		tfLevel = SwingUtils.createUndoTF();
		tfLevel.setName("Level");
		addValidators(tfLevel, StringValidators.REQUIRE_NON_NEGATIVE_NUMBER, StringValidators.REQUIRE_VALID_INTEGER);

		JLabel lblLevelMax = new JLabel("Maximales Level");
		tfLevelMax = SwingUtils.createUndoTF();
		tfLevelMax.setName("Maximales Level");
		addValidators(tfLevelMax, StringValidators.REQUIRE_NON_NEGATIVE_NUMBER, StringValidators.REQUIRE_VALID_INTEGER);

		add(lblVoice, "gapleft 7");
		add(lblLevel, "");
		add(lblLevelMax, "wrap");
		add(cbVoice, "width 100:150:200, gapleft 7, ");
		add(tfLevel, "width 50:100:100");
		add(tfLevelMax, "width 50:100:100, wrap");

		JLabel lblGender = new JLabel("Geschlecht");
		cbGender = new JEnumComboBox<>(gEGender.class);

		JLabel lblSpecies = new JLabel("Rasse");
		cbSpecies = new JEnumComboBox<>(gESpecies.class);

		JLabel lblPol = new JLabel("Politische Ausrichtung");
		cbPol = new JEnumComboBox<>(gEPoliticalAlignment.class);

		add(lblGender, "gapleft 7, ");
		add(lblSpecies, "");
		add(lblPol, "wrap");
		add(cbGender, "width 50:100:100, gapleft 7, ");
		add(cbSpecies, "width 50:100:100");
		add(cbPol, "width 50:100:100, wrap");

		JLabel lblClass = new JLabel("Klasse");
		cbClass = new JEnumComboBox<>(gEClass.class);

		JLabel lblType = new JLabel("Typ");
		cbType = new JEnumComboBox<>(gENPCType.class);

		cbTrade = new JCheckBox("Händler");

		add(lblClass, "gapleft 7, ");
		add(lblType, "wrap");
		add(cbClass, "width 50:100:100, gapleft 7, ");
		add(cbType, "width 50:100:100");
		add(cbTrade, "width 50:100:100, wrap");

		JLabel lblGuardStatus = new JLabel("GuardStatus");
		cbGuardStatus = new JEnumComboBox<>(gEGuardStatus.class);

		JLabel lblGuardPoint = new JLabel("GuardPoint");
		tfGuardPoint = new JEntityGuidField(ctx);
		tfGuardPoint.initValidation(validation(), "GuardPoint", GuidValidator.INSTANCE_ALLOW_EMPTY,
				new EntityExistenceValidator(validation(), ctx));

		JLabel lblGuard = SwingUtils.createBoldLabel("Wache");
		add(lblGuard, "gaptop 5, wrap");

		add(lblGuardPoint, "spanx 2, gapleft 7");
		add(lblGuardStatus, "wrap");
		add(tfGuardPoint, "width 100:300:300, spanx 2, gapleft 7");
		add(cbGuardStatus, "width 50:100:100, wrap");

		JLabel lblOther = SwingUtils.createBoldLabel("Sonstiges");
		add(lblOther, "gaptop 5, wrap");

		JLabel lblEnclave = new JLabel("Enclave");
		add(lblEnclave, "gapleft 7, wrap");

		tfEnclave = new JEntityGuidField(ctx);
		tfEnclave.initValidation(validation(), "Enclave", GuidValidator.INSTANCE_ALLOW_EMPTY,
				new EntityExistenceValidator(validation(), ctx));
		add(tfEnclave, "width 100:300:300, spanx 3, gapleft 7, wrap");

		tfEnclave.addMenuItem("Alle Mitglieder der Enclave auflisten", Icons.getImageIcon(Icons.Misc.GLOBE),
				(c, text) -> EntitySearchDialog.openEntitySearchGuid(c, MatchMode.Enclave, text));

		JLabel lblParts = SwingUtils.createBoldLabel("Körperteile");
		add(lblParts, "gaptop 5, wrap");

		slotTabs = new JTabbedPane();
		slotTabs.addTab("Kopf", new NPCSlotPanel(gESlot.gESlot_Head, ctx));
		slotTabs.addTab("Körper", new NPCSlotPanel(gESlot.gESlot_Body, ctx));
		slotTabs.addTab("Haare", new NPCSlotPanel(gESlot.gESlot_Hair, ctx));
		slotTabs.addTab("Bart", new NPCSlotPanel(gESlot.gESlot_Beard, ctx));
		slotTabs.addTab("Helm", new NPCSlotPanel(gESlot.gESlot_Helmet, ctx));
		for (int i = 0; i < slotTabs.getTabCount(); i++) {
			((NPCSlotPanel) slotTabs.getComponentAt(i)).initValidation(validation());
		}
		add(slotTabs, "width 100:300:400, grow, spanx 3, gapleft 7, wrap");
	}

	@Override
	public String getTabTitle() {
		return "NPC";
	}

	@Override
	public boolean isActive(eCEntity entity) {
		return NPCUtil.isNPC(entity);
	}

	@Override
	public void loadValues(eCEntity entity) {
		G3Class npc = entity.getClass(CD.gCNPC_PS.class);
		cbVoice.setSelectedItem(npc.property(CD.gCNPC_PS.Voice).getString());
		tfLevel.setText(String.valueOf(npc.property(CD.gCNPC_PS.Level).getLong()));
		tfLevelMax.setText(String.valueOf(npc.property(CD.gCNPC_PS.LevelMax).getLong()));
		cbGender.setSelectedValue(npc.property(CD.gCNPC_PS.Gender).getEnumValue());
		cbSpecies.setSelectedValue(npc.property(CD.gCNPC_PS.Species).getEnumValue());
		cbPol.setSelectedValue(npc.property(CD.gCNPC_PS.PoliticalAlignment).getEnumValue());
		cbClass.setSelectedValue(npc.property(CD.gCNPC_PS.Class).getEnumValue());
		cbType.setSelectedValue(npc.property(CD.gCNPC_PS.Type).getEnumValue());
		cbGuardStatus.setSelectedValue(npc.property(CD.gCNPC_PS.GuardStatus).getEnumValue());
		tfGuardPoint.setText(npc.property(CD.gCNPC_PS.GuardPoint).getGuid());
		tfEnclave.setText(npc.property(CD.gCNPC_PS.Enclave).getGuid());

		if (entity.hasClass(CD.gCDialog_PS.class)) {
			cbTrade.setSelected(entity.getClass(CD.gCDialog_PS.class).property(CD.gCDialog_PS.TradeEnabled).isBool());
			cbTrade.setVisible(true);
		} else {
			cbTrade.setVisible(false);
		}

		for (int i = 0; i < slotTabs.getTabCount(); i++) {
			((NPCSlotPanel) slotTabs.getComponentAt(i)).loadSlot(entity);
		}
	}

	@Override
	public void saveValues(eCEntity entity) {
		G3Class npc = entity.getClass(CD.gCNPC_PS.class);
		npc.property(CD.gCNPC_PS.Voice).setString(cbVoice.getSelectedItem().toString());
		npc.property(CD.gCNPC_PS.Level).setLong(Integer.valueOf(tfLevel.getText()));
		npc.property(CD.gCNPC_PS.LevelMax).setLong(Integer.valueOf(tfLevelMax.getText()));
		npc.property(CD.gCNPC_PS.Gender).setEnumValue(cbGender.getSelectedValue());
		npc.property(CD.gCNPC_PS.Species).setEnumValue(cbSpecies.getSelectedValue());
		npc.property(CD.gCNPC_PS.PoliticalAlignment).setEnumValue(cbPol.getSelectedValue());
		npc.property(CD.gCNPC_PS.Class).setEnumValue(cbClass.getSelectedValue());
		npc.property(CD.gCNPC_PS.Type).setEnumValue(cbType.getSelectedValue());
		npc.property(CD.gCNPC_PS.GuardStatus).setEnumValue(cbGuardStatus.getSelectedValue());
		npc.property(CD.gCNPC_PS.GuardPoint).setGuid(GuidUtil.parseGuid(tfGuardPoint.getText()));
		npc.property(CD.gCNPC_PS.Enclave).setGuid(GuidUtil.parseGuid(tfEnclave.getText()));

		if (entity.hasClass(CD.gCDialog_PS.class)) {
			entity.getClass(CD.gCDialog_PS.class).property(CD.gCDialog_PS.TradeEnabled).setBool(cbTrade.isSelected());
		}
	}

}

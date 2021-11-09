package de.george.g3dit.tab.template.views.header;

import java.awt.event.ItemEvent;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.netbeans.validation.api.builtin.stringvalidation.StringValidators;

import com.teamunify.i18n.I;

import de.george.g3dit.gui.components.HidingGroup;
import de.george.g3dit.gui.components.JEnumComboBox;
import de.george.g3dit.gui.components.JTemplateGuidField;
import de.george.g3dit.gui.validation.TemplateExistenceValidator;
import de.george.g3dit.tab.shared.QualityPanel;
import de.george.g3dit.tab.template.EditorTemplateTab;
import de.george.g3dit.util.PropertySync;
import de.george.g3utils.gui.SwingUtils;
import de.george.g3utils.validation.EmtpyWarnValidator;
import de.george.g3utils.validation.GuidValidator;
import de.george.lrentnode.classes.G3Class;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.enums.G3Enums;
import de.george.lrentnode.enums.G3Enums.gEDamageType;
import de.george.lrentnode.enums.G3Enums.gEItemCategory;
import de.george.lrentnode.enums.G3Enums.gEPoliticalAlignment;
import de.george.lrentnode.enums.G3Enums.gEUseType;
import de.george.lrentnode.properties.gBool;
import de.george.lrentnode.template.TemplateEntity;
import de.george.lrentnode.template.TemplateFile;
import de.george.lrentnode.util.EntityUtil;
import de.george.lrentnode.util.ItemUtil;
import net.miginfocom.swing.MigLayout;

public class ItemTab extends AbstractTemplateTab {
	private JTextField tfGoldValue, tfTexture, tfScriptUseFunc, tfDamageAmount, tfRange;
	private JEnumComboBox<gEItemCategory> cbCategory;
	private JEnumComboBox<gEUseType> cbUseType;
	private JEnumComboBox<gEDamageType> cbDamageType;
	private JTemplateGuidField gfArmorSet, gfSpell, gfSkill;
	private HidingGroup groupDamage;
	private JCheckBox cbMissionItem, cbPermanent, cbRobe, cbFullBody;

	private HidingGroup hgQuality;
	private QualityPanel pnQuality;

	private HidingGroup hgBodyPoliticalAlignment;
	private JEnumComboBox<gEPoliticalAlignment> cbBodyPoliticalAlignment;

	private boolean guardUseType = false;

	public ItemTab(EditorTemplateTab ctx) {
		super(ctx);
	}

	@Override
	public void initComponents() {
		setLayout(new MigLayout("hidemode 3", "[]20[]20[]20[]"));

		add(SwingUtils.createBoldLabel(I.tr("Properties")), "spanx, wrap");
		add(new JLabel("GoldValue"), "gapleft 7, spanx 2");
		add(new JLabel("Category"), "spanx 2, wrap");
		tfGoldValue = SwingUtils.createUndoTF();
		tfGoldValue.setName("GoldValue");
		addValidators(tfGoldValue, StringValidators.REQUIRE_NON_NEGATIVE_NUMBER, StringValidators.REQUIRE_VALID_INTEGER);
		add(tfGoldValue, "gapleft 7, spanx 2, width 125:150:175");
		cbCategory = new JEnumComboBox<>(gEItemCategory.class);
		add(cbCategory, "width 125:150:175, spanx 2, wrap");

		add(new JLabel("Icon"), "gapleft 7, spanx 2, wrap");
		tfTexture = SwingUtils.createUndoTF();
		tfTexture.setName("Icon");
		addValidators(tfTexture, EmtpyWarnValidator.INSTANCE);
		add(tfTexture, "gapleft 7, growx 100, spanx 4, wrap");

		JLabel lblDamage = SwingUtils.createBoldLabel("Schaden");
		add(lblDamage, "gaptop 10, spanx, wrap");
		cbDamageType = new JEnumComboBox<>(gEDamageType.class);
		JLabel lblDamageType = new JLabel("DamageType");
		add(lblDamageType, "gapleft 7, spanx 2");
		JLabel lblDamageAmount = new JLabel("DamageAmount");
		add(lblDamageAmount, "spanx 2, wrap");
		add(cbDamageType, "gapleft 7, spanx 2, width 125:150:175");
		tfDamageAmount = SwingUtils.createUndoTF();
		tfDamageAmount.setName("DamageAmount");
		addValidators(tfDamageAmount, StringValidators.REQUIRE_NON_NEGATIVE_NUMBER, StringValidators.REQUIRE_VALID_INTEGER);
		add(tfDamageAmount, "spanx 2, width 125:150:175, wrap");
		JLabel lblRange = new JLabel("Reichweite");
		add(lblRange, "gapleft 7, spanx 2, wrap");
		tfRange = new JTextField();
		tfRange.setEditable(false);
		add(tfRange, "gapleft 7, spanx 2, width 125:150:175, wrap");
		groupDamage = HidingGroup.create(lblDamage, lblDamageType, lblDamageAmount, lblRange, cbDamageType, tfDamageAmount, tfRange);

		add(SwingUtils.createBoldLabel("Sonstiges"), "gaptop 10, spanx, wrap");
		add(new JLabel("UseType"), "gapleft 7, spanx 2");
		add(new JLabel("ScriptUseFunc"), "spanx 2, wrap");
		cbUseType = new JEnumComboBox<>(gEUseType.class);
		add(cbUseType, "gapleft 7, spanx 2, width 125:150:175");
		tfScriptUseFunc = SwingUtils.createUndoTF();
		add(tfScriptUseFunc, "spanx 2, width 125:150:175, wrap");

		add(new JLabel("ArmorSet"), "gapleft 7, spanx, wrap");
		gfArmorSet = new JTemplateGuidField(ctx);
		gfArmorSet.initValidation(validation(), "ArmorSet", GuidValidator.INSTANCE_ALLOW_EMPTY,
				new TemplateExistenceValidator(validation(), ctx));
		add(gfArmorSet, "gapleft 7, growx 100, spanx 4, wrap");

		add(new JLabel("Spell"), "gapleft 7, spanx 2, wrap");
		gfSpell = new JTemplateGuidField(ctx);
		gfSpell.initValidation(validation(), "Spell", GuidValidator.INSTANCE_ALLOW_EMPTY,
				new TemplateExistenceValidator(validation(), ctx));
		add(gfSpell, "gapleft 7, growx 100, spanx 4, wrap");

		add(new JLabel("Skill"), "gapleft 7, spanx 2, wrap");
		gfSkill = new JTemplateGuidField(ctx);
		gfSkill.initValidation(validation(), "Skill", GuidValidator.INSTANCE_ALLOW_EMPTY,
				new TemplateExistenceValidator(validation(), ctx));
		add(gfSkill, "gapleft 7, growx 100, spanx 4, wrap");

		cbMissionItem = new JCheckBox("MissionItem");
		add(cbMissionItem, "gapleft 7, spanx 2");
		cbPermanent = new JCheckBox("Permanent");
		add(cbPermanent, "spanx 2, wrap");
		cbRobe = new JCheckBox("Robe");
		add(cbRobe, "gapleft 7, spanx 2");
		cbFullBody = new JCheckBox("FullBody");
		add(cbFullBody, "spanx 2, wrap");

		JLabel lblQuality = SwingUtils.createBoldLabel("Quality");
		add(lblQuality, "gaptop 10, spanx, wrap");
		pnQuality = new QualityPanel(CD.gCItem_PS.Quality);
		add(pnQuality, "gapleft 7, width 250:300:350, spanx 4, wrap");
		hgQuality = new HidingGroup();
		hgQuality.add(lblQuality);
		hgQuality.add(pnQuality);

		JLabel lblBodyPoliticalAlignment = new JLabel("PoliticalAlignment");
		add(lblBodyPoliticalAlignment, "gapleft 7, spanx, wrap");
		cbBodyPoliticalAlignment = new JEnumComboBox<>(gEPoliticalAlignment.class);
		add(cbBodyPoliticalAlignment, "gapleft 7, width 250:300:350, spanx 4, wrap");
		hgBodyPoliticalAlignment = new HidingGroup();
		hgBodyPoliticalAlignment.add(lblBodyPoliticalAlignment);
		hgBodyPoliticalAlignment.add(cbBodyPoliticalAlignment);

		cbUseType.addItemListener(e -> {
			if (!guardUseType && e.getStateChange() == ItemEvent.SELECTED) {
				loadQuality(G3Enums.asInt(gEUseType.class, e.getItem().toString()), 0);
			}
		});
	}

	@Override
	public String getTabTitle() {
		return "Item";
	}

	@Override
	public boolean isActive(TemplateFile tple) {
		return tple.getReferenceHeader().hasClass(CD.gCItem_PS.class) && tple.getReferenceHeader().hasClass(CD.gCInteraction_PS.class);
	}

	@Override
	public void loadValues(TemplateFile tple) {
		TemplateEntity header = tple.getReferenceHeader();

		loadQuality(EntityUtil.getUseType(header), header.getProperty(CD.gCItem_PS.Quality).getLong());

		G3Class item = header.getClass(CD.gCItem_PS.class);
		PropertySync syncItem = PropertySync.wrap(item);
		syncItem.readLong(tfGoldValue, CD.gCItem_PS.GoldValue);
		syncItem.readEnum(cbCategory, CD.gCItem_PS.Category);
		syncItem.readString(tfTexture, CD.gCItem_PS.Texture);
		syncItem.readGuid(gfArmorSet, CD.gCItem_PS.ArmorSet);
		syncItem.readGuid(gfSpell, CD.gCItem_PS.Spell);
		syncItem.readGuid(gfSkill, CD.gCItem_PS.Skill);
		syncItem.readBool(cbMissionItem, CD.gCItem_PS.MissionItem);
		syncItem.readBool(cbPermanent, CD.gCItem_PS.Permanent);
		syncItem.readBool(cbRobe, CD.gCItem_PS.Robe);
		if (item.hasProperty(CD.gCItem_PS.FullBody)) {
			syncItem.readBool(cbFullBody, CD.gCItem_PS.FullBody);
		} else {
			cbFullBody.setSelected(false);
		}

		PropertySync syncInteraction = PropertySync.wrap(header.getClass(CD.gCInteraction_PS.class));
		guardUseType = true;
		syncInteraction.readEnum(cbUseType, CD.gCInteraction_PS.UseType);
		guardUseType = false;
		syncInteraction.readString(tfScriptUseFunc, CD.gCInteraction_PS.ScriptUseFunc);

		if (header.hasClass(CD.gCDamage_PS.class)) {
			PropertySync syncDamage = PropertySync.wrap(header.getClass(CD.gCDamage_PS.class));
			syncDamage.readEnum(cbDamageType, CD.gCDamage_PS.DamageType);
			syncDamage.readLong(tfDamageAmount, CD.gCDamage_PS.DamageAmount);
			tfRange.setText(ItemUtil.getWeaponRange(header).map(Object::toString).orElse("-"));
			groupDamage.setVisible(true, validation());
		} else {
			groupDamage.setVisible(false, validation());
		}

	}

	private void loadQuality(int useType, int quality) {
		if (useType == gEUseType.gEUseType_Body) {
			cbBodyPoliticalAlignment.setSelectedValue(quality);
			pnQuality.setQuality(0);
			hgQuality.setVisible(false);
			hgBodyPoliticalAlignment.setVisible(true);
		} else {
			pnQuality.setQuality(quality);
			hgQuality.setVisible(true);
			cbBodyPoliticalAlignment.setSelectedValue(gEPoliticalAlignment.gEPoliticalAlignment_None);
			hgBodyPoliticalAlignment.setVisible(false);
		}
	}

	@Override
	public void saveValues(TemplateFile tple) {
		TemplateEntity header = tple.getReferenceHeader();

		G3Class item = header.getClass(CD.gCItem_PS.class);
		PropertySync syncItem = PropertySync.wrap(item);
		syncItem.writeLong(tfGoldValue, CD.gCItem_PS.GoldValue);
		syncItem.writeEnum(cbCategory, CD.gCItem_PS.Category);
		syncItem.writeString(tfTexture, CD.gCItem_PS.Texture);
		syncItem.writeGuid(gfArmorSet, CD.gCItem_PS.ArmorSet);
		syncItem.writeGuid(gfSpell, CD.gCItem_PS.Spell);
		syncItem.writeGuid(gfSkill, CD.gCItem_PS.Skill);
		syncItem.writeBool(cbMissionItem, CD.gCItem_PS.MissionItem);
		syncItem.writeBool(cbPermanent, CD.gCItem_PS.Permanent);
		syncItem.writeBool(cbRobe, CD.gCItem_PS.Robe);

		if (item.hasProperty(CD.gCItem_PS.FullBody)) {
			syncItem.writeBool(cbFullBody, CD.gCItem_PS.FullBody);
		} else if (cbFullBody.isSelected()) {
			item.addProperty(CD.gCItem_PS.FullBody, new gBool(cbFullBody.isSelected()), CD.gCItem_PS.Robe);
			syncItem.writeBool(cbFullBody, CD.gCItem_PS.FullBody);
		}

		PropertySync syncInteraction = PropertySync.wrap(header.getClass(CD.gCInteraction_PS.class));
		syncInteraction.writeEnum(cbUseType, CD.gCInteraction_PS.UseType);
		syncInteraction.writeString(tfScriptUseFunc, CD.gCInteraction_PS.ScriptUseFunc);

		int useType = EntityUtil.getUseType(header);
		if (useType == gEUseType.gEUseType_Body) {
			item.property(CD.gCItem_PS.Quality).setLong(cbBodyPoliticalAlignment.getSelectedValue());
		} else {
			pnQuality.writeQuality(item);
		}

		if (header.hasClass(CD.gCDamage_PS.class)) {
			PropertySync syncDamage = PropertySync.wrap(header.getClass(CD.gCDamage_PS.class));
			syncDamage.writeEnum(cbDamageType, CD.gCDamage_PS.DamageType);
			syncDamage.writeLong(tfDamageAmount, CD.gCDamage_PS.DamageAmount);
		}
	}
}

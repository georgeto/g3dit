package de.george.g3dit.tab.template.views.header;

import java.awt.event.ItemEvent;
import java.util.function.Predicate;

import org.netbeans.validation.api.builtin.stringvalidation.StringValidators;

import com.teamunify.i18n.I;

import de.george.g3dit.gui.components.JEnumComboBox;
import de.george.g3dit.gui.edit.PropertyPanel;
import de.george.g3dit.gui.edit.adapter.LambdaPropertyAdapter;
import de.george.g3dit.gui.edit.handler.QualityPanelPropertyHandler;
import de.george.g3dit.gui.validation.TemplateExistenceValidator;
import de.george.g3dit.tab.template.EditorTemplateTab;
import de.george.g3utils.util.Holder;
import de.george.g3utils.validation.EmtpyWarnValidator;
import de.george.g3utils.validation.GuidValidator;
import de.george.lrentnode.archive.G3ClassContainer;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.classes.desc.CD.gCDamage_PS;
import de.george.lrentnode.classes.desc.CD.gCInteraction_PS;
import de.george.lrentnode.classes.desc.CD.gCItem_PS;
import de.george.lrentnode.enums.G3Enums;
import de.george.lrentnode.enums.G3Enums.gEPoliticalAlignment;
import de.george.lrentnode.enums.G3Enums.gEUseType;
import de.george.lrentnode.properties.bCString;
import de.george.lrentnode.properties.bTPropertyContainer;
import de.george.lrentnode.template.TemplateFile;
import de.george.lrentnode.util.ItemUtil;

public class ItemTab extends AbstractPropertyTemplateTab {
	private G3ClassContainer currentEntity;
	private boolean loadingUseType = false;

	public ItemTab(EditorTemplateTab ctx) {
		super(ctx);
	}

	@Override
	protected void initPropertyPanel(PropertyPanel propertyPanel) {
		Predicate<G3ClassContainer> hasNoDamage = entity -> !entity.hasClass(CD.gCDamage_PS.class);

		Holder<JEnumComboBox<gEUseType>> cbUseType = new Holder<>();
		QualityPanelPropertyHandler qualityHandler = new QualityPanelPropertyHandler();
		Holder<JEnumComboBox<gEPoliticalAlignment>> cbBodyPoliticalAlignment = new Holder<>();
		Predicate<G3ClassContainer> hasBodyPoliticalAlignment = entity -> cbUseType.held() != null
				&& cbUseType.held().getSelectedValue() == gEUseType.gEUseType_Body;

		//@foff
		propertyPanel
			.addHeadline(I.tr("Properties"))
			.add(gCItem_PS.GoldValue).horizontalStart()
				.validate(validation(), StringValidators.REQUIRE_NON_NEGATIVE_NUMBER, StringValidators.REQUIRE_VALID_INTEGER)
			.add(gCItem_PS.Category).horizontal()
			.add(gCItem_PS.Texture).name("Icon")
				.validate(validation(), EmtpyWarnValidator.INSTANCE)

			.addHeadline(I.tr("Damage"), hasNoDamage)
			.add(gCDamage_PS.DamageType).horizontalStart()
				.hideIf(hasNoDamage)
			.add(gCDamage_PS.DamageAmount).horizontal()
				.validate(validation(), StringValidators.REQUIRE_NON_NEGATIVE_NUMBER, StringValidators.REQUIRE_VALID_INTEGER)
				.hideIf(hasNoDamage)
			.add(new LambdaPropertyAdapter<eCEntity, bCString>(
					entity -> new bCString(ItemUtil.getWeaponRange(entity).map(Object::toString).orElse("-")),
					null, null, bCString.class, "bCString")).horizontalStart()
				.name(I.tr("Range")).editable(false).hideIf(hasNoDamage)

			.addHeadline(I.tr("Misc"))
			.add(gCInteraction_PS.UseType).customize(cbUseType::hold).horizontalStart()
			.add(gCInteraction_PS.ScriptUseFunc).horizontal()
			.add(gCItem_PS.ArmorSet)
				.validate(validation(), GuidValidator.INSTANCE_ALLOW_EMPTY, new TemplateExistenceValidator(validation(), ctx))
			.add(gCItem_PS.Spell)
				.validate(validation(), GuidValidator.INSTANCE_ALLOW_EMPTY, new TemplateExistenceValidator(validation(), ctx))
			.add(gCItem_PS.Skill)
				.validate(validation(), GuidValidator.INSTANCE_ALLOW_EMPTY, new TemplateExistenceValidator(validation(), ctx))
			.add(gCItem_PS.MissionItem).horizontalStart()
			.add(gCItem_PS.Permanent).horizontal()
			.add(gCItem_PS.Robe).horizontalStart()
			.add(gCItem_PS.FullBody).horizontal()

			.addHeadline(I.tr("Quality"), hasBodyPoliticalAlignment)
			.add(gCItem_PS.Quality).handler(qualityHandler)
				.horizontalStartSpan().hideIf(hasBodyPoliticalAlignment)

			.addHeadline(I.tr("Armor"), hasBodyPoliticalAlignment.negate())
			.add(new LambdaPropertyAdapter<eCEntity, bTPropertyContainer>(
					entity -> new bTPropertyContainer<>(entity.getProperty(CD.gCItem_PS.Quality).getLong()),
					(entity, value) -> entity.getProperty(CD.gCItem_PS.Quality).setLong(value.getEnumValue()),
					null, bTPropertyContainer.class, G3Enums.classToEnumName(gEPoliticalAlignment.class)))
				.name("PoliticalAlignment").customize(cbBodyPoliticalAlignment::hold)
				.hideIf(hasBodyPoliticalAlignment.negate()).horizontalStart()

			.done();
		//@fon

		cbUseType.held().addItemListener(e -> {
			if (loadingUseType)
				return;

			int useType = G3Enums.asInt(gEUseType.class, e.getItem().toString());
			if (useType != gEUseType.gEUseType_Body)
				return;

			switch (e.getStateChange()) {
				case ItemEvent.SELECTED ->
					cbBodyPoliticalAlignment.held().setSelectedValue(gEPoliticalAlignment.gEPoliticalAlignment_None);
				case ItemEvent.DESELECTED -> qualityHandler.getContent().setQuality(0);
			}

			propertyPanel.updateComponentVisibility(currentEntity);
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
		currentEntity = tple.getReferenceHeader();
		loadingUseType = true;
		super.loadValues(tple);
		loadingUseType = false;
	}
}

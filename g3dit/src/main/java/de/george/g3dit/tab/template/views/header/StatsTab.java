package de.george.g3dit.tab.template.views.header;

import org.netbeans.validation.api.builtin.stringvalidation.StringValidators;

import com.google.common.collect.ImmutableList;

import de.george.g3dit.gui.components.JTemplateGuidField;
import de.george.g3dit.gui.edit.PropertyPanel;
import de.george.g3dit.gui.validation.TemplateExistenceValidator;
import de.george.g3dit.tab.template.EditorTemplateTab;
import de.george.g3utils.util.Misc;
import de.george.g3utils.validation.GuidValidator;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.classes.desc.PropertyDescriptor;
import de.george.lrentnode.enums.G3Enums.EAttribModOperation;
import de.george.lrentnode.enums.G3Enums.EAttribReqOperation;
import de.george.lrentnode.properties.bCString;
import de.george.lrentnode.properties.bTPropertyContainer;
import de.george.lrentnode.properties.eCEntityProxy;
import de.george.lrentnode.properties.gInt;
import de.george.lrentnode.template.TemplateFile;

public class StatsTab extends AbstractPropertyTemplateTab {
	private static final String[] ATTRIBUTES = {"STR", "DEX", "INT", "SMT", "THF", "ALC", "HP", "SP", "MP"};
	private static final String[] PROTECTIONS = {"PROT_BLADE", "PROT_IMPACT", "PROT_MISSILE", "PROT_FIRE", "PROT_ICE", "PROT_LIGHTNING"};

	private static final ImmutableList<PropertyDescriptor<bCString>> REQ_ATTRIB_TAGS = ImmutableList.of(CD.gCItem_PS.ReqAttrib1Tag,
			CD.gCItem_PS.ReqAttrib2Tag, CD.gCItem_PS.ReqAttrib3Tag, CD.gCItem_PS.ReqAttrib4Tag);
	private static final ImmutableList<PropertyDescriptor<bTPropertyContainer<EAttribReqOperation>>> REQ_ATTRIB_OPS = ImmutableList
			.of(CD.gCItem_PS.ReqAttrib1Op, CD.gCItem_PS.ReqAttrib2Op, CD.gCItem_PS.ReqAttrib3Op, CD.gCItem_PS.ReqAttrib4Op);
	private static final ImmutableList<PropertyDescriptor<gInt>> REQ_ATTRIB_VALUES = ImmutableList.of(CD.gCItem_PS.ReqAttrib1Value,
			CD.gCItem_PS.ReqAttrib2Value, CD.gCItem_PS.ReqAttrib3Value, CD.gCItem_PS.ReqAttrib4Value);

	private static final ImmutableList<PropertyDescriptor<bCString>> MOD_ATTRIB_TAGS = ImmutableList.of(CD.gCItem_PS.ModAttrib1Tag,
			CD.gCItem_PS.ModAttrib2Tag, CD.gCItem_PS.ModAttrib3Tag, CD.gCItem_PS.ModAttrib4Tag, CD.gCItem_PS.ModAttrib5Tag,
			CD.gCItem_PS.ModAttrib6Tag);
	private static final ImmutableList<PropertyDescriptor<bTPropertyContainer<EAttribModOperation>>> MOD_ATTRIB_OPS = ImmutableList.of(
			CD.gCItem_PS.ModAttrib1Op, CD.gCItem_PS.ModAttrib2Op, CD.gCItem_PS.ModAttrib3Op, CD.gCItem_PS.ModAttrib4Op,
			CD.gCItem_PS.ModAttrib5Op, CD.gCItem_PS.ModAttrib6Op);
	private static final ImmutableList<PropertyDescriptor<gInt>> MOD_ATTRIB_VALUES = ImmutableList.of(CD.gCItem_PS.ModAttrib1Value,
			CD.gCItem_PS.ModAttrib2Value, CD.gCItem_PS.ModAttrib3Value, CD.gCItem_PS.ModAttrib4Value, CD.gCItem_PS.ModAttrib5Value,
			CD.gCItem_PS.ModAttrib6Value);

	private static final ImmutableList<PropertyDescriptor<eCEntityProxy>> REQUIRED_SKILLS = ImmutableList.of(CD.gCItem_PS.RequiredSkill1,
			CD.gCItem_PS.RequiredSkill2);
	private static final ImmutableList<PropertyDescriptor<eCEntityProxy>> ACTIVATE_SKILLS = ImmutableList.of(CD.gCItem_PS.ActivateSkill1,
			CD.gCItem_PS.ActivateSkill2);

	public StatsTab(EditorTemplateTab ctx) {
		super(ctx);
	}

	@Override
	public void initPropertyPanel(PropertyPanel propertyPanel) {
		//@foff
		addAttributeHeader(propertyPanel, "Required Attribute");
		for (int i = 1; i <= 4; i++) {
			int opIndex = i;
			PropertyDescriptor<bTPropertyContainer<EAttribReqOperation>> opProperty = CD.gCItem_PS.ReqAttrib1Op;
			propertyPanel
				.add(Integer.toString(i)).horizontalStart()
				.add(REQ_ATTRIB_TAGS.get(i - 1))
				.valueList(Misc.concat(new String[] {""}, ATTRIBUTES))
					.noTitle().horizontal().growx()
				.add(opProperty.getPropertySet(), (ps) -> ps.property(opProperty, opIndex),
						(ps, v) -> ps.property(opProperty, opIndex).setEnumValue(v.getEnumValue()),
						opProperty::getDefaultValue, opProperty.getDataType(), opProperty.getDataTypeName())
					.noTitle().horizontal().growx()
				.add(REQ_ATTRIB_VALUES.get(i - 1))
					.validate(validation(), StringValidators.REQUIRE_VALID_INTEGER)
					.noTitle().horizontal().growx()
				.done();
		}

		addAttributeHeader(propertyPanel, "Modify Attribute");
		for (int i = 1; i <= 4; i++) {
			propertyPanel
				.add(Integer.toString(i)).horizontalStart()
				.add(MOD_ATTRIB_TAGS.get(i - 1))
				.valueList(Misc.concat(new String[] {""}, Misc.concat(ATTRIBUTES, PROTECTIONS)))
					.noTitle().horizontal().growx()
				.add(MOD_ATTRIB_OPS.get(i - 1))
					.noTitle().horizontal().growx()
				.add(MOD_ATTRIB_VALUES.get(i - 1))
					.validate(validation(), StringValidators.REQUIRE_VALID_INTEGER)
					.noTitle().horizontal().growx()
				.done();
		}

		propertyPanel.addHeadline("Required Skill");
		addSkillProperty(propertyPanel, CD.gCItem_PS.RequiredSkill1, 1);
		addSkillProperty(propertyPanel, CD.gCItem_PS.RequiredSkill2, 2);

		propertyPanel.addHeadline("Activate Skill");
		addSkillProperty(propertyPanel, CD.gCItem_PS.ActivateSkill1, 1);
		addSkillProperty(propertyPanel, CD.gCItem_PS.ActivateSkill2, 2);
		//@fon
	}

	private void addAttributeHeader(PropertyPanel propertyPanel, String headline) {
		//@foff
		propertyPanel
			.addHeadline(headline)
			.add("").horizontalStart()
			.add("Attribute").horizontal().constraints("width 100:125:150")
			.add("Operator").horizontal().constraints("width 125:150:175")
			.add("Value").horizontal().constraints("width 50:75:100")
			.done();
		//@fon
	}

	private void addSkillProperty(PropertyPanel propertyPanel, PropertyDescriptor<eCEntityProxy> property, int i) {
		//@foff
		propertyPanel
			.add(Integer.toString(i)).horizontalStart()
			.add(property).horizontalSpan().noTitle()
				.<JTemplateGuidField>customize(tfGuidField -> tfGuidField.setFilter(
						e -> e.hasAnyClass(CD.gCSkill_PS.class, CD.gCMagic_PS.class)))
				.validate(validation(), GuidValidator.INSTANCE_ALLOW_EMPTY, new TemplateExistenceValidator(validation(), ctx))
			.done();
		//@fon
	}

	@Override
	public String getTabTitle() {
		return "Stats";
	}

	@Override
	public boolean isActive(TemplateFile tple) {
		return tple.getReferenceHeader().hasClass(CD.gCItem_PS.class);
	}
}

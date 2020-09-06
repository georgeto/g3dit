package de.george.g3dit.tab.template.views.header;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.netbeans.validation.api.builtin.stringvalidation.StringValidators;

import com.google.common.collect.ImmutableList;

import de.george.g3dit.gui.components.JEnumComboBox;
import de.george.g3dit.gui.components.JTemplateGuidField;
import de.george.g3dit.gui.validation.TemplateExistenceValidator;
import de.george.g3dit.tab.template.EditorTemplateTab;
import de.george.g3dit.util.PropertySync;
import de.george.g3utils.gui.SwingUtils;
import de.george.g3utils.util.Misc;
import de.george.g3utils.validation.GuidValidator;
import de.george.lrentnode.classes.G3Class;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.classes.desc.PropertyDescriptor;
import de.george.lrentnode.enums.G3Enums.EAttribModOperation;
import de.george.lrentnode.enums.G3Enums.EAttribReqOperation;
import de.george.lrentnode.properties.bCString;
import de.george.lrentnode.properties.bTPropertyContainer;
import de.george.lrentnode.properties.eCEntityProxy;
import de.george.lrentnode.properties.gInt;
import de.george.lrentnode.template.TemplateFile;
import net.miginfocom.swing.MigLayout;

public class StatsTab extends AbstractTemplateTab {
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

	private List<JComboBox<String>> cbTags = new ArrayList<>(10);
	private List<JEnumComboBox<?>> cbOps = new ArrayList<>(10);
	private List<JTextField> tfAmount = new ArrayList<>(10);
	private List<JTemplateGuidField> tfSkill = new ArrayList<>(4);

	public StatsTab(EditorTemplateTab ctx) {
		super(ctx);
	}

	@Override
	public void initComponents() {
		setLayout(new MigLayout("", "[]20[]20[]20[]"));

		add(SwingUtils.createBoldLabel("Required Attribute"), "spanx, wrap");
		add(new JLabel("Attribute"), "skip");
		add(new JLabel("Operator"));
		add(new JLabel("Value"), "wrap");

		for (int i = 1; i <= 4; i++) {
			add(new JLabel(String.valueOf(i)), "gapleft 7");

			JComboBox<String> cbAttribute = new JComboBox<>(Misc.concat(new String[] {""}, ATTRIBUTES));
			cbTags.add(cbAttribute);
			add(cbAttribute, "gaptop 5, width 100:125:150");

			JEnumComboBox<EAttribReqOperation> cbOperation = new JEnumComboBox<>(EAttribReqOperation.class);
			cbOps.add(cbOperation);
			add(cbOperation, "width 125:150:175");

			JTextField tfTemp = SwingUtils.createUndoTF();
			tfTemp.setName("Value");
			addValidators(tfTemp, StringValidators.REQUIRE_VALID_INTEGER);
			tfAmount.add(tfTemp);
			add(tfTemp, "width 50:75:100, wrap");
		}

		add(SwingUtils.createBoldLabel("Modify Attribute"), "gaptop 10, spanx, wrap");
		add(new JLabel("Attribute"), "skip");
		add(new JLabel("Operator"));
		add(new JLabel("Value"), "wrap");

		for (int i = 1; i <= 6; i++) {
			add(new JLabel(String.valueOf(i)), "gapleft 7");

			JComboBox<String> cbTemp = new JComboBox<>(Misc.concat(new String[] {""}, Misc.concat(ATTRIBUTES, PROTECTIONS)));
			cbTags.add(cbTemp);
			add(cbTemp, "gaptop 5, width 100:125:150");

			JEnumComboBox<EAttribModOperation> cbOperation = new JEnumComboBox<>(EAttribModOperation.class);
			cbOps.add(cbOperation);
			add(cbOperation, "width 125:150:175");

			JTextField tfTemp = SwingUtils.createUndoTF();
			tfAmount.add(tfTemp);
			add(tfTemp, "width 50:75:100, wrap");
		}

		add(SwingUtils.createBoldLabel("Required Skill"), "gaptop 10, spanx, wrap");
		for (int i = 1; i <= 2; i++) {
			add(new JLabel(String.valueOf(i)), "gapleft 7");

			JTemplateGuidField tfTemp = new JTemplateGuidField(ctx);
			tfSkill.add(tfTemp);
			add(tfTemp, "gaptop 5, width 100:300:300, spanx, wrap");
		}

		add(SwingUtils.createBoldLabel("Activate Skill"), "gaptop 10, spanx, wrap");
		for (int i = 1; i <= 2; i++) {
			add(new JLabel(String.valueOf(i)), "gapleft 7");

			JTemplateGuidField tfTemp = new JTemplateGuidField(ctx);
			tfTemp.initValidation(validation(), "Guid", GuidValidator.INSTANCE_ALLOW_EMPTY,
					new TemplateExistenceValidator(validation(), ctx));
			tfSkill.add(tfTemp);
			add(tfTemp, "gaptop 5, width 100:300:300, spanx, wrap");
		}
	}

	@Override
	public String getTabTitle() {
		return "Stats";
	}

	@Override
	public boolean isActive(TemplateFile tple) {
		return tple.getReferenceHeader().hasClass(CD.gCItem_PS.class);
	}

	@Override
	public void loadValues(TemplateFile tple) {
		G3Class item = tple.getReferenceHeader().getClass(CD.gCItem_PS.class);
		PropertySync sync = PropertySync.wrap(item);

		for (int i = 1; i <= 4; i++) {
			sync.readString(cbTags.get(i - 1), REQ_ATTRIB_TAGS.get(i - 1));
			cbOps.get(i - 1).setSelectedValue(item.property(CD.gCItem_PS.ReqAttrib1Op, i).getEnumValue());
			sync.readInt(tfAmount.get(i - 1), REQ_ATTRIB_VALUES.get(i - 1));
		}

		for (int i = 1; i <= 6; i++) {
			sync.readString(cbTags.get(i + 3), MOD_ATTRIB_TAGS.get(i - 1));
			sync.readEnum(cbOps.get(i + 3), EAttribModOperation.class, MOD_ATTRIB_OPS.get(i - 1));
			sync.readInt(tfAmount.get(i + 3), MOD_ATTRIB_VALUES.get(i - 1));
		}

		for (int i = 1; i <= 2; i++) {
			sync.readGuid(tfSkill.get(i - 1), REQUIRED_SKILLS.get(i - 1));
		}

		for (int i = 1; i <= 2; i++) {
			sync.readGuid(tfSkill.get(i + 1), ACTIVATE_SKILLS.get(i - 1));
		}
	}

	@Override
	public void saveValues(TemplateFile tple) {
		G3Class item = tple.getReferenceHeader().getClass(CD.gCItem_PS.class);
		PropertySync sync = PropertySync.wrap(item);

		for (int i = 1; i <= 4; i++) {
			sync.writeString(cbTags.get(i - 1), REQ_ATTRIB_TAGS.get(i - 1));
			item.property(CD.gCItem_PS.ReqAttrib1Op, i).setEnumValue(cbOps.get(i - 1).getSelectedValue());
			sync.writeInt(tfAmount.get(i - 1), REQ_ATTRIB_VALUES.get(i - 1));
		}

		for (int i = 1; i <= 6; i++) {
			sync.writeString(cbTags.get(i + 3), MOD_ATTRIB_TAGS.get(i - 1));
			sync.writeEnum(cbOps.get(i + 3), EAttribModOperation.class, MOD_ATTRIB_OPS.get(i - 1));
			sync.writeInt(tfAmount.get(i + 3), MOD_ATTRIB_VALUES.get(i - 1));
		}

		for (int i = 1; i <= 2; i++) {
			sync.writeGuid(tfSkill.get(i - 1), REQUIRED_SKILLS.get(i - 1));
		}

		for (int i = 1; i <= 2; i++) {
			sync.writeGuid(tfSkill.get(i + 1), ACTIVATE_SKILLS.get(i - 1));
		}
	}

}

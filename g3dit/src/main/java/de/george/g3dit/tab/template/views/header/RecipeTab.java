package de.george.g3dit.tab.template.views.header;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.netbeans.validation.api.builtin.stringvalidation.StringValidators;

import com.google.common.collect.ImmutableList;
import com.teamunify.i18n.I;

import de.george.g3dit.gui.components.JEnumComboBox;
import de.george.g3dit.gui.components.JTemplateGuidField;
import de.george.g3dit.gui.validation.TemplateExistenceValidator;
import de.george.g3dit.tab.shared.QualityPanel;
import de.george.g3dit.tab.template.EditorTemplateTab;
import de.george.g3dit.util.PropertySync;
import de.george.g3utils.gui.SwingUtils;
import de.george.g3utils.util.Misc;
import de.george.g3utils.validation.GuidValidator;
import de.george.lrentnode.classes.G3Class;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.classes.desc.PropertyDescriptor;
import de.george.lrentnode.enums.G3Enums.gESkillCategory;
import de.george.lrentnode.properties.eCEntityProxy;
import de.george.lrentnode.properties.gLong;
import de.george.lrentnode.template.TemplateFile;
import net.miginfocom.swing.MigLayout;

public class RecipeTab extends AbstractTemplateTab {
	private static final String[] ATTRIBUTES = {"STR", "DEX", "INT", "SMT", "THF", "ALC", "HP", "SP", "MP"};
	private static final ImmutableList<PropertyDescriptor<eCEntityProxy>> INGREDIENT_ITEMS = ImmutableList.of(
			CD.gCRecipe_PS.Ingredient1Item, CD.gCRecipe_PS.Ingredient2Item, CD.gCRecipe_PS.Ingredient3Item,
			CD.gCRecipe_PS.Ingredient4Item);
	private static final ImmutableList<PropertyDescriptor<gLong>> INGREDIENT_AMOUNTS = ImmutableList.of(CD.gCRecipe_PS.Ingredient1Amount,
			CD.gCRecipe_PS.Ingredient2Amount, CD.gCRecipe_PS.Ingredient3Amount, CD.gCRecipe_PS.Ingredient4Amount);

	private JEnumComboBox<gESkillCategory> cbCraft;
	private List<JTemplateGuidField> gfIngridientItem = new ArrayList<>(4);
	private List<JTextField> tfIngridientAmount = new ArrayList<>(4);
	private JTemplateGuidField gfReqSkill;
	private JComboBox<String> cbReqAttribTag;
	private JTextField tfReqAttribValue;
	private JTemplateGuidField gfResultItem;
	private JTextField tfResultAmount;
	private QualityPanel pnResultQuality;

	public RecipeTab(EditorTemplateTab ctx) {
		super(ctx);
	}

	@Override
	public void initComponents() {
		setLayout(new MigLayout("", "[]20[]20[]20[]"));

		add(SwingUtils.createBoldLabel(I.tr("Kategorie")), "spanx, wrap");
		add(new JLabel("Craft"), "gapleft 7, spanx, wrap");
		cbCraft = new JEnumComboBox<>(gESkillCategory.class);
		add(cbCraft, "gapleft 7, width 125:150:175, spanx, wrap");

		add(SwingUtils.createBoldLabel(I.tr("Zutaten")), "gaptop 10, spanx, wrap");
		add(new JLabel("Item"), "gapleft 7, spanx 2");
		add(new JLabel("Amount"), "wrap");

		for (int i = 1; i <= 4; i++) {
			JTemplateGuidField gfTemp = new JTemplateGuidField(ctx);
			gfTemp.initValidation(validation(), "Item", GuidValidator.INSTANCE_ALLOW_EMPTY,
					new TemplateExistenceValidator(validation(), ctx));
			gfIngridientItem.add(gfTemp);
			add(gfTemp, "gapleft 7, gaptop 5, width 100:270:270, spanx 2");

			JTextField tfTemp = SwingUtils.createUndoTF();
			tfTemp.setName("Amount");
			addValidators(tfTemp, StringValidators.REQUIRE_VALID_INTEGER, StringValidators.REQUIRE_NON_NEGATIVE_NUMBER);
			tfIngridientAmount.add(tfTemp);
			add(tfTemp, "width 50:75:100, wrap");
		}

		add(SwingUtils.createBoldLabel(I.tr("Resultat")), "gaptop 10, spanx, wrap");
		add(new JLabel("Item"), "gapleft 7, spanx 2");
		add(new JLabel("Amount"), "wrap");
		gfResultItem = new JTemplateGuidField(ctx);
		gfResultItem.initValidation(validation(), "ResultItem", GuidValidator.INSTANCE, new TemplateExistenceValidator(validation(), ctx));
		add(gfResultItem, "gapleft 7, gaptop 5, width 100:270:270, spanx 2");
		tfResultAmount = SwingUtils.createUndoTF();
		tfResultAmount.setName("ResultAmount");
		addValidators(tfResultAmount, StringValidators.REQUIRE_VALID_INTEGER, StringValidators.REQUIRE_NON_NEGATIVE_NUMBER);
		add(tfResultAmount, "width 50:75:100, wrap");
		pnResultQuality = new QualityPanel(CD.gCRecipe_PS.ResultQuality);
		add(pnResultQuality, "gapleft 7, width 100:370:370, spanx, wrap");

		add(SwingUtils.createBoldLabel(I.tr("Voraussetzungen")), "gaptop 10, spanx, wrap");
		add(new JLabel("Skill"), "gapleft 7, spanx, wrap");
		gfReqSkill = new JTemplateGuidField(ctx);
		gfReqSkill.initValidation(validation(), "Skill", GuidValidator.INSTANCE_ALLOW_EMPTY,
				new TemplateExistenceValidator(validation(), ctx));
		add(gfReqSkill, "gapleft 7, width 100:300:300, spanx, wrap");

		add(new JLabel("Attribute Tag"), "gapleft 7");
		add(new JLabel("Attribute Value"), "wrap");
		cbReqAttribTag = new JComboBox<>(Misc.concat(new String[] {""}, ATTRIBUTES));
		add(cbReqAttribTag, "gapleft 7, width 125:150:175");
		tfReqAttribValue = SwingUtils.createUndoTF();
		tfReqAttribValue.setName("Attribute Value");
		addValidators(tfReqAttribValue, StringValidators.REQUIRE_VALID_INTEGER, StringValidators.REQUIRE_NON_NEGATIVE_NUMBER);
		add(tfReqAttribValue, "growx, wrap");

	}

	@Override
	public String getTabTitle() {
		return "Recipe";
	}

	@Override
	public boolean isActive(TemplateFile tple) {
		return tple.getReferenceHeader().hasClass(CD.gCRecipe_PS.class);
	}

	@Override
	public void loadValues(TemplateFile tple) {
		G3Class recipe = tple.getReferenceHeader().getClass(CD.gCRecipe_PS.class);
		PropertySync sync = PropertySync.wrap(recipe);

		sync.readEnum(cbCraft, CD.gCRecipe_PS.Craft);
		for (int i = 0; i < 4; i++) {
			sync.readGuid(gfIngridientItem.get(i), INGREDIENT_ITEMS.get(i));
			sync.readLong(tfIngridientAmount.get(i), INGREDIENT_AMOUNTS.get(i));
		}
		sync.readGuid(gfResultItem, CD.gCRecipe_PS.ResultItem);
		sync.readLong(tfResultAmount, CD.gCRecipe_PS.ResultAmount);
		pnResultQuality.readQuality(recipe);
		sync.readGuid(gfReqSkill, CD.gCRecipe_PS.ReqSkill1);
		sync.readString(cbReqAttribTag, CD.gCRecipe_PS.ReqAttrib1Tag);
		sync.readLong(tfReqAttribValue, CD.gCRecipe_PS.ReqAttrib1Value);
	}

	@Override
	public void saveValues(TemplateFile tple) {
		G3Class recipe = tple.getReferenceHeader().getClass(CD.gCRecipe_PS.class);
		PropertySync sync = PropertySync.wrap(recipe);

		sync.writeEnum(cbCraft, CD.gCRecipe_PS.Craft);
		for (int i = 0; i < 4; i++) {
			sync.writeGuid(gfIngridientItem.get(i), INGREDIENT_ITEMS.get(i));
			sync.writeLong(tfIngridientAmount.get(i), INGREDIENT_AMOUNTS.get(i));
		}
		sync.writeGuid(gfResultItem, CD.gCRecipe_PS.ResultItem);
		sync.writeLong(tfResultAmount, CD.gCRecipe_PS.ResultAmount);
		pnResultQuality.writeQuality(recipe);
		sync.writeGuid(gfReqSkill, CD.gCRecipe_PS.ReqSkill1);
		sync.writeString(cbReqAttribTag, CD.gCRecipe_PS.ReqAttrib1Tag);
		sync.writeLong(tfReqAttribValue, CD.gCRecipe_PS.ReqAttrib1Value);
	}
}

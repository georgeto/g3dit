package de.george.g3dit.tab.archive.views.entity;

import javax.swing.JLabel;
import javax.swing.JTextField;

import de.george.g3dit.cache.Caches;
import de.george.g3dit.gui.components.JEntityGuidField;
import de.george.g3dit.gui.components.JEnumComboBox;
import de.george.g3dit.gui.components.JTemplateGuidField;
import de.george.g3dit.gui.validation.EntityExistenceValidator;
import de.george.g3dit.gui.validation.TemplateExistenceValidator;
import de.george.g3dit.tab.archive.EditorArchiveTab;
import de.george.g3utils.gui.JGuidField;
import de.george.g3utils.gui.SwingUtils;
import de.george.g3utils.structure.GuidUtil;
import de.george.g3utils.validation.GuidValidator;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.classes.G3Class;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.enums.G3Enums.gEFocusPriority;
import de.george.lrentnode.enums.G3Enums.gEUseType;
import net.miginfocom.swing.MigLayout;

public class InteractionTab extends AbstractEntityTab {
	private JTextField tfScript;
	private JGuidField tfAnchorPoint, tfOwner;
	private JTemplateGuidField tfSpell;
	private JEnumComboBox<gEFocusPriority> cbFocusPriority;
	private JEnumComboBox<gEUseType> cbUseType;

	public InteractionTab(EditorArchiveTab ctx) {
		super(ctx);
	}

	@Override
	public void initComponents() {
		setLayout(new MigLayout("fillx", "[]20px[]20px[]push"));

		JLabel lblFocusPriority = new JLabel("FocusPriority");
		add(lblFocusPriority, "cell 0 0");

		cbFocusPriority = new JEnumComboBox<>(gEFocusPriority.class);
		add(cbFocusPriority, "cell 0 1, width 50:100:100");

		JLabel lblUseType = new JLabel("UseType");
		add(lblUseType, "cell 1 0");

		cbUseType = new JEnumComboBox<>(gEUseType.class);
		add(cbUseType, "cell 1 1, width 50:100:100");

		JLabel lblScript = new JLabel("ScriptUseFunc");
		add(lblScript, "cell 2 0");

		tfScript = SwingUtils.createUndoTF();
		add(tfScript, "cell 2 1, width 50:100:100");

		JLabel lblAnchorPoint = new JLabel("AnchorPoint");
		add(lblAnchorPoint, "cell 0 2");

		tfAnchorPoint = new JEntityGuidField(ctx);
		tfAnchorPoint.initValidation(validation(), "AnchorPoint", GuidValidator.INSTANCE_ALLOW_EMPTY,
				new EntityExistenceValidator(validation(), ctx));
		add(tfAnchorPoint, "cell 0 3, width 100:300:300, spanx 3");

		JLabel lblOwner = new JLabel("Owner");
		add(lblOwner, "cell 0 4");

		tfOwner = new JEntityGuidField(ctx);
		tfOwner.initValidation(validation(), "Owner", GuidValidator.INSTANCE_ALLOW_EMPTY, new EntityExistenceValidator(validation(), ctx));
		add(tfOwner, "cell 0 5, width 100:300:300, spanx 3");

		JLabel lblSpell = new JLabel("Spell");
		add(lblSpell, "cell 0 6");

		tfSpell = new JTemplateGuidField(Caches.template(ctx));
		tfSpell.initValidation(validation(), "Spell", GuidValidator.INSTANCE_ALLOW_EMPTY,
				new TemplateExistenceValidator(validation(), ctx));
		add(tfSpell, "cell 0 7, width 100:300:300, spanx 3");
	}

	@Override
	public String getTabTitle() {
		return "Interaction";
	}

	@Override
	public boolean isActive(eCEntity entity) {
		return entity.hasClass(CD.gCInteraction_PS.class);
	}

	@Override
	public void loadValues(eCEntity entity) {
		G3Class interaction = entity.getClass(CD.gCInteraction_PS.class);
		cbFocusPriority.setSelectedValue(interaction.property(CD.gCInteraction_PS.FocusPriority).getEnumValue());
		cbUseType.setSelectedValue(interaction.property(CD.gCInteraction_PS.UseType).getEnumValue());
		tfScript.setText(interaction.property(CD.gCInteraction_PS.ScriptUseFunc).getString());
		tfAnchorPoint.setText(interaction.property(CD.gCInteraction_PS.AnchorPoint).getGuid());
		tfOwner.setText(interaction.property(CD.gCInteraction_PS.Owner).getGuid());
		tfSpell.setText(interaction.property(CD.gCInteraction_PS.Spell).getGuid());
	}

	@Override
	public void saveValues(eCEntity entity) {
		G3Class interaction = entity.getClass(CD.gCInteraction_PS.class);
		interaction.property(CD.gCInteraction_PS.FocusPriority).setEnumValue(cbFocusPriority.getSelectedValue());

		interaction.property(CD.gCInteraction_PS.UseType).setEnumValue(cbUseType.getSelectedValue());
		interaction.property(CD.gCInteraction_PS.ScriptUseFunc).setString(tfScript.getText());
		interaction.property(CD.gCInteraction_PS.AnchorPoint).setGuid(GuidUtil.parseGuid(tfAnchorPoint.getText()));
		interaction.property(CD.gCInteraction_PS.Owner).setGuid(GuidUtil.parseGuid(tfOwner.getText()));
		interaction.property(CD.gCInteraction_PS.Spell).setGuid(GuidUtil.parseGuid(tfSpell.getText()));
	}
}

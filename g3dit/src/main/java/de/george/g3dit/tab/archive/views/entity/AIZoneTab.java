package de.george.g3dit.tab.archive.views.entity;

import javax.swing.JLabel;

import org.netbeans.validation.api.ui.ValidationGroup;

import de.george.g3dit.gui.components.JEntityGuidField;
import de.george.g3dit.gui.components.JEnumComboBox;
import de.george.g3dit.gui.validation.EntityExistenceValidator;
import de.george.g3dit.tab.archive.EditorArchiveTab;
import de.george.g3utils.gui.JGuidField;
import de.george.g3utils.structure.GuidUtil;
import de.george.g3utils.validation.GuidValidator;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.classes.G3Class;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.enums.G3Enums.gESecurityLevel;
import de.george.lrentnode.enums.G3Enums.gEZoneType;
import net.miginfocom.swing.MigLayout;

public class AIZoneTab extends AbstractEntityTab {
	private JGuidField tfOwner;
	private JEnumComboBox<gEZoneType> cbZoneType;
	private JEnumComboBox<gESecurityLevel> cbSecurityLevel;

	public AIZoneTab(EditorArchiveTab ctx) {
		super(ctx);

		setLayout(new MigLayout("fillx", "[]20px[]20px[]push"));

		JLabel lblFocusPriority = new JLabel("ZoneType");
		add(lblFocusPriority, "cell 0 0");

		cbZoneType = new JEnumComboBox<>(gEZoneType.class);
		add(cbZoneType, "cell 0 1, width 50:100:100");

		JLabel lblUseType = new JLabel("SecurityLevel");
		add(lblUseType, "cell 1 0");

		cbSecurityLevel = new JEnumComboBox<>(gESecurityLevel.class);
		add(cbSecurityLevel, "cell 1 1, width 50:100:100");

		JLabel lblOwner = new JLabel("Owner");
		add(lblOwner, "cell 0 2");

		tfOwner = new JEntityGuidField(ctx);
		add(tfOwner, "cell 0 3, width 100:300:300, spanx 3");
	}

	@Override
	public String getTabTitle() {
		return "AIZone";
	}

	@Override
	public boolean isActive(eCEntity entity) {
		return entity.hasClass(CD.gCAIZone_PS.class);
	}

	/**
	 * Einmalig nach erstellen des Panels aufrufen, um die Fehleranzeige zu initialisieren
	 */
	@Override
	public void initValidation() {
		ValidationGroup group = getValidationPanel().getValidationGroup();

		tfOwner.initValidation(group, "Owner", GuidValidator.INSTANCE_ALLOW_EMPTY, new EntityExistenceValidator(group, ctx));
	}

	/**
	 * Nach dem Umschalten auf eine Entity aufrufen, um deren Werte ins GUI zu laden
	 */
	@Override
	public void loadValues(eCEntity entity) {
		G3Class aizone = entity.getClass(CD.gCAIZone_PS.class);
		cbZoneType.setSelectedValue(aizone.property(CD.gCAIZone_PS.Type).getEnumValue());
		cbSecurityLevel.setSelectedValue(aizone.property(CD.gCAIZone_PS.SecurityLevel).getEnumValue());
		tfOwner.setText(aizone.property(CD.gCAIZone_PS.Owner).getGuid());
	}

	/**
	 * Vor dem Umschalten auf eine andere Entity aufrufen, um Änderungen zu speichern
	 */
	@Override
	public void saveValues(eCEntity entity) {
		G3Class aizone = entity.getClass(CD.gCAIZone_PS.class);
		aizone.property(CD.gCAIZone_PS.Type).setEnumValue(cbZoneType.getSelectedValue());

		aizone.property(CD.gCAIZone_PS.SecurityLevel).setEnumValue(cbSecurityLevel.getSelectedValue());
		aizone.property(CD.gCAIZone_PS.Owner).setGuid(GuidUtil.parseGuid(tfOwner.getText()));
	}
}

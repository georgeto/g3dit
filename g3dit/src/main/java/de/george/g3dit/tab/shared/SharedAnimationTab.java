package de.george.g3dit.tab.shared;

import java.awt.Container;

import javax.swing.JLabel;
import javax.swing.JScrollPane;

import org.netbeans.validation.api.builtin.stringvalidation.StringValidators;
import org.netbeans.validation.api.ui.ValidationGroup;

import de.george.g3dit.EditorContext;
import de.george.g3utils.gui.SwingUtils;
import de.george.g3utils.gui.UndoableTextField;
import de.george.g3utils.validation.EmtpyWarnValidator;
import de.george.lrentnode.archive.G3ClassContainer;
import de.george.lrentnode.classes.eCVisualAnimation_PS;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.util.EntityUtil;
import net.miginfocom.swing.MigLayout;

public class SharedAnimationTab extends AbstractSharedTab {
	private UndoableTextField tfFilePath, tfFacialFilePath, tfMaterialSwitch;

	public SharedAnimationTab(EditorContext ctx, Container container) {
		super(ctx, container);
	}

	@Override
	public void initComponents(ValidationGroup validation, JScrollPane scrollPane) {
		container.setLayout(new MigLayout("fillx", "[]20px[]20px[]push[]"));

		container.add(new JLabel("ResourceFilePath"), "wrap");
		tfFilePath = SwingUtils.createUndoTF();
		tfFilePath.setName("ResourceFilePath");
		validation.add(tfFilePath, EmtpyWarnValidator.INSTANCE);
		container.add(tfFilePath, "width 100:300:300, wrap");

		container.add(new JLabel("FacialAnimFilePath"), "wrap");
		tfFacialFilePath = SwingUtils.createUndoTF();
		container.add(tfFacialFilePath, "width 100:300:300, wrap");

		container.add(new JLabel("MaterialSwitch"), "wrap");
		tfMaterialSwitch = SwingUtils.createUndoTF();
		tfMaterialSwitch.setName("MaterialSwitch");
		validation.add(tfMaterialSwitch, StringValidators.REQUIRE_VALID_INTEGER, StringValidators.REQUIRE_NON_NEGATIVE_NUMBER);
		container.add(tfMaterialSwitch, "width 100:300:300, wrap");
	}

	@Override
	public String getTabTitle() {
		return "Animation";
	}

	@Override
	public boolean isActive(G3ClassContainer entity) {
		return entity.hasClass(CD.eCVisualAnimation_PS.class);
	}

	@Override
	public void loadValues(G3ClassContainer container) {
		eCVisualAnimation_PS animation = EntityUtil.getAnimatedMeshClass(container);
		tfFilePath.setText(animation.fxaSlot.fxaFile);
		tfFacialFilePath.setText(animation.fxaSlot.fxaFile2);
		tfMaterialSwitch.setText(Integer.toString(animation.fxaSlot.fxaSwitch));
	}

	@Override
	public void saveValues(G3ClassContainer container) {
		eCVisualAnimation_PS animation = EntityUtil.getAnimatedMeshClass(container);

		String filePath = tfFilePath.getText();
		String facialFilePath = tfFacialFilePath.getText();
		int materialSwitch = Integer.parseInt(tfMaterialSwitch.getText());

		animation.fxaSlot.fxaFile = filePath;
		animation.fxaSlot.fxaFile2 = !facialFilePath.isEmpty() ? facialFilePath : null;
		animation.fxaSlot.fxaSwitch = materialSwitch;
		animation.fxaSlot.fxaSwitch2 = materialSwitch;

		animation.property(CD.eCVisualAnimation_PS.ResourceFilePath).setString(filePath.replace(".fxa", ".FXA"));
		animation.property(CD.eCVisualAnimation_PS.FacialAnimFilePath).setString(facialFilePath.replace(".fxa", ".FXA"));
		animation.property(CD.eCVisualAnimation_PS.MaterialSwitch).setInt(materialSwitch);
	}
}

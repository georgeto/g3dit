package de.george.g3dit.tab.shared;

import java.awt.Container;

import javax.swing.JLabel;
import javax.swing.JScrollPane;

import org.netbeans.validation.api.builtin.stringvalidation.StringValidators;
import org.netbeans.validation.api.ui.ValidationGroup;

import de.george.g3dit.EditorContext;
import de.george.g3dit.util.PropertySync;
import de.george.g3utils.gui.SwingUtils;
import de.george.g3utils.gui.UndoableTextField;
import de.george.g3utils.validation.EmtpyWarnValidator;
import de.george.lrentnode.archive.G3ClassContainer;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.util.EntityUtil;
import net.miginfocom.swing.MigLayout;

public class SharedMeshTab extends AbstractSharedTab {
	private UndoableTextField tfFileName, tfFilePath, tfMaterialSwitch;

	public SharedMeshTab(EditorContext ctx, Container container) {
		super(ctx, container);
	}

	@Override
	public void initComponents(ValidationGroup validation, JScrollPane scrollPane) {
		container.setLayout(new MigLayout("fillx", "[]20px[]20px[]push[]"));

		container.add(new JLabel("ResourceFileName"), "wrap");
		tfFileName = SwingUtils.createUndoTF();
		tfFileName.setName("ResourceFileName");
		validation.add(tfFileName, EmtpyWarnValidator.INSTANCE);
		container.add(tfFileName, "width 100:300:300, wrap");

		container.add(new JLabel("ResourceFilePath"), "wrap");
		tfFilePath = SwingUtils.createUndoTF();
		container.add(tfFilePath, "width 100:300:300, wrap");

		container.add(new JLabel("MaterialSwitch"), "wrap");
		tfMaterialSwitch = SwingUtils.createUndoTF();
		tfMaterialSwitch.setName("MaterialSwitch");
		validation.add(tfMaterialSwitch, StringValidators.REQUIRE_VALID_INTEGER, StringValidators.REQUIRE_NON_NEGATIVE_NUMBER);
		container.add(tfMaterialSwitch, "width 100:300:300, wrap");
	}

	@Override
	public String getTabTitle() {
		return "Mesh";
	}

	@Override
	public boolean isActive(G3ClassContainer entity) {
		return entity.hasClass(CD.eCVisualMeshStatic_PS.class) || entity.hasClass(CD.eCVisualMeshDynamic_PS.class);
	}

	@Override
	public void loadValues(G3ClassContainer container) {
		PropertySync meshSync = PropertySync.wrap(EntityUtil.getStaticMeshClass(container));
		meshSync.readString(tfFileName, CD.eCVisualMeshBase_PS.ResourceFileName);
		meshSync.readString(tfFilePath, CD.eCVisualMeshStatic_PS.ResourceFilePath);
		meshSync.readInt(tfMaterialSwitch, CD.eCVisualMeshBase_PS.MaterialSwitch);
	}

	@Override
	public void saveValues(G3ClassContainer container) {
		PropertySync meshSync = PropertySync.wrap(EntityUtil.getStaticMeshClass(container));
		meshSync.writeString(tfFileName, CD.eCVisualMeshBase_PS.ResourceFileName);
		meshSync.writeString(tfFilePath, CD.eCVisualMeshStatic_PS.ResourceFilePath);
		meshSync.writeInt(tfMaterialSwitch, CD.eCVisualMeshBase_PS.MaterialSwitch);
	}
}

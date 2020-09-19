package de.george.g3dit.tab.shared;

import java.awt.Container;

import javax.swing.JScrollPane;

import org.netbeans.validation.api.builtin.stringvalidation.StringValidators;
import org.netbeans.validation.api.ui.ValidationGroup;

import de.george.g3dit.EditorContext;
import de.george.g3dit.gui.edit.PropertyPanel;
import de.george.g3utils.validation.EmtpyWarnValidator;
import de.george.lrentnode.archive.G3ClassContainer;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.util.EntityUtil;

public class SharedMeshTab extends AbstractPropertySharedTab {
	public SharedMeshTab(EditorContext ctx, Container container) {
		super(ctx, container);
	}

	@Override
	protected void initPropertyPanel(PropertyPanel propertyPanel, ValidationGroup validation, JScrollPane scrollPane) {
		//@foff
		propertyPanel
			.add(CD.eCVisualMeshBase_PS.ResourceFileName, EntityUtil::getStaticMeshClass).fullWidth()
				.validate(validation, EmtpyWarnValidator.INSTANCE)
			.add(CD.eCVisualMeshStatic_PS.ResourceFilePath, EntityUtil::getStaticMeshClass).fullWidth()
			.add(CD.eCVisualMeshBase_PS.MaterialSwitch, EntityUtil::getStaticMeshClass).fullWidth()
				.validate(validation, StringValidators.REQUIRE_VALID_INTEGER, StringValidators.REQUIRE_NON_NEGATIVE_NUMBER)
			.done();
		//@fon
	}

	@Override
	public String getTabTitle() {
		return "Mesh";
	}

	@Override
	public boolean isActive(G3ClassContainer entity) {
		return entity.hasClass(CD.eCVisualMeshStatic_PS.class) || entity.hasClass(CD.eCVisualMeshDynamic_PS.class);
	}
}

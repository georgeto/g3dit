package de.george.g3dit.tab.shared;

import java.awt.Container;

import javax.swing.JScrollPane;

import org.netbeans.validation.api.builtin.stringvalidation.StringValidators;
import org.netbeans.validation.api.ui.ValidationGroup;

import de.george.g3dit.EditorContext;
import de.george.g3dit.gui.edit.PropertyPanel;
import de.george.lrentnode.archive.G3ClassContainer;
import de.george.lrentnode.classes.desc.CD;

public class SharedEnclaveTab extends AbstractPropertySharedTab {
	public SharedEnclaveTab(EditorContext ctx, Container container) {
		super(ctx, container);
	}

	@Override
	protected void initPropertyPanel(PropertyPanel propertyPanel, ValidationGroup validation, JScrollPane scrollPane) {
		//@foff
		propertyPanel
			.add(CD.gCEnclave_PS.PoliticalAlignment).horizontalStart().sizegroup("common")
			.add(CD.gCEnclave_PS.KnownPlayerCrime).horizontal().sizegroup("common")
			.add(CD.gCEnclave_PS.PlayerFame).horizontal().sizegroup("common")
				.validate(validation, StringValidators.REQUIRE_NON_NEGATIVE_NUMBER, StringValidators.REQUIRE_VALID_INTEGER)
			.done();
		//@fon
	}

	@Override
	protected boolean isGrow() {
		return false;
	}

	@Override
	public String getTabTitle() {
		return "Enclave";
	}

	@Override
	public boolean isActive(G3ClassContainer entity) {
		return entity.hasClass(CD.gCEnclave_PS.class);
	}
}

package de.george.g3dit.tab.shared;

import java.awt.Container;

import javax.swing.JLabel;
import javax.swing.JTextField;

import org.netbeans.validation.api.builtin.stringvalidation.StringValidators;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.netbeans.validation.api.ui.swing.ValidationPanel;

import de.george.g3dit.EditorContext;
import de.george.g3dit.gui.components.JEnumComboBox;
import de.george.g3utils.gui.SwingUtils;
import de.george.lrentnode.archive.G3ClassContainer;
import de.george.lrentnode.classes.G3Class;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.enums.G3Enums.gEPlayerCrime;
import de.george.lrentnode.enums.G3Enums.gEPoliticalAlignment;
import net.miginfocom.swing.MigLayout;

public class SharedEnclaveTab extends AbstractSharedTab {
	private JTextField tfPlayerFame;
	private JEnumComboBox<gEPoliticalAlignment> cbPoliticalAlignment;
	private JEnumComboBox<gEPlayerCrime> cbKnownPlayerCrime;

	public SharedEnclaveTab(EditorContext ctx, Container container) {
		super(ctx, container);
		container.setLayout(new MigLayout("fillx", "[]20px[]20px[]push"));

		JLabel lblPoliticalAlignment = new JLabel("PoliticalAlignment");
		cbPoliticalAlignment = new JEnumComboBox<>(gEPoliticalAlignment.class);

		JLabel lblKnownPlayerCrime = new JLabel("KnownPlayerCrime");
		cbKnownPlayerCrime = new JEnumComboBox<>(gEPlayerCrime.class);

		JLabel lblPlayerFame = new JLabel("PlayerFame");
		tfPlayerFame = SwingUtils.createUndoTF();

		container.add(lblPoliticalAlignment, "");
		container.add(lblKnownPlayerCrime, "");
		container.add(lblPlayerFame, "wrap");
		container.add(cbPoliticalAlignment, "width 50:100:100");
		container.add(cbKnownPlayerCrime, "width 50:100:100");
		container.add(tfPlayerFame, "width 50:100:100, wrap");
	}

	@Override
	public String getTabTitle() {
		return "Enclave";
	}

	@Override
	public boolean isActive(G3ClassContainer entity) {
		return entity.hasClass(CD.gCEnclave_PS.class);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void initValidation(ValidationPanel validationPanel) {
		ValidationGroup group = validationPanel.getValidationGroup();

		tfPlayerFame.setName("PlayerFame");
		group.add(tfPlayerFame, StringValidators.REQUIRE_NON_NEGATIVE_NUMBER, StringValidators.REQUIRE_VALID_INTEGER);
	}

	@Override
	public void loadValues(G3ClassContainer entity) {
		G3Class enclave = entity.getClass(CD.gCEnclave_PS.class);
		cbPoliticalAlignment.setSelectedValue(enclave.property(CD.gCEnclave_PS.PoliticalAlignment).getEnumValue());
		cbKnownPlayerCrime.setSelectedValue(enclave.property(CD.gCEnclave_PS.KnownPlayerCrime).getEnumValue());
		tfPlayerFame.setText(Long.toString(enclave.property(CD.gCEnclave_PS.PlayerFame).getLong()));
	}

	@Override
	public void saveValues(G3ClassContainer entity) {
		G3Class enclave = entity.getClass(CD.gCEnclave_PS.class);
		enclave.property(CD.gCEnclave_PS.PoliticalAlignment).setEnumValue(cbPoliticalAlignment.getSelectedValue());

		enclave.property(CD.gCEnclave_PS.KnownPlayerCrime).setEnumValue(cbKnownPlayerCrime.getSelectedValue());
		enclave.property(CD.gCEnclave_PS.PlayerFame).setLong(Integer.valueOf(tfPlayerFame.getText()));
	}
}

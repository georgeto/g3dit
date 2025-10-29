package de.george.g3dit.tab.archive.views.entity.dialogs;

import java.awt.BorderLayout;
import java.awt.Window;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jidesoft.dialog.ButtonPanel;
import com.teamunify.i18n.I;

import de.george.g3dit.gui.dialogs.ExtStandardDialog;
import de.george.g3dit.gui.dialogs.TemplateSearchDialog;
import de.george.g3utils.gui.SwingUtils;
import de.george.lrentnode.classes.eCVegetation_Mesh.eSVegetationMeshID;
import de.george.lrentnode.classes.eCVegetation_PS;
import net.miginfocom.swing.MigLayout;

public class SelectMeshDialog extends ExtStandardDialog {
	private static final Logger logger = LoggerFactory.getLogger(TemplateSearchDialog.class);

	private MeshListComboBox cbMesh;

	public SelectMeshDialog(Window owner, String title, eCVegetation_PS vegetationPS, eSVegetationMeshID lastSelectedMeshID) {
		super(owner, title, true);
		setType(Type.UTILITY);
		cbMesh = new MeshListComboBox(vegetationPS, lastSelectedMeshID);
		autosize(400, 0);
	}

	@Override
	public JComponent createContentPanel() {
		JPanel mainPanel = new JPanel(new MigLayout("fill"));
		add(mainPanel, BorderLayout.CENTER);

		mainPanel.add(new JLabel(I.tr("Mesh")), "wrap");
		mainPanel.add(cbMesh.getComboBox(), "width 200:300:400, wrap");

		return mainPanel;
	}

	@Override
	public ButtonPanel createButtonPanel() {
		ButtonPanel buttonPanel = newButtonPanel();

		Action okAction = SwingUtils.createAction(I.tr("Ok"), () -> {
			dispose();
			setDialogResult(RESULT_AFFIRMED);
		});

		addButton(buttonPanel, okAction, ButtonPanel.AFFIRMATIVE_BUTTON);
		addDefaultCancelButton(buttonPanel);

		return buttonPanel;
	}

	public eSVegetationMeshID getLastSelectedMeshID() {
		return cbMesh.getSelectedMeshID();
	}
}

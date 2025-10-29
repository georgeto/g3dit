package de.george.g3dit.tab.archive.views.entity.dialogs;

import java.awt.BorderLayout;
import java.awt.Window;

import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jidesoft.dialog.ButtonPanel;
import com.teamunify.i18n.I;

import de.george.g3dit.gui.dialogs.ExtStandardDialog;
import de.george.g3dit.tab.shared.PositionPanel;
import de.george.g3utils.gui.SwingUtils;
import de.george.g3utils.structure.bCMatrix;
import net.miginfocom.swing.MigLayout;

public class ImportVegetationObjectsDialog extends ExtStandardDialog {
	private static final Logger logger = LoggerFactory.getLogger(ImportVegetationObjectsDialog.class);

	private PositionPanel importPositionPanel;
	private bCMatrix importPosition;
	private JCheckBox cbImportMeshes, cbSkipMissing;

	public ImportVegetationObjectsDialog(Window owner, String title) {
		super(owner, title, true);
		setType(Type.UTILITY);
		importPosition = bCMatrix.getIdentity();
		autosize(400, 0);
	}

	@Override
	public JComponent createContentPanel() {
		JPanel mainPanel = new JPanel(new MigLayout("fill"));

		importPositionPanel = new PositionPanel(I.tr("Import position"), getOwner(), p -> {
			importPosition = p;
			importPositionPanel.setPositionMatrix(importPosition);
		});
		importPositionPanel.setToolTipText(I.tr("Position of import objects will be relative to the import position."));
		importPositionPanel.setPositionMatrix(importPosition);
		mainPanel.add(importPositionPanel, "width 100:300:400, spanx 4, grow, wrap");

		cbImportMeshes = new JCheckBox(I.tr("Import meshes used by imported objects"), true);
		mainPanel.add(cbImportMeshes, "wrap");

		cbSkipMissing = new JCheckBox(I.tr("Skip objects with missing mesh"), false);
		mainPanel.add(cbSkipMissing, "");

		add(mainPanel, BorderLayout.CENTER);

		return mainPanel;
	}

	@Override
	public ButtonPanel createButtonPanel() {
		ButtonPanel buttonPanel = newButtonPanel();

		Action okAction = SwingUtils.createAction(I.tr("Import"), () -> {
			dispose();
			setDialogResult(RESULT_AFFIRMED);
		});

		addButton(buttonPanel, okAction, ButtonPanel.AFFIRMATIVE_BUTTON);
		addDefaultCancelButton(buttonPanel);

		return buttonPanel;
	}

	public bCMatrix getImportPosition() {
		return importPosition;
	}

	public boolean importMeshes() {
		return cbImportMeshes.isSelected();
	}

	public boolean skipMissing() {
		return cbSkipMissing.isSelected();
	}
}

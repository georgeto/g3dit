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

public class ExportVegetationObjectsDialog extends ExtStandardDialog {
	private static final Logger logger = LoggerFactory.getLogger(ExportVegetationObjectsDialog.class);

	private PositionPanel exportPositionPanel;
	private bCMatrix exportPosition;
	private JCheckBox cbIncludeMeshes;

	public ExportVegetationObjectsDialog(Window owner, String title) {
		super(owner, title, true);
		setType(Type.UTILITY);
		autosize(400, 0);
	}

	@Override
	public JComponent createContentPanel() {
		JPanel mainPanel = new JPanel(new MigLayout("fill"));

		exportPosition = bCMatrix.getIdentity();
		exportPositionPanel = new PositionPanel(I.tr("Export position"), getOwner(), p -> {
			exportPosition = p;
			exportPositionPanel.setPositionMatrix(exportPosition);
		});
		exportPositionPanel.setToolTipText(I.tr("Position of exported objects will be relative to the export position."));
		exportPositionPanel.setPositionMatrix(exportPosition);
		mainPanel.add(exportPositionPanel, "width 100:300:400, spanx 4, grow, wrap");

		cbIncludeMeshes = new JCheckBox(I.tr("Export meshes used by exported objects"), true);
		mainPanel.add(cbIncludeMeshes, "");

		add(mainPanel, BorderLayout.CENTER);

		return mainPanel;
	}

	@Override
	public ButtonPanel createButtonPanel() {
		ButtonPanel buttonPanel = newButtonPanel();

		Action okAction = SwingUtils.createAction(I.tr("Export"), () -> {
			dispose();
			setDialogResult(RESULT_AFFIRMED);
		});

		addButton(buttonPanel, okAction, ButtonPanel.AFFIRMATIVE_BUTTON);
		addDefaultCancelButton(buttonPanel);

		return buttonPanel;
	}

	public bCMatrix getExportPosition() {
		return exportPosition;
	}

	public boolean includeMeshes() {
		return cbIncludeMeshes.isSelected();
	}
}

package de.george.g3dit.tab.shared;

import java.awt.Color;
import java.awt.Window;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.ezware.dialog.task.TaskDialogs;
import com.teamunify.i18n.I;

import de.george.g3dit.gui.theme.LayoutUtils;
import de.george.g3dit.tab.archive.views.entity.dialogs.PositionDialog;
import de.george.g3dit.util.Icons;
import de.george.g3utils.structure.bCEulerAngles;
import de.george.g3utils.structure.bCMatrix;
import de.george.g3utils.structure.bCVector;
import de.george.g3utils.util.IOUtils;
import de.george.g3utils.util.Misc;
import net.miginfocom.swing.MigLayout;

public class PositionPanel extends JPanel {
	private bCMatrix positionMatrix;

	private JLabel lblTranslation, lblRotation, lblScaling;

	private Window dialogOwner;
	private Consumer<bCMatrix> changePositionConsumer;

	private Consumer<bCMatrix> changePositionConsumerKeepChilds;

	private String title;

	public PositionPanel(String title, Window dialogOwner, Consumer<bCMatrix> changePositionConsumer) {
		this(title, dialogOwner, changePositionConsumer, null);
	}

	public PositionPanel(String title, Window dialogOwner, Consumer<bCMatrix> changePositionConsumer,
			Consumer<bCMatrix> changePositionConsumerKeepChilds) {
		this.title = title;
		this.dialogOwner = dialogOwner;
		this.changePositionConsumer = changePositionConsumer;
		this.changePositionConsumerKeepChilds = changePositionConsumerKeepChilds;

		setLayout(new MigLayout("", "[grow]push[]5[]"));
		setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY, 1, true), title));

		lblTranslation = new JLabel();
		add(lblTranslation, "split 3, spany 2, flowy");

		lblRotation = new JLabel();
		add(lblRotation, "");

		lblScaling = new JLabel();
		add(lblScaling, "");

		JButton btnCopyPosition = new JButton(Icons.getImageIcon(Icons.Misc.COOKIE));
		btnCopyPosition.setToolTipText(I.tr("Copy position to clipboard"));
		add(btnCopyPosition, LayoutUtils.sqrBtn() + (changePositionConsumerKeepChilds != null ? ", skip" : ""));
		btnCopyPosition.addActionListener(e -> handleCopyPosition());

		JButton btnCopyMarvinPosition = new JButton(Icons.getImageIcon(Icons.Misc.COOKIE_BITE));
		btnCopyMarvinPosition.setToolTipText(I.tr("Copy position to clipboard (rounded for console)"));
		add(btnCopyMarvinPosition, LayoutUtils.sqrBtn("wrap"));
		btnCopyMarvinPosition.addActionListener(e -> handleCopyMarvinPosition());

		if (changePositionConsumerKeepChilds != null) {
			JButton btnEditPositionKeepChilds = new JButton(Icons.getImageIcon(Icons.Action.LAYER_EDIT));
			btnEditPositionKeepChilds.setToolTipText(I.tr("Change position, preserve keep world position of child entities"));
			add(btnEditPositionKeepChilds, LayoutUtils.sqrBtn());
			btnEditPositionKeepChilds.addActionListener(e -> handleChangePosition(true));
		}

		JButton btnEditPosition = new JButton(Icons.getImageIcon(Icons.Action.EDIT));
		btnEditPosition.setToolTipText(I.tr("Change position"));
		add(btnEditPosition, LayoutUtils.sqrBtn());
		btnEditPosition.addActionListener(e -> handleChangePosition(false));

		JButton btnPastePosition = new JButton(Icons.getImageIcon(Icons.IO.IMPORT));
		btnPastePosition.setToolTipText(I.tr("Use position from clipboard"));
		add(btnPastePosition, LayoutUtils.sqrBtn());
		btnPastePosition.addActionListener(e -> handlePastePosition());
	}

	public void setPositionMatrix(bCMatrix positionMatrix) {
		this.positionMatrix = positionMatrix.clone();

		bCVector translation = positionMatrix.getTranslation();
		lblTranslation.setText("x: " + translation.getX() + "  y: " + translation.getY() + "  z: " + translation.getZ());
		bCEulerAngles rotation = new bCEulerAngles(positionMatrix);
		lblRotation.setText("pitch: " + Misc.round(rotation.getPitchDeg(), 2) + "  yaw: " + Misc.round(rotation.getYawDeg(), 2)
				+ "  roll: " + Misc.round(rotation.getRollDeg(), 2));

		bCVector scaling = positionMatrix.getPureScaling().getApplied(f -> Misc.round(f, 2));
		// Uniform scaling
		if (scaling.getX() == scaling.getY() && scaling.getX() == scaling.getZ()) {
			lblScaling.setText("scale: " + Misc.round(scaling.getX(), 2));
		} else {
			lblScaling.setText("scalex: " + Misc.round(scaling.getX(), 2) + "  scaley: " + Misc.round(scaling.getY(), 2) + "  scalez: "
					+ Misc.round(scaling.getZ(), 2));
		}
	}

	private void handleChangePosition(boolean keepChildPosition) {
		PositionDialog dialog = new PositionDialog(dialogOwner,
				keepChildPosition ? I.trf("Change {0} (child position unchanged)", title) : I.trf("Change {0}", title), positionMatrix);

		if (dialog.openAndWasSuccessful(this)) {
			if (!keepChildPosition) {
				changePositionConsumer.accept(dialog.getPositionMatrix());
			} else {
				changePositionConsumerKeepChilds.accept(dialog.getPositionMatrix());
			}
		}
	}

	private void handleCopyMarvinPosition() {
		IOUtils.copyToClipboard(positionMatrix.getTranslation().toMarvinString());
	}

	private void handleCopyPosition() {
		IOUtils.copyToClipboard(Misc.positionToString(positionMatrix));
	}

	private void handlePastePosition() {
		String clipboardContent = IOUtils.getClipboardContent();

		bCVector position = Misc.stringToPosition(clipboardContent);
		if (position != null) {
			bCMatrix worldMatrix = new bCMatrix();
			worldMatrix.modifyRotation(Misc.stringToRotation(clipboardContent));
			worldMatrix.modifyScaling(Misc.stringToScaling(clipboardContent));
			worldMatrix.modifyTranslation(position);

			changePositionConsumer.accept(worldMatrix);
		} else {
			TaskDialogs.inform(dialogOwner, I.tr("Clipboard does not contain position data"), null);
		}
	}
}

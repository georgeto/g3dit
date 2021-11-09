package de.george.g3dit.tab.archive.views.entity.dialogs;

import java.awt.Window;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;

import com.jidesoft.dialog.ButtonPanel;
import com.teamunify.i18n.I;

import de.george.g3dit.gui.dialogs.ExtStandardDialog;
import de.george.g3utils.gui.SwingUtils;
import de.george.g3utils.structure.bCEulerAngles;
import de.george.g3utils.structure.bCMatrix;
import de.george.g3utils.structure.bCVector;
import de.george.g3utils.util.Misc;
import net.miginfocom.swing.MigLayout;

public class PositionDialog extends ExtStandardDialog {
	private boolean internalChange;

	private JTextArea taRawText;
	private JTextField tfPitch, tfYaw, tfRoll, tfScaleX, tfScaleY, tfScaleZ, tfX, tfY, tfZ;

	private bCMatrix positionMatrix;

	public PositionDialog(Window owner, String title, bCMatrix positionMatrix) {
		super(owner, title, true);
		this.positionMatrix = positionMatrix.clone();
		setType(Type.UTILITY);
		setResizable(false);
		setSize(325, 285);
	}

	@Override
	public JComponent createContentPanel() {
		JPanel mainPanel = new JPanel(
				new MigLayout("fill", "[sg, fill, grow]10[sg, fill, grow]10[sg, fill, grow]", "[]1[]8[]1[]8[]1[]8[fill, grow]"));

		mainPanel.add(new JLabel(I.tr("x-pos")));
		mainPanel.add(new JLabel(I.tr("y-pos")));
		mainPanel.add(new JLabel(I.tr("z-pos")), "wrap");

		tfX = SwingUtils.createUndoTF();
		mainPanel.add(tfX, "");
		tfY = SwingUtils.createUndoTF();
		mainPanel.add(tfY, "");
		tfZ = SwingUtils.createUndoTF();
		mainPanel.add(tfZ, "wrap");

		mainPanel.add(new JLabel(I.tr("pitch")));
		mainPanel.add(new JLabel(I.tr("yaw")));
		mainPanel.add(new JLabel(I.tr("roll")), "wrap");

		tfPitch = SwingUtils.createUndoTF();
		mainPanel.add(tfPitch, "");
		tfYaw = SwingUtils.createUndoTF();
		mainPanel.add(tfYaw, "");
		tfRoll = SwingUtils.createUndoTF();
		mainPanel.add(tfRoll, "wrap");

		mainPanel.add(new JLabel(I.tr("x-scale")));
		mainPanel.add(new JLabel(I.tr("y-scale")));
		mainPanel.add(new JLabel(I.tr("z-scale")), "wrap");

		tfScaleX = SwingUtils.createUndoTF();
		mainPanel.add(tfScaleX, "");
		tfScaleY = SwingUtils.createUndoTF();
		mainPanel.add(tfScaleY, "");
		tfScaleZ = SwingUtils.createUndoTF();
		mainPanel.add(tfScaleZ, "wrap");

		taRawText = new JTextArea();
		taRawText.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		mainPanel.add(taRawText, "spanx 4, height 80!, grow");

		taRawText.getDocument().addDocumentListener(SwingUtils.createDocumentListener(this::parseTextArea));

		loadPosition();
		return mainPanel;
	}

	@Override
	public ButtonPanel createButtonPanel() {
		ButtonPanel buttonPanel = newButtonPanel();
		buttonPanel.setAlignment(SwingConstants.CENTER);
		buttonPanel.setMinButtonWidth(80);

		Action saveAction = SwingUtils.createAction(I.tr("Save"), () -> {
			try {
				positionMatrix.setToIdentity();
				positionMatrix.modifyRotation(bCEulerAngles.fromDegree(Float.parseFloat(tfYaw.getText()),
						Float.parseFloat(tfPitch.getText()), Float.parseFloat(tfRoll.getText())));
				positionMatrix.modifyScaling(new bCVector(Float.parseFloat(tfScaleX.getText()), Float.parseFloat(tfScaleY.getText()),
						Float.parseFloat(tfScaleZ.getText())));
				positionMatrix.modifyTranslation(
						new bCVector(Float.parseFloat(tfX.getText()), Float.parseFloat(tfY.getText()), Float.parseFloat(tfZ.getText())));
				affirm();
			} catch (NumberFormatException e1) {
				e1.printStackTrace();
			}
		});

		addButton(buttonPanel, saveAction, ButtonPanel.AFFIRMATIVE_BUTTON);
		addDefaultCancelButton(buttonPanel);

		return buttonPanel;
	}

	private void setTextFields(bCVector position, bCEulerAngles rotation, bCVector scaling) {
		tfX.setText(Misc.formatFloat(position.getX()));
		tfY.setText(Misc.formatFloat(position.getY()));
		tfZ.setText(Misc.formatFloat(position.getZ()));
		tfPitch.setText(Misc.formatFloat(rotation.getPitchDeg()));
		tfYaw.setText(Misc.formatFloat(rotation.getYawDeg()));
		tfRoll.setText(Misc.formatFloat(rotation.getRollDeg()));
		tfScaleX.setText(Misc.formatFloat(scaling.getX()));
		tfScaleY.setText(Misc.formatFloat(scaling.getY()));
		tfScaleZ.setText(Misc.formatFloat(scaling.getZ()));
	}

	private void loadPosition() {
		// Daten eintragen
		bCVector position = positionMatrix.getTranslation();
		bCEulerAngles rotation = new bCEulerAngles(positionMatrix);
		bCVector scaling = positionMatrix.getPureScaling();

		setTextFields(position, rotation, scaling);

		String rawText = Misc.positionToString(position, rotation, scaling);
		internalChange = true;
		taRawText.setText(rawText);
		internalChange = false;
	}

	private void parseTextArea() {
		if (internalChange) {
			return;
		}

		String text = taRawText.getText();
		bCVector parsedPosition = Misc.stringToPosition(text);
		if (parsedPosition == null) {
			parsedPosition = bCVector.nullVector();
		}
		bCEulerAngles parsedRotation = Misc.stringToRotation(text);
		bCVector parsedScaling = Misc.stringToScaling(text);

		setTextFields(parsedPosition, parsedRotation, parsedScaling);
	}

	public bCMatrix getPositionMatrix() {
		return positionMatrix;
	}
}

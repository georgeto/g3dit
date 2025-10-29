package de.george.g3dit.tab.archive.views.entity.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Window;
import java.util.Optional;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ezware.dialog.task.TaskDialogs;
import com.jidesoft.dialog.ButtonPanel;
import com.teamunify.i18n.I;

import de.george.g3dit.gui.dialogs.ExtStandardDialog;
import de.george.g3utils.gui.ColorChooserButton;
import de.george.g3utils.gui.SwingUtils;
import de.george.g3utils.structure.bCEulerAngles;
import de.george.g3utils.structure.bCQuaternion;
import de.george.g3utils.structure.bCVector;
import de.george.g3utils.util.Misc;
import de.george.lrentnode.classes.eCVegetation_Mesh.eSVegetationMeshID;
import de.george.lrentnode.classes.eCVegetation_PS;
import de.george.lrentnode.classes.eCVegetation_PS.PlantRegionEntry;
import net.miginfocom.swing.MigLayout;

public class CreatePlantDialog extends ExtStandardDialog {
	private static final Logger logger = LoggerFactory.getLogger(CreatePlantDialog.class);

	private JTextField tfX, tfY, tfZ, tfPitch, tfYaw, tfRoll, tfScaleWidth, tfScaleHeight;
	private MeshListComboBox cbMesh;
	private ColorChooserButton ccbColor;

	private JTextArea taRawText;
	private String initialTextBoxContent;
	private Color defaultColor;

	private PlantRegionEntry createdEntry;

	public CreatePlantDialog(Window owner, String title, eCVegetation_PS vegetationPS, eSVegetationMeshID lastSelectedMeshID,
			String initialTextBoxContent, Color defaultColor) {
		this(owner, title, vegetationPS, lastSelectedMeshID, defaultColor);
		this.initialTextBoxContent = initialTextBoxContent;
	}

	public CreatePlantDialog(Window owner, String title, eCVegetation_PS vegetationPS, eSVegetationMeshID lastSelectedMeshID,
			Color defaultColor) {
		super(owner, title, true);
		setType(Type.UTILITY);
		setSize(400, 400);
		setResizable(true);

		this.defaultColor = defaultColor != null ? defaultColor : Color.WHITE;
		this.cbMesh = new MeshListComboBox(vegetationPS, lastSelectedMeshID);
	}

	@Override
	public JComponent createContentPanel() {
		JPanel mainPanel = new JPanel(new MigLayout("fill", "[][][]"));
		add(mainPanel, BorderLayout.CENTER);

		mainPanel.add(new JLabel(I.trc("coordinate", "X")));
		mainPanel.add(new JLabel(I.trc("coordinate", "Y")));
		mainPanel.add(new JLabel(I.trc("coordinate", "Z")), "wrap");

		tfX = SwingUtils.createUndoTF();
		mainPanel.add(tfX, "width 100:150:200");

		tfY = SwingUtils.createUndoTF();
		mainPanel.add(tfY, "width 100:150:200");

		tfZ = SwingUtils.createUndoTF();
		mainPanel.add(tfZ, "width 100:150:200, wrap");

		mainPanel.add(new JLabel(I.tr("Pitch")));
		mainPanel.add(new JLabel(I.tr("Yaw")));
		mainPanel.add(new JLabel(I.tr("Roll")), "wrap");

		tfPitch = SwingUtils.createUndoTF();
		mainPanel.add(tfPitch, "width 100:150:200");

		tfYaw = SwingUtils.createUndoTF();
		mainPanel.add(tfYaw, "width 100:150:200");

		tfRoll = SwingUtils.createUndoTF();
		mainPanel.add(tfRoll, "width 100:150:200, wrap");

		mainPanel.add(new JLabel(I.tr("Scale Width")));
		mainPanel.add(new JLabel(I.tr("Scale Height")));
		mainPanel.add(new JLabel(I.tr("Color")), "wrap");

		tfScaleWidth = SwingUtils.createUndoTF();
		mainPanel.add(tfScaleWidth, "width 100:150:200");

		tfScaleHeight = SwingUtils.createUndoTF();
		mainPanel.add(tfScaleHeight, "width 100:150:200");

		ccbColor = new ColorChooserButton(getOwner(), false);
		mainPanel.add(ccbColor, "height " + tfScaleHeight.getPreferredSize().height + "! ,width 100:150:200, wrap");

		mainPanel.add(new JLabel(I.tr("Mesh")), "wrap");
		mainPanel.add(cbMesh.getComboBox(), "spanx 3, width 200:300:400, wrap");

		taRawText = new JTextArea();
		taRawText.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		mainPanel.add(new JScrollPane(taRawText), "spanx 4, pushy, gaptop 7, grow, wrap");
		taRawText.getDocument().addDocumentListener(SwingUtils.createDocumentListener(this::parseTextArea));

		if (initialTextBoxContent != null && !initialTextBoxContent.isEmpty()) {
			taRawText.setText(initialTextBoxContent);
			initialTextBoxContent = null;
		}

		return mainPanel;
	}

	private void parseTextArea() {
		String text = taRawText.getText();

		bCVector parsedPosition = Misc.stringToPosition(text);
		if (parsedPosition == null) {
			parsedPosition = new bCVector(0, 0, 0);
		}

		tfX.setText(Misc.formatFloat(parsedPosition.getX()));
		tfY.setText(Misc.formatFloat(parsedPosition.getY()));
		tfZ.setText(Misc.formatFloat(parsedPosition.getZ()));

		bCEulerAngles parsedRotation = Misc.stringToRotation(text);
		tfPitch.setText(Misc.formatFloat(parsedRotation.getPitchDeg()));
		tfYaw.setText(Misc.formatFloat(parsedRotation.getYawDeg()));
		tfRoll.setText(Misc.formatFloat(parsedRotation.getRollDeg()));

		tfScaleWidth.setText(Misc.formatFloat(Misc.stringToPrefixedValue(text, 1, "scalewidth", "scalew", "swidth", "sw", "width")));
		tfScaleHeight.setText(Misc.formatFloat(Misc.stringToPrefixedValue(text, 1, "scaleheight", "scaleh", "sheight", "sh", "height")));

		ccbColor.setSelectedColor(Optional.ofNullable(Misc.stringToColor(text)).orElseGet(() -> defaultColor));
	}

	@Override
	public ButtonPanel createButtonPanel() {
		ButtonPanel buttonPanel = newButtonPanel();

		Action okAction = SwingUtils.createAction(I.tr("Ok"), () -> {
			try {
				bCVector pos = new bCVector(Float.parseFloat(tfX.getText()), Float.parseFloat(tfY.getText()),
						Float.parseFloat(tfZ.getText()));
				float pitch = Float.parseFloat(tfPitch.getText());
				float yaw = Float.parseFloat(tfYaw.getText());
				float roll = Float.parseFloat(tfRoll.getText());
				float scaleWidth = Float.parseFloat(tfScaleWidth.getText());
				float scaleHeight = Float.parseFloat(tfScaleHeight.getText());
				eSVegetationMeshID meshID = cbMesh.getSelectedMeshID();
				bCQuaternion rotation = new bCQuaternion(bCEulerAngles.fromDegree(yaw, pitch, roll));
				createdEntry = new PlantRegionEntry(meshID, pos, rotation, scaleWidth, scaleHeight, ccbColor.getSelectedColor().getRGB());
				dispose();
				setDialogResult(RESULT_AFFIRMED);
			} catch (NumberFormatException e) {
				logger.info("Malformed vegetation object data: {}", e.getMessage());
				TaskDialogs.error(CreatePlantDialog.this, I.tr("Malformed vegetation object data"), e.getMessage());
			}
		});

		addButton(buttonPanel, okAction, ButtonPanel.AFFIRMATIVE_BUTTON);
		addDefaultCancelButton(buttonPanel);

		return buttonPanel;
	}

	public eSVegetationMeshID getLastSelectedMeshID() {
		return cbMesh.getSelectedMeshID();
	}

	public PlantRegionEntry getCreatedEntry() {
		return createdEntry;
	}
}

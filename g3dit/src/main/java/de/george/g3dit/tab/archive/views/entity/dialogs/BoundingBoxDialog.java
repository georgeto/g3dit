package de.george.g3dit.tab.archive.views.entity.dialogs;

import java.awt.Window;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.jidesoft.dialog.ButtonPanel;
import com.teamunify.i18n.I;

import de.george.g3dit.gui.dialogs.ExtStandardDialog;
import de.george.g3utils.gui.SwingUtils;
import de.george.g3utils.structure.bCBox;
import de.george.g3utils.structure.bCVector;
import de.george.g3utils.util.Misc;
import net.miginfocom.swing.MigLayout;

public class BoundingBoxDialog extends ExtStandardDialog {
	private JTextField tfMinX, tfMinY, tfMinZ, tfMaxX, tfMaxY, tfMaxZ;

	private bCBox box;

	public BoundingBoxDialog(Window owner, bCBox box) {
		super(owner, I.tr("BoundingBox ändern"), true);
		this.box = box.clone();

		setType(Type.UTILITY);
		setResizable(false);
		setSize(300, 160);
	}

	@Override
	public JComponent createContentPanel() {
		JPanel mainPanel = new JPanel(new MigLayout("fill", "[sg, fill, grow]10[sg, fill, grow]10[sg, fill, grow]", "[]1[]8[]1[]"));

		mainPanel.add(new JLabel(I.tr("min-x")));
		mainPanel.add(new JLabel(I.tr("min-y")));
		mainPanel.add(new JLabel(I.tr("min-z")), "wrap");

		tfMinX = SwingUtils.createUndoTF();
		mainPanel.add(tfMinX);
		tfMinY = SwingUtils.createUndoTF();
		mainPanel.add(tfMinY);
		tfMinZ = SwingUtils.createUndoTF();
		mainPanel.add(tfMinZ, "wrap");

		mainPanel.add(new JLabel(I.tr("max-x")));
		mainPanel.add(new JLabel(I.tr("max-y")));
		mainPanel.add(new JLabel(I.tr("max-z")), "wrap");

		tfMaxX = SwingUtils.createUndoTF();
		mainPanel.add(tfMaxX);
		tfMaxY = SwingUtils.createUndoTF();
		mainPanel.add(tfMaxY);
		tfMaxZ = SwingUtils.createUndoTF();
		mainPanel.add(tfMaxZ, "");

		loadPosition();
		return mainPanel;
	}

	@Override
	public ButtonPanel createButtonPanel() {
		ButtonPanel buttonPanel = newButtonPanel();
		buttonPanel.setAlignment(SwingConstants.CENTER);
		buttonPanel.setMinButtonWidth(80);

		Action saveAction = SwingUtils.createAction(I.tr("Speichern"), () -> {
			try {
				box.setMin(new bCVector(Float.parseFloat(tfMinX.getText()), Float.parseFloat(tfMinY.getText()),
						Float.parseFloat(tfMinZ.getText())));
				box.setMax(new bCVector(Float.parseFloat(tfMaxX.getText()), Float.parseFloat(tfMaxY.getText()),
						Float.parseFloat(tfMaxZ.getText())));

				affirm();
			} catch (NumberFormatException e1) {
				e1.printStackTrace();
			}
		});

		addButton(buttonPanel, saveAction, ButtonPanel.AFFIRMATIVE_BUTTON);
		addDefaultCancelButton(buttonPanel);

		return buttonPanel;
	}

	private void loadPosition() {
		// Daten eintragen
		tfMinX.setText(Misc.formatFloat(box.getMin().getX()));
		tfMinY.setText(Misc.formatFloat(box.getMin().getY()));
		tfMinZ.setText(Misc.formatFloat(box.getMin().getZ()));
		tfMaxX.setText(Misc.formatFloat(box.getMax().getX()));
		tfMaxY.setText(Misc.formatFloat(box.getMax().getY()));
		tfMaxZ.setText(Misc.formatFloat(box.getMax().getZ()));
	}

	public bCBox getBox() {
		return box;
	}
}

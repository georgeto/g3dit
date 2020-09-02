package de.george.g3dit.gui.dialogs;

import java.awt.Window;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import com.teamunify.i18n.I;

import de.george.g3utils.gui.SwingUtils;
import de.george.g3utils.util.Converter;
import de.george.g3utils.util.Misc;

public class FloatCalcDialog extends JDialog {
	private JTextField tfFloatIn;
	private JTextField tfHexOut;
	private JTextField tfHexIn;
	private JTextField tfFloatOut;

	public FloatCalcDialog(Window owner) {
		super(owner);
		setType(Type.UTILITY);
		setResizable(false);
		setTitle(I.tr("Float <-> Hex"));
		setSize(230, 130);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		getContentPane().setLayout(null);

		JLabel lblNewLabel = new JLabel(I.tr("Float"));
		lblNewLabel.setBounds(10, 11, 46, 14);
		getContentPane().add(lblNewLabel);

		tfFloatIn = SwingUtils.createUndoTF();
		tfFloatIn.setBounds(10, 26, 86, 20);
		getContentPane().add(tfFloatIn);
		tfFloatIn.setColumns(10);
		tfFloatIn.getDocument().addDocumentListener(SwingUtils.createDocumentListener(this::floatToHex));

		JLabel lblNewLabel_1 = new JLabel("->");
		lblNewLabel_1.setBounds(102, 29, 14, 14);
		getContentPane().add(lblNewLabel_1);

		JLabel lblNewLabel_2 = new JLabel(I.tr("Hex"));
		lblNewLabel_2.setBounds(120, 11, 46, 14);
		getContentPane().add(lblNewLabel_2);

		tfHexOut = new JTextField();
		tfHexOut.setEditable(false);
		tfHexOut.setColumns(10);
		tfHexOut.setBounds(120, 26, 86, 20);
		getContentPane().add(tfHexOut);

		tfHexIn = SwingUtils.createUndoTF();
		tfHexIn.setColumns(10);
		tfHexIn.setBounds(10, 72, 86, 20);
		getContentPane().add(tfHexIn);
		tfHexIn.getDocument().addDocumentListener(SwingUtils.createDocumentListener(this::hexToFloat));

		JLabel lblFloat = new JLabel(I.tr("Float"));
		lblFloat.setBounds(120, 57, 46, 14);
		getContentPane().add(lblFloat);

		JLabel label_1 = new JLabel("->");
		label_1.setBounds(102, 75, 14, 14);
		getContentPane().add(label_1);

		tfFloatOut = new JTextField();
		tfFloatOut.setEditable(false);
		tfFloatOut.setColumns(10);
		tfFloatOut.setBounds(120, 72, 86, 20);
		getContentPane().add(tfFloatOut);

		JLabel lblHex = new JLabel(I.tr("Hex"));
		lblHex.setBounds(10, 57, 46, 14);
		getContentPane().add(lblHex);
	}

	private void floatToHex() {
		try {
			float tmp = Float.parseFloat(tfFloatIn.getText());
			tfHexOut.setText(Converter.FloatToHexLittle(tmp));
		} catch (NumberFormatException e1) {
			tfHexOut.setText(null);
		}
	}

	private void hexToFloat() {
		String tmp = tfHexIn.getText();
		if (tmp.length() == 8 && Misc.isValidHex(tmp)) {
			tfFloatOut.setText(String.valueOf(Converter.HexLittleToFloat(tmp)));
		} else {
			tfFloatOut.setText(null);
		}
	}
}

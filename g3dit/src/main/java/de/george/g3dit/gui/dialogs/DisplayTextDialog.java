package de.george.g3dit.gui.dialogs;

import java.awt.Window;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.border.EtchedBorder;

import de.george.g3dit.gui.components.JTextAreaExt;
import de.george.g3dit.gui.components.TextLineNumber;

public class DisplayTextDialog extends ExtStandardDialog {
	private String text;
	private JTextAreaExt area;

	public DisplayTextDialog(String title, String text, Window owner, boolean modal) {
		super(owner, title, modal);
		this.text = text;
		setSize(700, 500);
	}

	@Override
	public JComponent createContentPanel() {
		area = new JTextAreaExt();
		area.getScrollPane().setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
		TextLineNumber tln = new TextLineNumber(area);
		area.getScrollPane().setRowHeaderView(tln);
		area.setEditable(false);
		area.setText(text);
		return area.getScrollPane();
	}

	public void append(String data) {
		area.append(data);
	}

	public void clear() {
		area.setText("");
	}
}

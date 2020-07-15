package de.george.g3dit.gui.dialogs;

import java.awt.Window;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.text.html.HTMLEditorKit;

public class DisplayHtmlDialog extends ExtStandardDialog {
	private String html;

	public DisplayHtmlDialog(String title, String html, Window owner, int width, int height, boolean modal) {
		super(owner, title, modal);
		this.html = html;
		setSize(width, height);
	}

	@Override
	public JComponent createContentPanel() {
		JEditorPane ep = new JEditorPane();
		ep.setEditorKit(new HTMLEditorKit());
		ep.setEditable(false);
		ep.setText(html);
		return new JScrollPane(ep);
	}
}

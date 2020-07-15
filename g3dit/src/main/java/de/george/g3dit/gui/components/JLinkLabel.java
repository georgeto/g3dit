package de.george.g3dit.gui.components;

import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;

import javax.swing.JLabel;

public class JLinkLabel extends JLabel {
	public JLinkLabel(final String link) {
		this(link, link);
	}

	public JLinkLabel(final String name, final String link) {
		super("<HTML><FONT color=\"#0000FF\"><U>" + name + "</U></FONT>");
		setCursor(new Cursor(Cursor.HAND_CURSOR));
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(java.awt.event.MouseEvent evt) {
				setText("<HTML><FONT color=\"#FF0000\"><U>" + name + "</U></FONT>");
			}

			@Override
			public void mouseExited(java.awt.event.MouseEvent evt) {
				setText("<HTML><FONT color=\"#0000FF\"><U>" + name + "</U></FONT>");
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				if (Desktop.isDesktopSupported()) {
					try {
						Desktop.getDesktop().browse(new URI(link));
						setText("<HTML><FONT color=\"#0000FF\"><U>" + name + "</U></FONT>");
					} catch (Throwable t) {
						t.printStackTrace();
					}
				}
			}
		});
	}
}

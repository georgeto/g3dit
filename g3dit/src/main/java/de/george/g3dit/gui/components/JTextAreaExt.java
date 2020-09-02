package de.george.g3dit.gui.components;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import com.teamunify.i18n.I;

public class JTextAreaExt extends JTextArea {

	private boolean clearable;
	private JScrollPane scroll;

	public JTextAreaExt() {
		this(false);
	}

	public JTextAreaExt(boolean clearable) {
		super();

		scroll = new JScrollPane(this);

		this.clearable = clearable;
		initRightClickMenu();
	}

	private void initRightClickMenu() {
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e)) {
					JPopupMenu menu = new JPopupMenu();

					// Kopieren
					JMenuItem miCopy = new JMenuItem(I.tr("Kopieren"));
					menu.add(miCopy);
					miCopy.addActionListener(e1 -> copy());

					if (isEditable()) {
						// Ausschneiden
						JMenuItem miCut = new JMenuItem(I.tr("Ausschneiden"));
						menu.add(miCut);
						miCut.addActionListener(e1 -> cut());

						// Einfügen
						JMenuItem miPaste = new JMenuItem(I.tr("Einfügen"));
						menu.add(miPaste);
						miPaste.addActionListener(e1 -> paste());
					}

					if (clearable) {
						// Leeren
						JMenuItem miClear = new JMenuItem(I.tr("Inhalt löschen"));
						menu.add(miClear);
						miClear.addActionListener(e1 -> setText(null));
					}
					menu.show(JTextAreaExt.this, e.getX(), e.getY());
				}

			}
		});
	}

	public JScrollPane getScrollPane() {
		return scroll;
	}
}

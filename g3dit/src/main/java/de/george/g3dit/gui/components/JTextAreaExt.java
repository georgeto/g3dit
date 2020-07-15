package de.george.g3dit.gui.components;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

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
					JMenuItem miCopy = new JMenuItem("Kopieren");
					menu.add(miCopy);
					miCopy.addActionListener(e1 -> copy());

					if (isEditable()) {
						// Ausschneiden
						JMenuItem miCut = new JMenuItem("Ausschneiden");
						menu.add(miCut);
						miCut.addActionListener(e1 -> cut());

						// Einfügen
						JMenuItem miPaste = new JMenuItem("Einfügen");
						menu.add(miPaste);
						miPaste.addActionListener(e1 -> paste());
					}

					if (clearable) {
						// Leeren
						JMenuItem miClear = new JMenuItem("Inhalt löschen");
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

package de.george.g3dit.gui.components;

import java.awt.Component;

import javax.swing.JMenuItem;

import com.jidesoft.swing.JidePopupMenu;

/**
 * Reevalutes isEnabled() of every JMenuItem on show.
 */
public class JDynamicPopupMenu extends JidePopupMenu {
	@Override
	public void show(Component invoker, int x, int y) {
		for (Component comp : getComponents()) {
			if (comp instanceof JMenuItem) {
				// Trigger fireStateChanged so that isEnabled() is checked
				JMenuItem mi = (JMenuItem) comp;
				mi.setArmed(!mi.isArmed());
				mi.setArmed(!mi.isArmed());
			}
		}

		super.show(invoker, x, y);
	}
}

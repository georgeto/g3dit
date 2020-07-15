package de.george.g3dit.gui.components;

import javax.swing.JComponent;

public interface Keystroker {
	void registerKeyStrokes(JComponent container);

	void unregisterKeyStrokes(JComponent container);
}

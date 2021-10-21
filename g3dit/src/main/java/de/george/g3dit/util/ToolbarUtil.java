package de.george.g3dit.util;

import javax.swing.JToolBar;
import javax.swing.SwingConstants;

public class ToolbarUtil {
	public static JToolBar createTopToolbar() {
		JToolBar toolBar = new JToolBar(SwingConstants.HORIZONTAL);
		toolBar.setFloatable(false);
		return toolBar;
	}
}

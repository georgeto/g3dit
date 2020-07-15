package de.george.g3dit.util;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import com.jidesoft.plaf.UIDefaultsLookup;
import com.jidesoft.swing.PartialGradientLineBorder;
import com.jidesoft.swing.PartialSide;

public class ToolbarUtil {
	public static JToolBar createTopToolbar() {
		JToolBar toolBar = new JToolBar(SwingConstants.HORIZONTAL);
		toolBar.setFloatable(false);
		toolBar.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 0, 3, 0), new PartialGradientLineBorder(
						new Color[] {new Color(0, 0, 128), UIDefaultsLookup.getColor("control")}, 2, PartialSide.SOUTH)),
				BorderFactory.createEmptyBorder(0, 0, 2, 0)));
		return toolBar;
	}
}

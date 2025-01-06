package de.george.g3dit.util;

import com.teamunify.i18n.I;

import de.george.g3utils.gui.SwingUtils;

public class Tooltips {
	private Tooltips() {}

	public static String changeTime() {
		return SwingUtils.wrapTooltipText(I.tr(
				"Entities store the Guid and ChangeTime of the template they were created from. If the template's ChangeTime is updated in a patch, modified properties from the template are applied to the entity when a game is started or loaded."),
				80);
	}
}

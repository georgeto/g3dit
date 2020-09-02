package de.george.g3dit.util;

import java.awt.Window;

import com.ezware.dialog.task.CommandLink;
import com.ezware.dialog.task.TaskDialogs;
import com.teamunify.i18n.I;

public class Dialogs {
	public enum Answer {
		Yes,
		No,
		Cancel
	}

	public static Answer askSaveChanges(Window parent, String message) {
		int answer = TaskDialogs.choice(parent, I.tr("Änderungen speichern"), message, 0, new CommandLink(I.tr("Ja"), ""),
				new CommandLink(I.tr("Nein"), ""), new CommandLink(I.tr("Abbrechen"), ""));

		return switch (answer) {
			case 0 -> Answer.Yes;
			case 1 -> Answer.No;
			case 2, -1 -> Answer.Cancel;
			default -> throw new IllegalStateException();
		};
	}
}

package de.george.g3dit.util;

import java.awt.Window;
import java.util.ArrayList;
import java.util.List;

import com.ezware.dialog.task.CommandLink;
import com.ezware.dialog.task.TaskDialogs;
import com.teamunify.i18n.I;

public class Dialogs {
	public enum Answer {
		Yes,
		No,
		Cancel,
		AllNo,
		AllYes,
	}

	public static Answer askSaveChanges(Window parent, String message, boolean all) {
		List<CommandLink> links = new ArrayList<>();
		links.add(new CommandLink(I.tr("Yes"), ""));
		links.add(new CommandLink(I.tr("No"), ""));
		links.add(new CommandLink(I.tr("Cancel"), ""));
		if (all) {
			links.add(new CommandLink(I.tr("All No"), ""));
			links.add(new CommandLink(I.tr("All Yes"), ""));
		}
		int answer = TaskDialogs.choice(parent, I.tr("Apply changes"), message, 0, links);

		return switch (answer) {
			case 0 -> Answer.Yes;
			case 1 -> Answer.No;
			case 2, -1 -> Answer.Cancel;
			case 3 -> Answer.AllNo;
			case 4 -> Answer.AllYes;
			default -> throw new IllegalStateException();
		};
	}

	public static Answer askSaveChanges(Window parent, String message) {
		return askSaveChanges(parent, message, false);
	}
}

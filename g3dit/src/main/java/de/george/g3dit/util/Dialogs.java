package de.george.g3dit.util;

import java.awt.Window;

import com.ezware.dialog.task.CommandLink;
import com.ezware.dialog.task.TaskDialogs;

public class Dialogs {
	public enum Answer {
		Yes,
		No,
		Cancel;
	}

	public static Answer askSaveChanges(Window parent, String message) {
		int answer = TaskDialogs.choice(parent, "Änderungen speichern", message, 0, new CommandLink("Ja", ""), new CommandLink("Nein", ""),
				new CommandLink("Abbrechen", ""));

		switch (answer) {
			case 0:
				return Answer.Yes;
			case 1:
				return Answer.No;
			case 2:
			case -1:
				return Answer.Cancel;
			default:
				throw new IllegalStateException();
		}
	}
}

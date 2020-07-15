package de.george.g3dit.gui.dialogs;

import java.awt.Window;
import java.util.List;

public abstract class AbstractSelectDialog<T> extends ExtStandardDialog {

	public static final int SELECTION_SINGLE = 0;
	public static final int SELECTION_MULTIPLE = 2;

	public AbstractSelectDialog(Window owner, String title) {
		super(owner, title, true);
	}

	public abstract List<T> getSelectedEntries();

}

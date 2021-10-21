package de.george.g3utils.gui;

import javax.swing.border.Border;

public class NoBorderUndoableTextField extends UndoableTextField {
	public NoBorderUndoableTextField() {}

	public NoBorderUndoableTextField(String text) {
		super(text);
	}

	@Override
	public void setBorder(Border border) {
		// No border!
	}
}

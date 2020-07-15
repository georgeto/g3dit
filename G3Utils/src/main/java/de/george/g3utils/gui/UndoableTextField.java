package de.george.g3utils.gui;

import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.UndoableEditEvent;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoManager;

public class UndoableTextField extends JTextField {
	private UndoManager undo;
	private boolean keepHistoryByDefault;

	public UndoableTextField() {
		this(null);
	}

	public UndoableTextField(String text) {
		setDocument(new CustomUndoPlainDocument());
		setText(text);
		setupUndoManager();
	}

	public void keepHistoryByDefault() {
		keepHistoryByDefault = true;
	}

	private void setupUndoManager() {
		undo = new UndoManager();
		Document doc = getDocument();

		// Listen for undo and redo events
		doc.addUndoableEditListener(evt -> undo.addEdit(evt.getEdit()));

		// Create an undo action and add it to the text component
		getActionMap().put("Undo", SwingUtils.createAction("Undo", () -> {
			try {
				if (undo.canUndo()) {
					undo.undo();
				}
			} catch (CannotUndoException e) {
			}
		}));

		// Bind the undo action to ctl-Z
		this.getInputMap().put(KeyStroke.getKeyStroke("control Z"), "Undo");

		// Create a redo action and add it to the text component
		getActionMap().put("Redo", SwingUtils.createAction("Redo", () -> {
			try {
				if (undo.canRedo()) {
					undo.redo();
				}
			} catch (CannotRedoException e) {
			}
		}));

		// Bind the redo action to ctl-Y
		this.getInputMap().put(KeyStroke.getKeyStroke("control Y"), "Redo");
	}

	@Override
	public void setText(String t) {
		super.setText(t);
		if (!keepHistoryByDefault) {
			clearHistory();
		}
	}

	public void setText(String t, boolean keepHistory) {
		super.setText(t);
		if (!keepHistory) {
			clearHistory();
		}
	}

	public boolean hasChanged() {
		return undo.canUndo();
	}

	public void clearHistory() {
		if (undo != null) {
			undo.discardAllEdits();
		}
	}

	class CustomUndoPlainDocument extends PlainDocument {
		private CompoundEdit compoundEdit;

		@Override
		protected void fireUndoableEditUpdate(UndoableEditEvent e) {
			if (compoundEdit == null) {
				super.fireUndoableEditUpdate(e);
			} else {
				compoundEdit.addEdit(e.getEdit());
			}
		}

		@Override
		public void replace(int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
			if (length == 0) {
				super.replace(offset, length, text, attrs);
			} else {
				compoundEdit = new CompoundEdit();
				super.fireUndoableEditUpdate(new UndoableEditEvent(this, compoundEdit));
				super.replace(offset, length, text, attrs);
				compoundEdit.end();
				compoundEdit = null;
			}
		}
	}
}

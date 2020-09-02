package de.george.g3dit.tab.archive.views.property;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import com.jidesoft.dialog.ButtonPanel;
import com.l2fprod.common.beans.editor.AbstractPropertyEditor;
import com.teamunify.i18n.I;

import de.george.g3dit.gui.dialogs.ExtStandardDialog;
import de.george.g3dit.tab.archive.views.property.JsonPropertyValueConverter.JsonStringWrapper;
import de.george.g3utils.gui.SwingUtils;
import net.miginfocom.swing.MigLayout;

public class JsonStringPropertyEditor extends AbstractPropertyEditor {
	private JTextField editorComponent;
	private PopupDialog popup;
	private String currentText;

	public JsonStringPropertyEditor() {
		editor = editorComponent = new JTextField();
		popup = new PopupDialog();
		popup.initialize();
	}

	@Override
	public Component getCustomEditor() {
		SwingUtilities.invokeLater(() -> {
			popup.setText(currentText);
			// Point p = editorComponent.getLocationOnScreen();
			// popup.setLocation(p.x, p.y + editorComponent.getSize().height);
			popup.open();
		});

		return super.getCustomEditor();
	}

	@Override
	public Object getValue() {
		return new JsonStringWrapper(currentText);
	}

	@Override
	public void setValue(Object value) {
		if (value == null) {
			currentText = "";
		} else {
			currentText = String.valueOf(value);
		}
		editorComponent.setText(currentText);
	}

	private class PopupDialog extends ExtStandardDialog {
		private JTextArea textArea;

		public PopupDialog() {
			super((Frame) null, I.tr("Wert bearbeiten"), true);
			setType(Type.UTILITY);
			setResizable(true);
			setSize(400, 475);
		}

		@Override
		public JComponent createContentPanel() {
			JPanel mainPanel = new JPanel(new MigLayout("fill", "[fill, grow]", "[fill, grow]"));

			textArea = new JTextArea();
			textArea.setLineWrap(false);
			textArea.setWrapStyleWord(false);
			mainPanel.add(new JScrollPane(textArea));

			return mainPanel;
		}

		@Override
		public ButtonPanel createButtonPanel() {
			ButtonPanel buttonPanel = newButtonPanel();

			Action saveAction = SwingUtils.createAction(I.tr("Ok"), () -> {
				String oldValue = editorComponent.getText();
				currentText = textArea.getText();
				editorComponent.setText(currentText);
				JsonStringPropertyEditor.this.firePropertyChange(oldValue, currentText);
				affirm();
			});

			Action cancelAction = SwingUtils.createAction(I.tr("Abbrechen"), () -> {
				// Hacky workaround to cancel cell editing
				Object al = editorComponent.getInputMap(JComponent.WHEN_FOCUSED).get(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
				if (al instanceof ActionListener) {
					((ActionListener) al).actionPerformed(null);
				}

				cancel();
			});

			addButton(buttonPanel, saveAction, ButtonPanel.AFFIRMATIVE_BUTTON);
			addButton(buttonPanel, cancelAction, ButtonPanel.CANCEL_BUTTON);

			return buttonPanel;
		}

		@Override
		public void setVisible(boolean visible) {
			super.setVisible(visible);
			if (visible) {
				textArea.requestFocusInWindow();
			}
		}

		public void setText(String text) {
			textArea.setText(text);
		}
	}
}

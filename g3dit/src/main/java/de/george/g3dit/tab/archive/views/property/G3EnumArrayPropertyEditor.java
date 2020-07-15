package de.george.g3dit.tab.archive.views.property;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import com.jidesoft.dialog.ButtonPanel;
import com.l2fprod.common.beans.editor.AbstractPropertyEditor;

import de.george.g3dit.gui.components.JEnumComboBox;
import de.george.g3dit.gui.dialogs.ExtStandardDialog;
import de.george.g3dit.tab.shared.AbstractElementPanel;
import de.george.g3dit.tab.shared.AbstractElementsPanel;
import de.george.g3utils.gui.SwingUtils;
import de.george.lrentnode.enums.G3Enums.G3Enum;
import net.miginfocom.swing.MigLayout;

public class G3EnumArrayPropertyEditor extends AbstractPropertyEditor {
	private JTextField editorComponent;
	private PopupDialog popup;

	public G3EnumArrayPropertyEditor() {
		editor = editorComponent = new JTextField();
		popup = new PopupDialog();
		popup.initialize();
	}

	@Override
	public Component getCustomEditor() {
		SwingUtilities.invokeLater(() -> {
			// Point p = editorComponent.getLocationOnScreen();
			// popup.setLocation(p.x, p.y + editorComponent.getSize().height);
			popup.open();
		});

		return super.getCustomEditor();
	}

	@Override
	public Object getValue() {
		return popup.getWrapper();
	}

	@Override
	public void setValue(Object value) {
		popup.setWrapper((G3EnumArrayWrapper) value);
	}

	private class PopupDialog extends ExtStandardDialog {
		private G3EnumArrayWrapper oldValue;
		private EnumsPanel enumsPanel;

		public PopupDialog() {
			super((Frame) null, "Wert bearbeiten", true);
			setType(Type.UTILITY);
			setResizable(true);
			setSize(400, 475);
		}

		@Override
		public JComponent createContentPanel() {
			JPanel mainPanel = new JPanel(new MigLayout("fill", "[fill, grow]", "[fill, grow]"));

			JScrollPane scrollPane = new JScrollPane();
			enumsPanel = new EnumsPanel(scrollPane);
			scrollPane.setViewportView(enumsPanel);
			mainPanel.add(scrollPane);

			return mainPanel;
		}

		@Override
		public ButtonPanel createButtonPanel() {
			ButtonPanel buttonPanel = newButtonPanel();

			Action saveAction = SwingUtils.createAction("Ok", () -> {
				G3EnumArrayPropertyEditor.this.firePropertyChange(oldValue, getWrapper());
				affirm();
			});

			Action cancelAction = SwingUtils.createAction("Abbrechen", () -> {
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

		public G3EnumArrayWrapper getWrapper() {
			return enumsPanel.getWrapper();
		}

		public void setWrapper(G3EnumArrayWrapper wrapper) {
			oldValue = wrapper;
			enumsPanel.loadValues(wrapper);
		}
	}

	private class EnumsPanel extends AbstractElementsPanel<G3EnumArrayWrapper> {
		private Class<? extends G3Enum> enumType;

		public EnumsPanel(JScrollPane navScroll) {
			super("Value", navScroll, true);
		}

		@Override
		public void loadValuesInternal(G3EnumArrayWrapper wrapper) {
			enumType = wrapper.getEnumClass();

			for (int enumValue : wrapper.getEnumValues()) {
				insertElementRelative(new EnumPanel(enumValue, enumType, this), null, InsertPosition.After);
			}
		}

		@Override
		public void saveValuesInternal(G3EnumArrayWrapper wrapper) {
			List<Integer> enumValues = wrapper.getEnumValues();
			enumValues.clear();
			for (int i = 0; i < getComponentCount(); i++) {
				EnumPanel stack = (EnumPanel) getComponent(i);
				enumValues.add(stack.getEnumValue());
			}
		}

		@Override
		protected void removeValuesInternal(G3EnumArrayWrapper wrapper) {
			wrapper.getEnumValues().clear();
		}

		@Override
		protected AbstractElementPanel getNewElement() {
			return new EnumPanel(0, enumType, this);
		}

		public G3EnumArrayWrapper getWrapper() {
			G3EnumArrayWrapper wrapper = new G3EnumArrayWrapper(new ArrayList<>(), enumType);
			saveValues(wrapper);
			return wrapper;
		}
	}

	private class EnumPanel extends AbstractElementPanel {
		private JEnumComboBox<?> cbValue;

		public EnumPanel(int enumValue, Class<? extends G3Enum> enumType, EnumsPanel callback) {
			super("Value", callback);
			setLayout(new MigLayout("fillx", "[grow]10px[]push[]"));

			// add(new JLabel("Typ"), "cell 0 1");

			cbValue = new JEnumComboBox<>(enumType, false);
			cbValue.setSelectedValue(enumValue);
			add(cbValue);

			JPanel operationPanel = getOperationPanel();
			add(operationPanel, "cell 2 0, spanx 2, spany 3");
		}

		public int getEnumValue() {
			return cbValue.getSelectedValue();
		}

		@Override
		protected String getBorderTitle() {
			return position + ". Value";
		}
	}
}

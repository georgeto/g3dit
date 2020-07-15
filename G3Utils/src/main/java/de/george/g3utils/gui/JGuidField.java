package de.george.g3utils.gui;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;

import org.netbeans.validation.api.AbstractValidator;
import org.netbeans.validation.api.ui.ValidationGroup;

import de.george.g3utils.structure.GuidUtil;
import de.george.g3utils.validation.ValidationGroupWrapper;
import net.miginfocom.swing.MigLayout;

public class JGuidField extends JPanel {
	private CopyOnWriteArrayList<Consumer<String>> listeners;

	private UndoableTextField tfGuid;
	protected JTextField tfGuidText;

	private boolean editing = false;

	public JGuidField() {
		this(null);
	}

	public JGuidField(String text) {
		listeners = new CopyOnWriteArrayList<>();
		setLayout(new MigLayout("insets 0", "[]", "[]0[]"));

		tfGuid = SwingUtils.createUndoTF();
		SwingUtils.monospaceFont(tfGuid);
		add(tfGuid, "width 100%, height 19:19:19, wrap");
		tfGuid.getDocument().addDocumentListener(SwingUtils.createDocumentListener(() -> updateText()));

		tfGuidText = new JTextField();
		add(tfGuidText, "width 100%, height 19:19:19");
		tfGuidText.setEditable(false);

		setText(text);
	}

	public void setText(String guid) {
		setText(guid, false);
	}

	public void setText(String guid, boolean keepHistory) {
		editing = true;

		tfGuid.setText(guid, keepHistory);
		tfGuidText.setText(GuidUtil.hexToPlain(guid));

		editing = false;
		fireGuidFieldChanged();
	}

	public String getText() {
		return tfGuid.getText();
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public void initValidation(ValidationGroup group, String fieldName, AbstractValidator... validators) {
		tfGuid.setName(fieldName);
		group.add(tfGuid, validators);
	}

	public void removeValidation(ValidationGroupWrapper group) {
		group.remove(tfGuid);
	}

	private void updateText() {
		if (editing) {
			return;
		}
		editing = true;
		if (tfGuid.getText().isEmpty()) {
			tfGuidText.setText(null);
		} else {
			String guid = GuidUtil.parseGuid(tfGuid.getText());
			if (guid != null) {
				tfGuidText.setText(GuidUtil.hexToPlain(guid));
			}

		}
		fireGuidFieldChanged();
		editing = false;
	}

	public void addGuidFiedListener(Consumer<String> listener) {
		listeners.add(listener);
	}

	public void removeGuidFiedListener(Consumer<String> listener) {
		listeners.remove(listener);
	}

	protected void fireGuidFieldChanged() {
		for (Consumer<String> listener : listeners) {
			listener.accept(GuidUtil.parseGuidPartial(tfGuid.getText()));
		}
	}

	public void setEditable(boolean editable) {
		tfGuid.setEditable(editable);
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		tfGuid.setEnabled(enabled);
		tfGuidText.setEnabled(enabled);
	}

	@Override
	public void setComponentPopupMenu(JPopupMenu popup) {
		super.setComponentPopupMenu(popup);
		tfGuid.setComponentPopupMenu(popup);
		tfGuidText.setComponentPopupMenu(popup);
	}
}

package de.george.g3dit.gui.components;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.Document;

import org.netbeans.validation.api.AbstractValidator;
import org.netbeans.validation.api.ui.ValidationGroup;

import de.george.g3dit.EditorContext;
import de.george.g3dit.cache.Caches;
import de.george.g3dit.cache.StringtableCache;
import de.george.g3utils.gui.SwingUtils;
import de.george.g3utils.gui.UndoableTextField;
import de.george.g3utils.validation.ValidationGroupWrapper;
import net.miginfocom.swing.MigLayout;

public class JFocusNameField extends JPanel {
	private UndoableTextField tfName;
	private JTextField tfFocusName;

	private StringtableCache cache;

	public JFocusNameField(EditorContext ctx) {
		this(null, ctx);
	}

	public JFocusNameField(String text, EditorContext ctx) {
		cache = Caches.stringtable(ctx);
		setLayout(new MigLayout("insets 0", "[]", "[]0[]"));

		tfName = SwingUtils.createUndoTF();
		add(tfName, "width 100%, height 19:19:19, wrap");
		tfName.getDocument().addDocumentListener(SwingUtils.createDocumentListener(this::updateFocusName));

		tfFocusName = SwingUtils.createUndoTF();
		add(tfFocusName, "width 100%, height 19:19:19");
		tfFocusName.setEditable(false);

		setText(text);
		cache.addUpdateListener(this, t -> updateFocusName());
	}

	public void setText(String name) {
		setText(name, false);
	}

	public void setText(String name, boolean keepHistory) {
		tfName.setText(name, keepHistory);
	}

	public String getText() {
		return tfName.getText();
	}

	public Document getDocument() {
		return tfName.getDocument();
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public void initValidation(ValidationGroup group, String fieldName, AbstractValidator... validators) {
		tfName.setName(fieldName);
		group.add(tfName, validators);
	}

	public void removeValidation(ValidationGroupWrapper group) {
		group.remove(tfName);
	}

	private void updateFocusName() {
		tfFocusName.setText(cache.getFocusNamesOrEmpty().get(tfName.getText()));
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		tfName.setEnabled(enabled);
		tfFocusName.setEnabled(false);
	}
}

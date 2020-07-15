package de.george.g3dit.gui.components;

import javax.swing.JPopupMenu;

import de.george.g3dit.EditorContext;
import de.george.g3dit.cache.Caches;
import de.george.g3dit.cache.EntityCache;
import de.george.g3dit.gui.dialogs.EntityIntelliHints;
import de.george.g3utils.gui.SwingUtils;
import de.george.g3utils.gui.UndoableTextField;

public class JEntityGuidField extends JSearchGuidField {
	private UndoableTextField tfName;
	private EntityCache cache;

	private boolean editing = false;

	public JEntityGuidField(EditorContext ctx) {
		this(null, ctx);
	}

	public JEntityGuidField(String text, EditorContext ctx) {
		super(text, ctx);
		cache = Caches.entity(ctx);

		add(getOrCreateTextFieldName(), "cell 0 1, growx, pushx, height 19:19:19, split 2");
		getOrCreateTextFieldName().getDocument().addDocumentListener(SwingUtils.createDocumentListener(this::updateName));
		// Add name auto completion to guid field
		new EntityIntelliHints(getOrCreateTextFieldName(), cache);

		add(tfGuidText, "aligny top, gapleft 2, width 30%, height 19:19:19");
		SwingUtils.smallFont(tfGuidText);

		addGuidFiedListener(this::lookupName);
		lookupName(getText());
		cache.addUpdateListener(this, c -> lookupName(getText()));
	}

	@Override
	public void setText(String guid, boolean keepHistory) {
		super.setText(guid, keepHistory);
		if (!keepHistory) {
			getOrCreateTextFieldName().clearHistory();
		}
	}

	private void lookupName(String guid) {
		if (editing) {
			return;
		}

		editing = true;
		if (cache.isValid()) {
			getOrCreateTextFieldName().setText(cache.getDisplayName(guid));
		} else {
			getOrCreateTextFieldName().setText(null);
		}
		editing = false;
	}

	private void updateName() {
		if (editing) {
			return;
		}

		editing = true;
		String name = getOrCreateTextFieldName().getText();
		if (!name.isEmpty()) {
			cache.getGuidByUniqueName(name).ifPresent(guid -> setText(guid, true));
		}
		editing = false;
	}

	private UndoableTextField getOrCreateTextFieldName() {
		if (tfName == null) {
			tfName = SwingUtils.createUndoTF();
			tfName.keepHistoryByDefault();
		}
		return tfName;
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		getOrCreateTextFieldName().setEnabled(enabled);
	}

	@Override
	public void setEditable(boolean enabled) {
		super.setEditable(enabled);
		getOrCreateTextFieldName().setEditable(enabled);
	}

	@Override
	public void setComponentPopupMenu(JPopupMenu popup) {
		super.setComponentPopupMenu(popup);
		getOrCreateTextFieldName().setComponentPopupMenu(popup);

	}
}

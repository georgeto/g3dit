package de.george.g3dit.gui.components;

import java.awt.Font;
import java.util.Optional;

import javax.swing.JPopupMenu;

import de.george.g3dit.EditorContext;
import de.george.g3utils.gui.SwingUtils;
import de.george.g3utils.gui.UndoableTextField;

public abstract class JSearchNamedGuidField extends JSearchGuidField {
	public enum Layout {
		Horizontal,
		Vertical,
		VerticalNoName,
	}

	private Font defaultGuidTextFont;
	private UndoableTextField tfName;
	private boolean editing = false;

	public JSearchNamedGuidField(EditorContext ctx) {
		super(ctx);

		getOrCreateTextFieldName().getDocument().addDocumentListener(SwingUtils.createDocumentListener(this::updateName));
		addGuidFiedListener(this::lookupName);
		defaultGuidTextFont = tfGuidText.getFont();
		setFieldLayout(Layout.Vertical);
	}

	public void setFieldLayout(Layout layout) {
		setFieldLayout(layout, -1);
	}

	public void setFieldLayout(Layout layout, int distribution) {
		tfGuidText.setFont(defaultGuidTextFont);
		SwingUtils.smallTextField(tfGuid);
		SwingUtils.smallTextField(tfGuidText);
		removeAll();
		if (layout == Layout.Vertical) {
			add(tfGuid, "width 100%, height 20!, wrap");
			SwingUtils.smallTextField(getOrCreateTextFieldName());
			add(getOrCreateTextFieldName(), "growx, pushx, height 20!, split 2");
			SwingUtils.smallFont(tfGuidText);
			add(tfGuidText, "aligny top, gapleft 2, width 30%, height 20!");
		} else if (layout == Layout.VerticalNoName) {
			add(tfGuid, "width 100%, height 20!, wrap");
			add(tfGuidText, "width 100%, height 20!");
		} else if (layout == Layout.Horizontal) {
			if (distribution == -1) {
				distribution = 70;
			}
			add(tfGuid, "width " + distribution + "%, height 20!, egy fields, id tfGuid");
			SwingUtils.smallTextField(getOrCreateTextFieldName());
			add(getOrCreateTextFieldName(), "width " + (100 - distribution) + "%, height 20!, egy fields, y tfGuid.y");
		}
	}

	@Override
	public void setText(String guid, boolean keepHistory) {
		super.setText(guid, keepHistory);
		if (!keepHistory) {
			getOrCreateTextFieldName().clearHistory();
		}
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

	protected abstract Optional<String> guidToName(String guid);

	protected abstract Optional<String> nameToGuid(String name);

	protected void lookupName(String guid) {
		if (editing) {
			return;
		}

		editing = true;
		getOrCreateTextFieldName().setText(guidToName(guid).orElse(null));
		editing = false;
	}

	private void updateName() {
		if (editing) {
			return;
		}

		editing = true;
		String name = getOrCreateTextFieldName().getText();
		if (!name.isEmpty()) {
			nameToGuid(name).ifPresent(guid -> setText(guid, true));
		}
		editing = false;
	}

	protected UndoableTextField getOrCreateTextFieldName() {
		if (tfName == null) {
			tfName = SwingUtils.createUndoTF();
			tfName.keepHistoryByDefault();
		}
		return tfName;
	}
}

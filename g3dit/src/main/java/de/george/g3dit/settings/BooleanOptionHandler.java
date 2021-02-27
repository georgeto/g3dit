package de.george.g3dit.settings;

import java.awt.Window;
import java.util.Optional;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

public class BooleanOptionHandler extends TypedOptionHandler<Boolean> {
	private String title;
	private JCheckBox cbBoolean;
	private String tooltip;

	public BooleanOptionHandler(Window parent, String title) {
		super(parent);
		this.title = title;
	}

	public BooleanOptionHandler(Window parent, String title, String tooltip) {
		super(parent);
		this.title = title;
		this.tooltip = tooltip;
	}

	@Override
	protected JPanel initContent() {
		JPanel content = super.initContent();
		cbBoolean = new JCheckBox(title);
		cbBoolean.setToolTipText(tooltip);
		content.add(cbBoolean, "");
		return content;
	}

	@Override
	protected void load(Boolean value) {
		cbBoolean.setSelected(value);
	}

	@Override
	protected Optional<Boolean> save() {
		return Optional.of(cbBoolean.isSelected());
	}
}

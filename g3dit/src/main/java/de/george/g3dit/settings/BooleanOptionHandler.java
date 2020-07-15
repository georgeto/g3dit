package de.george.g3dit.settings;

import java.awt.Window;
import java.util.Optional;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

public class BooleanOptionHandler extends TypedOptionHandler<Boolean> {
	private String title;
	private JCheckBox cbBoolean;

	public BooleanOptionHandler(Window parent, String title) {
		super(parent);
		this.title = title;
	}

	@Override
	protected JPanel initContent() {
		JPanel content = super.initContent();
		cbBoolean = new JCheckBox(title);
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

package de.george.g3dit.settings;

import java.awt.Window;
import java.util.Optional;

import javax.swing.JPanel;

import de.george.g3utils.gui.SwingUtils;
import de.george.g3utils.gui.UndoableTextField;

public class TextFieldOptionHandler extends TitledOptionHandler<String> {
	private UndoableTextField tfValue;
	private String tooltip;

	public TextFieldOptionHandler(Window parent, String title) {
		this(parent, title, null);
	}

	public TextFieldOptionHandler(Window parent, String title, String tooltip) {
		super(parent, title);
		this.tooltip = tooltip;
	}

	@Override
	protected void addValueComponent(JPanel content) {
		tfValue = SwingUtils.createUndoTF();
		tfValue.setToolTipText(tooltip);
		content.add(tfValue, "grow");
	}

	@Override
	protected void load(String value) {
		tfValue.setText(value);
	}

	@Override
	protected Optional<String> save() {
		return Optional.ofNullable(tfValue.getText());
	}
}

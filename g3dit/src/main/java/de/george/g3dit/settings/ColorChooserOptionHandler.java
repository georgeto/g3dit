package de.george.g3dit.settings;

import java.awt.Color;
import java.awt.Window;
import java.util.Optional;

import javax.swing.JPanel;

import de.george.g3utils.gui.ColorChooserButton;

public class ColorChooserOptionHandler extends TitledOptionHandler<Color> {
	private ColorChooserButton ccbValue;

	public ColorChooserOptionHandler(Window parent, String title) {
		super(parent, title);
	}

	@Override
	protected void load(Color value) {
		ccbValue.setSelectedColor(value);
	}

	@Override
	protected Optional<Color> save() {
		return Optional.of(ccbValue.getSelectedColor());
	}

	@Override
	protected void addValueComponent(JPanel content) {
		ccbValue = new ColorChooserButton(getParent(), true);
		content.add(ccbValue, "grow, push");
	}
}

package de.george.g3dit.settings;

import java.awt.Window;

public class OptionPanel extends OptionPanelBase<OptionPanel> {
	public OptionPanel(Window parent) {
		super(parent);
	}

	public OptionPanel(Window parent, String layoutConstraints) {
		super(parent, layoutConstraints);
	}

	public OptionPanel(Window parent, String layoutConstraints, String colConstraints) {
		super(parent, layoutConstraints, colConstraints);
	}

	public OptionPanel(Window parent, String layoutConstraints, String colConstraints, String rowConstraints) {
		super(parent, layoutConstraints, colConstraints, rowConstraints);
	}
}

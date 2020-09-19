package de.george.g3dit.gui.edit;

import de.george.g3dit.EditorContext;

public class PropertyPanel extends PropertyPanelBase<PropertyPanel> {
	public PropertyPanel(EditorContext ctx) {
		super(ctx);
	}

	public PropertyPanel(EditorContext ctx, String layoutConstraints) {
		super(ctx, layoutConstraints);
	}

	public PropertyPanel(EditorContext ctx, String layoutConstraints, String colConstraints) {
		super(ctx, layoutConstraints, colConstraints);
	}

	public PropertyPanel(EditorContext ctx, String layoutConstraints, String colConstraints, String rowConstraints) {
		super(ctx, layoutConstraints, colConstraints, rowConstraints);
	}
}

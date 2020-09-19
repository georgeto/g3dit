package de.george.g3dit.gui.edit.handler;

import javax.swing.JLabel;
import javax.swing.JPanel;

import de.george.g3dit.gui.edit.PropertyPanelDef;
import de.george.g3utils.io.G3Serializable;

public abstract class TitledPropertyHandler<T extends G3Serializable> extends TypedPropertyHandler<T> {
	protected final PropertyPanelDef def;

	public TitledPropertyHandler(PropertyPanelDef def) {
		this.def = def;
	}

	@Override
	protected JPanel initContent() {
		JPanel content = super.initContent();
		if (def.hasTitle()) {
			content.add(new JLabel(def.getTitle()), "spanx, wrap");
		}
		addValueComponent(content);
		return content;
	}

	protected abstract void addValueComponent(JPanel content);
}

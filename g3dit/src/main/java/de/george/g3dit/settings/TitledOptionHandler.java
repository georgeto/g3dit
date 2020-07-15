package de.george.g3dit.settings;

import java.awt.Window;

import javax.swing.JLabel;
import javax.swing.JPanel;

public abstract class TitledOptionHandler<T> extends TypedOptionHandler<T> {
	private String title;

	public TitledOptionHandler(Window parent, String title) {
		super(parent);
		this.title = title;
	}

	@Override
	protected JPanel initContent() {
		JPanel content = super.initContent();
		content.add(new JLabel(title), "spanx, wrap");
		addValueComponent(content);
		return content;
	}

	protected abstract void addValueComponent(JPanel content);
}

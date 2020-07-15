package de.george.g3dit.settings;

import java.awt.Window;

import javax.swing.JComponent;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

public abstract class AbstractOptionHandler<T> implements OptionHandler<T> {
	private JPanel panel;
	private Window parent;

	private boolean contentInitiated = false;

	public AbstractOptionHandler(Window parent) {
		this.parent = parent;
	}

	@Override
	public JComponent getContent() {
		if (!contentInitiated) {
			initContent();
		}

		return panel;
	}

	protected JPanel initContent() {
		panel = new JPanel();
		MigLayout layout = getLayoutManager();
		panel.setLayout(layout);
		contentInitiated = true;
		return panel;
	}

	protected MigLayout getLayoutManager() {
		return new MigLayout("ins 0, fillx", "[grow]");
	}

	protected Window getParent() {
		return parent;
	}
}

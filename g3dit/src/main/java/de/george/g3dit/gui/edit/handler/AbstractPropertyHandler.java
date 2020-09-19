package de.george.g3dit.gui.edit.handler;

import javax.swing.JComponent;
import javax.swing.JPanel;

import de.george.g3utils.io.G3Serializable;
import net.miginfocom.swing.MigLayout;

public abstract class AbstractPropertyHandler<T extends G3Serializable> implements PropertyHandler<T> {
	private JPanel panel;

	private boolean contentInitiated = false;

	@Override
	public JComponent getContent() {
		if (!contentInitiated) {
			initContent();
		}

		return panel;
	}

	protected JPanel initContent() {
		panel = new JPanel();
		panel.setLayout(getLayoutManager());
		contentInitiated = true;
		return panel;
	}

	protected MigLayout getLayoutManager() {
		return new MigLayout("ins 0, fillx", "[grow]");
	}
}

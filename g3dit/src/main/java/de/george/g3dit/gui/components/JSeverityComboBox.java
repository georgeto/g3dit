package de.george.g3dit.gui.components;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;

public class JSeverityComboBox extends JComboBox<Severity> {
	private static final SeverityImageIcon[] IMAGE_ICONS = new SeverityImageIcon[] {new SeverityImageIcon(Severity.Info),
			new SeverityImageIcon(Severity.Warn), new SeverityImageIcon(Severity.Error)};

	public JSeverityComboBox() {
		super(new Severity[] {Severity.Info, Severity.Warn, Severity.Error});
		setRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean selected, boolean focus) {
				super.getListCellRendererComponent(list, value, index, selected, focus);
				setText(((Severity) value).getDisplayName());
				setIcon(IMAGE_ICONS[((Severity) value).ordinal()]);
				return this;
			}
		});
		setSelectedIndex(1);
	}

	@Override
	public Severity getSelectedItem() {
		return (Severity) super.getSelectedItem();
	}
}

package de.george.g3dit.settings;

import java.awt.Window;
import java.util.Optional;

import javax.swing.JPanel;

import de.george.g3utils.gui.JComboBoxExt;

public class ComboBoxOptionHandler<T> extends TitledOptionHandler<T> {
	private Iterable<T> values;
	private JComboBoxExt<T> cbValue;
	private boolean editable;

	public ComboBoxOptionHandler(Window parent, String title, Iterable<T> values, boolean editable) {
		super(parent, title);
		this.values = values;
		this.editable = editable;
	}

	@Override
	protected void addValueComponent(JPanel content) {
		cbValue = new JComboBoxExt<>(values, values.iterator().next().getClass());
		cbValue.setEditable(editable);
		content.add(cbValue, "");
	}

	@Override
	protected void load(T value) {
		cbValue.setSelectedItem(value);
	}

	@Override
	protected Optional<T> save() {
		return Optional.of(cbValue.getSelectedItem());
	}
}

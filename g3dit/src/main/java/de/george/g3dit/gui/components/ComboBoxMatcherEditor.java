package de.george.g3dit.gui.components;

import java.util.function.Function;

import javax.swing.JComboBox;

import ca.odell.glazedlists.matchers.AbstractMatcherEditor;

public class ComboBoxMatcherEditor<T, V> extends AbstractMatcherEditor<T> {
	private JComboBox<V> cbFilter;
	private Function<T, V> valueExtractor;

	public ComboBoxMatcherEditor(JComboBox<V> cbFilter, Function<T, V> valueExtractor) {
		this.cbFilter = cbFilter;
		this.valueExtractor = valueExtractor;
		cbFilter.addItemListener(e -> policyChanged());
		policyChanged();
	}

	public void policyChanged() {
		@SuppressWarnings("unchecked")
		V targetValue = (V) cbFilter.getSelectedItem();
		if (targetValue != null) {
			fireChanged(value -> targetValue.equals(valueExtractor.apply(value)));
		} else {
			fireMatchAll();
		}
	}
};

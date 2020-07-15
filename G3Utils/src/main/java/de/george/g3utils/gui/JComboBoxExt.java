package de.george.g3utils.gui;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;

import com.google.common.collect.Iterables;

public class JComboBoxExt<T> extends JComboBox<T> {
	public JComboBoxExt() {}

	@SuppressWarnings("unchecked")
	public JComboBoxExt(Iterable<T> items, Class<?> itemType) {
		super(Iterables.toArray(items, (Class<T>) itemType));
	}

	public JComboBoxExt(T... items) {
		super(items);
	}

	public JComboBoxExt(ComboBoxModel<T> aModel) {
		super(aModel);
	}

	public void setSelectedItemTyped(T object) {
		super.setSelectedItem(object);
	}

	@SuppressWarnings("unchecked")
	@Override
	public T getSelectedItem() {
		return (T) super.getSelectedItem();
	}

	@SuppressWarnings("unchecked")
	public List<T> getSelectedItems() {
		return Arrays.stream(super.getSelectedObjects()).map(o -> (T) o).collect(Collectors.toList());
	}

}

package de.george.g3dit.gui.complete;

import java.util.function.Function;

import javax.swing.JComboBox;

import com.jidesoft.swing.ComboBoxSearchable;

public class FunctionalComboBoxSearchable<T> extends ComboBoxSearchable {
	private Function<T, ? extends String> textExtractor;

	@SuppressWarnings("unchecked")
	public FunctionalComboBoxSearchable(JComboBox<T> comboBox, Function<? extends T, String> textExtractor) {
		super(comboBox);
		this.textExtractor = (Function<T, ? extends String>) textExtractor;
		setFromStart(false);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected String convertElementToString(Object object) {
		return object != null ? textExtractor.apply((T) object) : "";
	}

}

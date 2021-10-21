package de.george.g3dit.settings;

import javax.swing.JComponent;

public interface OptionHandler<T> {
	public JComponent getContent();

	public void load(OptionStore optionStore, Option<T> option);

	public void save(OptionStore optionStore, Option<T> option);

	public void cancel(OptionStore optionStore, Option<T> option);
}

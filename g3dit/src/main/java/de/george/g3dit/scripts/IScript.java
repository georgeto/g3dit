package de.george.g3dit.scripts;

import de.george.g3dit.settings.OptionPanel;

public interface IScript {
	public String getTitle();

	public String getDescription();

	public boolean execute(IScriptEnvironment env);

	default void installOptions(OptionPanel optionPanel) {}
}

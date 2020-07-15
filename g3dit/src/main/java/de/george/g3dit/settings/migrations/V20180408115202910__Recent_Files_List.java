package de.george.g3dit.settings.migrations;

import java.util.Arrays;

import de.george.g3dit.settings.MigratableOptionStore;
import de.george.g3dit.settings.OptionStoreMigration;

public class V20180408115202910__Recent_Files_List implements OptionStoreMigration {
	public static final String OPTION_NAME = "EditorOptions.MainMenu.RECENT_FILES";

	@Override
	public void migrate(MigratableOptionStore optionStore) {
		String storeString = optionStore.get(OPTION_NAME);
		if (storeString != null) {
			optionStore.put(OPTION_NAME, Arrays.asList(storeString.split(";")));
		}
	}
}

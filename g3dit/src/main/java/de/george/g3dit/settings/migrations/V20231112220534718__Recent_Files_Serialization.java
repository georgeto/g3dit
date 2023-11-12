package de.george.g3dit.settings.migrations;

import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import de.george.g3dit.settings.MigratableOptionStore;
import de.george.g3dit.settings.OptionStoreMigration;

public class V20231112220534718__Recent_Files_Serialization implements OptionStoreMigration {
	public static final String OPTION_NAME = "EditorOptions.MainMenu.RECENT_FILES";

	@Override
	public void migrate(MigratableOptionStore optionStore) {
		List<String> stringList = optionStore.get(OPTION_NAME);
		if (stringList != null) {
			optionStore.put(OPTION_NAME, stringList.stream().map(Paths::get).collect(Collectors.toList()));
		}
	}
}

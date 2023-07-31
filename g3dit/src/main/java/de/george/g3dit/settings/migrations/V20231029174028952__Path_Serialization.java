package de.george.g3dit.settings.migrations;

import java.nio.file.Paths;

import de.george.g3dit.settings.MigratableOptionStore;
import de.george.g3dit.settings.OptionStoreMigration;

public class V20231029174028952__Path_Serialization implements OptionStoreMigration {
	public static final String[] OPTION_NAMES = {"EditorOptions.Path.PRIMARY_DATA_FOLDER", "EditorOptions.Path.SECONDARY_DATA_FOLDER",
			"EditorOptions.D3View.SCREENSHOT_FOLDER"};

	@Override
	public void migrate(MigratableOptionStore optionStore) {
		for (String optionName : OPTION_NAMES) {
			String storeString = optionStore.get(optionName);
			if (storeString != null) {
				optionStore.put(optionName, Paths.get(storeString));
			}
		}
	}
}

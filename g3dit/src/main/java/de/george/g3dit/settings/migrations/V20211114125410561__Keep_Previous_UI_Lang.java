package de.george.g3dit.settings.migrations;

import de.george.g3dit.Editor.UiLanguage;
import de.george.g3dit.settings.MigratableOptionStore;
import de.george.g3dit.settings.OptionStoreMigration;

public class V20211114125410561__Keep_Previous_UI_Lang implements OptionStoreMigration {
	public static final String UI_LANG_OPTION_NAME = "EditorOptions.Language.UI_LANGUAGE";
	public static final String INDICATOR_OPTION_NAME = "EditorOptions.MainWindow.SIZE";

	@Override
	public void migrate(MigratableOptionStore optionStore) {
		if (optionStore.get(UI_LANG_OPTION_NAME) == null) {
			boolean establishedUser = optionStore.get(INDICATOR_OPTION_NAME) != null;
			// For existing users, the previous and thus default UI language is German.
			optionStore.put(UI_LANG_OPTION_NAME, establishedUser ? UiLanguage.DE : UiLanguage.DEFAULT);
		}
	}
}

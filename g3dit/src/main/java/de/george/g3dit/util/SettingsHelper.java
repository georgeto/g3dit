package de.george.g3dit.util;

import java.io.File;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import de.george.g3dit.settings.EditorOptions;
import de.george.g3dit.settings.Option;
import de.george.g3dit.settings.OptionStore;

public class SettingsHelper {
	public static Map<String, String> getDataFolderAlias(OptionStore optionStore) {
		Map<String, String> aliasMap = new TreeMap<>(new StringLengthComparator());
		addAliasToMap(optionStore, aliasMap, EditorOptions.Path.PRIMARY_DATA_FOLDER, EditorOptions.Path.PRIMARY_DATA_FOLDER_ALIAS);
		addAliasToMap(optionStore, aliasMap, EditorOptions.Path.SECONDARY_DATA_FOLDER, EditorOptions.Path.SECONDARY_DATA_FOLDER_ALIAS);
		return aliasMap;
	}

	private static void addAliasToMap(OptionStore optionStore, Map<String, String> aliasMap, Option<String> pathSetting,
			Option<String> aliasSetting) {
		String alias = optionStore.get(aliasSetting);
		String path = optionStore.get(pathSetting);
		if (!alias.isEmpty() && !path.isEmpty()) {
			aliasMap.put(path, alias);
			if (optionStore.get(EditorOptions.Path.HIDE_PROJECTS_COMPILED)) {
				aliasMap.put(path.concat("Projects_compiled").concat(File.separator).concat("G3_World_01").concat(File.separator), alias);
			}
		}

	}

	public static String applyAliasMap(Map<String, String> aliasMap, String filePath) {
		for (Map.Entry<String, String> alias : aliasMap.entrySet()) {
			if (filePath.toLowerCase().startsWith(alias.getKey().toLowerCase())) {
				return "|" + alias.getValue() + "| " + filePath.substring(alias.getKey().length());
			}
		}
		return filePath;
	}

	public static String applyAlias(OptionStore optionStore, String filePath) {
		return applyAliasMap(getDataFolderAlias(optionStore), filePath);
	}

	public static Optional<String> getAlias(OptionStore optionStore, String filePath) {
		for (Map.Entry<String, String> alias : getDataFolderAlias(optionStore).entrySet()) {
			if (filePath.toLowerCase().startsWith(alias.getKey().toLowerCase())) {
				return Optional.of("|" + alias.getValue() + "|");
			}
		}
		return Optional.empty();
	}

}

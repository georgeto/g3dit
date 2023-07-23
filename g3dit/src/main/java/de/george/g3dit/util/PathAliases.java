package de.george.g3dit.util;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import de.george.g3dit.settings.EditorOptions;
import de.george.g3dit.settings.Option;
import de.george.g3dit.settings.OptionStore;
import de.george.g3utils.util.FilesEx;

public class PathAliases {
	private static final Comparator<Path> DESCENDING_PATH_LENGTH_COMPARATOR = (left, right) -> {
		int compare = Integer.compare(left.getNameCount(), right.getNameCount());
		return -(compare == 0 ? left.compareTo(right) : compare);
	};

	private Map<Path, String> aliasMap = new TreeMap<>(DESCENDING_PATH_LENGTH_COMPARATOR);

	public static PathAliases empty() {
		return new PathAliases();
	}

	public static PathAliases from(OptionStore optionStore) {
		PathAliases aliases = new PathAliases();
		aliases.addAlias(optionStore, EditorOptions.Path.PRIMARY_DATA_FOLDER, EditorOptions.Path.PRIMARY_DATA_FOLDER_ALIAS);
		aliases.addAlias(optionStore, EditorOptions.Path.SECONDARY_DATA_FOLDER, EditorOptions.Path.SECONDARY_DATA_FOLDER_ALIAS);
		return aliases;
	}

	private void addAlias(OptionStore optionStore, Option<String> pathSetting, Option<String> aliasSetting) {
		String alias = optionStore.get(aliasSetting);
		String path = optionStore.get(pathSetting);
		if (!alias.isEmpty() && !path.isEmpty()) {
			aliasMap.put(Paths.get(path), alias);
			if (optionStore.get(EditorOptions.Path.HIDE_PROJECTS_COMPILED)) {
				aliasMap.put(Paths.get(path, "Projects_compiled", "G3_World_01"), alias);
			}
		}
	}

	public String apply(Path filePath) {
		for (var alias : aliasMap.entrySet()) {
			if (filePath.startsWith(alias.getKey())) {
				return "|" + alias.getValue() + "| " + alias.getKey().relativize(filePath);
			}
		}
		return FilesEx.getAbsolutePath(filePath);
	}

	public Optional<String> getAlias(Path filePath) {
		for (var alias : aliasMap.entrySet()) {
			if (filePath.startsWith(alias.getKey())) {
				return Optional.of("|" + alias.getValue() + "|");
			}
		}
		return Optional.empty();
	}
}

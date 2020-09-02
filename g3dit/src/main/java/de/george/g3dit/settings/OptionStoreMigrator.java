package de.george.g3dit.settings;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.george.g3dit.util.ClasspathScanUtil;

public class OptionStoreMigrator {
	private static final Logger logger = LoggerFactory.getLogger(OptionStoreMigrator.class);
	private static final Pattern MIGRATION_PATTERN = Pattern.compile("^V(?<version>\\d+)__(?<name>.*)$");

	private MigratableOptionStore optionStore;

	private static final Option<Set<Long>> APPLIED_MIGRATIONS = new NoHandlerOption<>(Collections.emptySet(), "APPLIED_MIGRATIONS",
			"Applied migrations");

	public OptionStoreMigrator(MigratableOptionStore optionStore) {
		this.optionStore = optionStore;
	}

	public void migrate() {
		SortedMap<Long, Class<? extends OptionStoreMigration>> migrations = new TreeMap<>();
		for (Class<? extends OptionStoreMigration> migration : ClasspathScanUtil.findSubtypesOf(OptionStoreMigration.class,
				"de.george.g3dit.settings.migrations")) {
			String migrationFullName = migration.getSimpleName();
			Matcher matcher = MIGRATION_PATTERN.matcher(migrationFullName);
			if (!matcher.matches()) {
				throw new IllegalStateException("Migration name is invalid: " + migrationFullName);
			}

			Long migrationVersion = Long.valueOf(matcher.group("version"));
			migrations.put(migrationVersion, migration);
		}

		Set<Long> appliedMigrations = new HashSet<>(optionStore.get(APPLIED_MIGRATIONS));
		try {
			for (Map.Entry<Long, Class<? extends OptionStoreMigration>> migration : migrations.entrySet()) {
				if (!appliedMigrations.contains(migration.getKey())) {
					logger.info("Applying migration '{}'.", migration.getValue().getSimpleName());
					migration.getValue().newInstance().migrate(optionStore);
					appliedMigrations.add(migration.getKey());
				}
			}
		} catch (Exception e) {
			logger.error("Failed to apply OptionStore migrations.", e);
			throw new IllegalStateException("Failed to apply OptionStore migrations.", e);
		}
		optionStore.put(APPLIED_MIGRATIONS, appliedMigrations);
	}
}

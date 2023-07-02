package de.george.g3dit.cache;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.FutureCallback;

import de.george.g3dit.EditorContext;
import de.george.g3dit.settings.EditorOptions;
import de.george.g3dit.settings.SettingsUpdatedEvent;
import de.george.g3dit.util.ConcurrencyUtil;
import de.george.g3dit.util.FileManager;
import de.george.g3utils.util.Holder;
import de.george.g3utils.util.Pair;

public class StringtableCache extends AbstractCache<StringtableCache> {
	private static final Logger logger = LoggerFactory.getLogger(StringtableCache.class);

	private List<String> languages;
	private String cachedLanguage;
	private ConcurrentMap<String, String> focusNames;
	private Set<String> focusNameSet;

	private EditorContext ctx;

	public StringtableCache(EditorContext ctx) {
		this.ctx = ctx;
		ctx.eventBus().register(this);
	}

	@Override
	public boolean isValid() {
		return focusNames != null;
	}

	public Map<String, String> getFocusNamesOrEmpty() {
		return isValid() ? focusNames : Collections.emptyMap();
	}

	public Set<String> getFocusNameSet() {
		return isValid() ? focusNameSet : Collections.emptySet();
	}

	public List<String> getLanguages() {
		return languages;
	}

	public String getCachedLanguage() {
		return cachedLanguage;
	}

	@Override
	public void create() throws Exception {
		ConcurrencyUtil.executeAndInvokeLater(() -> {
			Optional<Path> file = ctx.getFileManager().searchFile(FileManager.RP_STRINGS, "stringtable.ini");
			if (!file.isPresent()) {
				logger.warn("Unable to locate stringtable.");
				return null;
			}

			try (Stream<String> stream = Files.lines(file.get(), StandardCharsets.UTF_16)) {
				Holder<String> rawLanguages = new Holder<>();
				Holder<String> rawCurrentLanguage = new Holder<>();
				List<String> rawFocusNames = new ArrayList<>();

				stream.forEach(line -> {
					if (rawLanguages.held() == null && line.startsWith("Languages=")) {
						rawLanguages.hold(line);
					} else if (rawCurrentLanguage.held() == null && line.startsWith("CurrentLanguage=")) {
						rawCurrentLanguage.hold(line);
					} else if (line.startsWith("FO_")) {
						rawFocusNames.add(line);
					}
				});

				Pattern entrySeparator = Pattern.compile("=");
				Pattern langSeparator = Pattern.compile(";");

				languages = ImmutableList.copyOf(langSeparator.split(entrySeparator.split(rawLanguages.held(), 2)[1]));
				String currentLanguage = entrySeparator.split(rawCurrentLanguage.held(), 2)[1];

				String stringtableLanguage = ctx.getOptionStore().get(EditorOptions.Language.STRINGTABLE_LANGUAGE);
				int langIndex = languages.indexOf(stringtableLanguage);
				if (langIndex == -1) {
					logger.warn(
							"The selected stringtable language is not contained in the stringtable's language list. Falling back to stringtable's current language.");

					langIndex = languages.indexOf(currentLanguage);
					if (langIndex == -1) {
						logger.warn("The current language of the stringtable is not contained in its language list.");
						cachedLanguage = null;
						return null;
					} else {
						cachedLanguage = currentLanguage;
					}
				} else {
					cachedLanguage = stringtableLanguage;
				}

				ConcurrentMap<String, String> focusNames = new ConcurrentHashMap<>();
				for (String rawFocusName : rawFocusNames) {
					String[] splitted = entrySeparator.split(rawFocusName, 2);
					String templateName = splitted[0].substring(3);
					String[] langFocusName = langSeparator.split(splitted[1]);
					if (langIndex * 2 >= langFocusName.length) {
						continue;
					}
					focusNames.put(templateName, langFocusName[langIndex * 2]);
				}
				return Pair.of(focusNames, ImmutableSet.copyOf(focusNames.values()));
			}
		}, new FutureCallback<Pair<ConcurrentMap<String, String>, Set<String>>>() {

			@Override
			public void onSuccess(Pair<ConcurrentMap<String, String>, Set<String>> result) {
				focusNames = result != null ? result.el0() : null;
				focusNameSet = result != null ? result.el1() : null;
				notifyCacheUpdated();
			}

			@Override
			public void onFailure(Throwable t) {
				logger.warn("Failed to create stringtable cache.", t);
			}
		}, ctx.getExecutorService());
	}

	@Override
	public void load(Path file) {
		try {
			create();
		} catch (Exception e) {
			logger.warn("Failed to create stringtable cache.", e);
		}
	}

	@Override
	public void save(Path file) throws IOException {}

	@Subscribe
	public void onSettingsUpdated(SettingsUpdatedEvent event) {
		if (!Objects.equals(ctx.getOptionStore().get(EditorOptions.Language.STRINGTABLE_LANGUAGE), cachedLanguage)) {
			load();
		}
	}
}

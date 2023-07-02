package de.george.g3dit.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ezware.dialog.task.TaskDialogs;
import com.teamunify.i18n.I;

import de.george.g3dit.EditorContext;
import de.george.g3dit.cache.Caches;
import de.george.g3dit.cache.NavCache;
import de.george.g3dit.settings.EditorOptions;
import de.george.g3dit.util.event.EventBusProvider;
import de.george.g3utils.io.G3FileReaderEx;
import de.george.navmap.sections.NavMap;
import de.george.navmap.util.NavCalc;

public class NavMapManager extends EventBusProvider {
	private static final Logger logger = LoggerFactory.getLogger(NavMapManager.class);

	private Path navMapFile;
	private NavMap navMap;

	private EditorContext ctx;

	public NavMapManager(EditorContext ctx) {
		this.ctx = ctx;
	}

	public NavMap getNavMap(boolean displayErrorDialog) {
		if (navMap == null) {
			loadNavMap(displayErrorDialog);
		}

		return navMap;
	}

	public boolean loadNavMap(boolean displayErrorDialog) {
		navMapFile = Paths.get(
				ctx.getOptionStore().get(EditorOptions.Path.PRIMARY_DATA_FOLDER) + "Projects_compiled/G3_World_01/NavigationMap.xnav");
		if (!Files.exists(navMapFile)) {
			navMapFile = Paths.get(ctx.getOptionStore().get(EditorOptions.Path.SECONDARY_DATA_FOLDER)
					+ "Projects_compiled/G3_World_01/NavigationMap.xnav");
		}
		if (!Files.exists(navMapFile)) {
			navMap = null;
			eventBus().post(new NavMapLoadedEvent(false));
			if (displayErrorDialog) {
				TaskDialogs.error(ctx.getParentWindow(), I.tr("NavMap could not be found"), I.tr(
						"The NavMap (NavigationMap.xnav) could not be found.\nPlease check if the data directories are configured correctly."));
			}
			return false;
		}
		try (G3FileReaderEx reader = new G3FileReaderEx(navMapFile)) {
			navMap = new NavMap(reader);
		} catch (Exception e) {
			navMap = null;
			eventBus().post(new NavMapLoadedEvent(false));
			logger.warn("Error while loading NavMap.", e);
			if (displayErrorDialog) {
				TaskDialogs.error(ctx.getParentWindow(), I.tr("Error while loading the NavMap"),
						I.tr("An error occurred while loading the NavMap:") + "\n" + e.getMessage());
			}
			return false;
		}
		eventBus().post(new NavMapLoadedEvent(true));
		return true;
	}

	public boolean saveMap() {
		if (navMapFile != null) {
			return saveMap(navMapFile);
		} else {
			return saveNavMapAs();
		}
	}

	public boolean saveMap(Path file) {
		try {
			file = ctx.getFileManager().confirmSaveInSecondary(file).orElse(null);
			if (file == null) {
				return false;
			}

			navMapFile = file;
			navMap.save(file);
		} catch (IOException e) {
			TaskDialogs.error(ctx.getParentWindow(), I.tr("Saving failed"), I.trf("NavMap could not be saved: {0}", e.getMessage()));
			return false;
		}
		return true;
	}

	public boolean saveNavMapAs() {
		Path navMapSave = FileDialogWrapper.saveFile(I.tr("Save NavMap as"), "NavigationMap.xnav", ctx.getParentWindow(),
				FileDialogWrapper.XNAV_FILTER);
		if (navMapSave != null) {
			if (!this.saveMap(navMapSave)) {
				return false;
			}
			navMapFile = navMapSave;
		}
		return true;

	}

	public boolean isNavMapChanged() {
		return navMap != null && navMap.isChanged();
	}

	public Path getNavMapFile() {
		return navMapFile;
	}

	public NavCalc getNavCalc(boolean displayErrorDialog) {
		NavMap navMap = getNavMap(displayErrorDialog);
		if (navMap == null) {
			return null;
		}

		NavCache cache = Caches.nav(ctx);
		if (!cache.isValid()) {
			ctx.getCacheManager().createCache(NavCache.class);
		}
		return new NavCalc(navMap, cache::getZone, cache::getPath);
	}

	public static class NavMapLoadedEvent {
		private boolean successful;

		private NavMapLoadedEvent(boolean successful) {
			this.successful = successful;
		}

		public boolean isSuccessful() {
			return successful;
		}
	}
}

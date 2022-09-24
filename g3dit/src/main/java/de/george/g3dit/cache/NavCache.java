package de.george.g3dit.cache;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ezware.dialog.task.TaskDialogs;
import com.teamunify.i18n.I;

import de.george.g3dit.EditorContext;
import de.george.g3dit.tab.EditorTab.EditorTabType;
import de.george.g3dit.tab.archive.EditorArchiveTab;
import de.george.g3dit.util.AbstractDialogFileWorker;
import de.george.g3dit.util.ConcurrencyUtil;
import de.george.g3dit.util.ConcurrencyUtil.Awaitable;
import de.george.g3utils.util.Pair;
import de.george.lrentnode.archive.ArchiveFile;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.util.FileUtil;
import de.george.navmap.data.NavPath;
import de.george.navmap.data.NavZone;

public class NavCache extends AbstractCache<NavCache> {
	private static final Logger logger = LoggerFactory.getLogger(NavCache.class);

	private Map<String, NavZone> zones;
	private Map<String, NavPath> paths;
	private EditorContext ctx;

	public NavCache(EditorContext ctx) {
		this.ctx = ctx;
	}

	@Override
	public boolean isValid() {
		return zones != null && paths != null;
	}

	public Collection<NavZone> getZones() {
		return isValid() ? zones.values() : Collections.emptySet();
	}

	public Collection<NavPath> getPaths() {
		return isValid() ? paths.values() : Collections.emptySet();
	}

	public NavZone getZoneByGuid(String guid) {
		return isValid() && guid != null ? zones.get(guid) : null;
	}

	public NavPath getPathByGuid(String guid) {
		return isValid() && guid != null ? paths.get(guid) : null;
	}

	public Optional<NavZone> getZone(String guid) {
		return Optional.ofNullable(getZoneByGuid(guid));
	}

	public Optional<NavPath> getPath(String guid) {
		return Optional.ofNullable(getPathByGuid(guid));
	}

	@Override
	@SuppressWarnings("unchecked")
	public void load(final File inFile) {
		LoadObjectWorker worker = new LoadObjectWorker(inFile) {
			@Override
			protected void done() {
				try {
					Object[] data = loadIntern(get());
					zones = (Map<String, NavZone>) data[1];
					paths = (Map<String, NavPath>) data[2];
				} catch (Exception e) {
					logger.info("NavCache Ladefehler: {}", e.getMessage());
				} finally {
					notifyCacheUpdated();
				}
			}
		};
		worker.execute();
	}

	@Override
	public void save(File file) throws IOException {
		saveIntern(file, zones, paths);
		markChanged(false);
	}

	private static void processFile(ArchiveFile archive, Map<String, NavZone> zones, Map<String, NavPath> paths) {
		for (eCEntity entity : archive.getEntities()) {
			if (entity.hasClass(CD.gCNavZone_PS.class)) {
				zones.put(entity.getGuid(), NavZone.fromArchiveEntity(entity));
			} else if (entity.hasClass(CD.gCNavPath_PS.class)) {
				paths.put(entity.getGuid(), NavPath.fromArchiveEntity(entity));
			}
		}
	}

	@Override
	public void create() throws Exception {
		List<File> openFiles = new ArrayList<>();
		Map<String, NavZone> myZonesP = new HashMap<>();
		Map<String, NavPath> myPathsP = new HashMap<>();
		Map<String, NavZone> myZonesS = new HashMap<>();
		Map<String, NavPath> myPathsS = new HashMap<>();
		for (EditorArchiveTab tab : ctx.getEditor().<EditorArchiveTab>getTabsInDataFolders(EditorTabType.Archive)) {
			File file = tab.getDataFile().get();
			boolean inPrimary = ctx.getFileManager().isInPrimaryDataFolder(file);
			processFile(tab.getCurrentFile(), inPrimary ? myZonesP : myZonesS, inPrimary ? myPathsP : myPathsS);
			openFiles.add(file);
		}

		CreateNavCacheWorker worker = new CreateNavCacheWorker(ctx.getFileManager().worldFilesCallable(), openFiles);
		worker.executeAndShowDialog();

		// Creation was successful
		if (isValid()) {
			// Edge case: The same file opened twice in editor, once from primary and once from
			// secondary data folder. Primary files take precedence.
			myZonesS.putAll(myZonesP);
			myPathsS.putAll(myPathsP);

			// Edge case: File from secondary data folder opened in editor, would otherwise
			// "override" the corresponding unopened file in the prirmary data folder
			myZonesS.forEach(zones::putIfAbsent);
			myPathsS.forEach(paths::putIfAbsent);

			// TODO: Can still produce "ghost" entries (NavZones/Paths only present in file from
			// secondary data folder, so deleted in primary file).
		}
		notifyCacheUpdated();
	}

	public void update(NavZone zone) {
		if (isValid()) {
			zones.put(zone.getGuid(), zone.clone());
			markChanged(true);
		}
	}

	public void update(NavPath path) {
		if (isValid()) {
			paths.put(path.getGuid(), path.clone());
			markChanged(true);
		}
	}

	private class CreateNavCacheWorker extends AbstractDialogFileWorker<Pair<Map<String, NavZone>, Map<String, NavPath>>> {
		public CreateNavCacheWorker(Callable<List<File>> fileProvider, List<File> openFiles) {
			super(fileProvider, openFiles, I.tr("Create NavCache"), ctx.getParentWindow());
			statusFormat = I.tr("{0, number} NavZones and NavPaths found");
		}

		@Override
		protected Pair<Map<String, NavZone>, Map<String, NavPath>> doInBackground() throws Exception {
			List<File> files = getFiles();

			Map<String, NavZone> myZones = new ConcurrentHashMap<>();
			Map<String, NavPath> myPaths = new ConcurrentHashMap<>();

			Awaitable awaitProcess = ConcurrencyUtil.processInPartitions(file -> {
				try {
					processFile(FileUtil.openArchive(file, false), myZones, myPaths);
				} catch (IOException e) {
					// Ignore
				}
				filesDone.incrementAndGet();
			}, files, 3);

			do {
				publish(myZones.size() + myPaths.size());
			} while (!awaitProcess.await(10, TimeUnit.MILLISECONDS));

			return Pair.of(myZones, myPaths);
		}

		@Override
		protected void done() {
			try {
				if (!isCancelled()) {
					Pair<Map<String, NavZone>, Map<String, NavPath>> pair = get();
					zones = pair.el0();
					paths = pair.el1();
					generateCreationTimestamp();
				}

				progDlg.dispose();
			} catch (Exception ex) {
				progDlg.dispose();
				TaskDialogs.showException(ex);
			}
		}
	}
}

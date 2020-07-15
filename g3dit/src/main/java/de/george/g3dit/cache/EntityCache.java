package de.george.g3dit.cache;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ezware.dialog.task.TaskDialogs;

import de.george.g3dit.EditorContext;
import de.george.g3dit.check.FileDescriptor;
import de.george.g3dit.tab.EditorTab.EditorTabType;
import de.george.g3dit.tab.archive.EditorArchiveTab;
import de.george.g3dit.util.AbstractDialogFileWorker;
import de.george.g3dit.util.ConcurrencyUtil;
import de.george.g3dit.util.ConcurrencyUtil.Awaitable;
import de.george.g3utils.util.Pair;
import de.george.lrentnode.archive.ArchiveFile;
import de.george.lrentnode.iterator.ArchiveFileIterator;

public class EntityCache extends AbstractCache<EntityCache> {
	private static final Logger logger = LoggerFactory.getLogger(EntityCache.class);

	public static class EntityCacheEntry implements Serializable {
		private final String name;
		private final FileDescriptor file;

		public EntityCacheEntry(String name, FileDescriptor file) {
			this.name = name;
			this.file = file;
		}

		public String getName() {
			return name;
		}

		public FileDescriptor getFile() {
			return file;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	private Map<String, EntityCacheEntry> entries;
	private Map<String, String> uniqueNames;
	private EditorContext ctx;

	public EntityCache(EditorContext ctx) {
		this.ctx = ctx;
	}

	@Override
	public boolean isValid() {
		return entries != null && uniqueNames != null;
	}

	public Set<String> getGuids() {
		return entries.keySet();
	}

	public Optional<EntityCacheEntry> getEntry(String guid) {
		return isValid() ? Optional.ofNullable(guid).map(entries::get) : Optional.empty();
	}

	public String getDisplayName(String guid) {
		return getEntry(guid).map(EntityCacheEntry::getName).orElse(null);
	}

	public Optional<FileDescriptor> getFile(String guid) {
		return getEntry(guid).map(EntityCacheEntry::getFile);
	}

	public Optional<String> getGuidByUniqueName(String name) {
		return isValid() ? Optional.ofNullable(name).map(uniqueNames::get) : Optional.empty();
	}

	public Set<String> getUniqueNames() {
		return isValid() ? Collections.unmodifiableSet(uniqueNames.keySet()) : Collections.emptySet();
	}

	public boolean isExisting(String guid) {
		return isValid() && guid != null && entries.containsKey(guid);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void load(final File inFile) {
		LoadObjectWorker worker = new LoadObjectWorker(inFile) {
			@Override
			protected void done() {
				try {
					Object[] data = loadIntern(get());
					entries = (Map<String, EntityCacheEntry>) data[1];
					uniqueNames = extractUniqueNames(entries);
				} catch (Exception e) {
					logger.info("Ladefehler: {}", e.getMessage());
				} finally {
					notifyCacheUpdated();
				}
			}
		};
		worker.execute();
	}

	@Override
	public void save(File file) throws IOException {
		saveIntern(file, entries);
	}

	@Override
	public void create() throws Exception {
		List<File> openFiles = new ArrayList<>();
		ConcurrentMap<String, EntityCacheEntry> openEntries = new ConcurrentHashMap<>();
		for (EditorArchiveTab tab : ctx.getEditor().<EditorArchiveTab>getTabsInDataFolders(EditorTabType.Archive)) {
			File file = tab.getDataFile().get();
			processFile(tab.getCurrentFile(), tab.getDataFile().get(), openEntries);
			openFiles.add(file);
		}

		CreateEntityCacheWorker worker = new CreateEntityCacheWorker(openEntries, ctx.getFileManager().worldFilesCallable(), openFiles);
		worker.executeAndShowDialog();
	}

	private void processFile(ArchiveFile archive, File file, Map<String, EntityCacheEntry> entries) {
		archive.getEntities().forEach(e -> entries.merge(e.getGuid(),
				new EntityCacheEntry(e.toString(), new FileDescriptor(file, archive.getArchiveType())), this::mergeEntry));
	}

	private EntityCacheEntry mergeEntry(EntityCacheEntry oldValue, EntityCacheEntry newValue) {
		if (ctx.getFileManager().isInPrimaryDataFolder(oldValue.getFile().getPath())) {
			return oldValue;
		}

		if (ctx.getFileManager().isInPrimaryDataFolder(newValue.getFile().getPath())) {
			return newValue;
		}

		return oldValue;
	}

	private static Map<String, String> extractUniqueNames(Map<String, EntityCacheEntry> entries) {
		Set<String> knownNames = new HashSet<>();
		Map<String, String> uniqueNames = new HashMap<>();
		for (Map.Entry<String, EntityCacheEntry> entry : entries.entrySet()) {

			String name = entry.getValue().getName();
			if (knownNames.add(name)) {
				uniqueNames.put(name, entry.getKey());
			} else {
				// Name is not unique
				uniqueNames.remove(name);
			}
		}
		return uniqueNames;
	}

	private class CreateEntityCacheWorker extends AbstractDialogFileWorker<Pair<Map<String, EntityCacheEntry>, Map<String, String>>> {
		private ConcurrentMap<String, EntityCacheEntry> workerEntries;

		public CreateEntityCacheWorker(ConcurrentMap<String, EntityCacheEntry> openEntries, Callable<List<File>> fileProvider,
				List<File> openFiles) {
			super(fileProvider, openFiles, "Erstelle EntityCache", ctx.getParentWindow());
			workerEntries = openEntries;
			statusFormat = "%d Entities gefunden";
		}

		@Override
		protected Pair<Map<String, EntityCacheEntry>, Map<String, String>> doInBackground() throws Exception {
			Awaitable awaitProcess = ConcurrencyUtil.processInListPartitions(partFiles -> {
				ArchiveFileIterator iter = new ArchiveFileIterator(partFiles);
				while (iter.hasNext() && !isCancelled()) {
					processFile(iter.next(), iter.nextFile(), workerEntries);
					filesDone.incrementAndGet();
				}
			}, getFiles(), 3);

			do {
				publish(workerEntries.size());
			} while (!awaitProcess.await(50, TimeUnit.MILLISECONDS));

			return Pair.of(workerEntries, extractUniqueNames(workerEntries));
		}

		@Override
		protected void done() {
			try {
				if (!isCancelled()) {
					entries = get().el0();
					uniqueNames = get().el1();
					generateCreationTimestamp();
				}

				progDlg.dispose();
			} catch (Exception ex) {
				progDlg.dispose();
				TaskDialogs.showException(ex);
			} finally {
				notifyCacheUpdated();
			}
		}
	}
}

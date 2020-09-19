package de.george.g3dit.cache;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collector;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ezware.dialog.task.TaskDialogs;
import com.google.common.collect.ImmutableSet;

import de.george.g3dit.EditorContext;
import de.george.g3dit.util.AbstractDialogFileWorker;
import de.george.lrentnode.classes.G3Class;
import de.george.lrentnode.classes.desc.ClassDescriptor;
import de.george.lrentnode.iterator.TemplateFileIterator;
import de.george.lrentnode.template.TemplateEntity;
import de.george.lrentnode.template.TemplateFile;

public class TemplateCache extends AbstractCache<TemplateCache> {
	private static final Logger logger = LoggerFactory.getLogger(TemplateCache.class);

	private List<TemplateCacheEntry> templates;
	private EditorContext ctx;

	public TemplateCache(EditorContext ctx) {
		this.ctx = ctx;
	}

	@Override
	public boolean isValid() {
		return templates != null;
	}

	private List<TemplateCacheEntry> getEntitiesOrEmpty() {
		return isValid() ? templates : Collections.emptyList();
	}

	/**
	 * @return Alle TemplateEntities, bis auf HelperParents und Referenzen auf andere Templates
	 */
	public Stream<TemplateCacheEntry> getEntities() {
		return getEntitiesOrEmpty().stream().filter(t -> !t.helperParent && t.refTemplate == null);
	}

	/**
	 * @return Alle TemplateEntities
	 */
	public Stream<TemplateCacheEntry> getAllEntities() {
		return getEntitiesOrEmpty().stream();
	}

	public Stream<TemplateCacheEntry> getEntities(boolean all) {
		return all ? getAllEntities() : getEntities();
	}

	public Stream<TemplateCacheEntry> getHelperParents() {
		return getEntitiesOrEmpty().stream().filter(t -> t.helperParent);
	}

	public Optional<TemplateCacheEntry> getEntryByName(String name) {
		return getEntryByName(name, false);
	}

	public Optional<TemplateCacheEntry> getEntryByName(String name, boolean all) {
		return getEntryByName(name, getEntities(all));
	}

	public Optional<TemplateCacheEntry> getEntryByName(String name, Stream<TemplateCacheEntry> entities) {
		return entities.filter(e -> e.name.equalsIgnoreCase(name)).findFirst();
	}

	public Optional<TemplateCacheEntry> getEntryByGuid(String guid) {
		return getEntryByGuid(guid, false);
	}

	public Optional<TemplateCacheEntry> getEntryByGuid(String guid, boolean all) {
		return getEntryByGuid(guid, getEntities(all));
	}

	public Optional<TemplateCacheEntry> getEntryByGuid(String guid, Stream<TemplateCacheEntry> entities) {
		return entities.filter(e -> e.guid.equalsIgnoreCase(guid)).findFirst();
	}

	@Override
	public void create() throws Exception {
		CreateTemplateCacheWorker worker = new CreateTemplateCacheWorker(ctx.getFileManager().templateFilesCallable());
		worker.executeAndShowDialog();
	}

	@Override
	@SuppressWarnings("unchecked")
	public void load(final File inFile) {
		LoadObjectWorker worker = new LoadObjectWorker(inFile) {
			@Override
			protected void done() {
				try {
					Object[] data = loadIntern(get());
					templates = (List<TemplateCacheEntry>) data[1];
				} catch (Exception e) {
					logger.info("TemplateCache Ladefehler: {}", e.getMessage());
				} finally {
					notifyCacheUpdated();
				}
			}
		};
		worker.execute();
	}

	@Override
	public void save(File file) throws IOException {
		saveIntern(file, templates);
	}

	private class CreateTemplateCacheWorker extends AbstractDialogFileWorker<List<TemplateCacheEntry>> {
		public CreateTemplateCacheWorker(Callable<List<File>> fileProvider) {
			super(fileProvider, null, "Erstelle TemplateCache", ctx.getParentWindow());
			statusFormat = "%d Templates gefunden";
		}

		@Override
		protected List<TemplateCacheEntry> doInBackground() throws Exception {
			List<File> files = getFiles();

			Collector<String, ?, ImmutableSet<String>> toImmutableSet = ImmutableSet.toImmutableSet();

			List<TemplateCacheEntry> myTemplates = new ArrayList<>();
			TemplateFileIterator iter = new TemplateFileIterator(files);
			while (iter.hasNext() && !isCancelled()) {
				TemplateFile tple1 = iter.next();
				for (TemplateEntity tpleEntity : tple1.getHeaders()) {
					TemplateCacheEntry tpleEntry = new TemplateCacheEntry(tpleEntity.getName(), tpleEntity.getGuid(),
							tpleEntity.isHelperParent(), tpleEntity.getRefTemplate(), tpleEntity.getDataChangedTimeStamp(),
							iter.nextFile(), tpleEntity.getClasses().stream().map(G3Class::getClassName).collect(toImmutableSet));
					myTemplates.add(tpleEntry);
				}

				filesDone.incrementAndGet();
				publish(myTemplates.size());
			}
			return myTemplates;
		}

		@Override
		protected void done() {
			try {
				if (!isCancelled()) {
					templates = get();
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

	public static class TemplateCacheEntry implements Serializable {
		private final String name;
		private final String guid;
		private final boolean helperParent;
		private final String refTemplate;
		private final long changeTime;
		private final File file;
		private final Set<String> classes;

		public TemplateCacheEntry(String name, String guid, boolean helperParent, String refTemplate, long changeTime, File file,
				Set<String> classes) {
			this.name = name;
			this.guid = guid;
			this.helperParent = helperParent;
			this.refTemplate = refTemplate;
			this.changeTime = changeTime;
			this.file = file;
			this.classes = classes;
		}

		public String getName() {
			return name;
		}

		public String getGuid() {
			return guid;
		}

		public boolean isHelperParent() {
			return helperParent;
		}

		public String getRefTemplate() {
			return refTemplate;
		}

		public long getChangeTime() {
			return changeTime;
		}

		public File getFile() {
			return file;
		}

		public Set<String> getClasses() {
			return classes;
		}

		@SafeVarargs
		public final boolean hasAnyClass(Class<? extends ClassDescriptor>... propertySets) {
			for (Class<? extends ClassDescriptor> propertySet : propertySets) {
				if (classes.contains(ClassDescriptor.getName(propertySet))) {
					return true;
				}
			}
			return false;
		}

		@Override
		public String toString() {
			return name;
		}
	}
}

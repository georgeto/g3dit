package de.george.g3dit.gui.dialogs;

import java.lang.ref.WeakReference;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.teamunify.i18n.I;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.SortedList;
import de.george.g3dit.EditorContext;
import de.george.g3dit.check.EntityDescriptor;
import de.george.g3dit.check.FileDescriptor;
import de.george.g3dit.check.FileDescriptor.FileType;
import de.george.g3dit.entitytree.filter.GuidEntityFilter;
import de.george.g3dit.gui.components.search.ByteSearchFilterBuilder;
import de.george.g3dit.gui.components.search.EntityGuidSearchFilterBuilder;
import de.george.g3dit.gui.components.search.EntityPositionSearchFilterBuilder;
import de.george.g3dit.gui.components.search.ModularSearchPanel;
import de.george.g3dit.gui.components.search.PropertySearchFilterBuilder;
import de.george.g3dit.gui.components.search.SearchFilter;
import de.george.g3dit.gui.components.search.TemplateNameSearchFilterBuilder;
import de.george.g3dit.gui.table.TableColumnDef;
import de.george.g3dit.gui.table.TableUtil;
import de.george.g3dit.gui.table.TableUtil.SortableEventTable;
import de.george.g3dit.tab.EditorTab;
import de.george.g3dit.tab.EditorTab.EditorTabType;
import de.george.g3dit.tab.template.EditorTemplateTab;
import de.george.g3dit.util.AbstractFileWorker;
import de.george.g3dit.util.ConcurrencyUtil;
import de.george.g3dit.util.ConcurrencyUtil.Awaitable;
import de.george.g3dit.util.Icons;
import de.george.g3utils.util.FilesEx;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.iterator.TemplateFileIterator;
import de.george.lrentnode.template.TemplateEntity;
import de.george.lrentnode.template.TemplateFile;
import net.miginfocom.swing.MigLayout;

public class TemplateSearchDialog extends AbstractTableProgressDialog {
	private static final Logger logger = LoggerFactory.getLogger(TemplateSearchDialog.class);

	private ModularSearchPanel<eCEntity> searchPanel;
	private JButton btnSearch;

	private EventList<Result> results;
	private SortableEventTable<Result> table;

	public static final TemplateSearchDialog openTemplateSearch(EditorContext ctx) {
		TemplateSearchDialog searchDialog = new TemplateSearchDialog(ctx, I.tr("Template Search"));
		searchDialog.open();
		return searchDialog;
	}

	public static final TemplateSearchDialog openTemplateSearch(EditorContext ctx, SearchFilter<eCEntity> filter) {
		TemplateSearchDialog dialog = openTemplateSearch(ctx);
		dialog.searchPanel.loadFilter(filter);
		dialog.btnSearch.doClick(0);
		return dialog;
	}

	public static final TemplateSearchDialog openTemplateSearchGuid(EditorContext ctx, GuidEntityFilter.MatchMode matchMode, String guid) {
		return openTemplateSearch(ctx, new GuidEntityFilter(matchMode, guid));
	}

	private TemplateSearchDialog(EditorContext ctx, String title) {
		super(ctx, title);
		setSize(1000, 700);
	}

	private static final TableColumnDef COLUMN_POSITION = TableColumnDef.withName("Position").displayName(I.tr("Position")).maxSize(60)
			.comparator(Comparator.naturalOrder()).b();

	private static final TableColumnDef COLUMN_NAME = TableColumnDef.withName("Name").displayName(I.tr("Name")).size(300).b();

	private static final TableColumnDef COLUMN_GUID = TableColumnDef.withName("Guid").displayName(I.tr("Guid")).size(300).b();

	private static final TableColumnDef COLUMN_PATH = TableColumnDef.withName("Path").displayName(I.tr("Path")).size(350).b();

	@Override
	public void open() {
		super.open();
		searchPanel.initFocus();
	}

	@Override
	public JComponent createContentPanel() {
		JPanel mainPanel = new JPanel(new MigLayout("fill"));

		searchPanel = new ModularSearchPanel(ctx, TemplateNameSearchFilterBuilder.class, EntityGuidSearchFilterBuilder.class,
				EntityPositionSearchFilterBuilder.class, PropertySearchFilterBuilder.class, ByteSearchFilterBuilder.class);
		btnSearch = registerAction(I.tr("Search"), Icons.getImageIcon(Icons.Action.FIND), this::doWork, true);
		JButton btnErase = new JButton(Icons.getImageIcon(Icons.Action.ERASE));
		btnErase.setFocusable(false);
		btnErase.setToolTipText(I.tr("Clear search"));
		btnErase.addActionListener(e -> searchPanel.reset(false));

		mainPanel.add(searchPanel.getComponent(), "split 3, width 100%, spanx");
		mainPanel.add(btnSearch);
		mainPanel.add(btnErase, "wrap");

		results = new BasicEventList<>();
		SortedList<Result> sortedResults = new SortedList<>(results,
				Comparator.comparing(Result::getRawPath).thenComparing(Result::getPosition));
		table = TableUtil.createSortableTable(ctx, sortedResults, Result.class, COLUMN_POSITION, COLUMN_NAME, COLUMN_GUID, COLUMN_PATH);
		appendBarAndTable(mainPanel, table.table);

		setEntryActivationListener(i -> table.getRowAt(i).open());

		ctx.eventBus().register(this);

		return mainPanel;
	}

	@Override
	public void dispose() {
		ctx.eventBus().unregister(this);
		super.dispose();
	}

	public void doWork() {
		SearchFilter<eCEntity> filter = searchPanel.buildFilter();
		if (!filter.isValid() || worker != null) {
			progressBar.setString(I.tr("Invalid filter settings"));
			return;
		}

		results.clear();

		// Geöffnete Dateien
		List<Path> openFiles = new ArrayList<>();
		for (EditorTemplateTab tab : ctx.getEditor().<EditorTemplateTab>getTabs(EditorTabType.Template)) {
			TemplateFile currentFile = tab.getCurrentTemplate();
			FileDescriptor fileDescriptor = tab.getFileDescriptor();
			for (TemplateEntity entity : currentFile.getHeaders()) {
				if (filter.matches(entity)) {
					results.add(
							new MemoryResult(new EntityDescriptor(entity, currentFile.getEntityPosition(entity), fileDescriptor), tab));
				}
			}

			tab.getDataFile().ifPresent(openFiles::add);
		}

		worker = new SearchEntityWorker(ctx.getFileManager().templateFilesCallable(), openFiles, filter);
		executeWorker();
	}

	@Subscribe
	public void onTabOpenend(EditorTab.OpenedEvent event) {
		onTabEvent(event.getTab(), this::onFileOpened);
	}

	@Subscribe
	public void onTabClosed(EditorTab.ClosedEvent event) {
		onTabEvent(event.getTab(), this::onFileClosed);
	}

	private void onTabEvent(EditorTab tab, Consumer<EditorTemplateTab> callback) {
		if (tab.type() == EditorTabType.Template) {
			EditorTemplateTab templateTab = (EditorTemplateTab) tab;
			if (!templateTab.getDataFile().isPresent()) {
				return;
			}

			callback.accept(templateTab);
		}
	}

	private void onFileClosed(EditorTemplateTab tab) {
		ListIterator<Result> iter = results.listIterator();
		while (iter.hasNext()) {
			Result result = iter.next();
			if (result instanceof MemoryResult memoryResult) {
				if (tab.equals(memoryResult.weakTab.get())) {
					iter.set(memoryResult.toFileResult(tab.getDataFile().get()));
				}
			}
		}
	}

	private void onFileOpened(EditorTemplateTab tab) {
		// Datei ist nun geladen
		ListIterator<Result> iter = results.listIterator();
		while (iter.hasNext()) {
			Result result = iter.next();
			if (result instanceof FileResult fileResult) {
				if (tab.getDataFile().get().equals(fileResult.getFile())) {
					iter.set(fileResult.toMemoryResult(tab));
				}
			}
		}
	}

	private class SearchEntityWorker extends AbstractFileWorker<Void, Result> {
		private SearchFilter<eCEntity> filter;

		protected SearchEntityWorker(Callable<List<Path>> fileProvider, List<Path> openFiles, SearchFilter<eCEntity> filter) {
			super(fileProvider, openFiles, I.tr("Determine files to be searched..."), I.tr("{0, number}/{1, number} files searched"),
					I.tr("Search completed"));
			this.filter = filter;
			setProgressBar(progressBar);
			doneMessageSupplier = this::getDoneMessage;
		}

		@Override
		protected void process(List<Result> resultsToProcess) {
			super.process(resultsToProcess);

			if (!isCancelled()) {
				results.addAll(resultsToProcess);
			}
		}

		@Override
		protected Void doInBackground() throws Exception {
			Awaitable awaitProcess = ConcurrencyUtil.processInListPartitions(files -> {
				TemplateFileIterator iterator = new TemplateFileIterator(files);
				while (iterator.hasNext()) {
					TemplateFile templateFile = iterator.next();
					FileDescriptor fileDescriptor = new FileDescriptor(iterator.nextFile(), FileType.Template);
					for (TemplateEntity entity : templateFile.getEntities()) {
						if (isCancelled()) {
							return;
						}
						try {
							if (filter.matches(entity)) {
								publish(new FileResult(
										new EntityDescriptor(entity, templateFile.getEntityPosition(entity), fileDescriptor)));
							}
						} catch (Exception e) {
							logger.warn("Filter error.", e);
							throw e;
						}
					}
					filesDone.incrementAndGet();
				}
			}, getFiles(), 3);

			do {
				publish();
			} while (!awaitProcess.await(25, TimeUnit.MILLISECONDS));

			return null;
		}

		private String getDoneMessage() {
			return I.trf("Search completed ({0, number} templates found)", results.size());
		}
	}

	public abstract static class Result {
		protected final EntityDescriptor descriptor;

		public Result(EntityDescriptor descriptor) {
			this.descriptor = descriptor;
		}

		public String getRawPath() {
			return getPath();
		}

		public String getName() {
			return descriptor.getDisplayName();
		}

		public String getGuid() {
			return descriptor.getGuid();
		}

		public int getPosition() {
			return descriptor.getIndex();
		}

		public abstract String getPath();

		public abstract void open();
	}

	public class FileResult extends Result {
		public FileResult(EntityDescriptor descriptor) {
			super(descriptor);
		}

		public Path getFile() {
			return descriptor.getFile().getPath();
		}

		@Override
		public String getPath() {
			return FilesEx.getFileName(getFile());
		}

		@Override
		public void open() {
			ctx.getEditor().openFile(getFile());
		}

		public MemoryResult toMemoryResult(EditorTemplateTab templateTab) {
			return new MemoryResult(descriptor, templateTab);
		}
	}

	public class MemoryResult extends Result {
		private WeakReference<EditorTemplateTab> weakTab;

		public MemoryResult(EntityDescriptor descriptor, EditorTemplateTab templateTab) {
			super(descriptor);
			weakTab = new WeakReference<>(templateTab);
		}

		@Override
		public String getRawPath() {
			EditorTemplateTab archiveTab = weakTab.get();
			if (archiveTab != null) {
				return archiveTab.getTitle();
			}
			return I.tr("<Meanwhile closed>");
		}

		@Override
		public String getPath() {
			EditorTemplateTab templateTab = weakTab.get();
			if (templateTab != null) {
				return I.trf("Loaded: {0}", templateTab.getTitle());
			}
			return I.tr("<Meanwhile closed>");
		}

		@Override
		public void open() {
			EditorTemplateTab templateTab = weakTab.get();
			if (templateTab != null) {
				ctx.getEditor().selectTab(templateTab);
			}
		}

		public FileResult toFileResult(Path file) {
			return new FileResult(descriptor.withFile(new FileDescriptor(file, FileType.Template)));
		}
	}
}

package de.george.g3dit.gui.dialogs;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ezware.dialog.task.TaskDialogs;
import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.Subscribe;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.SortedList;
import de.george.g3dit.EditorContext;
import de.george.g3dit.EntityMap;
import de.george.g3dit.check.EntityDescriptor;
import de.george.g3dit.check.FileDescriptor;
import de.george.g3dit.entitytree.filter.GuidEntityFilter;
import de.george.g3dit.entitytree.filter.NameEntityFilter;
import de.george.g3dit.entitytree.filter.PositionEntityFilter;
import de.george.g3dit.gui.components.search.ByteSearchFilterBuilder;
import de.george.g3dit.gui.components.search.EntityGuidSearchFilterBuilder;
import de.george.g3dit.gui.components.search.EntityNameSearchFilterBuilder;
import de.george.g3dit.gui.components.search.EntityPositionSearchFilterBuilder;
import de.george.g3dit.gui.components.search.ModularSearchPanel;
import de.george.g3dit.gui.components.search.PropertySearchFilterBuilder;
import de.george.g3dit.gui.components.search.SearchFilter;
import de.george.g3dit.gui.table.TableColumnDef;
import de.george.g3dit.gui.table.TableUtil;
import de.george.g3dit.gui.table.TableUtil.SortableEventTable;
import de.george.g3dit.tab.EditorTab;
import de.george.g3dit.tab.EditorTab.EditorTabType;
import de.george.g3dit.tab.archive.EditorArchiveTab;
import de.george.g3dit.util.AbstractFileWorker;
import de.george.g3dit.util.ConcurrencyUtil;
import de.george.g3dit.util.ConcurrencyUtil.Awaitable;
import de.george.g3dit.util.Icons;
import de.george.g3dit.util.ImportHelper;
import de.george.g3utils.structure.bCMatrix;
import de.george.g3utils.structure.bCVector;
import de.george.g3utils.util.IOUtils;
import de.george.g3utils.util.Misc;
import de.george.lrentnode.archive.ArchiveEntity;
import de.george.lrentnode.archive.ArchiveFile;
import de.george.lrentnode.archive.ArchiveFile.ArchiveType;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.iterator.ArchiveFileIterator;
import de.george.lrentnode.util.FileUtil;
import net.miginfocom.swing.MigLayout;

public class EntitySearchDialog extends AbstractTableProgressDialog {
	private static final Logger logger = LoggerFactory.getLogger(EntitySearchDialog.class);

	private ModularSearchPanel<eCEntity> searchPanel;
	private JButton btnSearch;

	private JButton btnImport;
	private JButton btnShowOnMap;
	private JCheckBox cbPositionFromClipboard;
	private JCheckBox cbRandomGuids;

	private EventList<Result> results;
	private SortableEventTable<Result> table;

	public static final EntitySearchDialog openEntitySearch(EditorContext ctx) {
		EntitySearchDialog searchDialog = new EntitySearchDialog(ctx, "Entity-Suche");
		searchDialog.open();
		return searchDialog;
	}

	public static final EntitySearchDialog openEntitySearch(EditorContext ctx, SearchFilter<eCEntity> filter) {
		EntitySearchDialog dialog = openEntitySearch(ctx);
		dialog.searchPanel.loadFilter(filter);
		dialog.btnSearch.doClick(0);
		return dialog;
	}

	public static final EntitySearchDialog openEntitySearchName(EditorContext ctx, NameEntityFilter.MatchMode matchMode, String searchText,
			boolean regex) {
		return openEntitySearch(ctx, new NameEntityFilter(matchMode, searchText, regex, null));
	}

	public static final EntitySearchDialog openEntitySearchGuid(EditorContext ctx, GuidEntityFilter.MatchMode matchMode, String guid) {
		return openEntitySearch(ctx, new GuidEntityFilter(matchMode, guid));
	}

	public static final EntitySearchDialog openEntitySearchPosition(EditorContext ctx, bCVector position, float tolerance,
			boolean ignoreY) {
		return openEntitySearch(ctx, new PositionEntityFilter(position, tolerance, ignoreY));
	}

	private EntitySearchDialog(EditorContext ctx, String title) {
		super(ctx, title);
		setSize(1000, 700);
	}

	private static final TableColumnDef COLUMN_POSITION = TableColumnDef.withName("Position").maxSize(60)
			.comparator(Comparator.naturalOrder()).b();

	private static final TableColumnDef COLUMN_NAME = TableColumnDef.withName("Name").size(300).b();

	private static final TableColumnDef COLUMN_GUID = TableColumnDef.withName("Guid").size(300).b();

	private static final TableColumnDef COLUMN_PATH = TableColumnDef.withName("Path").size(350).b();

	@Override
	public void open() {
		super.open();
		searchPanel.initFocus();
	}

	@Override
	public JComponent createContentPanel() {
		JPanel mainPanel = new JPanel(new MigLayout("fill"));

		searchPanel = new ModularSearchPanel(ctx, EntityNameSearchFilterBuilder.class, EntityGuidSearchFilterBuilder.class,
				EntityPositionSearchFilterBuilder.class, PropertySearchFilterBuilder.class, ByteSearchFilterBuilder.class);
		btnSearch = registerAction("Suchen", Icons.getImageIcon(Icons.Action.FIND), this::doWork, true);
		JButton btnErase = new JButton(Icons.getImageIcon(Icons.Action.ERASE));
		btnErase.setFocusable(false);
		btnErase.setToolTipText("Suche leeren");
		btnErase.addActionListener(e -> searchPanel.reset(false));

		mainPanel.add(searchPanel.getComponent(), "split 3, width 100%, spanx");
		mainPanel.add(btnSearch, "height 23!");
		mainPanel.add(btnErase, "width 23!, height 23!, wrap");

		cbPositionFromClipboard = new JCheckBox("Position aus Zwischenablage");
		cbRandomGuids = new JCheckBox("Zufällige Guid");
		btnImport = new JButton("Entity importieren", Icons.getImageIcon(Icons.IO.IMPORT));
		btnImport.setEnabled(false);
		btnShowOnMap = new JButton("Karte", Icons.getImageIcon(Icons.Misc.MAP));
		btnShowOnMap.setEnabled(false);

		mainPanel.add(cbPositionFromClipboard, "gapx push");
		mainPanel.add(cbRandomGuids, "gapleft 5");
		mainPanel.add(btnImport, "gapleft 5");
		mainPanel.add(btnShowOnMap, "gapleft 5, wrap");

		results = new BasicEventList<>();
		SortedList<Result> sortedResults = new SortedList<>(results,
				Comparator.comparing(Result::getRawPath).thenComparing(Result::getPosition));
		table = TableUtil.createSortableTable(sortedResults, Result.class, COLUMN_POSITION, COLUMN_NAME, COLUMN_GUID, COLUMN_PATH);
		appendBarAndTable(mainPanel, table.table);

		setEntryActivationListener(i -> table.getRowAt(i).open());

		// Import entity
		table.addSelectionListener(e -> assessEnableImport(ctx.getEditor().getSelectedTab()));
		btnImport.addActionListener(a -> {
			EditorArchiveTab archiveTab = ctx.getEditor().<EditorArchiveTab>getSelectedTab(EditorTabType.Archive).get();
			Result entry = table.getSelectedRow().get();
			ArchiveEntity entity = (ArchiveEntity) entry.getEntity().clone();
			if (cbPositionFromClipboard.isSelected()) {
				Optional<bCMatrix> matrix = Misc.stringToMatrix(IOUtils.getClipboardContent());
				if (matrix.isPresent()) {
					entity.setToWorldMatrix(matrix.get());
				} else if (!TaskDialogs.ask(ctx.getParentWindow(), "Zwischenablage enthält keine Positionsdaten",
						"Soll der Import, unter Verwendung der originalen Position, trotzdem fortgesetzt werden?")) {
					return;
				}
			}

			if (ImportHelper.importFromList(ImmutableList.of(entity), archiveTab.getCurrentFile(), ctx.getParentWindow(),
					cbRandomGuids.isSelected())) {
				archiveTab.refreshTree(false);
			}
		});

		btnShowOnMap.addActionListener(a -> table.getSelectedRows().forEach(Result::showOnMap));
		TableUtil.enableOnGreaterEqual(table.table, btnShowOnMap, 1);

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
			progressBar.setString("Ungültige Filtereinstellungen");
			return;
		}

		results.clear();

		// Geöffnete Dateien
		List<File> openFiles = new ArrayList<>();
		for (EditorArchiveTab tab : ctx.getEditor().<EditorArchiveTab>getTabs(EditorTabType.Archive)) {
			ArchiveFile currentFile = tab.getCurrentFile();
			FileDescriptor fileDescriptor = tab.getFileDescriptor();
			for (eCEntity entity : currentFile.getEntities()) {
				if (filter.matches(entity)) {
					results.add(new MemoryResult(new EntityDescriptor(entity, currentFile.getEntityPosition(entity), fileDescriptor),
							entity.getWorldPosition(), tab));
				}
			}

			tab.getDataFile().ifPresent(openFiles::add);
		}

		worker = new SearchEntityWorker(ctx.getFileManager().worldFilesCallable(), openFiles, filter);
		executeWorker();
	}

	@Subscribe
	public void onSelectTab(EditorTab.SelectedEvent event) {
		assessEnableImport(event.getTab());
	}

	@Subscribe
	public void onTabOpenend(EditorTab.OpenedEvent event) {
		onTabEvent(event.getTab(), this::onFileOpened);
	}

	@Subscribe
	public void onTabClosed(EditorTab.ClosedEvent event) {
		onTabEvent(event.getTab(), this::onFileClosed);
	}

	private void onTabEvent(EditorTab tab, Consumer<EditorArchiveTab> callback) {
		if (tab.type() == EditorTabType.Archive) {
			EditorArchiveTab archiveTab = (EditorArchiveTab) tab;
			if (!archiveTab.getDataFile().isPresent()) {
				return;
			}

			callback.accept(archiveTab);
		}
	}

	private void assessEnableImport(Optional<EditorTab> tab) {
		btnImport.setEnabled(table.getSelectedRow().isPresent() && tab.isPresent() && tab.get().type() == EditorTabType.Archive);
	}

	private void onFileClosed(EditorArchiveTab tab) {
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

	private void onFileOpened(EditorArchiveTab tab) {
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

		protected SearchEntityWorker(Callable<List<File>> fileProvider, List<File> openFiles, SearchFilter<eCEntity> filter) {
			super(fileProvider, openFiles, "Ermittele zu durchsuchende Dateien...", "%d/%d Dateien durchsucht", "Suche abgeschlossen");
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
				ArchiveFileIterator iterator = new ArchiveFileIterator(files);
				while (iterator.hasNext()) {
					ArchiveFile aFile = iterator.next();
					FileDescriptor fileDescriptor = new FileDescriptor(iterator.nextFile(), aFile.getArchiveType());
					for (eCEntity entity : aFile.getEntities()) {
						if (isCancelled()) {
							return;
						}
						try {
							if (filter.matches(entity)) {
								publish(new FileResult(new EntityDescriptor(entity, aFile.getEntityPosition(entity), fileDescriptor),
										entity.getWorldPosition()));
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
			return String.format("Suche abgeschlossen (%d Entities gefunden)", results.size());
		}
	}

	public abstract class Result {
		protected final EntityDescriptor descriptor;
		protected final bCVector worldPosition;

		public Result(EntityDescriptor descriptor, bCVector worldPosition) {
			this.descriptor = descriptor;
			this.worldPosition = worldPosition;
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

		public void showOnMap() {
			EntityMap.getInstance(ctx).addEntity(descriptor, worldPosition);
		}

		public abstract ArchiveEntity getEntity();
	}

	public class FileResult extends Result {
		public FileResult(EntityDescriptor descriptor, bCVector worldPosition) {
			super(descriptor, worldPosition);
		}

		public File getFile() {
			return descriptor.getFile().getPath();
		}

		@Override
		public String getPath() {
			return getFile().getName();
		}

		@Override
		public void open() {
			if (!ctx.getEditor().openFile(getFile())) {
				return;
			}

			Optional<EditorArchiveTab> archiveTab = ctx.getEditor().getSelectedTab(EditorTabType.Archive);
			if (archiveTab.isPresent()) {
				eCEntity entity = archiveTab.get().getCurrentFile().getEntityByGuid(getGuid()).orElse(null);
				archiveTab.get().getEntityTree().selectEntity(entity);
			}
		}

		@Override
		public ArchiveEntity getEntity() {
			try {
				return (ArchiveEntity) FileUtil.openArchive(getFile(), false).getEntityByGuid(getGuid()).orElse(null);
			} catch (Exception e) {
				logger.warn("Fehler beim Öffnen von Datei: ", e);
				TaskDialogs.showException(e);
				return null;
			}
		}

		public MemoryResult toMemoryResult(EditorArchiveTab archiveTab) {
			return new MemoryResult(descriptor, worldPosition, archiveTab);
		}
	}

	public class MemoryResult extends Result {
		private WeakReference<EditorArchiveTab> weakTab;

		public MemoryResult(EntityDescriptor descriptor, bCVector worldPosition, EditorArchiveTab archiveTab) {
			super(descriptor, worldPosition);
			weakTab = new WeakReference<>(archiveTab);
		}

		@Override
		public String getRawPath() {
			EditorArchiveTab archiveTab = weakTab.get();
			if (archiveTab != null) {
				return archiveTab.getTitle();
			}
			return "<Inzwischen geschlossen>";
		}

		@Override
		public String getPath() {
			EditorArchiveTab archiveTab = weakTab.get();
			if (archiveTab != null) {
				return "Geladen: " + archiveTab.getTitle();
			}
			return "<Inzwischen geschlossen>";
		}

		@Override
		public void open() {
			EditorArchiveTab archiveTab = weakTab.get();
			if (archiveTab != null) {
				ctx.getEditor().selectTab(archiveTab);
				eCEntity entity = archiveTab.getCurrentFile().getEntityByGuid(getGuid()).orElse(null);
				archiveTab.getEntityTree().selectEntity(entity);
			}
		}

		@Override
		public ArchiveEntity getEntity() {
			return (ArchiveEntity) Optional.ofNullable(weakTab.get()).flatMap(t -> t.getCurrentFile().getEntityByGuid(getGuid()))
					.orElse(null);
		}

		public FileResult toFileResult(File file) {
			FileDescriptor fileDescriptor = new FileDescriptor(file, Optional.ofNullable(weakTab.get())
					.map(EditorArchiveTab::getCurrentFile).map(ArchiveFile::getArchiveType).orElse(ArchiveType.Lrentdat));
			return new FileResult(descriptor.withFile(fileDescriptor), worldPosition);
		}
	}
}

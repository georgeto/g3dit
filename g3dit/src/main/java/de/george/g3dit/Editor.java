package de.george.g3dit;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Window;
import java.awt.dnd.DropTarget;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Locale.Category;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.logging.Level;

import javax.swing.JFrame;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.ezware.dialog.task.TaskDialogs;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.io.Files;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.jgoodies.looks.Options;
import com.jidesoft.plaf.LookAndFeelFactory;
import com.jme3.system.JmeSystem;
import com.tulskiy.keymaster.common.Provider;

import de.ailis.oneinstance.OneInstance;
import de.ailis.oneinstance.OneInstanceListener.InstanceAction;
import de.danielbechler.diff.node.DiffNode;
import de.danielbechler.diff.node.ToMapPrintingVisitor;
import de.george.g3dit.cache.CacheManager;
import de.george.g3dit.cache.Caches;
import de.george.g3dit.cache.EntityCache;
import de.george.g3dit.cache.LightCache;
import de.george.g3dit.cache.NavCache;
import de.george.g3dit.cache.StringtableCache;
import de.george.g3dit.cache.TemplateCache;
import de.george.g3dit.cache.TemplateCache.TemplateCacheEntry;
import de.george.g3dit.check.EntityDescriptor;
import de.george.g3dit.check.FileDescriptor;
import de.george.g3dit.gui.components.tab.JSplittedTypedTabbedPane;
import de.george.g3dit.gui.components.tab.JSplittedTypedTabbedPane.Side;
import de.george.g3dit.gui.components.tab.TabCloseEvent;
import de.george.g3dit.gui.components.tab.TabSelectedEvent;
import de.george.g3dit.gui.dialogs.DisplayTextDialog;
import de.george.g3dit.gui.theme.EditorUnitConverter;
import de.george.g3dit.gui.theme.ThemeInfo;
import de.george.g3dit.gui.theme.ThemeManager;
import de.george.g3dit.jme.EntityViewer;
import de.george.g3dit.jme.JmeTransparentDesktopSystem;
import de.george.g3dit.settings.EditorOptions;
import de.george.g3dit.settings.KryoFileOptionStore;
import de.george.g3dit.settings.MigratableOptionStore;
import de.george.g3dit.settings.OptionStore;
import de.george.g3dit.settings.OptionStoreMigrator;
import de.george.g3dit.settings.SettingsUpdatedEvent;
import de.george.g3dit.tab.EditorAbstractFileTab;
import de.george.g3dit.tab.EditorTab;
import de.george.g3dit.tab.EditorTab.EditorTabType;
import de.george.g3dit.tab.archive.EditorArchiveTab;
import de.george.g3dit.tab.effectmap.EditorEffectMapTab;
import de.george.g3dit.tab.negcircle.EditorNegCircleTab;
import de.george.g3dit.tab.negzone.EditorNegZoneTab;
import de.george.g3dit.tab.prefpath.EditorPrefPathTab;
import de.george.g3dit.tab.secdat.EditorSecdatTab;
import de.george.g3dit.tab.template.EditorTemplateTab;
import de.george.g3dit.util.ConcurrencyUtil;
import de.george.g3dit.util.Dialogs;
import de.george.g3dit.util.FileDialogWrapper;
import de.george.g3dit.util.FileManager;
import de.george.g3dit.util.NavMapManager;
import de.george.g3dit.util.event.FileDropListener;
import de.george.g3dit.util.event.VisibilityManager;
import de.george.g3utils.gui.SwingUtils;
import de.george.g3utils.io.G3FileReaderEx;
import de.george.g3utils.util.Holder;
import de.george.g3utils.util.IOUtils;
import de.george.lrentnode.archive.ArchiveFile;
import de.george.lrentnode.archive.SecDat;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.diff.EntityDiffer;
import de.george.lrentnode.util.FileUtil;
import de.george.navmap.sections.NavMap;
import net.tomahawk.XFileDialog;
import one.util.streamex.StreamEx;

public class Editor implements EditorContext {
	private static final Logger logger = LoggerFactory.getLogger(Editor.class);

	public static final String EDITOR_TITLE = "g3dit";
	public static final String EDITOR_VERSION = IOUtils.getManifestAttribute(Editor.class, "g3dit-Version");
	public static final String EDITOR_CONFIG_FOLDER = "config";

	private EventBus eventBus;

	private CacheManager cacheManager;
	private FileManager fileManager;
	private NavMapManager navMapManager;
	private Provider hotKeyProvider;
	private VisibilityManager visibilityManager;
	private JFrame frame;
	private MainMenu mainMenu;
	private StatusBar statusBar;

	private ListeningExecutorService executorService;
	private IpcMonitor ipcMonitor;

	private OptionStore optionStore;

	private List<EditorTab> tabs;

	private JSplittedTypedTabbedPane<EditorTab> tbTabs;

	@Override
	public OptionStore getOptionStore() {
		return optionStore;
	}

	public Editor(final String[] args) {
		registerOneInstanceListener(args);

		try {
			init(args);
		} catch (Exception e) {
			logger.error("Unerwarteter Fehler bei der Programmausführung.", e);
			logger.error("Beende g3dit.");
			System.exit(EditorCli.EXIT_CODE_ERROR);
		}
	}

	private void init(final String[] args) {
		Locale.setDefault(Category.FORMAT, Locale.UK);

		eventBus = new EventBus("g3dit");
		eventBus.register(this);
		executorService = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());

		// Einstellungen
		File configDir = new File(EDITOR_CONFIG_FOLDER);
		if (!configDir.exists()) {
			configDir.mkdir();
		}
		loadOptionStore();

		fileManager = new FileManager(this);

		navMapManager = new NavMapManager(this);

		// Logging auf der Konsole deaktivieren
		XFileDialog.setTraceLevel(0);

		// java.util.Logging umleiten nach slf4j
		SLF4JBridgeHandler.removeHandlersForRootLogger();
		SLF4JBridgeHandler.install();
		java.util.logging.Logger.getLogger("").setLevel(Level.WARNING);

		ch.qos.logback.classic.Logger diffLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("de.danielbechler.diff");
		diffLogger.setLevel(ch.qos.logback.classic.Level.INFO);

		ch.qos.logback.classic.Logger webLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("cz.vutbr.web");
		webLogger.setLevel(ch.qos.logback.classic.Level.ERROR);

		JmeSystem.setSystemDelegate(new JmeTransparentDesktopSystem());

		EditorUnitConverter.register();
		applyTheme(true);
		frame = new JFrame();

		hotKeyProvider = Provider.getCurrentProvider(true);
		visibilityManager = new VisibilityManager(frame, hotKeyProvider);

		ipcMonitor = new IpcMonitor();

		tabs = new ArrayList<>();

		registerCaches();

		createAndShowGUI();

		loadCaches();

		// Globales Drag'n'Drop
		FileFilter dropFilter = f -> f.isFile() && FileDialogWrapper.COMBINED_FILTER.accept(f);
		FileDropListener dropListener = new FileDropListener(dropFilter);
		dropListener.eventBus().register(new EditorFileDropSubscriber(Side.LEFT));
		new DropTarget(frame, dropListener);

		// Drag'n'Drop auf die einzelnen TabbedPanes
		tbTabs.addDropListener(Side.LEFT, new FileDropListener(dropFilter)).eventBus().register(new EditorFileDropSubscriber(Side.LEFT));
		tbTabs.addDropListener(Side.RIGHT, new FileDropListener(dropFilter)).eventBus().register(new EditorFileDropSubscriber(Side.RIGHT));

		if (!processArguments(null, args, new PrintWriter(System.out))) {
			System.exit(EditorCli.EXIT_CODE_ERROR);
		}
	}

	private boolean processArguments(File workingDir, String[] args, PrintWriter writer) {
		return new EditorCli(this, workingDir, writer).processCommandLine(args, false);
	}

	private void diffEntity(String base, String mine, eCEntity baseRoot, eCEntity mineRoot) throws IOException {
		// TODO: Also diff DynamicEntityContext
		DiffNode diff = new EntityDiffer(true).diff(mineRoot, baseRoot);
		if (!diff.hasChanges()) {
			String binaryCompare = getOptionStore().get(EditorOptions.Path.BINARY_COMPARE);
			if (binaryCompare.isEmpty()) {
				TaskDialogs.error(frame, "", "Kein Vergleichsprogramm für Binärdateien konfiguriert.");
				return;
			}

			Runtime.getRuntime().exec(binaryCompare.replace("%base", base).replace("%mine", mine));
		}

		ToMapPrintingVisitor mapPrintingVisitor = new ToMapPrintingVisitor(mineRoot, baseRoot);
		diff.visit(mapPrintingVisitor);
		SwingUtilities.invokeLater(() -> {
			DisplayTextDialog dialog = new DisplayTextDialog(
					String.format("Dateivergleich - [%s - %s]", new File(base).getName(), new File(mine).getName()),
					mapPrintingVisitor.getMessagesAsString(), frame, false);
			// dialog.setLocationRelativeTo(editor.getOwner());
			dialog.setVisible(true);
		});
	}

	private void diffNavigationMap(String base, String mine) throws IOException {
		String textCompare = getOptionStore().get(EditorOptions.Path.TEXT_COMPARE);
		if (textCompare.isEmpty()) {
			TaskDialogs.error(frame, "", "Kein Vergleichsprogramm für Textdateien konfiguriert.");
			return;
		}

		// NavigationMap
		try (G3FileReaderEx baseFile = new G3FileReaderEx(new File(base)); G3FileReaderEx mineFile = new G3FileReaderEx(new File(mine))) {
			NavMap baseMap = new NavMap(baseFile);
			File baseDump = File.createTempFile(baseFile.getFileName(), null);
			baseDump.deleteOnExit();
			baseMap.saveText(baseDump);
			NavMap mineMap = new NavMap(mineFile);
			File mineDump = File.createTempFile(mineFile.getFileName(), null);
			mineDump.deleteOnExit();
			mineMap.saveText(mineDump);
			Runtime.getRuntime()
					.exec(textCompare.replace("%base", baseDump.getAbsolutePath()).replace("%mine", mineDump.getAbsolutePath()));
		}
	}

	public void diffFiles(File baseFile, File mineFile) {
		try {
			String base = baseFile.getAbsolutePath();
			String mine = mineFile.getAbsolutePath();
			String baseExt = Files.getFileExtension(base).toLowerCase();
			String mineExt = Files.getFileExtension(mine).toLowerCase();
			if (!baseExt.equals(mineExt)) {
				TaskDialogs.error(frame, "", "Zu vergleichende Dateien haben unterschiedliche Dateiendungen.");
				return;
			}

			if (FileUtils.contentEquals(baseFile, mineFile)) {
				TaskDialogs.inform(frame, "", "Zu vergleichende Dateien sind identisch.");
			}

			eCEntity baseRoot;
			eCEntity mineRoot;
			switch (baseExt) {
				case "node", "lrentdat" -> {
					baseRoot = FileUtil.openArchive(baseFile, true).getGraph();
					mineRoot = FileUtil.openArchive(mineFile, true).getGraph();
					diffEntity(base, mine, baseRoot, mineRoot);
				}
				case "tple" -> {
					baseRoot = FileUtil.openTemplate(baseFile).getGraph();
					mineRoot = FileUtil.openTemplate(mineFile).getGraph();
					diffEntity(base, mine, baseRoot, mineRoot);
				}
				case "xnav" -> diffNavigationMap(base, mine);
				default -> {
					logger.error("Zu diffende Dateien haben nicht unterstützte Dateiendung '{}'.", baseExt);
					return;
				}
			}
		} catch (IOException e) {
			logger.error("Fehler beim Öffnen der zu vergleichenden Dateien.", e);
			TaskDialogs.showException(e);
		} catch (Exception e) {
			logger.error("Fehler beim Diffen.", e);
			TaskDialogs.showException(e);
		}
	}

	private void registerOneInstanceListener(String[] args) {
		// Early exit if invalid args are passed or help is requested.
		if (!new EditorCli(this, null, new PrintWriter(System.out)).processCommandLine(args, true)) {
			System.exit(EditorCli.EXIT_CODE_ERROR);
		}

		// Install listener which processes the start of secondary instances
		OneInstance.getInstance().setListener((workingDir, args1, writer, exitCode) -> {
			SwingUtilities.invokeLater(() -> {
				try {
					frame.setState(Frame.NORMAL);
					exitCode.accept(processArguments(workingDir, args1, writer) ? EditorCli.EXIT_CODE_OK : EditorCli.EXIT_CODE_ERROR);
				} finally {
					writer.close();
				}
			});
			return InstanceAction.Wait;
		});

		OneInstance oneInstance = OneInstance.getInstance();

		// Exit the application if we are NOT the first instance and the
		// real first instance decided that there can be only ONE instance.
		Holder<Integer> exitCode = new Holder<>(EditorCli.EXIT_CODE_OK);
		if (!oneInstance.register(Editor.class, args, System.out, exitCode::hold)) {
			System.exit(exitCode.held());
		}
	}

	private void loadOptionStore() {
		String basePath = EDITOR_CONFIG_FOLDER + File.separator + EDITOR_TITLE;
		File kryoStoreFile = new File(basePath + ".options");
		optionStore = new KryoFileOptionStore(kryoStoreFile);

		if (optionStore instanceof MigratableOptionStore) {
			new OptionStoreMigrator((MigratableOptionStore) optionStore).migrate();
		}
	}

	private void registerCaches() {
		// Caches
		cacheManager = new CacheManager(this);

		cacheManager.registerCache(NavCache.class, new NavCache(this));
		cacheManager.registerCache(TemplateCache.class, new TemplateCache(this));
		cacheManager.registerCache(EntityCache.class, new EntityCache(this));
		cacheManager.registerCache(LightCache.class, new LightCache(this));
		cacheManager.registerCache(StringtableCache.class, new StringtableCache(this));
		// cacheManager.registerCache(LowPolyMeshCache.class, new LowPolyMeshCache(this));
	}

	private void loadCaches() {
		getCacheManager().getCache(NavCache.class).load();
		getCacheManager().getCache(TemplateCache.class).load();
		getCacheManager().getCache(EntityCache.class).load();
		getCacheManager().getCache(StringtableCache.class).load();
		// getCacheManager().getCache(LowPolyMeshCache.class).load();
	}

	public final void createAndShowGUI() {
		// GUI initialisieren
		frame.setSize(optionStore.get(EditorOptions.MainWindow.SIZE));
		frame.setLocation(optionStore.get(EditorOptions.MainWindow.LOCATION));
		frame.setExtendedState(optionStore.get(EditorOptions.MainWindow.EXTENDED_STATE));
		frame.setTitle(EDITOR_TITLE);
		frame.setIconImage(SwingUtils.getG3Icon());

		Options.setUseNarrowButtons(true);
		LookAndFeelFactory.installJideExtension(LookAndFeelFactory.EXTENSION_STYLE_VSNET_WITHOUT_MENU);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		ToolTipManager.sharedInstance().setDismissDelay(6000);

		// Hauptfenster
		tbTabs = new JSplittedTypedTabbedPane<>(true);
		frame.add(tbTabs.getComponent(), BorderLayout.CENTER);
		tbTabs.eventBus().register(this);

		// Menü
		mainMenu = new MainMenu(this);
		frame.setJMenuBar(mainMenu);

		// StatusBar
		statusBar = new StatusBar(this);
		statusBar.addCacheStatus(TemplateCache.class, e -> (int) e.getEntities().count());
		statusBar.addCacheStatus(NavCache.class, e -> e.getZones().size() + e.getPaths().size());
		statusBar.addCacheStatus(EntityCache.class, e -> e.getGuids().size());
		frame.add(statusBar, BorderLayout.SOUTH);

		// Programm wird geschlossen
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				// Alle Tabs schließen
				Iterator<EditorTab> iterTabs = tabs.iterator();
				while (iterTabs.hasNext()) {
					EditorTab tab = iterTabs.next();

					tbTabs.selectTab(tab);
					if (!tab.onClose(true)) {
						return;
					}
					tbTabs.removeTab(tab);
					iterTabs.remove();
					tab.eventBus().unregister(this);
				}

				if (navMapManager.isNavMapChanged()) {
					switch (Dialogs.askSaveChanges(frame, "Änderungen an NavMap vor dem Beenden speichern?")) {
						case Yes:
							navMapManager.saveMap();
						case No:
							break;
						case Cancel:
							return;
					}
				}

				cacheManager.save();

				// Einstellungen Speichern
				saveSettings();

				hotKeyProvider.close();

				System.exit(EditorCli.EXIT_CODE_OK);
			}
		});

		frame.setVisible(true);
	}

	@Override
	public EventBus eventBus() {
		return eventBus;
	}

	public void selectTab(EditorTab tab) {
		tbTabs.selectTab(tab);
	}

	public Optional<EditorTab> getSelectedTab() {
		return tbTabs.getSelectedTab();
	}

	@SuppressWarnings("unchecked")
	public <T extends EditorTab> Optional<T> getSelectedTab(EditorTab.EditorTabType filterByType) {
		return (Optional<T>) tbTabs.getSelectedTab().filter(tab -> tab.type() == filterByType);
	}

	public boolean closeTab(EditorTab tab) {
		return tbTabs.closeTab(tab);
	}

	public List<EditorTab> getTabs() {
		return tabs;
	}

	@SuppressWarnings("unchecked")
	public <T extends EditorTab> StreamEx<T> getTabs(EditorTabType type) {
		return (StreamEx<T>) StreamEx.of(tabs).filterBy(EditorTab::type, type);
	}

	public <T extends EditorTab> StreamEx<T> getTabsInDataFolders(EditorTabType type) {
		return this.<T>getTabs(type).filter(tab -> {
			if (!(tab instanceof EditorAbstractFileTab fileTab)) {
				return false;
			}

			return fileTab.getDataFile().isPresent() && (fileManager.isInPrimaryDataFolder(fileTab.getDataFile().get())
					|| fileManager.isInSecondaryDataFolder(fileTab.getDataFile().get()));
		});
	}

	private void openTab(EditorTab tab, Side side) {
		tabs.add(tab);
		tbTabs.addTab(tab, side);
		tbTabs.selectTab(tab);
		tab.getTabContent().requestFocus();
		eventBus().post(new EditorTab.OpenedEvent(tab));
	}

	@Subscribe
	public void onCloseTab(TabCloseEvent<EditorTab> event) {
		EditorTab tab = event.getTab();
		if (tab.onClose(false)) {
			tabs.remove(tab);
			event.setCancelled(false);
			eventBus().post(new EditorTab.ClosedEvent(tab));
			runGC();
		} else {
			event.setCancelled(true);
		}
	}

	@Subscribe
	public void onTabSelected(TabSelectedEvent<EditorTab> event) {
		refreshTitle();
		statusBar.setFileStatus(null);
		statusBar.setExtensionPanel(null);
		event.getTab().ifPresent(tab -> tab.getTabContent().requestFocus());
		eventBus().post(new EditorTab.SelectedEvent(event.getPreviousTab(), event.getTab()));
	}

	@Subscribe
	public void onTabStateChange(EditorTab.StateChangedEvent event) {
		tbTabs.updateTab(event.getTab());
		refreshTitle();
	}

	@Subscribe
	public void onSettingsUpdated(SettingsUpdatedEvent event) {
		// Data folder aliases could have changed.
		refreshTitle();
		// Add data folders to file dialog places
		FileDialogWrapper.setPlaces(getFileManager().getDataFolders());
		// Apply theme
		applyTheme(false);
	}

	private LookAndFeel nativeLookAndFeel;

	private void applyTheme(boolean early) {
		ThemeInfo theme = getOptionStore().get(EditorOptions.TheVoid.THEME);
		ThemeManager.setThemeOrDefault(theme, early);
	}

	private void refreshTitle() {
		Optional<EditorTab> tab = getSelectedTab();
		if (tab.isPresent()) {
			frame.setTitle(tab.get().getEditorTitle());
		} else {
			frame.setTitle(Editor.EDITOR_TITLE);
		}
	}

	public boolean openFile(File file) {
		return openFile(file, Side.LEFT);
	}

	public boolean openFile(File file, Side side) {
		EditorAbstractFileTab tab = null;
		String fileExt = Files.getFileExtension(file.getName()).toLowerCase();

		switch (fileExt) {
			case "lrentdat":
			case "node":
				tab = new EditorArchiveTab(this);
				break;
			case "secdat":
				tab = new EditorSecdatTab(this);
				break;
			case "tple":
				tab = new EditorTemplateTab(this);
				break;
			case "efm":
				tab = new EditorEffectMapTab(this);
				break;
			case "xcmsh":
			case "xact":
			case "xlmsh":
				EntityViewer.getInstance(this).showMesh(file.getAbsolutePath(), 0);
				return true;
			default:
				return false;
		}

		if (tab.openFile(file)) {
			openTab(tab, side);
			mainMenu.addRecentFile(file.getAbsolutePath());
			runGC();
			return true;
		}
		return false;
	}

	public boolean openOrSelectFile(File file) {
		for (EditorTab tab : getTabs()) {
			if (!(tab instanceof EditorAbstractFileTab fileTab)) {
				continue;
			}
			if (fileTab.getDataFile().filter(file::equals).isPresent()) {
				selectTab(fileTab);
				return true;
			}
		}

		return openFile(file);
	}

	public void openArchive(ArchiveFile file, boolean isChanged) {
		openArchive(file, Side.LEFT, isChanged);
	}

	public void openArchive(ArchiveFile file, Side side, boolean isChanged) {
		EditorArchiveTab tab = new EditorArchiveTab(this);
		tab.setCurrentFile(file, null);
		tab.setFileChanged(isChanged);
		openTab(tab, side);
	}

	public void openSecdat(SecDat file, boolean isChanged) {
		openSecdat(file, Side.LEFT, isChanged);
	}

	public void openSecdat(SecDat file, Side side, boolean isChanged) {
		EditorSecdatTab tab = new EditorSecdatTab(this);
		tab.setCurrentFile(file, null);
		tab.setFileChanged(isChanged);
		openTab(tab, side);
	}

	public EditorNegZoneTab openEditNegZones() {
		Optional<EditorTab> tab = getTabs(EditorTabType.NegZone).findFirst();
		if (tab.isPresent()) {
			tbTabs.selectTab(tab.get());
			return (EditorNegZoneTab) tab.get();
		} else {
			EditorNegZoneTab negZoneTab = new EditorNegZoneTab(this);
			openTab(negZoneTab, Side.LEFT);
			return negZoneTab;
		}
	}

	public EditorNegCircleTab openEditNegCircles() {
		Optional<EditorTab> tab = getTabs(EditorTabType.NegCircle).findFirst();
		if (tab.isPresent()) {
			tbTabs.selectTab(tab.get());
			return (EditorNegCircleTab) tab.get();

		} else {
			EditorNegCircleTab negCircleTab = new EditorNegCircleTab(this);
			openTab(negCircleTab, Side.LEFT);
			return negCircleTab;
		}
	}

	public EditorPrefPathTab openEditPrefPaths() {
		Optional<EditorTab> tab = getTabs(EditorTabType.PrefPath).findFirst();
		if (tab.isPresent()) {
			tbTabs.selectTab(tab.get());
			return (EditorPrefPathTab) tab.get();
		} else {
			EditorPrefPathTab prefPathTab = new EditorPrefPathTab(this);
			openTab(prefPathTab, Side.LEFT);
			return prefPathTab;
		}
	}

	public boolean openEntity(EntityDescriptor entityDescriptor) {
		if (!openOrSelectFile(entityDescriptor.getFile().getPath())) {
			return false;
		}

		switch (entityDescriptor.getFile().getType()) {
			case Lrentdat:
			case Node:
				Optional<EditorArchiveTab> selectedTab = getSelectedTab(EditorTabType.Archive);
				return selectedTab.get().selectEntity(entityDescriptor);
			default:
				return true;
		}
	}

	public static Optional<EntityDescriptor> getEntityDescriptor(String guid, File file) {
		Optional<ArchiveFile> archive = FileUtil.openArchiveSafe(file, false, true);
		return archive.flatMap(a -> a.getEntityByGuid(guid))
				.map(entity -> new EntityDescriptor(entity, new FileDescriptor(file, archive.get().getArchiveType())));
	}

	public Optional<EntityDescriptor> getEntityDescriptor(String guid) {
		Optional<EntityDescriptor> fromCache = Caches.entity(this).getFile(guid)
				.flatMap(file -> getEntityDescriptor(guid, file.getPath()));
		if (fromCache.isPresent()) {
			return fromCache;
		}

		return Optional.ofNullable(ConcurrencyUtil
				.processInPartitionsAndGet(file -> getEntityDescriptor(guid, file).orElse(null), fileManager.listWorldFiles(), 3).get());
	}

	public boolean openEntity(String guid) {
		return getEntityDescriptor(guid).map(this::openEntity).orElse(false);
	}

	public boolean modifyEntity(EntityDescriptor entityDescriptor, boolean hidden) {
		// Modify open file or search file
		return false;
	}

	public boolean openTemplate(String guid) {
		Optional<File> templateFile = Caches.template(this).getEntryByGuid(guid).map(TemplateCacheEntry::getFile);
		return templateFile.map(this::openOrSelectFile).orElse(false);
	}

	@Override
	public FileManager getFileManager() {
		return fileManager;
	}

	@Override
	public CacheManager getCacheManager() {
		return cacheManager;
	}

	@Override
	public NavMapManager getNavMapManager() {
		return navMapManager;
	}

	public MainMenu getMainMenu() {
		return mainMenu;
	}

	public StatusBar getStatusBar() {
		return statusBar;
	}

	@Override
	public Editor getEditor() {
		return this;
	}

	@Override
	public Window getParentWindow() {
		return frame;
	}

	@Override
	public ListeningExecutorService getExecutorService() {
		return executorService;
	}

	@Override
	public IpcMonitor getIpcMonitor() {
		return ipcMonitor;
	}

	public void saveCurrentTab() {
		getSelectedTab().ifPresent(EditorTab::onSave);
		runGC();
	}

	public void saveCurrentTabAs() {
		getSelectedTab().ifPresent(EditorTab::onSaveAs);
		runGC();
	}

	@Override
	public void runGC() {
		// Avoid memory problems
		if (optionStore.get(EditorOptions.Misc.OPTIMIZE_MEMORY_USAGE)) {
			System.gc();
		}
	}

	public Provider getHotKeyProvider() {
		return hotKeyProvider;
	}

	/**
	 * Speichert Größe, Position der GUI und alle weiteren Einstellungen
	 */
	private void saveSettings() {
		optionStore.put(EditorOptions.MainWindow.LOCATION, frame.getLocation());
		optionStore.put(EditorOptions.MainWindow.SIZE, frame.getSize());
		optionStore.put(EditorOptions.MainWindow.EXTENDED_STATE,
				frame.getExtendedState() != Frame.ICONIFIED ? frame.getExtendedState() : Frame.NORMAL);
		mainMenu.saveSettings(optionStore);
		optionStore.save();
	}

	private class EditorFileDropSubscriber {
		private Side side;

		public EditorFileDropSubscriber(Side side) {
			this.side = side;
		}

		@Subscribe
		public void onFileDrop(FileDropListener.FileDropEvent drop) {
			SwingUtilities.invokeLater(() -> openFile(drop.getFile(), side));
		}
	}

	public static void main(final String[] args) {
		new Editor(args);
	}
}

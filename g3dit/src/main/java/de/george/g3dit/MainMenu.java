package de.george.g3dit;

import java.awt.Desktop;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ezware.dialog.task.TaskDialogs;
import com.formdev.flatlaf.extras.FlatInspector;
import com.formdev.flatlaf.extras.FlatUIDefaultsInspector;
import com.google.common.eventbus.Subscribe;
import com.teamunify.i18n.I;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.FileAppender;
import de.george.g3dit.cache.NavCache;
import de.george.g3dit.cache.TemplateCache.TemplateCacheEntry;
import de.george.g3dit.check.CheckManager;
import de.george.g3dit.config.ConfigFiles;
import de.george.g3dit.gui.components.EnableGroup;
import de.george.g3dit.gui.components.RecentFileMenu;
import de.george.g3dit.gui.dialogs.AboutDialog;
import de.george.g3dit.gui.dialogs.DisplayTextDialog;
import de.george.g3dit.gui.dialogs.EditGuidWithCommentConfigDialog;
import de.george.g3dit.gui.dialogs.EntitySearchDialog;
import de.george.g3dit.gui.dialogs.FileSearchDialog;
import de.george.g3dit.gui.dialogs.FloatCalcDialog;
import de.george.g3dit.gui.dialogs.GuidFormatsDialog;
import de.george.g3dit.gui.dialogs.ImportStaticLightdataDialog;
import de.george.g3dit.gui.dialogs.NavigateTemplateDialog;
import de.george.g3dit.gui.dialogs.TemplateSearchDialog;
import de.george.g3dit.jme.NodeViewer;
import de.george.g3dit.nav.NavMapSync;
import de.george.g3dit.settings.EditorOptions;
import de.george.g3dit.settings.OptionStore;
import de.george.g3dit.settings.SettingsDialog;
import de.george.g3dit.settings.SettingsUpdatedEvent;
import de.george.g3dit.tab.EditorAbstractFileTab;
import de.george.g3dit.tab.EditorTab;
import de.george.g3dit.tab.EditorTab.EditorTabType;
import de.george.g3dit.tab.archive.EditorArchiveTab;
import de.george.g3dit.tab.template.EditorTemplateTab;
import de.george.g3dit.util.FileDialogWrapper;
import de.george.g3dit.util.Icons;
import de.george.g3dit.util.ImportHelper;
import de.george.g3dit.util.LowPolyGenerator;
import de.george.g3dit.util.NavMapManager.NavMapLoadedEvent;
import de.george.g3dit.util.SettingsHelper;
import de.george.g3dit.util.StringtableHelper;
import de.george.g3utils.gui.SwingUtils;
import de.george.g3utils.util.Holder;
import de.george.g3utils.util.IOUtils;
import de.george.lrentnode.archive.ArchiveFile;
import de.george.lrentnode.archive.SecDat;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.template.TemplateFile;
import de.george.lrentnode.util.FileUtil;
import de.george.navmap.sections.NavMap;
import de.george.navmap.util.NodeExporter;

public class MainMenu extends JMenuBar {
	private static final Logger logger = LoggerFactory.getLogger(MainMenu.class);

	private JMenuItem miSave, miSaveAs, miClose;
	private JMenuItem miImport, miCleanStringtable, miSaveNavMap, miSaveNavMapAs;
	private RecentFileMenu fileMenu;
	private EditorContext ctx;

	private EnableGroup egHasDataFile;

	private JMenuItem miOpenTinyHexer;
	private JMenu muDeveloper;

	public MainMenu(EditorContext ctx) {
		this.ctx = ctx;
		this.ctx.eventBus().register(this);
		this.ctx.getNavMapManager().eventBus().register(this);

		egHasDataFile = new EnableGroup();

		createMenuData();

		createMenuTools();

		createMenuRemote();

		createMenuNavMap();

		createMenuDeveloper();

		createMenuAbout();

		settingsUpdated();
	}

	private void createMenuData() {
		JMenu muData = new JMenu(I.tr("File"));
		muData.setMnemonic(KeyEvent.VK_D);
		add(muData);

		/*
		 * Datei öffnen
		 */
		JMenuItem miOpen = new JMenuItem(I.tr("Open"), Icons.getImageIcon(Icons.IO.OPEN));
		miOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
		miOpen.setMnemonic(KeyEvent.VK_F);
		muData.add(miOpen);
		miOpen.addActionListener(e -> {
			List<File> files = FileDialogWrapper.openFiles(I.tr("Open file"), ctx.getParentWindow(), FileDialogWrapper.COMBINED_FILTER,
					FileDialogWrapper.ARCHIVE_FILTER, FileDialogWrapper.TEMPLATE_FILTER, FileDialogWrapper.SECDAT_FILTER,
					FileDialogWrapper.EFFECT_MAP_FILTER, FileDialogWrapper.MESH_FILTER, FileDialogWrapper.NO_FILTER);
			for (File file : files) {
				ctx.getEditor().openFile(file);
			}
		});

		/*
		 * Zuletzt geöffnete Dateien
		 */
		fileMenu = new RecentFileMenu(I.tr("Recent files"), 10) {
			@Override
			public void onSelectFile(String filePath) {
				File file = new File(filePath);
				ctx.getEditor().openFile(file);
			}
		};
		fileMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK));
		fileMenu.setMnemonic(KeyEvent.VK_D);
		fileMenu.addRecentFiles(ctx.getOptionStore().get(EditorOptions.MainMenu.RECENT_FILES));
		muData.add(fileMenu);

		/*
		 * Neue Datei erstellen
		 */
		JMenu miNew = new JMenu(I.tr("New"));
		miNew.setMnemonic(KeyEvent.VK_N);
		miNew.setIcon(Icons.getImageIcon(Icons.Document.PLUS));
		muData.add(miNew);

		JMenuItem miNewLrentdat = new JMenuItem(I.tr("Lrentdat"), Icons.getImageIcon(Icons.Document.LETTER_L));
		miNewLrentdat.setMnemonic(KeyEvent.VK_L);
		miNewLrentdat.addActionListener(l -> ctx.getEditor().openArchive(FileUtil.createEmptyLrentdat(), true));
		miNew.add(miNewLrentdat);

		JMenuItem miNewNode = new JMenuItem(I.tr("Node"), Icons.getImageIcon(Icons.Document.LETTER_N));
		miNewNode.setMnemonic(KeyEvent.VK_N);
		miNewNode.addActionListener(l -> ctx.getEditor().openArchive(FileUtil.createEmptyNode(), true));
		miNew.add(miNewNode);

		JMenuItem miNewSecdat = new JMenuItem(I.tr("Secdat"), Icons.getImageIcon(Icons.Document.LETTER_S));
		miNewSecdat.setMnemonic(KeyEvent.VK_S);
		miNewSecdat.addActionListener(l -> ctx.getEditor().openSecdat(new SecDat(), true));
		miNew.add(miNewSecdat);

		JMenuItem miCompare = new JMenuItem(I.tr("Compare"));
		miCompare.setMnemonic(KeyEvent.VK_C);
		miCompare.setIcon(Icons.getImageIcon(Icons.Action.DIFF));
		muData.add(miCompare);
		miCompare.addActionListener(l -> {
			File base = FileDialogWrapper.openFile(I.tr("Original file"), ctx.getParentWindow());
			File mine = FileDialogWrapper.openFile(I.tr("Modified file"), ctx.getParentWindow());
			if (base != null && mine != null) {
				ctx.getEditor().diffFiles(base, mine);
			}
		});

		muData.addSeparator();

		/*
		 * Speichern
		 */
		miSave = new JMenuItem(I.tr("Save"), Icons.getImageIcon(Icons.IO.SAVE));
		miSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
		miSave.setMnemonic(KeyEvent.VK_S);
		muData.add(miSave);
		miSave.addActionListener(l -> ctx.getEditor().saveCurrentTab());

		/*
		 * Speichern unter
		 */
		miSaveAs = new JMenuItem(I.tr("Save as"), Icons.getImageIcon(Icons.IO.SAVE_AS));
		miSaveAs.setMnemonic(KeyEvent.VK_U);
		muData.add(miSaveAs);
		miSaveAs.addActionListener(l -> ctx.getEditor().saveCurrentTabAs());

		/*
		 * Aktualisieren
		 */
		JMenuItem miRefresh = new JMenuItem(I.tr("Update"), Icons.getImageIcon(Icons.Arrow.CIRCLE_DOUBLE));
		miRefresh.setMnemonic(KeyEvent.VK_A);
		muData.add(miRefresh);
		miRefresh.addActionListener(l -> {
			EditorAbstractFileTab tab = (EditorAbstractFileTab) ctx.getEditor().getSelectedTab().get();
			if (tab.isFileChanged()) {
				if (!TaskDialogs.isConfirmed(ctx.getParentWindow(), "",
						I.trf("Do you really want to discard all changes to ''{0}''\nand reload the file?",
								tab.getDataFile().get().getName()))) {
					return;
				}
			}
			tab.openFile(tab.getDataFile().get());
		});
		egHasDataFile.add(miRefresh);

		/*
		 * Schließen
		 */
		miClose = new JMenuItem(I.tr("Close"), Icons.getImageIcon(Icons.Action.DELETE));
		miClose.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK));
		muData.add(miClose);
		miClose.addActionListener(l -> ctx.getEditor().getSelectedTab().ifPresent(t -> ctx.getEditor().closeTab(t)));

		muData.addSeparator();

		JMenuItem miSearchFile = new JMenuItem(I.tr("Search for file..."), Icons.getImageIcon(Icons.Action.FIND));
		miSearchFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK));
		muData.add(miSearchFile);
		miSearchFile.addActionListener(e -> new FileSearchDialog(ctx, I.tr("Search for file...")).open());

		JMenuItem miOpenFileManager = new JMenuItem(I.tr("Open in file manager"), Icons.getImageIcon(Icons.IO.FOLDER_EXPLORE));
		miOpenFileManager = new JMenuItem(I.tr("Open in file manager"), Icons.getImageIcon(Icons.IO.FOLDER_EXPLORE));
		miOpenFileManager.setMnemonic(KeyEvent.VK_X);
		muData.add(miOpenFileManager);
		miOpenFileManager.addActionListener(e -> {
			ctx.getFileManager().explorePath(((EditorAbstractFileTab) ctx.getEditor().getSelectedTab().get()).getDataFile().get());
		});
		egHasDataFile.add(miOpenFileManager);

		JMenuItem miCopyFileName = new JMenuItem(I.tr("Copy file name to clipboard"), Icons.getImageIcon(Icons.Misc.CLIPBOARD));
		miCopyFileName.setMnemonic(KeyEvent.VK_X);
		muData.add(miCopyFileName);
		miCopyFileName.addActionListener(e -> {
			File file = ((EditorAbstractFileTab) ctx.getEditor().getSelectedTab().get()).getDataFile().get();
			IOUtils.copyToClipboard(file.getName());
		});
		egHasDataFile.add(miCopyFileName);

		JMenuItem miCopyFilePath = new JMenuItem(I.tr("Copy file path to clipboard"), Icons.getImageIcon(Icons.Misc.CLIPBOARD));
		miCopyFilePath.setMnemonic(KeyEvent.VK_X);
		muData.add(miCopyFilePath);
		miCopyFilePath.addActionListener(e -> {
			File file = ((EditorAbstractFileTab) ctx.getEditor().getSelectedTab().get()).getDataFile().get();
			IOUtils.copyToClipboard(file.getAbsolutePath());
		});
		egHasDataFile.add(miCopyFilePath);

		miOpenTinyHexer = new JMenuItem(I.tr("Open in TinyHexer"), Icons.getImageIcon(Icons.Misc.TINY_HEXER));
		miOpenTinyHexer.setMnemonic(KeyEvent.VK_T);
		muData.add(miOpenTinyHexer);
		miOpenTinyHexer.addActionListener(e -> {
			try {
				String executable = ctx.getOptionStore().get(EditorOptions.Path.TINY_HEXER);
				String path = ((EditorAbstractFileTab) ctx.getEditor().getSelectedTab().get()).getDataFile().get().getAbsolutePath();
				String script = ctx.getOptionStore().get(EditorOptions.Path.TINY_HEXER_SCRIPT);
				new ProcessBuilder(executable, path, "/s", script).start();
			} catch (Exception e1) {
				TaskDialogs.error(ctx.getParentWindow(), I.tr("TinyHexer could not be opened."), e1.getMessage());
			}
		});

		muData.addSeparator();

		/*
		 * Einstellungen
		 */
		JMenuItem miSettings = new JMenuItem(I.tr("Settings"), Icons.getImageIcon(Icons.Data.SETTINGS));
		miSettings.setMnemonic(KeyEvent.VK_E);
		muData.add(miSettings);
		miSettings.addActionListener(e -> new SettingsDialog(ctx.getParentWindow(), ctx.getOptionStore(), this::settingsUpdated).open());

		/*
		 * Programm beenden
		 */
		JMenuItem miExit = new JMenuItem(I.tr("Exit"), Icons.getImageIcon(Icons.Data.EXIT));
		miExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.CTRL_DOWN_MASK));
		miExit.setMnemonic(KeyEvent.VK_B);
		muData.add(miExit);
		miExit.addActionListener(e -> {
			WindowEvent windowClosing = new WindowEvent(ctx.getParentWindow(), WindowEvent.WINDOW_CLOSING);
			ctx.getParentWindow().dispatchEvent(windowClosing);
		});
	}

	private void createMenuTools() {
		JMenu muTools = new JMenu(I.tr("Tools"));
		muTools.setMnemonic(KeyEvent.VK_T);
		add(muTools);

		/*
		 * Entities aus Datei importieren
		 */
		miImport = new JMenuItem(I.tr("Import entities from file"), Icons.getImageIcon(Icons.IO.IMPORT));
		miImport.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK));
		miImport.setMnemonic(KeyEvent.VK_I);
		muTools.add(miImport);
		miImport.addActionListener(e -> {
			Optional<EditorArchiveTab> archiveTab = getSelectedArchiveTab();
			if (!archiveTab.isPresent()) {
				return;
			}

			File file = FileDialogWrapper.openFile(I.tr("Open file"), ctx.getParentWindow(), FileDialogWrapper.ARCHIVE_FILTER,
					FileDialogWrapper.NO_FILTER);
			if (file != null && ImportHelper.importFromFile(file, archiveTab.get().getCurrentFile(), ctx)) {
				archiveTab.get().refreshTree(false);
			}
		});

		JMenuItem miSearchEntity = new JMenuItem(I.tr("Entity Search"), Icons.getImageIcon(Icons.Action.FIND));
		miSearchEntity.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK));
		miSearchEntity.setMnemonic(KeyEvent.VK_E);
		muTools.add(miSearchEntity);
		miSearchEntity.addActionListener(e -> EntitySearchDialog.openEntitySearch(ctx));

		JMenuItem miSearchTemplateEntity = new JMenuItem(I.tr("Template Search"), Icons.getImageIcon(Icons.Action.FIND));
		muTools.add(miSearchTemplateEntity);
		miSearchTemplateEntity
				.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
		miSearchTemplateEntity.addActionListener(e -> TemplateSearchDialog.openTemplateSearch(ctx));

		JMenuItem miOpenTemplate = new JMenuItem(I.tr("Open template"), Icons.getImageIcon(Icons.Action.FIND));
		miOpenTemplate.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_DOWN_MASK));
		miOpenTemplate.setMnemonic(KeyEvent.VK_T);
		muTools.add(miOpenTemplate);
		miOpenTemplate.addActionListener(e -> {
			NavigateTemplateDialog dialog = new NavigateTemplateDialog(ctx.getParentWindow(), ctx);
			if (dialog.openAndWasSuccessful()) {
				for (TemplateCacheEntry template : dialog.getSelectedEntries()) {
					ctx.getEditor().openOrSelectFile(template.getFile());
				}
			}
		});

		JMenuItem miImportStaticLightdata = new JMenuItem(I.tr("Import StaticLightdata"), Icons.getImageIcon(Icons.IO.IMPORT));
		miImportStaticLightdata.setMnemonic(KeyEvent.VK_H);
		muTools.add(miImportStaticLightdata);
		miImportStaticLightdata.addActionListener(e -> new ImportStaticLightdataDialog(ctx).open());

		muTools.addSeparator();

		JMenuItem miEditObjectsWithoutObject = new JMenuItem(I.tr("Edit Objects without LowPoly"), Icons.getImageIcon(Icons.Action.EDIT));
		muTools.add(miEditObjectsWithoutObject);
		miEditObjectsWithoutObject.addActionListener(l -> new EditGuidWithCommentConfigDialog(ctx.getParentWindow(),
				I.tr("Edit Objects without LowPoly"), ConfigFiles.objectsWithoutLowPoly(ctx)).open());

		JMenuItem miGenerateLowPoly = new JMenuItem(I.tr("Generate LowPoly"), Icons.getImageIcon(Icons.Action.DIFF));
		muTools.add(miGenerateLowPoly);
		miGenerateLowPoly.addActionListener(a -> {
			String result = String.join("\n", LowPolyGenerator.generate(ctx));
			new DisplayTextDialog(I.tr("Generate LowPoly"), result, ctx.getParentWindow(), false).open();
		});

		muTools.addSeparator();

		/*
		 * Stringtable aufräumen
		 */
		miCleanStringtable = new JMenuItem(I.tr("Clean up string table"), Icons.getImageIcon(Icons.Data.OPEN_BOOK));
		muTools.add(miCleanStringtable);
		miCleanStringtable.setMnemonic(KeyEvent.VK_A);
		miCleanStringtable.addActionListener(e -> {
			Optional<EditorArchiveTab> archiveTab = getSelectedArchiveTab();
			if (archiveTab.isPresent()) {
				ArchiveFile file = archiveTab.get().getCurrentFile();
				StringtableHelper.clearStringtableSafe(file.getEntities().toList(), file.getStringtable(), true, ctx.getParentWindow());
				archiveTab.get().fileChanged();
				return;
			}

			Optional<EditorTemplateTab> templateTab = getSelectedTemplateTab();
			if (templateTab.isPresent()) {
				TemplateFile template = templateTab.get().getCurrentTemplate();
				StringtableHelper.clearStringtableSafe(template.getHeaders().toList(), template.getStringtable(), true,
						ctx.getParentWindow());
				templateTab.get().fileChanged();
			}

		});

		muTools.addSeparator();

		JMenuItem miWorldMap = new JMenuItem(I.tr("World map"), Icons.getImageIcon(Icons.Misc.MAP));
		miWorldMap.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.CTRL_DOWN_MASK));
		miWorldMap.setMnemonic(KeyEvent.VK_M);
		miWorldMap.addActionListener(e -> EntityMap.getInstance(ctx).setVisible(true));
		muTools.add(miWorldMap);

		JMenuItem miEditChests = new JMenuItem(I.tr("Chest Editor"), Icons.getImageIcon(Icons.Misc.CHEST));
		miEditChests.addActionListener(e -> new ChestEditor(ctx).setVisible(true));
		muTools.add(miEditChests);

		muTools.addSeparator();

		JMenuItem miViewFile = new JMenuItem(I.tr("Show 3D view"), Icons.getImageIcon(Icons.IO.IMPORT));
		muTools.add(miViewFile);
		miViewFile.addActionListener(e -> {
			NodeViewer viewer = NodeViewer.getInstance(ctx);
			for (EditorArchiveTab tab : ctx.getEditor().<EditorArchiveTab>getTabs(EditorTabType.Archive)) {
				for (eCEntity entity : tab.getCurrentFile()) {
					viewer.addContainer(entity, entity.getWorldMatrix());
				}

			}
			viewer.centerCamera();
		});

		muTools.addSeparator();

		/*
		 * Log anzeigen
		 */
		JMenuItem miLog = new JMenuItem(I.tr("Show log"), Icons.getImageIcon(Icons.Data.LOG));
		miLog.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK));
		miLog.setMnemonic(KeyEvent.VK_L);
		muTools.add(miLog);
		miLog.addActionListener(e -> {
			String path = null;
			try {
				LoggerContext ctx = (LoggerContext) LoggerFactory.getILoggerFactory();
				Iterator<Appender<ILoggingEvent>> iterator = ctx.getLogger(Logger.ROOT_LOGGER_NAME).iteratorForAppenders();
				while (iterator.hasNext()) {
					Appender<ILoggingEvent> appender = iterator.next();
					if (appender instanceof FileAppender) {
						path = ((FileAppender<?>) appender).getFile();
						break;
					}
				}

				if (path != null) {
					File logFile = new File(path);
					if (logFile.exists() && logFile.isFile()) {
						Desktop.getDesktop().open(logFile);
					}
				}
			} catch (Exception ex) {
				TaskDialogs.error(ctx.getParentWindow(), I.tr("Log could not be opened."), ex.getMessage());
			}
		});

		JMenuItem miFloatToHex = new JMenuItem(I.tr("Float <-> Hex"), Icons.getImageIcon(Icons.Document.CONVERT));
		miFloatToHex.setMnemonic(KeyEvent.VK_F);
		muTools.add(miFloatToHex);
		miFloatToHex.addActionListener(e -> {
			FloatCalcDialog dialog = new FloatCalcDialog(ctx.getParentWindow());
			dialog.setLocationRelativeTo(ctx.getParentWindow());
			dialog.setVisible(true);
		});

		JMenuItem miGuidFormats = new JMenuItem(I.tr("Guid formats"), Icons.getImageIcon(Icons.Document.CONVERT));
		miGuidFormats.setMnemonic(KeyEvent.VK_F);
		muTools.add(miGuidFormats);
		miGuidFormats.addActionListener(e -> new GuidFormatsDialog(ctx.getParentWindow()).open());

		muTools.addSeparator();

		final ScriptManager scriptManager = new ScriptManager(ctx);
		scriptManager.addDefaultScripts();

		JMenuItem miExecuteScripts = new JMenuItem(I.tr("Execute scripts"), Icons.getImageIcon(Icons.Misc.SCRIPT));
		miExecuteScripts.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
		miExecuteScripts.setMnemonic(KeyEvent.VK_S);
		muTools.add(miExecuteScripts);
		miExecuteScripts.addActionListener(a -> scriptManager.showScriptDialog());

		final CheckManager checkManager = new CheckManager(ctx);
		checkManager.addDefaultChecks();

		JMenuItem miExecuteChecks = new JMenuItem(I.tr("Execute checks"), Icons.getImageIcon(Icons.Misc.SCRIPT));
		miExecuteChecks.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
		miExecuteChecks.setMnemonic(KeyEvent.VK_C);
		muTools.add(miExecuteChecks);
		miExecuteChecks.addActionListener(a -> checkManager.showCheckDialog());
	}

	private void createMenuRemote() {
		JMenu muRemote = new JMenu(I.tr("Remote"));
		muRemote.setMnemonic(KeyEvent.VK_R);
		add(muRemote);

		JMenuItem miShowLiveEntityPosition = new JMenuItem(I.tr("Live Entity Position"), Icons.getImageIcon(Icons.Arrow.CIRCLE_DOUBLE));
		muRemote.add(miShowLiveEntityPosition);
		miShowLiveEntityPosition.addActionListener(e -> {
			LivePositionFrame dialog = new LivePositionFrame(ctx);
			dialog.setLocationRelativeTo(ctx.getParentWindow());
			dialog.setVisible(true);
		});
	}

	private void createMenuNavMap() {
		JMenu muNavMap = new JMenu(I.tr("NavMap"));
		muNavMap.setMnemonic(KeyEvent.VK_N);
		add(muNavMap);

		JMenuItem miReloadNavMap = new JMenuItem(I.tr("Reload"), Icons.getImageIcon(Icons.Arrow.CIRCLE_DOUBLE));
		miReloadNavMap.setMnemonic(KeyEvent.VK_N);
		muNavMap.add(miReloadNavMap);
		miReloadNavMap.addActionListener(e -> ctx.getNavMapManager().loadNavMap(true));

		muNavMap.addSeparator();

		miSaveNavMap = new JMenuItem(I.tr("Save"), Icons.getImageIcon(Icons.IO.SAVE));
		miSaveNavMap.setMnemonic(KeyEvent.VK_S);
		miSaveNavMap.setEnabled(false);
		muNavMap.add(miSaveNavMap);
		miSaveNavMap.addActionListener(e -> ctx.getNavMapManager().saveMap());

		miSaveNavMapAs = new JMenuItem(I.tr("Save as"), Icons.getImageIcon(Icons.IO.SAVE_AS));
		miSaveNavMapAs.setMnemonic(KeyEvent.VK_U);
		miSaveNavMapAs.setEnabled(false);
		muNavMap.add(miSaveNavMapAs);
		miSaveNavMapAs.addActionListener(e -> ctx.getNavMapManager().saveNavMapAs());

		muNavMap.addSeparator();

		JMenuItem miCreateNavCache = new JMenuItem(I.tr("Create NavZone cache"), Icons.getImageIcon(Icons.Arrow.CIRCLE_DOUBLE));
		miCreateNavCache.setMnemonic(KeyEvent.VK_A);
		muNavMap.add(miCreateNavCache);
		miCreateNavCache.addActionListener(e -> ctx.getCacheManager().createCache(NavCache.class));
		muNavMap.addSeparator();

		JMenuItem miEditNegZones = new JMenuItem(I.tr("Edit NegZones"), Icons.getImageIcon(Icons.Action.EDIT));
		muNavMap.add(miEditNegZones);
		miEditNegZones.addActionListener(l -> ctx.getEditor().openEditNegZones());

		JMenuItem miEditNegCircles = new JMenuItem(I.tr("Edit NegCircles"), Icons.getImageIcon(Icons.Action.EDIT));
		muNavMap.add(miEditNegCircles);
		miEditNegCircles.addActionListener(l -> ctx.getEditor().openEditNegCircles());

		JMenuItem miEditNegCirclesWithoutObject = new JMenuItem(I.tr("Edit NegCircles without object"),
				Icons.getImageIcon(Icons.Action.EDIT));
		muNavMap.add(miEditNegCirclesWithoutObject);
		miEditNegCirclesWithoutObject.addActionListener(l -> new EditGuidWithCommentConfigDialog(ctx.getParentWindow(),
				I.tr("Edit NegCircles without object"), ConfigFiles.negCirclesWithoutObject(ctx)).open());

		JMenuItem miEditObjectsWithoutNegCircle = new JMenuItem(I.tr("Edit objects without NegCircle"),
				Icons.getImageIcon(Icons.Action.EDIT));
		muNavMap.add(miEditObjectsWithoutNegCircle);
		miEditObjectsWithoutNegCircle.addActionListener(l -> new EditGuidWithCommentConfigDialog(ctx.getParentWindow(),
				I.tr("Edit objects without NegCircle"), ConfigFiles.objectsWithoutNegCircles(ctx)).open());

		JMenuItem miEditPrefPaths = new JMenuItem(I.tr("Edit PrefPaths"), Icons.getImageIcon(Icons.Action.EDIT));
		muNavMap.add(miEditPrefPaths);
		miEditPrefPaths.addActionListener(l -> ctx.getEditor().openEditPrefPaths());

		JMenuItem miNavMapSync = new JMenuItem(I.tr("Synchronize NavMap"), Icons.getImageIcon(Icons.Action.DIFF));
		miNavMapSync.setMnemonic(KeyEvent.VK_S);
		muNavMap.add(miNavMapSync);
		miNavMapSync.addActionListener(l -> new NavMapSync(ctx).setVisible(true));

		muNavMap.addSeparator();

		JMenuItem miExNegZones = new JMenuItem(I.tr("Export NegZones"), Icons.getImageIcon(Icons.Document.EXPORT));
		miExNegZones.setMnemonic(KeyEvent.VK_Z);
		muNavMap.add(miExNegZones);
		miExNegZones.addActionListener(e -> {
			File outDir = FileDialogWrapper.chooseDirectory(I.tr("Select output directory"), ctx.getParentWindow());
			if (outDir == null) {
				return;
			}

			NavMap navMap = ctx.getNavMapManager().getNavMap(true);
			if (navMap == null) {
				return;
			}

			ArchiveFile aFile = NodeExporter.exportNegZones(navMap.getNegZones());
			try {
				outDir = new File(outDir, "Nav_NegZones");
				outDir.mkdirs();

				aFile.save(new File(outDir, "Nav_NegZones_01_SHyb.node"));

				SecDat secDat = new SecDat();
				secDat.addNodeFile("Nav_NegZones_01_SHyb");
				secDat.save(new File(outDir, "Nav_NegZones.secdat"));
				FileUtil.createSec(new File(outDir, "Nav_NegZones"));

				FileUtil.createLrgeo(new File(outDir, "Nav_NegZones_01_SHyb"));
				FileUtil.createLrgeodat(new File(outDir, "Nav_NegZones_01_SHyb"), aFile.getGraph().getWorldTreeBoundary());
			} catch (Exception e1) {
				TaskDialogs.showException(e1);
			}
		});

		JMenuItem miExNegCircles = new JMenuItem(I.tr("Export NegCircles"), Icons.getImageIcon(Icons.Document.EXPORT));
		miExNegCircles.setMnemonic(KeyEvent.VK_C);
		muNavMap.add(miExNegCircles);
		miExNegCircles.addActionListener(e -> {
			File outDir = FileDialogWrapper.chooseDirectory(I.tr("Select output directory"), ctx.getParentWindow());
			if (outDir == null) {
				return;
			}

			NavMap navMap = ctx.getNavMapManager().getNavMap(true);
			if (navMap == null) {
				return;
			}

			List<ArchiveFile> files = NodeExporter.exportNegCircles(navMap.getNegCircles());

			SecDat secDat = new SecDat();

			try {
				outDir = new File(outDir, "Nav_NegCircles");
				outDir.mkdirs();
				for (int i = 0; i < files.size(); i++) {
					ArchiveFile aFile = files.get(i);
					aFile.save(new File(outDir, "Nav_NegCircles_0" + i + "_SHyb.node"));
					secDat.addNodeFile("Nav_NegCircles_0" + i + "_SHyb");

					FileUtil.createLrgeo(new File(outDir, "Nav_NegCircles_0" + i + "_SHyb"));
					FileUtil.createLrgeodat(new File(outDir, "Nav_NegCircles_0" + i + "_SHyb"), aFile.getGraph().getWorldTreeBoundary());
				}
				secDat.save(new File(outDir, "Nav_NegCircles.secdat"));
				FileUtil.createSec(new File(outDir, "Nav_NegCircles"));
			} catch (Exception e1) {
				TaskDialogs.showException(e1);
			}
		});

		JMenuItem miExPrefPaths = new JMenuItem(I.tr("Export PrefPaths"), Icons.getImageIcon(Icons.Document.EXPORT));
		miExPrefPaths.setMnemonic(KeyEvent.VK_P);
		muNavMap.add(miExPrefPaths);
		miExPrefPaths.addActionListener(e -> {
			File outDir = FileDialogWrapper.chooseDirectory(I.tr("Select output directory"), ctx.getParentWindow());
			if (outDir == null) {
				return;
			}

			NavMap navMap = ctx.getNavMapManager().getNavMap(true);
			if (navMap == null) {
				return;
			}

			try {
				outDir = new File(outDir, "Nav_PrefPaths");
				outDir.mkdirs();

				ArchiveFile aFile = NodeExporter.exportPrefPaths(navMap.getPrefPaths());
				aFile.save(new File(outDir, "Nav_PrefPaths_01_SHyb.node"));

				SecDat secDat = new SecDat();
				secDat.addNodeFile("Nav_PrefPaths_01_SHyb");
				secDat.save(new File(outDir, "Nav_PrefPaths.secdat"));
				FileUtil.createSec(new File(outDir, "Nav_PrefPaths"));

				FileUtil.createLrgeo(new File(outDir, "Nav_PrefPaths_01_SHyb"));
				FileUtil.createLrgeodat(new File(outDir, "Nav_PrefPaths_01_SHyb"), aFile.getGraph().getWorldTreeBoundary());
			} catch (Exception e1) {
				TaskDialogs.showException(e1);
			}
		});
	}

	private void createMenuDeveloper() {
		muDeveloper = new JMenu(I.tr("Developer"));
		muDeveloper.setMnemonic(KeyEvent.VK_D);
		muDeveloper.setVisible(false);
		add(muDeveloper);

		Holder<Boolean> uiInspectorInstalled = new Holder<>(false);
		JMenuItem miUiInspector = new JMenuItem(I.tr("UI inspector"));
		miUiInspector.setMnemonic(KeyEvent.VK_U);
		miUiInspector.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK | InputEvent.ALT_DOWN_MASK));
		muDeveloper.add(miUiInspector);
		miUiInspector.addActionListener(e -> {
			if (!ctx.getOptionStore().get(EditorOptions.Misc.DEVELOPER_MODE))
				return;

			KeyStroke keyStroke = miUiInspector.getAccelerator();
			if (!uiInspectorInstalled.held()) {
				FlatInspector.install(keyStroke.toString());
				uiInspectorInstalled.hold(true);
				// Open Inspector
				SwingUtils.triggerKeyStroke(miUiInspector, keyStroke);
			} else if (e.getModifiers() != SwingUtils.mapNewKeyModifiers(keyStroke.getModifiers()))
				// Action was performed by clicking on the menu item
				SwingUtils.triggerKeyStroke(miUiInspector, keyStroke);
		});

		Holder<Boolean> uiDefaultsInspectorInstalled = new Holder<>(false);
		JMenuItem miUiDefaultsInspector = new JMenuItem(I.tr("UI defaults inspector"));
		miUiDefaultsInspector.setMnemonic(KeyEvent.VK_D);
		miUiDefaultsInspector.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK | InputEvent.ALT_DOWN_MASK));
		muDeveloper.add(miUiDefaultsInspector);
		miUiDefaultsInspector.addActionListener(e -> {
			if (!ctx.getOptionStore().get(EditorOptions.Misc.DEVELOPER_MODE))
				return;

			KeyStroke keyStroke = miUiDefaultsInspector.getAccelerator();
			if (!uiDefaultsInspectorInstalled.held()) {
				FlatUIDefaultsInspector.install(keyStroke.toString());
				// From now on the keystroke triggers FlatInspector directly!
				uiDefaultsInspectorInstalled.hold(true);
				// Open Inspector
				SwingUtils.triggerKeyStroke(miUiDefaultsInspector, keyStroke);
			} else if (e.getModifiers() != SwingUtils.mapNewKeyModifiers(keyStroke.getModifiers()))
				// Action was performed by clicking on the menu item
				SwingUtils.triggerKeyStroke(miUiInspector, keyStroke);
		});
	}

	private void createMenuAbout() {
		/*
		 * Über g3dit
		 */
		final JMenu miAbout = new JMenu(I.tr("About"));
		miAbout.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				new AboutDialog(ctx.getParentWindow()).open();
				miAbout.setSelected(false);
			}
		});
		miAbout.addMenuListener(new MenuListener() {
			@Override
			public void menuSelected(MenuEvent e) {
				miAbout.setSelected(false);
			}

			@Override
			public void menuDeselected(MenuEvent e) {}

			@Override
			public void menuCanceled(MenuEvent e) {}
		});
		add(miAbout);
	}

	private Optional<EditorArchiveTab> getSelectedArchiveTab() {
		return ctx.getEditor().getSelectedTab(EditorTabType.Archive);
	}

	private Optional<EditorTemplateTab> getSelectedTemplateTab() {
		return ctx.getEditor().getSelectedTab(EditorTabType.Template);
	}

	@Subscribe
	public void onSelectTab(EditorTab.SelectedEvent event) {
		adaptToTab(event.getTab());
	}

	@Subscribe
	public void onTabStateChange(EditorTab.StateChangedEvent event) {
		adaptToTab(Optional.of(event.getTab()));
	}

	@Subscribe
	public void onNavMapLoaded(NavMapLoadedEvent event) {
		miSaveNavMap.setEnabled(event.isSuccessful());
		miSaveNavMapAs.setEnabled(event.isSuccessful());
	}

	/**
	 * (De)Aktiviert Menüeintrage, je nachdem ob sie zum ausgewählten Tab passen oder nicht
	 */
	public void adaptToTab(Optional<EditorTab> tab) {
		boolean isPresent = tab.isPresent();
		boolean isArchive = isPresent && tab.get().type() == EditorTabType.Archive;
		boolean isTemplate = isPresent && tab.get().type() == EditorTabType.Template;
		boolean hasDataFile = isPresent && tab.get() instanceof EditorAbstractFileTab
				&& ((EditorAbstractFileTab) tab.get()).getDataFile().isPresent();

		miSave.setEnabled(isPresent);
		miSaveAs.setEnabled(isPresent);
		miImport.setEnabled(isArchive);
		miCleanStringtable.setEnabled(isArchive || isTemplate);
		egHasDataFile.setEnabled(hasDataFile);
		miOpenTinyHexer.setEnabled(hasDataFile && !ctx.getOptionStore().get(EditorOptions.Path.TINY_HEXER).isEmpty());
	}

	private void settingsUpdated() {
		fileMenu.setAliasMap(SettingsHelper.getDataFolderAlias(ctx.getOptionStore()));
		fileMenu.generateMenu();
		adaptToTab(ctx.getEditor().getSelectedTab());

		muDeveloper.setVisible(ctx.getOptionStore().get(EditorOptions.Misc.DEVELOPER_MODE));

		ctx.eventBus().post(new SettingsUpdatedEvent());
	}

	public void saveSettings(OptionStore optionStore) {
		optionStore.put(EditorOptions.MainMenu.RECENT_FILES, new ArrayList<>(fileMenu.getRecentFiles()));
	}

	public void addRecentFile(String filePath) {
		fileMenu.addEntry(filePath);
	}
}

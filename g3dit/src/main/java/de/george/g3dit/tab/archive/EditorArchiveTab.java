package de.george.g3dit.tab.archive;

import java.awt.Component;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingWorker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ezware.dialog.task.TaskDialogs;
import com.google.common.collect.FluentIterable;
import com.google.common.eventbus.Subscribe;
import com.teamunify.i18n.I;

import de.george.g3dit.EditorContext;
import de.george.g3dit.StatusBar;
import de.george.g3dit.cache.Caches;
import de.george.g3dit.check.EntityDescriptor;
import de.george.g3dit.check.FileDescriptor;
import de.george.g3dit.entitytree.EntityTree;
import de.george.g3dit.gui.dialogs.ProgressDialog;
import de.george.g3dit.settings.EditorOptions;
import de.george.g3dit.tab.EditorAbstractFileTab;
import de.george.g3dit.tab.EditorTab;
import de.george.g3dit.tab.archive.ArchiveContentPane.ArchiveViewType;
import de.george.g3dit.util.FileDialogWrapper;
import de.george.g3dit.util.Icons;
import de.george.g3dit.util.StringtableHelper;
import de.george.g3utils.gui.SwingUtils;
import de.george.g3utils.io.G3FileReaderEx;
import de.george.g3utils.io.Saveable;
import de.george.g3utils.structure.bCBox;
import de.george.g3utils.util.IOUtils;
import de.george.lrentnode.archive.ArchiveFile;
import de.george.lrentnode.archive.ArchiveFile.ArchiveType;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.classes.eCGeometrySpatialContext;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.util.FileUtil;
import de.george.navmap.data.NavPath;
import de.george.navmap.data.NavZone;
import net.miginfocom.swing.MigLayout;
import net.tomahawk.ExtensionsFilter;

public class EditorArchiveTab extends EditorAbstractFileTab {
	private static final Logger logger = LoggerFactory.getLogger(EditorArchiveTab.class);

	private ArchiveContentPane contentPane;
	private JPanel statusBarExtension;

	private ArchiveFile currentFile;
	private eCEntity currentEntity;

	public EditorArchiveTab(EditorContext ctx) {
		super(ctx, EditorTabType.Archive);
		contentPane = new ArchiveContentPane(this);
		contentPane.initGUI();
		ctx.eventBus().register(this);
	}

	private void initStatusBarExtension() {
		statusBarExtension = new JPanel(new MigLayout("ins 0"));
		statusBarExtension.setOpaque(false);
		JToggleButton btnEntity = new JToggleButton("E", true);
		btnEntity.setToolTipText(I.tr("Edit entity"));
		btnEntity.setMargin(new Insets(0, 0, 0, 0));
		btnEntity.addActionListener(l -> contentPane.showView(ArchiveViewType.ENTITY));
		btnEntity.setMnemonic(KeyEvent.VK_1);
		JToggleButton btnPropertySheet = new JToggleButton("P", false);
		btnPropertySheet.setToolTipText(I.tr("Property"));
		btnPropertySheet.setMargin(new Insets(0, 0, 0, 0));
		btnPropertySheet.addActionListener(l -> contentPane.showView(ArchiveViewType.PROPERTY));
		btnPropertySheet.setMnemonic(KeyEvent.VK_2);
		JToggleButton btnIllumination = new JToggleButton("B", false);
		btnIllumination.setToolTipText(I.tr("Calculate lighting"));
		btnIllumination.setMargin(new Insets(0, 0, 0, 0));
		btnIllumination.addActionListener(l -> contentPane.showView(ArchiveViewType.ILLUMINATION));
		btnIllumination.setMnemonic(KeyEvent.VK_3);
		JToggleButton btnNegCircle = new JToggleButton("N", false);
		btnNegCircle.setToolTipText(I.tr("NegCircle"));
		btnNegCircle.setMargin(new Insets(0, 0, 0, 0));
		btnNegCircle.addActionListener(l -> contentPane.showView(ArchiveViewType.NEGCIRCLE));
		btnNegCircle.setMnemonic(KeyEvent.VK_4);
		statusBarExtension.add(btnEntity, "width 20!, height 20!");
		statusBarExtension.add(btnPropertySheet, "width 20!, height 20!");
		statusBarExtension.add(btnIllumination, "width 20!, height 20!");
		statusBarExtension.add(btnNegCircle, "width 20!, height 20!");
		SwingUtils.createButtonGroup(btnEntity, btnIllumination, btnNegCircle, btnPropertySheet);
	}

	@Override
	public Icon getTabIcon() {
		if (getCurrentFile() == null) {
			return null;
		}

		if (getCurrentFile().isLrentdat()) {
			return Icons.getImageIcon(Icons.Document.LETTER_L);
		} else {
			return Icons.getImageIcon(Icons.Document.LETTER_N);
		}
	}

	public EntityTree getEntityTree() {
		return contentPane.getEntityTree();
	}

	/**
	 * Nach dem Auswählen/Verändern einer Entity aufrufen, um GUI zu aktualisieren
	 */
	public void refreshTree(boolean resetMarkedEntities) {
		contentPane.refreshTree(resetMarkedEntities);
	}

	public void saveView() {
		contentPane.saveView();
	}

	/**
	 * Nach dem Auswählen/Verändern einer Entity aufrufen, um GUI zu aktualisieren
	 */
	public void refreshView() {
		contentPane.refreshView();
	}

	/**
	 * Liefert die aktuell geöffnete Datei zurück
	 *
	 * @return aktuelle geöffnete Datei
	 */
	public ArchiveFile getCurrentFile() {
		return currentFile;
	}

	/**
	 * @param aFile geladene lrentdat/node Datei, darf nicht null sein.
	 * @param file File Objekt der Datei
	 */
	public void setCurrentFile(ArchiveFile aFile, File file) {
		currentFile = null;
		currentEntity = null;
		setFileChanged(false);

		if (aFile != null) {
			setDataFile(file);
			currentFile = aFile;
			contentPane.initFileView(aFile);
			contentPane.setVisible(true);
		} else {
			contentPane.setVisible(false);
		}

		eventBus().post(new StateChangedEvent(this));
	}

	@Override
	public boolean openFile(File file) {
		if (file != null) {
			OpenFileWorker worker = new OpenFileWorker(file);
			worker.execute();
			worker.getProgressDialog().setVisible(true);
			return worker.isFileLoaded();
		}
		return false;
	}

	@Override
	public boolean isFileChangedReliable() {
		return true;
	}

	@Override
	protected Saveable getSaveable() {
		contentPane.saveView();

		if (getOptionStore().get(EditorOptions.Misc.CLEAN_STRINGTABLE)) {
			StringtableHelper.clearStringtableSafe(getCurrentFile().getEntities().toList(), getCurrentFile().getStringtable(), true,
					ctx.getParentWindow());
		}

		return getCurrentFile();

	}

	@Override
	public boolean saveFile(Optional<File> file) {
		boolean result = super.saveFile(file);
		if (result && file.isPresent()) {
			processLayer();
		}

		return result;
	}

	/**
	 * Liefert die aktuell selektierte Entity zurück
	 *
	 * @return aktuelle selektierte Datei
	 */
	public eCEntity getCurrentEntity() {
		return currentEntity;
	}

	public void setCurrentEntity(eCEntity currentEntity) {
		this.currentEntity = currentEntity;
	}

	public void changeEntity(eCEntity entity) {
		if (currentEntity != null) {
			fileChanged();
		}

		if (entity != currentEntity) {
			setCurrentEntity(entity);
		}
	}

	public void selectEntity(eCEntity entity) {
		getEntityTree().selectEntity(entity);
	}

	public boolean selectEntity(EntityDescriptor descriptor) {
		FluentIterable<eCEntity> entitiesByGuid = currentFile.getEntities().filter(e -> e.getGuid().equals(descriptor.getGuid()));
		if (entitiesByGuid.size() == 1) {
			selectEntity(entitiesByGuid.first().get());
			return true;
		}

		FluentIterable<eCEntity> entitiesByName = currentFile.getEntities().filter(e -> e.getName().equals(descriptor.getName()));

		if (descriptor.hasIndex() && descriptor.getIndex() < currentFile.getEntityCount()) {
			eCEntity entity = currentFile.getEntityByPosition(descriptor.getIndex());
			if (entitiesByName.size() != 1 || entitiesByName.anyMatch(e -> e == entity) || entitiesByGuid.anyMatch(e -> e == entity)) {
				selectEntity(entity);
				return true;
			}
		}

		if (entitiesByName.size() == 1) {
			selectEntity(entitiesByName.first().get());
			return true;
		}

		return false;
	}

	public boolean modifyEntity(eCEntity entity, Function<eCEntity, Boolean> modify) {
		if (entity == getCurrentEntity())
			contentPane.saveView();

		if (!modify.apply(entity))
			return false;

		if (entity == getCurrentEntity())
			contentPane.loadView();
		updateCaches(entity);
		fileChanged();
		return true;
	}

	public boolean modifyEntity(EntityDescriptor desc, Function<eCEntity, Boolean> modify) {
		return currentFile.getEntityByGuid(desc.getGuid()).map(entity -> modifyEntity(entity, modify)).orElse(false);
	}

	public <T> void modifyEntity(eCEntity entity, BiConsumer<eCEntity, T> modify, T value) {
		modifyEntity(entity, e -> {
			modify.accept(e, value);
			return true;
		});
	}

	public void updateCaches(eCEntity entity) {
		if (entity.hasClass(CD.gCNavZone_PS.class)) {
			Caches.nav(ctx).update(NavZone.fromArchiveEntity(entity));
		} else if (entity.hasClass(CD.gCNavPath_PS.class)) {
			Caches.nav(ctx).update(NavPath.fromArchiveEntity(entity));
		}
	}

	@Override
	public Component getTabContent() {
		return contentPane;
	}

	public void updateStatusBar() {
		StatusBar statusBar = getStatusBar();
		ArchiveFile archiveFile = getCurrentFile();
		if (archiveFile != null) {
			statusBar.setFileStatus(I.trf("Number of entities: {0, number}", archiveFile.getEntityCount()));
			if (statusBarExtension == null) {
				initStatusBarExtension();
			}
			statusBar.setExtensionPanel(statusBarExtension);
		}
	}

	@Subscribe
	public void onTabSelected(EditorTab.SelectedEvent event) {
		if (event.getTab().isPresent() && event.getTab().get().equals(this)) {
			updateStatusBar();
		}
	}

	@Override
	public boolean onClose(boolean appExit) {
		if (super.onClose(appExit)) {
			contentPane.onClose();
			ctx.eventBus().unregister(this);
			setCurrentFile(null, null);
			return true;
		}
		return false;
	}

	@Override
	public String getDefaultFileExtension() {
		return getCurrentFile().isLrentdat() ? "lrentdat" : "node";
	}

	@Override
	public ExtensionsFilter getFileFilter() {
		return getCurrentFile().isLrentdat() ? FileDialogWrapper.LRENTDAT_FILTER : FileDialogWrapper.NODE_FILTER;
	}

	public FileDescriptor getFileDescriptor() {
		return getDataFile().map(f -> new FileDescriptor(f, getCurrentFile().getArchiveType()))
				.orElseGet(() -> FileDescriptor.none(getCurrentFile().getArchiveType()));
	}

	private void processLayer() {
		ArchiveFile archiveFile = getCurrentFile();
		File dataFile = getDataFile().get();
		if (archiveFile.getArchiveType() == ArchiveType.Lrentdat) {
			File dynamicLayer = new File(IOUtils.changeExtension(dataFile.getAbsolutePath(), "lrent"));
			if (!dynamicLayer.exists()
					&& !ctx.getFileManager().moveFromPrimaryToSecondary(dynamicLayer).filter(File::exists).isPresent()) {
				boolean result = TaskDialogs.ask(ctx.getParentWindow(), I.tr("Create .lrent"),
						I.trf("No corresponding .lrent could be found for ''{0}''.\nShould a .lrent be created?", dataFile.getName()));
				if (result) {
					try {
						FileUtil.createLrent(dynamicLayer);
					} catch (IOException e) {
						TaskDialogs.error(ctx.getParentWindow(),
								I.trf("Error while creating the DynamicLayer for {0}.", dataFile.getName()), e.getMessage());
					}
				}
			}

		} else if (archiveFile.getArchiveType() == ArchiveType.Node) {
			File geometryLayer = new File(IOUtils.changeExtension(dataFile.getAbsolutePath(), "lrgeo"));
			File geometryLayerDat = new File(IOUtils.changeExtension(dataFile.getAbsolutePath(), "lrgeodat"));
			bCBox newContextBox = currentFile.getGraph() != null ? currentFile.getGraph().getWorldTreeBoundary() : new bCBox();
			if (!geometryLayer.exists() && !ctx.getFileManager().moveFromPrimaryToSecondary(geometryLayer).filter(File::exists).isPresent()
					|| !geometryLayerDat.exists()
							&& !ctx.getFileManager().moveFromPrimaryToSecondary(geometryLayerDat).filter(File::exists).isPresent()) {
				boolean result = TaskDialogs.ask(ctx.getParentWindow(), I.tr("Create .lrgeo/.lrgeodat"),
						I.trf("No corresponding .lrgeo/.lrgeodat could be found for ''{0}''.\nShould a .lrgeo/.lrgeodat be created?",
								dataFile.getName()));
				if (result) {
					try {
						FileUtil.createLrgeo(geometryLayer);
						FileUtil.createLrgeodat(geometryLayerDat, newContextBox);
					} catch (IOException e) {
						TaskDialogs.error(ctx.getParentWindow(),
								I.trf("Error while creating the GeometryLayer for {0}.", dataFile.getName()), e.getMessage());
					}
				}
			} else {
				try {
					eCGeometrySpatialContext lrgeodat;
					if (geometryLayerDat.exists()) {
						lrgeodat = FileUtil.openLrgeodat(geometryLayerDat);
					} else {
						Optional<File> origGeometryLayerDat = ctx.getFileManager().moveFromPrimaryToSecondary(geometryLayerDat);
						if (origGeometryLayerDat.map(File::exists).orElse(false)) {
							lrgeodat = FileUtil.openLrgeodat(origGeometryLayerDat.get());
						} else {
							lrgeodat = FileUtil.createLrgeodat();
						}
					}

					bCBox contextBox = lrgeodat.property(CD.eCContextBase.ContextBox);
					if (!contextBox.isEqual(newContextBox)) {
						lrgeodat.setPropertyData(CD.eCContextBase.ContextBox, newContextBox);
						FileUtil.saveLrgeodat(lrgeodat, geometryLayerDat);
					}
				} catch (IOException e) {
					logger.warn("Error while updating the eCGeometrySpatialContexts of {}: ", dataFile.getName(), e);
				}
			}
		}
	}

	private class OpenFileWorker extends SwingWorker<ArchiveFile, Integer> {

		private ProgressDialog progDlg;
		private File file;
		private boolean fileLoaded = false;

		public OpenFileWorker(File file) {
			progDlg = new ProgressDialog(ctx.getParentWindow(), I.tr("Loading file..."), file.getName(), false);
			progDlg.setLocationRelativeTo(ctx.getParentWindow());
			progDlg.getProgressBar().setIndeterminate(true);
			this.file = file;
		}

		public ProgressDialog getProgressDialog() {
			return progDlg;
		}

		public boolean isFileLoaded() {
			return fileLoaded;
		}

		@Override
		protected ArchiveFile doInBackground() throws Exception {
			long time = System.currentTimeMillis();
			try (G3FileReaderEx reader = new G3FileReaderEx(file)) {
				ArchiveFile aFile = FileUtil.openArchive(reader, true);
				logger.info("{} Load Time: {} s", file.getName(), (System.currentTimeMillis() - time) / 1000F);
				updateChecksum(reader.getBuffer());
				return aFile;
			}
		}

		@Override
		protected void done() {
			try {
				ArchiveFile aFile = get();
				setCurrentFile(aFile, file);
				fileLoaded = true;
				progDlg.dispose();
			} catch (Exception ex) {
				progDlg.dispose();
				logger.warn("Failed to load the file {}.", file.getName(), ex);
				TaskDialogs.error(ctx.getParentWindow(), I.tr("Error while opening the file."), ex.getMessage());
			}
		}
	}
}

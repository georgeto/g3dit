package de.george.g3dit.tab.archive;

import java.awt.BorderLayout;
import java.awt.event.InputEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;

import com.ezware.dialog.task.TaskDialogs;

import de.danielbechler.diff.node.DiffNode;
import de.danielbechler.diff.node.ToMapPrintingVisitor;
import de.george.g3dit.EntityMap;
import de.george.g3dit.check.FileDescriptor;
import de.george.g3dit.entitytree.EntityTree;
import de.george.g3dit.entitytree.TreePopupExtension;
import de.george.g3dit.gui.dialogs.AbstractSelectDialog;
import de.george.g3dit.gui.dialogs.DisplayTextDialog;
import de.george.g3dit.gui.dialogs.ListSelectDialog;
import de.george.g3dit.gui.dialogs.SelectClassDialog;
import de.george.g3dit.gui.dialogs.TreeEntitySelectDialog;
import de.george.g3dit.jme.EntityViewer;
import de.george.g3dit.rpc.IpcUtil;
import de.george.g3dit.tab.archive.views.ArchiveView;
import de.george.g3dit.tab.archive.views.EntityView;
import de.george.g3dit.tab.archive.views.IlluminationView;
import de.george.g3dit.tab.archive.views.NegCircleView;
import de.george.g3dit.tab.archive.views.PropertyView;
import de.george.g3dit.util.AssetResolver;
import de.george.g3dit.util.Icons;
import de.george.g3dit.util.ToolbarUtil;
import de.george.g3utils.gui.SwingUtils;
import de.george.lrentnode.archive.ArchiveFile;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.classes.G3Class;
import de.george.lrentnode.diff.EntityDiffer;
import de.george.lrentnode.util.ClassUtil;
import de.george.lrentnode.util.FileUtil;

public class ArchiveContentPane extends JPanel {
	private EditorArchiveTab ctx;

	public static enum ArchiveViewType {
		ENTITY,
		ILLUMINATION,
		NEGCIRCLE,
		PROPERTY;
	}

	private JSplitPane splitPane;
	private EntityTree entityTree;

	private JToolBar toolBar;

	private Map<ArchiveViewType, ArchiveView> views = new HashMap<>();
	private ArchiveView curView;

	public ArchiveContentPane(EditorArchiveTab ctx) {
		this.ctx = ctx;
	}

	public void initGUI() {
		setLayout(new BorderLayout());

		splitPane = new JSplitPane();
		splitPane.setResizeWeight(0);
		add(splitPane, BorderLayout.CENTER);

		// Entity Auflistung
		entityTree = new EntityTree(ctx);
		entityTree.setPopupExtension(new TreePopupExtension());
		splitPane.setLeftComponent(entityTree);

		createToolbar();

		// Entity View
		views.put(ArchiveViewType.ENTITY, new EntityView(ctx));
		views.put(ArchiveViewType.ILLUMINATION, new IlluminationView(ctx));
		views.put(ArchiveViewType.NEGCIRCLE, new NegCircleView(ctx));
		views.put(ArchiveViewType.PROPERTY, new PropertyView(ctx));

		showView(ArchiveViewType.ENTITY);

		// Entity wurde ausgewählt
		entityTree.addTreeSelectionListener(e -> Optional.ofNullable(curView).ifPresent(entity -> entity.entitySelectionChanged(e)));

		// Anzahl der Entities in StatusBar aktuell halten
		entityTree.getTree().getModel().addTreeModelListener(new TreeModelListener() {
			@Override
			public void treeStructureChanged(TreeModelEvent e) {
				updateEntityCount();
			}

			@Override
			public void treeNodesRemoved(TreeModelEvent e) {
				updateEntityCount();
			}

			@Override
			public void treeNodesInserted(TreeModelEvent e) {
				updateEntityCount();
			}

			@Override
			public void treeNodesChanged(TreeModelEvent e) {
				updateEntityCount();
			}

			public void updateEntityCount() {
				// Beim Schließen des Tabs würde sonst die StatusBar übernommen werden
				if (entityTree.getArchiveFile() != null) {
					ctx.updateStatusBar();
				}
			}
		});

		setVisible(true);
	}

	private void createToolbar() {
		toolBar = ToolbarUtil.createTopToolbar();

		JButton btnRemClasses = new JButton(Icons.getImageIcon(Icons.Data.CLASS_MINUS));
		btnRemClasses.setToolTipText("Klassen entfernen");
		toolBar.add(btnRemClasses);
		btnRemClasses.addActionListener(e -> {
			eCEntity entity = ctx.getCurrentEntity();
			SelectClassDialog dialog = new SelectClassDialog(ctx.getParentWindow(), "Klassen entfernen", "Entfernen", entity);
			if (dialog.openAndWasSuccessful()) {
				for (G3Class clazz : dialog.getResultClasses()) {
					entity.removeClass(clazz.getClassName());
				}
				ctx.refreshView();
			}
		});

		JButton btnAddClasses = new JButton(Icons.getImageIcon(Icons.Data.CLASS_PLUS));
		btnAddClasses.setToolTipText("Klassen hinzufügen");
		toolBar.add(btnAddClasses);
		btnAddClasses.addActionListener(e -> {
			eCEntity entity = ctx.getCurrentEntity();
			TreeEntitySelectDialog entitySelect = new TreeEntitySelectDialog(ctx, "Entity auswählen",
					AbstractSelectDialog.SELECTION_SINGLE, ctx.getCurrentFile());
			if (entitySelect.openAndWasSuccessful()) {
				eCEntity classContainer = entitySelect.getSelectedEntries().get(0);
				SelectClassDialog classSelect = new SelectClassDialog(ctx.getParentWindow(), "Klassen hinzufügen", "Hinzufügen",
						classContainer);
				if (classSelect.openAndWasSuccessful()) {
					for (G3Class clazz : classSelect.getResultClasses()) {
						entity.addClass(ClassUtil.clone(clazz), classContainer.getClassVersion(clazz));
					}
					ctx.refreshView();
				}
			}
		});

		toolBar.addSeparator();

		JButton btn3dView = new JButton("3D-Ansicht", Icons.getImageIcon(Icons.Data.THREED));
		btn3dView.setFocusable(false);
		toolBar.add(btn3dView);
		btn3dView.addActionListener(e -> EntityViewer.getInstance(ctx).showEntity(ctx.getCurrentEntity()));

		JButton btnAssetInfo = new JButton("Asset-Info", Icons.getImageIcon(Icons.Data.INFORMATION));
		btnAssetInfo.setToolTipText("Ermittelt die von der Entity verwendeten Assets und kopiert sie in die Zwischenablage.");
		btnAssetInfo.setFocusable(false);
		toolBar.add(btnAssetInfo);
		btnAssetInfo.addActionListener(e -> {
			eCEntity entity = ctx.getCurrentEntity();
			AssetResolver resolver = new AssetResolver(ctx, false);
			new DisplayTextDialog("Asset-Info", resolver.resolveContainer(entity).print(), ctx.getParentWindow(), false).open();
		});

		toolBar.addSeparator();

		JButton btnGotoEntity = new JButton("Goto", Icons.getImageIcon(Icons.Misc.GEOLOCATION));
		btnGotoEntity.setToolTipText(SwingUtils.getMultilineText("Teleportiert den Spieler zur Entity.",
				"Wenn beim Klicken Strg gedrückt ist, wird nicht die Guid,", "sondern die Position zur Ermittlung des Ziels verwendet."));
		btnGotoEntity.setFocusable(false);
		toolBar.add(btnGotoEntity);
		btnGotoEntity.addActionListener(e -> {
			eCEntity entity = ctx.getCurrentEntity();
			boolean gotoPosition = (e.getModifiers() & InputEvent.CTRL_MASK) != 0;
			if (gotoPosition) {
				IpcUtil.gotoPosition(entity.getWorldPosition());
			} else {
				IpcUtil.gotoGuid(entity.getGuid());
			}
		});
		ctx.getIpcMonitor().addListener(this, ipcMonitor -> btnGotoEntity.setEnabled(ipcMonitor.isAvailable()), true, false, true);

		JButton btnShowOnMap = new JButton("Karte", Icons.getImageIcon(Icons.Misc.MAP));
		btnShowOnMap.setToolTipText("Zeigt ausgewählte Entities auf Karte an.");
		btnShowOnMap.setFocusable(false);
		toolBar.add(btnShowOnMap);
		btnShowOnMap.addActionListener(e -> {
			FileDescriptor file = ctx.getFileDescriptor();
			for (eCEntity entity : entityTree.getSelectedEntities()) {
				EntityMap.getInstance(ctx).addEntity(entity, file);
			}
		});

		toolBar.addSeparator();
		JButton btnDiff = new JButton("Mit Originaldaten vergleichen", Icons.getImageIcon(Icons.Action.DIFF));
		btnDiff.setToolTipText("Vergleicht Entity mit Version aus den Originaldaten.");
		btnDiff.setFocusable(false);
		toolBar.add(btnDiff);
		btnDiff.addActionListener(a -> {
			try {
				Optional<File> originalFile = ctx.getFileManager().moveFromPrimaryToSecondary(ctx.getDataFile().get());
				if (!originalFile.isPresent() || !originalFile.get().isFile()) {
					TaskDialogs.error(ctx.getParentWindow(), "",
							"Es gibt keine Version der Datei in den Originaldaten bzw. die Datei befindet sich selbst in den Originaldaten.");
					return;
				}

				ArchiveFile archive = FileUtil.openArchive(originalFile.get(), false);
				eCEntity entity = ctx.getCurrentEntity();
				eCEntity originalEntity = archive.getEntityByGuid(entity.getGuid()).orElse(null);
				if (originalEntity == null) {
					TaskDialogs.error(ctx.getParentWindow(), "", "Es gibt keine Version der Entity in den Originaldaten.");
					return;
				}

				DiffNode diff = new EntityDiffer(true).diff(entity, originalEntity);
				ToMapPrintingVisitor mapPrintingVisitor = new ToMapPrintingVisitor(entity, originalEntity);
				diff.visit(mapPrintingVisitor);
				DisplayTextDialog dialog = new DisplayTextDialog("Vergleich: Entity - Original-Entity",
						mapPrintingVisitor.getMessagesAsString(), ctx.getParentWindow(), true);
				dialog.setVisible(true);
			} catch (IOException e) {
				TaskDialogs.showException(e);
			}
		});
	}

	public void loadView() {
		curView.load(ctx.getCurrentEntity());
	}

	/**
	 * Vor dem Umschalten auf eine andere Entity aufrufen, um Änderungen zu speichern
	 */
	public void saveView() {
		curView.save(ctx.getCurrentEntity());
	}

	/**
	 * Schaltet auf das angegebene View um.
	 *
	 * @param viewType Typ des Views
	 */
	public void showView(ArchiveViewType viewType) {
		// Änderungen vor dem Wechseln auf ein anderes View speichern
		if (curView != null) {
			saveView();
			curView.onLeave();
		}

		// View wechseln
		curView = views.get(viewType);
		curView.onEnter();
		entityTree.setExtension(curView.getTreeExtension());
		loadView();

		// GUI aktualisieren
		// JToolBar toolBar = curView.getToolBar();
		if (toolBar == null) {
			splitPane.setRightComponent(curView.getContent());
		} else {
			JPanel container = new JPanel(new BorderLayout());
			container.add(toolBar, BorderLayout.NORTH);
			container.add(curView.getContent(), BorderLayout.CENTER);
			splitPane.setRightComponent(container);
		}
		splitPane.setDividerLocation((int) entityTree.getMinimumSize().getWidth());
	}

	/**
	 * Nach dem Laden einer Datei aufrufen, um GUI zu aktualisieren
	 */
	public void initFileView(ArchiveFile aFile) {
		if (aFile != null) {
			splitPane.setVisible(true);

			// Missing Mapping
			checkEntityMapping(aFile);

			// EntityTree
			entityTree.setArchiveFile(aFile);
			entityTree.refreshTree(true, true);

			// View
			loadView();
		} else {
			splitPane.setVisible(false);
		}
		ctx.updateStatusBar();
	}

	/**
	 * Nach dem Auswählen/Verändern einer Entity aufrufen, um GUI zu aktualisieren
	 */
	public void refreshTree(boolean resetMarkedEntities) {
		entityTree.refreshTree(false, resetMarkedEntities);
	}

	/**
	 * Nach dem Auswählen/Verändern einer Entity aufrufen, um GUI zu aktualisieren
	 */
	public void refreshView() {
		saveView();
		loadView();
	}

	public EntityTree getEntityTree() {
		return entityTree;
	}

	private void checkEntityMapping(ArchiveFile aFile) {
		if (aFile.getGraph() == null) {
			return;
		}

		List<eCEntity> orphanEntities = aFile.pullOrphanEntities();
		if (orphanEntities.size() > 0) {
			ListSelectDialog<eCEntity> dialog = new ListSelectDialog<>(ctx.getParentWindow(), "Fehlerhafte SubEntityDefintion",
					AbstractSelectDialog.SELECTION_MULTIPLE, orphanEntities);
			if (dialog.openAndWasSuccessful()) {
				List<eCEntity> selected = dialog.getSelectedEntries();
				for (eCEntity entity : selected) {
					entity.moveToWorldNode(aFile.getGraph());
				}
			}
		}
	}

	public void onClose() {
		ctx.getIpcMonitor().removeListeners(this, true, true);
		for (ArchiveView view : views.values()) {
			view.cleanUp();
		}
		views = null;
		curView = null;
		entityTree.setArchiveFile(null);
		entityTree.refreshTree(true, true);
		entityTree = null;
	}
}

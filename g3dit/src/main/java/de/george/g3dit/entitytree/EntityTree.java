package de.george.g3dit.entitytree;

import java.awt.BorderLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.TransferHandler;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jidesoft.swing.JidePopupMenu;

import de.george.g3dit.EditorContext;
import de.george.g3dit.cache.Caches;
import de.george.g3dit.entitytree.filter.AbstractEntityFilter;
import de.george.g3dit.entitytree.filter.GuidEntityFilter;
import de.george.g3dit.entitytree.filter.ITreeExtension;
import de.george.g3dit.entitytree.filter.NameEntityFilter;
import de.george.g3dit.entitytree.filter.PositionEntityFilter;
import de.george.g3dit.entitytree.filter.TreeNodeBuilder;
import de.george.g3dit.util.Icons;
import de.george.g3utils.gui.SwingUtils;
import de.george.lrentnode.archive.ArchiveFile;
import de.george.lrentnode.archive.eCEntity;
import net.miginfocom.swing.MigLayout;

public class EntityTree extends JPanel {
	private static final Logger logger = LoggerFactory.getLogger(EntityTree.class);

	private JScrollPane scrollpane = new JScrollPane();
	private JTree tree = new JTree();

	private EntityTreeNode rootNode;
	private DefaultTreeModel treeModel;

	private List<eCEntity> markedEntities = new ArrayList<>();

	private JTextField searchField, tfPositionTolerance;
	private JRadioButton rbName, rbGuid, rbPosition;
	private JCheckBoxMenuItem cbKeepChilds;

	private ITreeExtension extension;
	private JPanel extensionPanel, searchPanel;
	private ActionListener extActionListener;
	private TreeRenderer renderer;
	private ITreePopupExtension popupExtension;

	private EditorContext ctx;
	private ArchiveFile archiveFile;

	public EntityTree(EditorContext ctx) {
		this.ctx = ctx;
		guiLayout();
		tree.setDragEnabled(true);
		tree.setTransferHandler(new TreeTransferHandler());
		tree.setDropMode(DropMode.ON_OR_INSERT);
	}

	private void guiLayout() {

		rbName = new JRadioButton("Name", true);
		rbName.setFocusable(false);
		rbName.setToolTipText(NameEntityFilter.getToolTipText());
		rbGuid = new JRadioButton("Guid");
		rbGuid.setFocusable(false);
		rbGuid.setToolTipText(GuidEntityFilter.getToolTipText());
		rbPosition = new JRadioButton("Position (");
		rbPosition.setToolTipText(PositionEntityFilter.getToolTipText());
		rbPosition.setFocusable(false);
		SwingUtils.createButtonGroup(rbName, rbGuid, rbPosition);

		ActionListener rbActionListener = e -> filterTree(false);
		rbName.addActionListener(rbActionListener);
		rbGuid.addActionListener(rbActionListener);
		rbPosition.addActionListener(rbActionListener);

		DocumentListener tfDocumentListener = SwingUtils.createDocumentListener(() -> filterTree(true));
		tfPositionTolerance = SwingUtils.createUndoTF();
		tfPositionTolerance.getDocument().addDocumentListener(tfDocumentListener);
		searchField = SwingUtils.createUndoTF();
		searchField.getDocument().addDocumentListener(tfDocumentListener);

		JButton btnErase = new JButton(Icons.getImageIcon(Icons.Action.ERASE));
		btnErase.setFocusable(false);
		btnErase.setToolTipText("Suche leeren");
		btnErase.addActionListener(e -> searchField.setText(null));

		JidePopupMenu pmSettings = new JidePopupMenu("Einstellungen");

		JButton btnSettings = new JButton(Icons.getImageIcon(Icons.Data.SETTINGS));
		btnSettings.setFocusable(false);
		btnSettings.addActionListener(l -> pmSettings.show(btnSettings, 15, 15));

		final JCheckBoxMenuItem cbShowNumber = new JCheckBoxMenuItem("Entity Nummer anzeigen", Icons.getImageIcon(Icons.Data.NUMBER),
				false);
		cbShowNumber.addActionListener(e -> {
			renderer.showEntityNumbers(cbShowNumber.isSelected());
			refreshTree(false, false);
		});

		cbKeepChilds = new JCheckBoxMenuItem("Subentities anzeigen ", Icons.getImageIcon(Icons.Data.SORT), false);
		cbKeepChilds.addActionListener(e -> filterTree(false));

		pmSettings.add(cbShowNumber);
		pmSettings.add(cbKeepChilds);

		tree = new JTree();
		tree.setLargeModel(true); // Größen Caching verhindern
		treeModel = new DefaultTreeModel(null);
		tree.setModel(treeModel);
		ToolTipManager.sharedInstance().registerComponent(tree);
		renderer = new TreeRenderer();
		tree.setCellRenderer(renderer);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setEditable(false);

		tree.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e)) {
					handleTreeRightClick(e.getX(), e.getY());
				}
			}
		});

		setLayout(new BorderLayout());

		searchPanel = new JPanel(new MigLayout("fillx", "[][][]push[]"));

		searchPanel.add(searchField, "cell 0 0, split 2, width 95%, spanx 4");
		searchPanel.add(btnErase, "width 23!, height 23!");

		searchPanel.add(rbName, "cell 0 1");
		searchPanel.add(rbGuid, "cell 1 1, gapleft 5");
		searchPanel.add(rbPosition, "cell 2 1, split 3, gapleft 5, gapright 1");
		searchPanel.add(tfPositionTolerance, "cell 2 1, width 40!, height 18!, gapright 1");
		searchPanel.add(new JLabel(")"), "cell 2 1");
		searchPanel.add(btnSettings, "spanx 2, gapright 0, gapleft 5, width 23!, height 23!");

		extensionPanel = new JPanel(new MigLayout("insets 0"));

		extActionListener = e -> filterTree(true);

		add(searchPanel, BorderLayout.NORTH);
		add(scrollpane = new JScrollPane(tree), BorderLayout.CENTER);
	}

	public void setArchiveFile(ArchiveFile archiveFile) {
		this.archiveFile = archiveFile;
		renderer.setArchiveFile(archiveFile);
		markedEntities.clear();
	}

	public int getSelectedEntityCount() {
		return tree.getSelectionCount();
	}

	public eCEntity getSelectedEntity() {
		TreePath path = tree.getSelectionPath();
		if (path != null) {
			return ((EntityTreeNode) path.getLastPathComponent()).getEntity();
		}
		return null;
	}

	public List<eCEntity> getSelectedEntities() {
		LinkedList<eCEntity> entities = new LinkedList<>();
		TreePath[] selectionPaths = tree.getSelectionPaths();
		if (selectionPaths != null) {
			for (TreePath path : selectionPaths) {
				if (path != null) {
					entities.add(((EntityTreeNode) path.getLastPathComponent()).getEntity());
				}
			}
		}
		return entities;
	}

	public void selectEntity(eCEntity entity) {
		selectEntity(entity, rootNode);
	}

	private void selectEntity(eCEntity entity, TreeNode altNode) {
		TreeNode parent = getNodeFromEntity(entity);
		if (parent == null) {
			parent = altNode;
		}
		TreePath path = getPathFromNode(parent);
		tree.setSelectionPath(path);
		tree.scrollPathToVisible(path);
	}

	private EntityTreeNode getNodeFromEntity(eCEntity entity) {
		@SuppressWarnings("unchecked")
		Enumeration<EntityTreeNode> enumeration = (Enumeration)((EntityTreeNode) treeModel.getRoot()).depthFirstEnumeration();
		while (enumeration.hasMoreElements()) {
			EntityTreeNode node = enumeration.nextElement();
			if (node.getEntity().equals(entity)) {
				return node;
			}
		}
		return null;
	}

	private TreePath getPathFromNode(TreeNode node) {
		List<TreeNode> nodes = new ArrayList<>();
		nodes.add(node);
		while ((node = node.getParent()) != null) {
			nodes.add(0, node);
		}
		return new TreePath(nodes.toArray());
	}

	public List<eCEntity> getMarkedEntities() {
		return markedEntities;
	}

	public void setMarkedEntities(List<eCEntity> entities) {
		markedEntities = entities;
	}

	public ArchiveFile getArchiveFile() {
		return archiveFile;
	}

	public JTree getTree() {
		return tree;
	}

	private void handleTreeRightClick(int x, int y) {
		if (popupExtension == null) {
			return;
		}

		TreePath path = tree.getPathForLocation(x, y);
		if (path == null) {
			return;
		}

		final eCEntity clickedEntity = ((EntityTreeNode) path.getLastPathComponent()).getEntity();
		final List<eCEntity> selEntities = getSelectedEntities();

		if (!selEntities.contains(clickedEntity)) {
			selEntities.clear();
			selEntities.add(clickedEntity);
		}

		popupExtension.showMenu(selEntities, clickedEntity, this, x, y);
	}

	/**
	 * Lädt den Baum neu
	 */
	public void refreshTree(boolean resetSearch, boolean resetMarkedEntites) {
		// Ausgewählte Entity vor dem Neuerstellen
		eCEntity selectedEntity = getSelectedEntity();

		if (resetMarkedEntites) {
			markedEntities.clear();
		}

		// Keine Datei geladen oder Datei ohne Entities
		if (archiveFile == null || archiveFile.getGraph() == null) {
			rootNode = null;
			treeModel.setRoot(null);
			// tree.setModel(treeModel);
			return;
		}

		// Aktuellen Knotenstatus speichern, greift NICHT wenn der Baum gerade gefiltert ist
		Enumeration<TreePath> expandedPaths = null;
		if (rootNode != null) {
			expandedPaths = tree.getExpandedDescendants(new TreePath(rootNode));
		}

		// Wurzelknoten ist immer die oberste RootEntity -> Position 0
		eCEntity root = archiveFile.getGraph();
		rootNode = new EntityTreeNode(root);

		for (eCEntity entity : root.getChilds()) {
			buildTree(rootNode, archiveFile, entity);
		}

		treeModel.setRoot(rootNode);

		// Vor dem Neuerstellen des Baumes gespeicherte Pfade wieder expanden
		while (expandedPaths != null && expandedPaths.hasMoreElements()) {
			TreePath path = expandedPaths.nextElement();
			TreeNode entityNode = getNodeFromEntity(((EntityTreeNode) path.getLastPathComponent()).getEntity());
			if (entityNode != null) {
				tree.expandPath(getPathFromNode(entityNode));
			}
		}

		// Wenn selectedEntity während des Erstellen des Baumes nicht gefunden wird, wird der
		// Wurzelknoten
		// ausgewählt
		selectEntity(selectedEntity, rootNode);

		if (resetSearch) {
			searchField.setText(null);
		} else if (extension != null && extension.isFilterActive() || !searchField.getText().isEmpty()) {
			// notwendig
			filterTree(false);
		}
	}

	/**
	 * Erstellt TreeNode entsprechend für <code>entity</code> und eventuellen Childs entsprechend
	 * des in <code>file</code> gegeben Mappings
	 *
	 * @param rootNode Parent TreeNode
	 * @param file Zugehöriges ArchiveFile
	 * @param entity eCEntity die dieser Knoten repräsentieren soll
	 * @param selectedEntity Zuletzt markierte Entity
	 * @return TreeNode der zuletzt markierten Entity, falls gefunden
	 */
	public void buildTree(EntityTreeNode rootNode, ArchiveFile file, eCEntity entity) {
		EntityTreeNode entityNode = new EntityTreeNode(entity);
		rootNode.add(entityNode);

		for (eCEntity subEntity : entity.getChilds()) {
			buildTree(entityNode, file, subEntity);
		}
	}

	private void filterTree(boolean resetOnInvalidFilter) {
		eCEntity selectedEntity = getSelectedEntity();

		if (rootNode == null) {
			return;
		}

		// get a copy
		EntityTreeNode filteredRoot = copyNode(rootNode);

		String text = searchField.getText();
		AbstractEntityFilter filter = rbName.isSelected()
				? new NameEntityFilter(text, false, archiveFile, Caches.stringtable(ctx).getFocusNamesOrEmpty())
				: rbGuid.isSelected() ? new GuidEntityFilter(text) : new PositionEntityFilter(text, tfPositionTolerance.getText());
		filter.setKeepChilds(cbKeepChilds.isSelected());

		boolean invalidFilter = !filter.isValid();
		boolean extonly = false;
		if (invalidFilter && extension != null && extension.isFilterActive()) {
			extonly = true;
			filter = null;
		}

		// Ungültige Eingabe und keine TreeExtension aktiv
		if (invalidFilter && !extonly) {

			if (!resetOnInvalidFilter) {
				return;
			}

			// reset with the original root
			treeModel.setRoot(rootNode);

			tree.setModel(treeModel);
			// tree.updateUI();
			scrollpane.getViewport().setView(tree);

			selectEntity(selectedEntity, rootNode);
		} else {// Gültiger Filter und/oder TreeExtension aktiv
			TreeNodeBuilder b = new TreeNodeBuilder(filter, extension);

			filteredRoot = b.prune((EntityTreeNode) filteredRoot.getRoot());

			treeModel.setRoot(filteredRoot);

			tree.setModel(treeModel);
			tree.updateUI();
			scrollpane.getViewport().setView(tree);

			for (int i = 0; i < tree.getRowCount(); i++) {
				tree.expandRow(i);
			}

			selectEntity(selectedEntity, filteredRoot);
		}
	}

	/*
	 * public void setBounds(int x, int y, int width, int height) { scrollpane.setBounds(x, y,
	 * width, height); }
	 */

	public void setSelectionMode(int mode) {
		tree.getSelectionModel().setSelectionMode(mode);
	}

	/**
	 * Clone/Copy a tree node. TreeNodes in Swing don't support deep cloning.
	 *
	 * @param orig to be cloned
	 * @return cloned copy
	 */
	private EntityTreeNode copyNode(EntityTreeNode orig) {

		EntityTreeNode newOne = new EntityTreeNode(orig.getEntity());

		@SuppressWarnings("unchecked")
		Enumeration<EntityTreeNode> enm = (Enumeration)orig.children();

		while (enm.hasMoreElements()) {

			EntityTreeNode child = enm.nextElement();
			newOne.add(copyNode(child));
		}
		return newOne;
	}

	public void setExtension(ITreeExtension extension) {
		boolean wasFilterActive = this.extension != null && this.extension.isFilterActive();

		this.extension = extension;
		renderer.setExtension(extension);
		extensionPanel.removeAll();
		searchPanel.remove(extensionPanel);

		if (extension != null) {
			searchPanel.add(extensionPanel, "cell 0 2, spanx 4, gapleft 0, gapright 0");
			extension.guiInit(extensionPanel, extActionListener);
		}

		if (wasFilterActive || extension != null && extension.isFilterActive()) {
			filterTree(true);
		}

	}

	public void setPopupExtension(ITreePopupExtension popupExtension) {
		this.popupExtension = popupExtension;
		tree.setDragEnabled(popupExtension == null ? false : true);
	}

	public void addTreeSelectionListener(TreeSelectionListener treeSelectionListener) {
		tree.addTreeSelectionListener(treeSelectionListener);
	}

	// Source Taken from http://www.coderanch.com/t/346509/GUI/java/JTree-drag-drop-inside-one
	private class TreeTransferHandler extends TransferHandler {

		DataFlavor entitiesFlavor;
		DataFlavor[] flavors = new DataFlavor[1];

		public TreeTransferHandler() {
			try {
				String mimeType = DataFlavor.javaJVMLocalObjectMimeType + ";class=\"" + eCEntity[].class.getName() + "\"";
				entitiesFlavor = new DataFlavor(mimeType);
				flavors[0] = entitiesFlavor;
			} catch (ClassNotFoundException e) {
				logger.warn("TreeTransferHandler mime ClassNotFound: {}", e.getMessage());
			}
		}

		@Override
		public boolean canImport(TransferHandler.TransferSupport support) {
			if (!support.isDrop()) {
				return false;
			}
			support.setShowDropLocation(true);
			if (!support.isDataFlavorSupported(entitiesFlavor)) {
				return false;
			}

			JTree.DropLocation dl = (JTree.DropLocation) support.getDropLocation();
			if (dl.getPath() == null) {
				return false;
			}

			eCEntity targetEntity = ((EntityTreeNode) dl.getPath().getLastPathComponent()).getEntity();
			eCEntity[] transferEntities = null;
			try {
				transferEntities = (eCEntity[]) support.getTransferable().getTransferData(entitiesFlavor);
			} catch (UnsupportedFlavorException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			// Drop Entity darf keine Quell-Entity sein
			for (eCEntity transferEntity : transferEntities) {
				if (targetEntity.equals(transferEntity)) {
					return false;
				}
			}

			for (eCEntity transferEntity : transferEntities) {
				if (transferEntity.equals(targetEntity) // Ziel-Entity gleich der Quell-Entity
						|| transferEntity.isIndirectChild(targetEntity) // Ziel-Entity ist SubEntity
																		// der Quell-Entity
				// || archiveFile.isDirectSubEntityOf(targetEntity, transferEntity) //Quell-Entity
				// ist bereits
				// direkte SubEntity der Ziel-Entity
				) {
					return false;
				}
			}

			return true;
		}

		@Override
		protected Transferable createTransferable(JComponent c) {
			JTree tree = (JTree) c;
			TreePath[] paths = tree.getSelectionPaths();
			if (paths != null) {
				Set<eCEntity> entities = new LinkedHashSet<>(paths.length);
				for (int i = 0; i < paths.length; i++) {
					entities.add(((EntityTreeNode) paths[i].getLastPathComponent()).getEntity());
				}

				Set<eCEntity> cleanEntities = new LinkedHashSet<>(entities);
				boolean status = true;
				checkFailed: for (eCEntity markedEntity : entities) {
					// Bereits entfernte SubEntities Überspringen
					if (!cleanEntities.contains(markedEntity)) {
						continue;
					}
					for (eCEntity posSubEntity : entities) {
						if (markedEntity.isIndirectChild(posSubEntity)) {
							// Sind auch alle anderen SubEntities markiert?
							Set<eCEntity> subEntities = markedEntity.getIndirectChilds().toSet();
							if (entities.containsAll(subEntities)) {
								cleanEntities.removeAll(subEntities);
							} else {
								status = false;
								break checkFailed;
							}
						}
					}
				}
				if (status) {
					return new ArchiveEntityTransferable(cleanEntities.toArray(new eCEntity[cleanEntities.size()]));
				}
			}
			return null;
		}

		@Override
		protected void exportDone(JComponent source, Transferable data, int action) {
			refreshTree(false, true);
		}

		@Override
		public int getSourceActions(JComponent c) {
			return MOVE;// COPY_OR_MOVE;
		}

		@Override
		public boolean importData(TransferHandler.TransferSupport support) {
			if (!canImport(support)) {
				return false;
			}

			eCEntity[] droppedEntities = null;
			try {
				droppedEntities = (eCEntity[]) support.getTransferable().getTransferData(entitiesFlavor);
			} catch (UnsupportedFlavorException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			// Get drop location info.
			JTree.DropLocation dl = (JTree.DropLocation) support.getDropLocation();
			eCEntity targetEntity = ((EntityTreeNode) dl.getPath().getLastPathComponent()).getEntity();
			int childIndex = dl.getChildIndex();

			// Bestehende Mappings entfernen
			if (support.getDropAction() == MOVE) {

			}

			if (childIndex == -1 || childIndex == targetEntity.getChilds().size()) {
				for (eCEntity entity : droppedEntities) {
					entity.moveToWorldNode(targetEntity);
				}
			} else { // DropMode.INSERT
				List<eCEntity> wrappedEntities = new ArrayList<>(targetEntity.getChilds());
				eCEntity entityBefore = wrappedEntities.get(childIndex);
				for (eCEntity entity : droppedEntities) {
					if (entity.equals(entityBefore)) {
						childIndex = wrappedEntities.indexOf(entity);
						if (childIndex > 0) {
							entityBefore = wrappedEntities.get(childIndex - 1);
						}

					}
					wrappedEntities.remove(entity);
				}

				if (childIndex > 0) {
					childIndex = wrappedEntities.indexOf(entityBefore);
				}
				for (eCEntity entity : droppedEntities) {
					wrappedEntities.add(childIndex++, entity);
				}

				// Add the new entities
				for (eCEntity entity : wrappedEntities) {
					if (!targetEntity.isChild(entity)) {
						entity.moveToWorldNode(targetEntity);
					}
				}

				// Sort them according to drop position
				targetEntity.removeAllChildren(false);
				for (eCEntity entity : wrappedEntities) {
					targetEntity.attachChild(entity);
				}
			}

			return true;
		}

		@Override
		public String toString() {
			return getClass().getName();
		}

		public class ArchiveEntityTransferable implements Transferable {
			private WeakReference<eCEntity[]> entities;

			public ArchiveEntityTransferable(eCEntity[] nodes) {
				entities = new WeakReference<>(nodes);
			}

			@Override
			public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
				if (!isDataFlavorSupported(flavor)) {
					throw new UnsupportedFlavorException(flavor);
				}

				eCEntity[] dEntities = entities.get();
				return dEntities != null ? dEntities : new eCEntity[0];
			}

			@Override
			public DataFlavor[] getTransferDataFlavors() {
				return flavors;
			}

			@Override
			public boolean isDataFlavorSupported(DataFlavor flavor) {
				return entitiesFlavor.equals(flavor);
			}
		}
	}

}

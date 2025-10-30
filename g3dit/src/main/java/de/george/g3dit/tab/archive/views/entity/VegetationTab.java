package de.george.g3dit.tab.archive.views.entity;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableModel;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.table.ColumnFactory;
import org.jdesktop.swingx.table.TableColumnExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ezware.dialog.task.TaskDialogs;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.github.bsideup.jabel.Desugar;
import com.google.common.collect.ImmutableBiMap;
import com.l2fprod.common.swing.renderer.ColorCellRenderer;
import com.teamunify.i18n.I;

import de.george.g3dit.gui.table.TableUtil;
import de.george.g3dit.gui.theme.LayoutUtils;
import de.george.g3dit.rpc.IpcUtil;
import de.george.g3dit.tab.archive.EditorArchiveTab;
import de.george.g3dit.tab.archive.views.entity.dialogs.CreatePlantDialog;
import de.george.g3dit.tab.archive.views.entity.dialogs.EditVegetationMeshesDialog;
import de.george.g3dit.tab.archive.views.entity.dialogs.ExportVegetationObjectsDialog;
import de.george.g3dit.tab.archive.views.entity.dialogs.ImportVegetationObjectsDialog;
import de.george.g3dit.tab.archive.views.entity.dialogs.SelectMeshDialog;
import de.george.g3dit.util.Dialogs;
import de.george.g3dit.util.FileDialogWrapper;
import de.george.g3dit.util.Icons;
import de.george.g3dit.util.json.JsonUtil;
import de.george.g3utils.gui.ColorChooserButton;
import de.george.g3utils.gui.ListTableModel;
import de.george.g3utils.gui.SwingUtils;
import de.george.g3utils.io.G3FileReaderVirtual;
import de.george.g3utils.io.G3FileWriterVirtual;
import de.george.g3utils.structure.bCEulerAngles;
import de.george.g3utils.structure.bCMatrix;
import de.george.g3utils.structure.bCQuaternion;
import de.george.g3utils.structure.bCVector;
import de.george.g3utils.util.FilesEx;
import de.george.g3utils.util.IOUtils;
import de.george.g3utils.util.Misc;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.classes.eCVegetation_Mesh;
import de.george.lrentnode.classes.eCVegetation_Mesh.eSVegetationMeshID;
import de.george.lrentnode.classes.eCVegetation_PS;
import de.george.lrentnode.classes.eCVegetation_PS.PlantRegionEntry;
import de.george.lrentnode.classes.eCVegetation_PS.eCVegetation_GridNode;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.util.ClassUtil;
import de.george.lrentnode.util.EntityUtil;
import net.miginfocom.swing.MigLayout;

public class VegetationTab extends AbstractEntityTab {
	private static final Logger logger = LoggerFactory.getLogger(VegetationTab.class);

	private JXTable table;
	private JTextField tfPosX, tfPosY, tfPosZ, tfRadius;
	private JCheckBox cbInvert;

	private eCEntity entity;
	private eCVegetation_PS vegetationPS;

	private PlantTableModel model;
	private eSVegetationMeshID lastSelectedMeshID = null;

	private boolean boundsNeedUpdate = false;

	private ColorChooserButton ccbDefaultColor;

	public VegetationTab(EditorArchiveTab ctx) {
		super(ctx);
	}

	@Override
	public void initComponents() {
		setLayout(new MigLayout("fill", "", "[]push[grow]push[]"));

		JPanel filterPanel = new JPanel(new MigLayout("gapx 10:20:30"));

		tfPosX = SwingUtils.createUndoTF();
		tfPosY = SwingUtils.createUndoTF();
		tfPosZ = SwingUtils.createUndoTF();
		tfRadius = SwingUtils.createUndoTF();
		cbInvert = new JCheckBox(I.tr("Invert"));
		cbInvert.setToolTipText(I.tr("Show entries outside instead of within the specified radius."));

		JButton btnPastePosition = new JButton(Icons.getImageIcon(Icons.IO.IMPORT));
		btnPastePosition.setToolTipText(I.tr("Use position from clipboard"));
		btnPastePosition.addActionListener(e -> handlePastePosition());

		filterPanel.add(new JLabel(I.trc("coordinate", "X")));
		filterPanel.add(new JLabel(I.trc("coordinate", "Y")));
		filterPanel.add(new JLabel(I.trc("coordinate", "Z")), "");
		filterPanel.add(new JLabel(I.tr("Radius")), "wrap");
		filterPanel.add(tfPosX, "width 50:100:150");
		filterPanel.add(tfPosY, "width 50:100:150");
		filterPanel.add(tfPosZ, "width 50:100:150");
		filterPanel.add(tfRadius, "width 50:100:150");
		filterPanel.add(cbInvert);
		filterPanel.add(btnPastePosition, LayoutUtils.sqrBtn());
		add(filterPanel, "wrap");

		DocumentListener tfDocumentListener = SwingUtils.createDocumentListener(this::filterPlants);
		tfPosX.getDocument().addDocumentListener(tfDocumentListener);
		tfPosY.getDocument().addDocumentListener(tfDocumentListener);
		tfPosZ.getDocument().addDocumentListener(tfDocumentListener);
		tfRadius.getDocument().addDocumentListener(tfDocumentListener);
		cbInvert.addActionListener(a -> filterPlants());

		table = new JXTable();
		table.setColumnControlVisible(true);
		table.setColumnFactory(new PlantTableColumnFactory());
		JScrollPane tableScroll = new JScrollPane(table);
		add(tableScroll, "grow, wrap");

		model = new PlantTableModel();
		table.setModel(model);

		model.setPlantTableListener((tableEntry, rotation) -> {
			tableEntry.gridNode.removeEntry(tableEntry.entry);

			// Änderungen durchführen
			PlantRegionEntry plantEntry = tableEntry.entry;
			plantEntry.position = tableEntry.position;
			rotation.ifPresent(r -> plantEntry.rotation = r);
			plantEntry.scaleWidth = tableEntry.scaleWidth;
			plantEntry.scaleHeight = tableEntry.scaleHeight;
			plantEntry.meshID = tableEntry.meshID;

			// An neuer Position einfügen
			tableEntry.gridNode = vegetationPS.getGrid().insertEntry(plantEntry);
			boundsNeedUpdate = true;
			ctx.fileChanged();
		});

		JButton btnCreate = new JButton(I.tr("Insert new object"), Icons.getImageIcon(Icons.Action.ADD));
		btnCreate.setMnemonic(KeyEvent.VK_E);
		add(btnCreate, "split 5, gaptop 5");
		btnCreate.addActionListener((e) -> insertPlant(false));

		JButton btnCreateFromClipboard = new JButton(I.tr("New object from clipboard"), Icons.getImageIcon(Icons.IO.IMPORT));
		btnCreateFromClipboard.setMnemonic(KeyEvent.VK_C);
		add(btnCreateFromClipboard, "");
		btnCreateFromClipboard.addActionListener((e) -> insertPlant(true));

		JButton btnEdit = new JButton(I.tr("Edit object"), Icons.getImageIcon(Icons.Action.EDIT));
		btnEdit.setMnemonic(KeyEvent.VK_A);
		TableUtil.enableOnEqual(table, btnEdit, 1);
		add(btnEdit, "");
		btnEdit.addActionListener((e) -> editSelectedPlant());

		JButton btnDelete = new JButton(I.tr("Delete selected"), Icons.getImageIcon(Icons.Action.DELETE));
		btnDelete.setMnemonic(KeyEvent.VK_S);
		TableUtil.enableOnGreaterEqual(table, btnDelete, 1);
		add(btnDelete, "");
		btnDelete.addActionListener((e) -> removeSelectedPlants());

		JLabel lblCount = new JLabel();
		Runnable updateObjectCount = () -> lblCount.setText(I.trf("{0, number} objects", model.getRowCount()));
		updateObjectCount.run();
		add(lblCount, "gapx push, wrap");
		model.addTableModelListener(e -> updateObjectCount.run());

		JButton btnApplyColor = new JButton(I.tr("Colorize"), Icons.getImageIcon(Icons.Color.ARROW));
		add(btnApplyColor, "gaptop 10, split 4, sgy color");
		TableUtil.enableOnGreaterEqual(table, btnApplyColor, 1);
		btnApplyColor.addActionListener(e -> applyColor());

		ccbDefaultColor = new ColorChooserButton(Color.WHITE, ctx.getParentWindow(), false);
		add(ccbDefaultColor, "gaptop 10, width 40!, sgy color");
		ccbDefaultColor.setToolTipText(I.tr("Default color"));

		JButton btnSetDefaultColor = new JButton(I.tr("Default color from selection"), Icons.getImageIcon(Icons.Color.PENCIL));
		add(btnSetDefaultColor, "gaptop 10, sgy color");
		TableUtil.enableOnEqual(table, btnSetDefaultColor, 1);
		btnSetDefaultColor.addActionListener(e -> setDefaultColor());

		JButton btnSetMesh = new JButton(I.tr("Set mesh"), Icons.getImageIcon(Icons.Action.BOOK_EDIT));
		btnSetMesh.setMnemonic(KeyEvent.VK_M);
		TableUtil.enableOnGreaterEqual(table, btnSetMesh, 1);
		add(btnSetMesh, "gaptop 10, sgy color, wrap");
		btnSetMesh.addActionListener((e) -> setMeshSelectedPlants());

		JButton btnGotoPlant = new JButton(I.tr("Goto"), Icons.getImageIcon(Icons.Misc.GEOLOCATION));
		btnGotoPlant.setToolTipText(I.tr("Teleports the player to the object."));
		btnGotoPlant.setMnemonic(KeyEvent.VK_G);
		add(btnGotoPlant, "gaptop 10, wrap");
		btnGotoPlant.addActionListener(e -> gotoPlant());
		TableUtil.enableOnGreaterEqual(table, btnGotoPlant, 1, ctx.getIpcMonitor()::isAvailable);
		ctx.getIpcMonitor().addListener(this,
				ipcMonitor -> btnGotoPlant.setEnabled(ipcMonitor.isAvailable() && table.getSelectedRowCount() >= 1), true, false, true);

		JButton btnImportObjects = new JButton(I.tr("Import objects from JSON"), Icons.getImageIcon(Icons.Arrow.BAR_UP));
		add(btnImportObjects, "gaptop 20, split 2");
		btnImportObjects.addActionListener((e) -> importFromJson());

		JButton btnExportObjects = new JButton(I.tr("Export objects to JSON"), Icons.getImageIcon(Icons.Arrow.BAR_DOWN));
		TableUtil.enableOnGreaterEqual(table, btnExportObjects, 1);
		add(btnExportObjects, "wrap");
		btnExportObjects.addActionListener((e) -> exportToJson());

		JButton btnEditMeshes = new JButton(I.tr("View meshes"), Icons.getImageIcon(Icons.Action.BOOK));
		add(btnEditMeshes, "gaptop 10, split 2");
		btnEditMeshes.addActionListener((e) -> editMeshes());

		JButton btnUpdateBounds = new JButton(I.tr("Recalculate BoundaryBox"), Icons.getImageIcon(Icons.Arrow.CIRCLE_DOUBLE));
		btnUpdateBounds.setToolTipText(I.tr(
				"<html>Normally not needed, as the bounding box is automatically recalculated when changes are made.<br>Can be used to repair a file that was edited manually or with a version of g3dit,<br>which did not yet support recalculation of the bounding box (anything before 1.5a).</html>"));
		add(btnUpdateBounds, "gaptop 10");
		btnUpdateBounds.addActionListener((e) -> updateBounds());
	}

	@Override
	public String getTabTitle() {
		return "Vegetation";
	}

	@Override
	public boolean isActive(eCEntity entity) {
		return entity.hasClass(CD.eCVegetation_PS.class);
	}

	private void insertPlant(boolean fromClipboard) {
		CreatePlantDialog dialog = new CreatePlantDialog(ctx.getParentWindow(), I.tr("Insert new object"), vegetationPS,
				lastSelectedMeshID, fromClipboard ? IOUtils.getClipboardContent() : null, ccbDefaultColor.getSelectedColor());

		if (dialog.openAndWasSuccessful()) {
			lastSelectedMeshID = dialog.getLastSelectedMeshID();

			PlantRegionEntry createdEntry = dialog.getCreatedEntry();
			if (!isInsideStaticNode(createdEntry.position)) {
				boolean result = TaskDialogs.isConfirmed(ctx.getParentWindow(), I.tr("Should the new object be saved?"),
						I.trf("The position ({0}) of the new object ''{1}''\nis outside the scope of this file.\n"
								+ "The object should instead be inserted into the file responsible for the scope.",
								createdEntry.position.toString(), vegetationPS.getMeshClass(createdEntry.meshID).getName()));

				// Änderungen verwerfen
				if (!result) {
					return;
				}
			}

			vegetationPS.getGrid().insertEntry(createdEntry);
			boundsNeedUpdate = true;
			ctx.fileChanged();
			loadValues();
		}
	}

	private void editSelectedPlant() {
		PlantTableEntry entry = model.getEntry(TableUtil.getSelectedRow(table));

		lastSelectedMeshID = entry.meshID;
		String encodedPlant = String.format("%s\npitch: %s yaw: %s roll: %s\nscalewidth: %s scaleheight: %s\ncolor: %s",
				entry.position.toString().replace(",", "."), Misc.formatFloat(entry.pitch), Misc.formatFloat(entry.yaw),
				Misc.formatFloat(entry.roll), Misc.formatFloat(entry.scaleWidth), Misc.formatFloat(entry.scaleHeight),
				Misc.colorToHexStringRGB(new Color(entry.colorARGB)));

		CreatePlantDialog dialog = new CreatePlantDialog(ctx.getParentWindow(), I.tr("Edit object"), vegetationPS, lastSelectedMeshID,
				encodedPlant, ccbDefaultColor.getSelectedColor());

		if (dialog.openAndWasSuccessful()) {
			lastSelectedMeshID = dialog.getLastSelectedMeshID();

			PlantRegionEntry createdEntry = dialog.getCreatedEntry();
			if (!isInsideStaticNode(createdEntry.position)) {
				boolean result = TaskDialogs.isConfirmed(ctx.getParentWindow(), I.tr("Should the new position be saved?"),
						I.trf("The new position ({0}) of object ''{1}''\nis outside the scope of this file.\n"
								+ "The object should instead be moved to the file responsible for the area.", entry.position.toString(),
								vegetationPS.getMeshClass(entry.meshID).getName()));

				// Änderungen verwerfen
				if (!result) {
					return;
				}
			}

			entry.gridNode.removeEntry(entry.entry);
			vegetationPS.getGrid().insertEntry(createdEntry);
			boundsNeedUpdate = true;
			ctx.fileChanged();
			loadValues();
		}
	}

	private void handlePastePosition() {
		String clipboardContent = IOUtils.getClipboardContent();

		bCVector position = Misc.stringToPosition(clipboardContent);
		if (position != null) {
			tfPosX.setText(Misc.formatFloat(position.getX()));
			tfPosY.setText(Misc.formatFloat(position.getY()));
			tfPosZ.setText(Misc.formatFloat(position.getZ()));
		} else {
			TaskDialogs.inform(ctx.getParentWindow(), I.tr("Clipboard does not contain position data"), null);
		}
	}

	private void setMeshSelectedPlants() {
		var dialog = new SelectMeshDialog(ctx.getParentWindow(), I.tr("Select mesh to set"), vegetationPS, lastSelectedMeshID);
		if (!dialog.openAndWasSuccessful() || dialog.getLastSelectedMeshID() == null)
			return;

		lastSelectedMeshID = dialog.getLastSelectedMeshID();

		for (int row : TableUtil.getSelectedRows(table)) {
			PlantTableEntry entry = model.getEntry(row);
			entry.meshID = lastSelectedMeshID;
			entry.entry.meshID = lastSelectedMeshID;
		}

		boundsNeedUpdate = true;
		ctx.fileChanged();
		loadValues();
	}

	private void removeSelectedPlants() {
		for (int row : TableUtil.getSelectedRows(table)) {
			PlantTableEntry entry = model.getEntry(row);
			entry.gridNode.removeEntry(entry.entry);
		}

		boundsNeedUpdate = true;
		ctx.fileChanged();
		loadValues();
	}

	private void applyColor() {
		Color color = ccbDefaultColor.getSelectedColor();

		for (int row : TableUtil.getSelectedRows(table)) {
			PlantTableEntry entry = model.getEntry(row);
			entry.colorARGB = color.getRGB();
			entry.entry.colorARGB = color.getRGB();
		}

		ctx.fileChanged();
		model.fireTableDataChanged();
	}

	private void setDefaultColor() {
		ccbDefaultColor.setSelectedColor(new Color(model.getEntry(TableUtil.getSelectedRow(table)).colorARGB));
	}

	private void gotoPlant() {
		PlantTableEntry entry = model.getEntry(TableUtil.getSelectedRow(table));
		IpcUtil.gotoPosition(entry.position);
	}

	private ObjectMapper jsonMapper = JsonUtil.noGetterAutodetectMapper().registerModule(JsonUtil.getExtensionModule())
			.registerModule(new ParameterNamesModule())
			.enable(SerializationFeature.INDENT_OUTPUT, SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);

	private void importFromJson() {
		Path openFile = FileDialogWrapper.saveFile(I.tr("Import objects from JSON"), null, "json", ctx.getParentWindow(),
				FileDialogWrapper.JSON_FILTER);
		if (openFile == null)
			return;

		var dialog = new ImportVegetationObjectsDialog(ctx.getParentWindow(), I.tr("Import objects from JSON"));
		if (!dialog.openAndWasSuccessful())
			return;

		PlantJsonObjectDb db;
		try {
			db = jsonMapper.readValue(openFile.toFile(), PlantJsonObjectDb.class);
		} catch (IOException e) {
			logger.error("Failed to import objects from JSON.", e);
			TaskDialogs.error(ctx.getParentWindow(), I.tr("Failed to import objects from JSON"), e.getMessage());
			return;
		}

		Function<PlantJsonObject, Optional<eCVegetation_Mesh>> lookupMesh = object -> vegetationPS.getMeshClasses().stream()
				.filter(m -> m.getName().equals(object.meshName)).findFirst();

		for (var object : db.objects) {
			var mesh = lookupMesh.apply(object).orElse(null);
			if (mesh == null) {
				byte[] meshData = db.meshes.get(object.meshName);
				if (meshData == null) {
					if (dialog.skipMissing())
						continue;

					TaskDialogs.error(ctx.getParentWindow(), I.tr("Malformed vegetation object"),
							I.trf("Vegetation object to be imported refers to non-existing mesh:\n{0}", object.meshName));
					return;
				}

				if (!dialog.importMeshes()) {
					if (dialog.skipMissing())
						continue;

					TaskDialogs.error(ctx.getParentWindow(), I.tr("Malformed vegetation object"), I.trf(
							"Vegetation object to be imported refers to non-existing mesh (contained in JSON, but mesh import is disabled):\n{0}",
							object.meshName));
					return;
				}

				try (var reader = new G3FileReaderVirtual(meshData)) {
					mesh = (eCVegetation_Mesh) ClassUtil.readSubClass(reader);
					vegetationPS.addMeshClass(mesh);
				} catch (IOException e) {
					logger.error("Failed to parse mesh data from JSON.", e);
					TaskDialogs.error(ctx.getParentWindow(), I.tr("Failed to parse mesh data from JSON"), e.getMessage());
					return;
				}
			}
		}

		Dialogs.Answer answer = null;
		for (var object : db.objects) {
			var mesh = lookupMesh.apply(object);
			if (dialog.skipMissing() && !mesh.isPresent())
				continue;

			var relativeMatrix = new bCMatrix(bCEulerAngles.fromDegree(object.yaw, object.pitch, object.roll),
					new bCVector(object.scaleWidth, object.scaleHeight, object.scaleWidth), object.position);
			var entryMatrix = dialog.getImportPosition().getProduct(relativeMatrix);
			var entryScaling = entryMatrix.getPureScaling();

			bCQuaternion rotation = new bCQuaternion(entryMatrix);
			var entry = new PlantRegionEntry(mesh.get().getMeshID(), entryMatrix.getTranslation(), rotation,
					Math.max(entryScaling.getX(), entryScaling.getZ()), entryScaling.getY(), object.color.getRGB());

			if (!isInsideStaticNode(entry.position) && answer != Dialogs.Answer.AllYes) {
				answer = answer == Dialogs.Answer.AllNo ? answer
						: Dialogs.askYesNoCancel(ctx.getParentWindow(), I.tr("Should the object be imported?"),
								I.trf("The position ({0}) of the new object ''{1}''\nis outside the scope of this file.\n"
										+ "The object should instead be inserted into the file responsible for the scope.",
										entry.position.toString(), mesh.get().getName()),
								true);

				switch (answer) {
					case Yes:
					case AllYes:
						vegetationPS.getGrid().insertEntry(entry);
						break;
					case Cancel:
						return;
					case No:
					case AllNo:
						break;
				}
			} else
				vegetationPS.getGrid().insertEntry(entry);
		}

		boundsNeedUpdate = true;
		ctx.fileChanged();
		loadValues();
	}

	private void exportToJson() {
		var objects = new ArrayList<PlantJsonObject>();
		var meshes = new HashMap<String, byte[]>();

		var dialog = new ExportVegetationObjectsDialog(ctx.getParentWindow(), I.tr("Export objects to JSON"));
		if (!dialog.openAndWasSuccessful())
			return;

		for (int row : TableUtil.getSelectedRows(table)) {
			PlantTableEntry entry = model.getEntry(row);
			var mesh = vegetationPS.getMeshClass(entry.meshID);
			if (!meshes.containsKey(mesh.getName()) && dialog.includeMeshes()) {
				var writer = new G3FileWriterVirtual();
				ClassUtil.writeSubClass(writer, mesh);
				meshes.put(mesh.getName(), writer.getData());
			}

			var entryMatrix = new bCMatrix(bCEulerAngles.fromDegree(entry.yaw, entry.pitch, entry.roll),
					new bCVector(entry.scaleWidth, entry.scaleHeight, entry.scaleWidth), entry.position);
			var relativeMatrix = dialog.getExportPosition().getInverted();
			relativeMatrix.multiply(entryMatrix);
			var relativeRotation = new bCEulerAngles(relativeMatrix);
			var relativeScaling = relativeMatrix.getPureScaling();

			objects.add(new PlantJsonObject(mesh.getName(), relativeMatrix.getTranslation(), relativeRotation.getPitchDeg(),
					relativeRotation.getYawDeg(), relativeRotation.getRollDeg(), Math.max(relativeScaling.getX(), relativeScaling.getZ()),
					relativeScaling.getY(), new Color(entry.colorARGB)));
		}

		Path saveFile = FileDialogWrapper.saveFile(I.tr("Export objects to JSON"), null, "json", ctx.getParentWindow(),
				FileDialogWrapper.JSON_FILTER);
		if (saveFile == null)
			return;

		var db = new PlantJsonObjectDb(objects, meshes);
		try {

			jsonMapper.writeValue(saveFile.toFile(), db);
		} catch (IOException e) {
			logger.error("Failed to export objects to JSON.", e);
			TaskDialogs.error(ctx.getParentWindow(), I.tr("Failed to export objects to JSON"), e.getMessage());
		}
	}

	private void editMeshes() {
		EditVegetationMeshesDialog dialog = new EditVegetationMeshesDialog(ctx.getParentWindow(), ctx, vegetationPS,
				() -> boundsNeedUpdate = true);

		if (dialog.openAndWasSuccessful()) {
			ctx.fileChanged();
			loadValues();
		}
	}

	private void updateBounds() {
		logger.info("eCVegetation_PS old boundary: min: {} max: {}", vegetationPS.getBounds().getMin(), vegetationPS.getBounds().getMax());

		for (eCVegetation_GridNode node : vegetationPS.getGrid().getGridNodes()) {
			node.updateBounds();
		}

		logger.info("eCVegetation_PS new boundary: min: {} max: {}", vegetationPS.getBounds().getMin(), vegetationPS.getBounds().getMax());

		entity.updateLocalNodeBoundary(vegetationPS.getBounds());
	}

	private boolean isInsideStaticNode(bCVector position) {
		return !EntityUtil.isOutsideOfStaticNodesArea(position, ctx.getDataFile().map(FilesEx::getFileName).orElse(null));
	}

	protected void filterPlants() {
		try {
			bCVector position = new bCVector(Float.valueOf(tfPosX.getText()), Float.valueOf(tfPosY.getText()),
					Float.valueOf(tfPosZ.getText()));
			float radius = Float.valueOf(tfRadius.getText());
			table.setRowFilter(new PlantTableRowFilter(position, radius, cbInvert.isSelected()));
		} catch (NumberFormatException e) {
			// Ungültige Sucheingabe -> Alle Einträge anzeigen
			table.setRowFilter(null);
		}
	}

	@Override
	public void loadValues(eCEntity entity) {
		boundsNeedUpdate = false;
		this.entity = entity;
		vegetationPS = entity.getClass(CD.eCVegetation_PS.class);
		loadValues();
	}

	private void loadValues() {
		List<PlantTableEntry> entries = new ArrayList<>();

		for (eCVegetation_GridNode gridNode : vegetationPS.getGrid().getGridNodes()) {
			for (PlantRegionEntry entry : gridNode.getEntries()) {
				entries.add(new PlantTableEntry(gridNode, entry));
			}
		}

		model.setEntries(entries);
	}

	@Override
	public void saveValues(eCEntity entity) {
		if (boundsNeedUpdate) {
			vegetationPS.getGrid().updateBounds();
			entity.updateLocalNodeBoundary(vegetationPS.getBounds());
		}
	}

	private static final ImmutableBiMap<String, String> PLANT_COLUMN_MAPPING = ImmutableBiMap.of("Position X", I.tr("Position X"),
			"Position Y", I.tr("Position Y"), "Position Z", I.tr("Position Z"), "Pitch", I.tr("Pitch"), "Yaw", I.tr("Yaw"), "Roll",
			I.tr("Roll"), "Scale Width", I.tr("Scale Width"), "Scale Height", I.tr("Scale Height"), "Mesh", I.tr("Mesh"), "Color",
			I.tr("Color"));

	public class PlantTableModel extends ListTableModel<PlantTableEntry> {
		private PlantTableListener listener;

		public PlantTableModel() {
			super(PLANT_COLUMN_MAPPING.values().toArray(new String[0]));
		}

		@Override
		public Object getValueAt(PlantTableEntry entry, int col) {
			return switch (col) {
				case 0 -> Misc.round(entry.position.getX(), 2);
				case 1 -> Misc.round(entry.position.getY(), 2);
				case 2 -> Misc.round(entry.position.getZ(), 2);
				case 3 -> Misc.round(entry.pitch, 2);
				case 4 -> Misc.round(entry.yaw, 2);
				case 5 -> Misc.round(entry.roll, 2);
				case 6 -> Misc.round(entry.scaleWidth, 2);
				case 7 -> Misc.round(entry.scaleHeight, 2);
				case 8 -> vegetationPS.getMeshClass(entry.meshID).getName();
				case 9 -> new Color(entry.colorARGB);
				default -> null;
			};
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return columnIndex < 8;
		}

		@Override
		public void setValueAt(Object aValue, PlantTableEntry tableEntry, int columnIndex) {
			float newValue = 0;

			try {
				newValue = Float.valueOf(aValue.toString().replace(",", "."));
			} catch (NumberFormatException e) {
				// Ungültiger Wert, abbrechen!
				return;
			}

			int changed = 0;
			bCVector position = tableEntry.position.clone();
			switch (columnIndex) {
				case 0:
					if (!Misc.compareFloat(position.getX(), newValue, 0.01f)) {
						position.setX(newValue);
						changed = 1;
					}
					break;
				case 1:
					if (!Misc.compareFloat(position.getY(), newValue, 0.01f)) {
						position.setY(newValue);
						changed = 1;
					}
					break;
				case 2:
					if (!Misc.compareFloat(position.getZ(), newValue, 0.01f)) {
						position.setZ(newValue);
						changed = 1;
					}
					break;
				case 3:
					if (!Misc.compareFloat(tableEntry.pitch, newValue, 0.01f)) {
						tableEntry.pitch = newValue;
						changed = 2;
					}
					break;
				case 4:
					if (!Misc.compareFloat(tableEntry.yaw, newValue, 0.01f)) {
						tableEntry.yaw = newValue;
						changed = 2;
					}
					break;
				case 5:
					if (!Misc.compareFloat(tableEntry.roll, newValue, 0.01f)) {
						tableEntry.roll = newValue;
						changed = 2;
					}
					break;
				case 6:
					if (!Misc.compareFloat(tableEntry.scaleWidth, newValue, 0.01f)) {
						tableEntry.scaleWidth = newValue;
						changed = 3;
					}
					break;
				case 7:
					if (!Misc.compareFloat(tableEntry.scaleHeight, newValue, 0.01f)) {
						tableEntry.scaleHeight = newValue;
						changed = 3;
					}
					break;
			}
			if (changed != 0) {
				if (changed == 1) {
					if (!isInsideStaticNode(position)) {
						boolean result = TaskDialogs.isConfirmed(ctx.getParentWindow(), I.tr("Should the new position be saved?"),
								I.trf("The new position ({0}) of object ''{1}''\nis outside the scope of this file.\n"
										+ "The object should instead be moved to the file responsible for the area.", position,
										vegetationPS.getMeshClass(tableEntry.meshID).getName()));

						// Änderungen verwerfen
						if (!result) {
							return;
						}
					}
					tableEntry.position = position;
				}

				if (listener != null) {
					bCQuaternion quat = null;
					if (changed == 2) {
						quat = new bCQuaternion(bCEulerAngles.fromDegree(tableEntry.yaw, tableEntry.pitch, tableEntry.roll));
					}
					listener.plantChanged(tableEntry, Optional.ofNullable(quat));
				}
			}
		}

		public void setPlantTableListener(PlantTableListener listener) {
			this.listener = listener;
		}
	}

	@FunctionalInterface
	public interface PlantTableListener {
		public void plantChanged(PlantTableEntry entry, Optional<bCQuaternion> rotation);
	}

	private static class PlantTableColumnFactory extends ColumnFactory {
		@Override
		public void configureTableColumn(TableModel model, TableColumnExt column) {
			super.configureTableColumn(model, column);
			if (column.getModelIndex() <= 7) {
				column.setComparator(Comparator.naturalOrder());
			}

			if (column.getModelIndex() == 9) {
				column.setCellRenderer(new ColorCellRenderer());
			}
		}

		@Override
		public void configureColumnWidths(JXTable table, TableColumnExt columnExt) {
			switch (PLANT_COLUMN_MAPPING.inverse().get(columnExt.getTitle())) {
				case "Position X", "Position Y", "Position Z" -> columnExt.setPreferredWidth(85);
				case "Pitch", "Yaw", "Roll" -> columnExt.setPreferredWidth(60);
				case "Scale Width", "Scale Height" -> columnExt.setPreferredWidth(60);
				case "Mesh" -> columnExt.setPreferredWidth(200);
				case "Color" -> columnExt.setPreferredWidth(40);
			}
		}
	}

	private static class PlantTableRowFilter extends RowFilter<Object, Object> {
		private bCVector position;
		private float radius;
		private boolean invert;

		public PlantTableRowFilter(bCVector position, float radius, boolean invert) {
			this.position = position;
			this.radius = radius;
			this.invert = invert;
		}

		@Override
		public boolean include(RowFilter.Entry<?, ?> value) {
			Float posX = (Float) value.getValue(0);
			Float posY = (Float) value.getValue(1);
			Float posZ = (Float) value.getValue(2);

			boolean contained = position.getRelative(new bCVector(posX, posY, posZ)).length() <= radius;
			return invert ? !contained : contained;
		}

	}

	private static class PlantTableEntry {
		bCVector position;
		float pitch, yaw, roll, scaleWidth, scaleHeight;
		eSVegetationMeshID meshID;
		eCVegetation_GridNode gridNode;
		PlantRegionEntry entry;
		int colorARGB;

		public PlantTableEntry(eCVegetation_GridNode gridNode, PlantRegionEntry entry) {
			position = entry.position;
			bCEulerAngles rotation = new bCEulerAngles(entry.rotation);
			pitch = rotation.getPitchDeg();
			yaw = rotation.getYawDeg();
			roll = rotation.getRollDeg();
			scaleWidth = entry.scaleWidth;
			scaleHeight = entry.scaleHeight;
			meshID = entry.meshID;
			colorARGB = entry.colorARGB;
			this.gridNode = gridNode;
			this.entry = entry;
		}
	}

	@Desugar
	@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
	private record PlantJsonObject(String meshName, bCVector position, float pitch, float yaw, float roll, float scaleWidth,
			float scaleHeight, Color color) {
	}

	@Desugar
	@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
	private record PlantJsonObjectDb(List<PlantJsonObject> objects, Map<String, byte[]> meshes) {
	}
}

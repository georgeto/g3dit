package de.george.g3dit;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.table.TableModel;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.jdesktop.swingx.table.ColumnFactory;
import org.jdesktop.swingx.table.TableColumnExt;

import com.ezware.dialog.task.TaskDialogs;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.google.common.io.Resources;
import com.teamunify.i18n.I;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.impl.beans.BeanProperty;
import ca.odell.glazedlists.impl.beans.BeanTableFormat;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;
import de.george.g3dit.cache.Caches;
import de.george.g3dit.cache.TemplateCache.TemplateCacheEntry;
import de.george.g3dit.gui.dialogs.DisplayHtmlDialog;
import de.george.g3dit.gui.map.MapAndTableComponent;
import de.george.g3dit.gui.map.MapItem;
import de.george.g3dit.rpc.IpcUtil;
import de.george.g3dit.tab.shared.QualityPanel;
import de.george.g3dit.util.Dialogs;
import de.george.g3dit.util.FileDialogWrapper;
import de.george.g3dit.util.json.JsonUtil;
import de.george.g3utils.gui.SwingUtils;
import de.george.g3utils.gui.UndoableTextField;
import de.george.g3utils.structure.bCVector;
import de.george.lrentnode.archive.ArchiveFile;
import de.george.lrentnode.archive.ArchiveFile.ArchiveType;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.classes.G3Class;
import de.george.lrentnode.classes.gCInventory_PS;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.classes.desc.CD.gCInventorySlot;
import de.george.lrentnode.enums.G3Enums.gELockStatus;
import de.george.lrentnode.enums.G3Enums.gEUseType;
import de.george.lrentnode.iterator.ArchiveFileIterator;
import de.george.lrentnode.util.EntityUtil;
import de.george.lrentnode.util.FileUtil;
import net.miginfocom.swing.MigLayout;
import one.util.streamex.MoreCollectors;
import one.util.streamex.StreamEx;

public class ChestEditor extends JFrame {
	private EditorContext ctx;

	private boolean chestsChanged;

	private MapAndTableComponent<Chest> map;
	private JComboBox<String> cbSearchProperty;

	public ChestEditor(EditorContext ctx) {
		this.ctx = ctx;
		setTitle(I.tr("Truheneditor"));
		setIconImage(SwingUtils.getG3Icon());
		setSize(SwingUtils.getScreenWorkingWidth(), SwingUtils.getScreenWorkingHeight());
		setResizable(true);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (chestsChanged) {
					switch (Dialogs.askSaveChanges(ChestEditor.this, I.tr("Änderungen vor dem Schließen speichern?"))) {
						case Cancel:
							return;
						case Yes:
							saveAsJson();
							break;
						case No:
							break;
					}
				}
				dispose();
			}
		});
		createContentPanel();
	}

	private void setChests(Collection<Chest> chests) {
		map.setItems(chests);
		chestsChanged = false;
	}

	private void loadFromJson() {
		try {
			File file = FileDialogWrapper.openFile(I.tr("Truhen laden"), this, FileDialogWrapper.JSON_FILTER);
			if (file != null) {
				List<Chest> loaded = JsonUtil.noAutodetectMapper().readValue(file,
						TypeFactory.defaultInstance().constructCollectionLikeType(ArrayList.class, Chest.class));
				setChests(loaded);
			}
		} catch (IOException e) {
			TaskDialogs.showException(e);
		}
	}

	private void saveAsJson() {
		if (map.getItems().isEmpty()) {
			return;
		}

		try {
			File file = FileDialogWrapper.saveFile(I.tr("Truhen speichern"), this, FileDialogWrapper.JSON_FILTER);
			if (file != null) {
				JsonUtil.noAutodetectMapper().writeValue(file, map.getItems());
				chestsChanged = false;
			}
		} catch (IOException e) {
			TaskDialogs.showException(e);
		}
	}

	private void loadFromCsv() {
		try {
			File file = FileDialogWrapper.openFile(I.tr("Truhen laden"), this, FileDialogWrapper.CSV_FILTER);
			if (file == null) {
				return;
			}

			List<Chest> chests = new ArrayList<>();
			try (BufferedReader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
				CSVParser parser = CSVFormat.DEFAULT.withFirstRecordAsHeader().withNullString("").parse(reader);
				for (CSVRecord record : parser) {
					Chest chest = new Chest();
					for (String field : Chest.FIELDS) {
						String fieldName = Chest.FIELD_MAPPING.get(field);
						if (!parser.getHeaderMap().containsKey(fieldName)) {
							continue;
						}

						String value = record.get(fieldName);
						if (value == null) {
							// TODO: Warn?
							continue;
						}

						BeanProperty<Chest> accessor = Chest.FIELD_ACCESORS.get(field);
						if (accessor.getValueClass() == boolean.class) {
							boolean parsedValue = false;
							if (value.equalsIgnoreCase(I.tr("Ja"))) {
								parsedValue = true;
							} else if (!value.equalsIgnoreCase(I.tr("Nein"))) {
								throw new IllegalArgumentException(
										I.trf("'{0}' kann nicht in einen boolschen Wert konvertiert werden.", value));
							}
							accessor.set(chest, parsedValue);
						} else if (accessor.getValueClass() == int.class) {
							accessor.set(chest, Integer.valueOf(value));
						} else if (accessor.getValueClass() == bCVector.class) {
							accessor.set(chest, bCVector.fromString(value));
						} else {
							accessor.set(chest, value);
						}
					}

					if (chest.getName() == null) {
						chest.setName("");
					}

					chests.add(chest);
				}
			}
			setChests(chests);
		} catch (IOException | IllegalArgumentException e) {
			TaskDialogs.showException(e);
		}
	}

	private void saveAsCsv() {
		if (map.getItems().isEmpty()) {
			return;
		}

		try {
			File file = FileDialogWrapper.saveFile(I.tr("Truhen speichern"), this, FileDialogWrapper.CSV_FILTER);
			if (file == null) {
				return;
			}

			try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8);
					CSVPrinter printer = CSVFormat.DEFAULT.withHeader(Iterables.toArray(Chest.FIELD_NAMES, String.class)).print(writer)) {

				for (Chest chest : map.getItems()) {
					for (String field : Chest.FIELDS) {
						BeanProperty<Chest> accesor = Chest.FIELD_ACCESORS.get(field);
						if (accesor.getValueClass() == boolean.class) {
							printer.print((boolean) accesor.get(chest) ? I.tr("Ja") : I.tr("Nein"));
						} else {
							printer.print(accesor.get(chest));
						}
					}
					printer.println();
				}
			}
		} catch (IOException e) {
			TaskDialogs.showException(e);
		}
	}

	private TextFilterator<Chest> getTextFilterator() {
		return GlazedLists.textFilterator(Chest.class, Chest.FIELDS.get(cbSearchProperty.getSelectedIndex()));
	}

	private void createContentPanel() {
		cbSearchProperty = new JComboBox<>(Iterables.toArray(Chest.FIELD_NAMES, String.class));
		UndoableTextField tfSearch = SwingUtils.createUndoTF();

		TextComponentMatcherEditor<Chest> filteredChestsMatcherEditor = new TextComponentMatcherEditor<>(tfSearch, getTextFilterator());
		cbSearchProperty.addItemListener(l -> filteredChestsMatcherEditor.setFilterator(getTextFilterator()));

		map = new MapAndTableComponent<>(new ChestColumnFactory(), new ChestTableFormat(), filteredChestsMatcherEditor);
		map.setCallbackChange(this::onChange);
		map.setCallbackGoto(this::onGoto);
		map.setCallbackNavigate(this::onNavigate);

		JButton btnSaveJson = new JButton(I.tr("Truhen als .json speichern"));
		btnSaveJson.addActionListener(e -> saveAsJson());

		JButton btnLoadJson = new JButton(I.tr("Truhen aus .json laden"));
		btnLoadJson.addActionListener(e -> loadFromJson());

		JButton btnSaveCsv = new JButton(I.tr("Truhen als .csv speichern"));
		btnSaveCsv.addActionListener(e -> saveAsCsv());

		JButton btnLoadCsv = new JButton(I.tr("Truhen aus .csv laden"));
		btnLoadCsv.addActionListener(e -> loadFromCsv());

		JButton btnSetProperty = new JButton(I.tr("Wert für alle Ausgewählten setzen"));
		btnSetProperty.addActionListener(a -> {
			List<String> choices = Arrays.asList(I.tr("Name"), I.tr("Gebiet"), I.tr("Beschreibung"));
			int index = TaskDialogs.radioChoice(this, I.tr("Eigenschaft auswählen"), null, 1, choices);
			if (index == -1) {
				return;
			}

			String input = TaskDialogs.input(this, I.tr("Wert eingeben"), null, "");
			if (input == null) {
				return;
			}

			String name = choices.get(index);
			int propertyIndex = Chest.FIELD_NAMES.indexOf(name);

			for (Chest chest : new ArrayList<>(map.getItemsSelectedInList())) {
				map.setValueAt(input, chest, propertyIndex);
			}
		});

		JButton btnLoadAll = new JButton(I.tr("Alle Truhen aus Weltdaten laden"));
		btnLoadAll.addActionListener(e -> loadAll());

		JButton btnLoadFile = new JButton(I.tr("Truhen aus .lrentdat laden"));
		btnLoadFile.addActionListener(e -> loadFromLrentdat());

		JButton btnSyncFile = new JButton(I.tr("Truhen in .lrentdat übertragen"));
		btnSyncFile.addActionListener(e -> syncToLrentdat());

		JButton btnHelp = new JButton(I.tr("Hilfe"));
		btnHelp.addActionListener(a -> {
			try {
				new DisplayHtmlDialog(I.tr("Hilfe zu Truheneditor"),
						Resources.toString(Resources.getResource(ChestEditor.class, "/res/ChestEditorHelp.html"), StandardCharsets.UTF_8),
						ChestEditor.this, 700, 700, false).setVisible(true);
			} catch (IOException e) {
				TaskDialogs.showException(e);
			}
		});

		JPanel tablePanel = new JPanel(new MigLayout("fill", "[][]", "[][grow][]"));
		tablePanel.add(cbSearchProperty, "spanx, split 2");
		tablePanel.add(tfSearch, "grow, wrap");
		tablePanel.add(new JScrollPane(map.getTable()), "spanx, grow, wrap");
		tablePanel.add(map.getCbOnlySelected(), "split 2");
		tablePanel.add(btnSetProperty);
		tablePanel.add(btnHelp, "align right, wrap");
		tablePanel.add(btnLoadCsv, "spanx, split 4");
		tablePanel.add(btnSaveCsv);
		tablePanel.add(btnLoadJson);
		tablePanel.add(btnSaveJson, "wrap");
		tablePanel.add(btnLoadAll, "spanx, split 3");
		tablePanel.add(btnLoadFile);
		tablePanel.add(btnSyncFile);

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setLeftComponent(tablePanel);
		splitPane.setRightComponent(map.getMap().getViewer().getComponent());
		splitPane.getLeftComponent().setMinimumSize(new Dimension(0, 0));
		splitPane.getRightComponent().setMinimumSize(new Dimension(0, 0));
		add(splitPane, BorderLayout.CENTER);
		splitPane.setResizeWeight(0.5);
		splitPane.setDividerLocation(getWidth() / 2);
	}

	private void onChange() {
		chestsChanged = true;
	}

	private void onGoto(Chest chest) {
		IpcUtil.gotoPosition(chest.getPosition());
	}

	private void onNavigate(Chest chest) {
		// ctx.getEditor().openEntity(entityDescriptor);
	}

	private void loadAll() {
		List<Chest> loaded = new ArrayList<>();
		ArchiveFileIterator worldFilesIterator = ctx.getFileManager().worldFilesIterator();
		while (worldFilesIterator.hasNext()) {
			ArchiveFile archive = worldFilesIterator.next();
			for (eCEntity entity : archive) {
				if (EntityUtil.getUseType(entity) == gEUseType.gEUseType_Chest) {
					loaded.add(chestFromEntity(entity, worldFilesIterator.nextFile().getName()));
				}
			}
		}
		setChests(loaded);
	}

	private void loadFromLrentdat() {
		File file = FileDialogWrapper.openFile(I.tr("Truhen aus .lrentdat laden"), this, FileDialogWrapper.LRENTDAT_FILTER);
		if (file == null) {
			return;
		}

		try {
			ArchiveFile archive = FileUtil.openArchive(file, false);
			List<Chest> loaded = new ArrayList<>();
			for (eCEntity entity : archive) {
				if (EntityUtil.getUseType(entity) == gEUseType.gEUseType_Chest) {
					loaded.add(chestFromEntity(entity, file.getName()));
				}
			}
			setChests(loaded);
		} catch (IOException e1) {
			TaskDialogs.showException(e1);
		}
	}

	private void syncToLrentdat() {
		File file = FileDialogWrapper.openFile(I.tr("Truhen in .lrentdat synchronisieren"), this, FileDialogWrapper.LRENTDAT_FILTER);
		if (file == null) {
			return;
		}

		try {
			ArchiveFile archive = FileUtil.openArchive(file, false);
			Set<String> regions = map.getItems().stream().map(Chest::getRegion).filter(Objects::nonNull).collect(Collectors.toSet());
			regions.add("RootEntity");
			Map<String, eCEntity> regionMap = new HashMap<>();
			for (eCEntity entity : archive.getGraph().getChilds()) {
				if (EntityUtil.isRootLike(entity) && regions.contains(entity.getName())) {
					regionMap.put(entity.getName(), entity);
				}
			}

			for (String region : Sets.difference(regions, regionMap.keySet())) {
				eCEntity regionEntity = EntityUtil.newRootEntity(ArchiveType.Lrentdat);
				regionEntity.setName(region);
				archive.getGraph().attachChild(regionEntity);
				regionMap.put(region, regionEntity);
			}

			for (Chest chest : map.getItems()) {
				eCEntity chestEntity = archive.getEntityByGuid(chest.getGuid()).orElse(null);
				if (chestEntity == null) {
					// TODO: ???
					continue;
				}

				if (chest.getRegion() == null) {
					regionMap.get("RootEntity").attachChild(chestEntity);
					continue;
				}

				if (chestEntity.getParent() != regionMap.get(chest.getRegion())) {
					regionMap.get(chest.getRegion()).attachChild(chestEntity);
				}
			}

			List<eCEntity> emptyRoots = archive.getGraph().getChilds().stream().filter(EntityUtil::isRootLike)
					.filter(e -> e.getChilds().isEmpty()).collect(Collectors.toList());
			for (eCEntity root : emptyRoots) {
				root.removeFromParent(true);
			}

			archive.save(file);
		} catch (IOException e1) {
			TaskDialogs.showException(e1);
		}
	}

	private String lookupTemplate(String tpleGuid) {
		Optional<TemplateCacheEntry> tce = Caches.template(ctx).getEntryByGuid(tpleGuid);
		if (tce.isPresent()) {
			return tce.get().getName();
		} else {
			return "#" + tpleGuid;
		}
	}

	private Chest chestFromEntity(eCEntity entity, String file) {
		Chest chest = new Chest();
		chest.setGuid(entity.getGuid());
		chest.setName(entity.getName());
		chest.setPosition(entity.getWorldPosition().clone());
		if (entity.hasParent()) {
			String parentName = entity.getParent().getName();
			if (!parentName.contains("Root")) {
				chest.setRegion(parentName);
			}
		}

		if (entity.hasClass(CD.gCInventory_PS.class)) {
			gCInventory_PS inv = entity.getClass(CD.gCInventory_PS.class);
			chest.setTreasureSet1(inv.property(CD.gCInventory_PS.TreasureSet1).getString());
			chest.setTreasureSet2(inv.property(CD.gCInventory_PS.TreasureSet2).getString());
			chest.setTreasureSet3(inv.property(CD.gCInventory_PS.TreasureSet3).getString());
			chest.setTreasureSet4(inv.property(CD.gCInventory_PS.TreasureSet4).getString());
			chest.setTreasureSet5(inv.property(CD.gCInventory_PS.TreasureSet5).getString());

			if (!inv.stacks.isEmpty()) {
				String stacks = "";
				for (G3Class stack : inv.stacks) {
					if (!stacks.isEmpty()) {
						stacks += ", ";
					}

					stacks += lookupTemplate(stack.property(gCInventorySlot.Template).getGuid());
					int amount = stack.property(CD.gCInventoryStack.Amount).getInt();
					int quality = stack.property(CD.gCInventoryStack.Quality).getInt();
					if (amount != 1 || quality != 0) {
						stacks += ":" + amount;
					}
					if (quality != 0) {
						stacks += ":" + QualityPanel.getQualityAsString(quality);
					}
				}
				chest.setStacks(stacks);
			}
		}

		if (entity.hasClass(CD.gCLock_PS.class)) {
			G3Class lock = entity.getClass(CD.gCLock_PS.class);
			chest.setLocked(lock.property(CD.gCLock_PS.Status).getEnumValue() == gELockStatus.gELockStatus_Locked);
			chest.setDifficulty(lock.property(CD.gCLock_PS.Difficulty).getInt());
			String key = lock.property(CD.gCLock_PS.Key).getGuid();
			if (!Strings.isNullOrEmpty(key)) {
				chest.setKey(lookupTemplate(key));
			}
		}

		chest.setFile(file);

		return chest;
	}

	private static class ChestTableFormat extends BeanTableFormat<Chest> {
		public ChestTableFormat() {
			super(Chest.class, Iterables.toArray(Chest.FIELDS, String.class), Iterables.toArray(Chest.FIELD_NAMES, String.class),
					StreamEx.of(Chest.FIELDS).collect(MoreCollectors.toBooleanArray(f -> !f.equals("Guid"))));
		}

		@Override
		public Comparator<?> getColumnComparator(int column) {
			if (column == 1) {
				return Chest.REGION_COMPARATOR;
			}

			return super.getColumnComparator(column);
		}
	}

	private static class ChestColumnFactory extends ColumnFactory {
		@Override
		public void configureTableColumn(TableModel model, TableColumnExt columnExt) {
			super.configureTableColumn(model, columnExt);

			int fieldIndex = Chest.FIELD_NAMES.indexOf(columnExt.getTitle());
			if (fieldIndex != -1) {
				switch (Chest.FIELDS.get(fieldIndex)) {
					case "Name" -> columnExt.setPrototypeValue("Unique_Chest");
					case "Region" -> columnExt.setPrototypeValue("Myrtana_Outdoor");
					case "Description" -> columnExt
							.setPrototypeValue(I.tr("Ruinenfelder westlich von Mora Sul, am östlichen Rand, sehr gut versteckt"));
				}
			}

			if (columnExt.getModelIndex() >= 3) {
				columnExt.setVisible(false);
			}
		}
	}

	public static class Chest implements MapItem, Comparable<Chest> {
		public static final Comparator<String> REGION_COMPARATOR = Ordering.natural().nullsLast();

		public static final ImmutableMap<String, String> FIELD_MAPPING = ImmutableMap.<String, String>builder().put("Name", I.tr("Name"))
				.put("Region", I.tr("Gebiet")).put("Description", I.tr("Beschreibung")).put("Guid", I.tr("Guid"))
				.put("Position", I.tr("Position")).put("TreasureSet1", I.tr("TS1")).put("TreasureSet2", I.tr("TS2"))
				.put("TreasureSet3", I.tr("TS3")).put("TreasureSet4", I.tr("TS4")).put("TreasureSet5", I.tr("TS5"))
				.put("Locked", I.tr("Verschlossen")).put("Difficulty", I.tr("Schwierigkeit")).put("Key", I.tr("Schlüssel"))
				.put("Kind", I.tr("Truhenart")).put("Deleted", I.tr("Gelöscht")).put("Moved", I.tr("Verschoben"))
				.put("Stacks", I.tr("Fester Inhalt")).put("File", I.tr("Datei")).put("OldDescription", I.tr("Alte Beschreibung")).build();

		public static final ImmutableList<String> FIELDS = FIELD_MAPPING.keySet().asList();

		public static final ImmutableList<String> FIELD_NAMES = FIELD_MAPPING.values().asList();

		public static final ImmutableMap<String, BeanProperty<Chest>> FIELD_ACCESORS = Chest.FIELDS.stream()
				.collect(ImmutableMap.toImmutableMap(Function.identity(), f -> new BeanProperty<>(Chest.class, f, true, true)));

		@JsonProperty
		private String guid;
		@JsonProperty
		private String region;
		@JsonProperty
		private String name;
		@JsonProperty
		private bCVector position;
		@JsonProperty
		private String description;
		@JsonProperty
		private String treasureSet1;
		@JsonProperty
		private String treasureSet2;
		@JsonProperty
		private String treasureSet3;
		@JsonProperty
		private String treasureSet4;
		@JsonProperty
		private String treasureSet5;
		@JsonProperty
		private boolean locked;
		@JsonProperty
		private int difficulty;
		@JsonProperty
		private String key;
		@JsonProperty
		private String kind;
		@JsonProperty
		private boolean deleted;
		@JsonProperty
		private boolean moved;
		@JsonProperty
		private String stacks;
		@JsonProperty
		private String file;
		@JsonProperty
		private String oldDescription;

		public String getGuid() {
			return guid;
		}

		public void setGuid(String guid) {
			this.guid = guid;
		}

		public String getRegion() {
			return region;
		}

		public void setRegion(String region) {
			this.region = region;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		@Override
		public bCVector getPosition() {
			return position;
		}

		public void setPosition(bCVector position) {
			this.position = position;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public String getTreasureSet1() {
			return treasureSet1;
		}

		public void setTreasureSet1(String treasureSet1) {
			this.treasureSet1 = treasureSet1;
		}

		public String getTreasureSet2() {
			return treasureSet2;
		}

		public void setTreasureSet2(String treasureSet2) {
			this.treasureSet2 = treasureSet2;
		}

		public String getTreasureSet3() {
			return treasureSet3;
		}

		public void setTreasureSet3(String treasureSet3) {
			this.treasureSet3 = treasureSet3;
		}

		public String getTreasureSet4() {
			return treasureSet4;
		}

		public void setTreasureSet4(String treasureSet4) {
			this.treasureSet4 = treasureSet4;
		}

		public String getTreasureSet5() {
			return treasureSet5;
		}

		public void setTreasureSet5(String treasureSet5) {
			this.treasureSet5 = treasureSet5;
		}

		public boolean isLocked() {
			return locked;
		}

		public void setLocked(boolean locked) {
			this.locked = locked;
		}

		public int getDifficulty() {
			return difficulty;
		}

		public void setDifficulty(int difficulty) {
			this.difficulty = difficulty;
		}

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}

		public String getKind() {
			return kind;
		}

		public void setKind(String kind) {
			this.kind = kind;
		}

		public boolean isDeleted() {
			return deleted;
		}

		public void setDeleted(boolean deleted) {
			this.deleted = deleted;
		}

		public boolean isMoved() {
			return moved;
		}

		public void setMoved(boolean moved) {
			this.moved = moved;
		}

		public String getStacks() {
			return stacks;
		}

		public void setStacks(String stacks) {
			this.stacks = stacks;
		}

		public String getOldDescription() {
			return oldDescription;
		}

		public void setOldDescription(String oldDescription) {
			this.oldDescription = oldDescription;
		}

		public String getFile() {
			return file;
		}

		public void setFile(String file) {
			this.file = file;
		}

		@Override
		public String getTitle() {
			if (!Strings.isNullOrEmpty(region)) {
				return "[" + region + "] " + name;
			}
			return name;
		}

		@Override
		public int compareTo(Chest o) {
			int result = Objects.compare(region, o.region, REGION_COMPARATOR);
			if (result == 0 && name != null) {
				result = name.compareTo(o.name);
			}
			return result;
		}
	}
}

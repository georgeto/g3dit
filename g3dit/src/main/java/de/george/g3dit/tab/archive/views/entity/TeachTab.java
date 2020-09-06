package de.george.g3dit.tab.archive.views.entity;

import java.util.Optional;
import java.util.stream.Collectors;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.table.ColumnFactory;
import org.jdesktop.swingx.table.TableColumnExt;

import de.george.g3dit.cache.Caches;
import de.george.g3dit.cache.TemplateCache.TemplateCacheEntry;
import de.george.g3dit.gui.components.TableModificationControl;
import de.george.g3dit.gui.dialogs.TemplateIntelliHints;
import de.george.g3dit.gui.table.TableUtil;
import de.george.g3dit.tab.archive.EditorArchiveTab;
import de.george.g3utils.gui.ListTableModel;
import de.george.g3utils.gui.SwingUtils;
import de.george.g3utils.gui.UndoableTextField;
import de.george.g3utils.structure.GuidUtil;
import de.george.g3utils.util.Misc;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.properties.bTObjArray_bCString;
import de.george.lrentnode.properties.bTObjArray_eCEntityProxy;
import net.miginfocom.swing.MigLayout;

public class TeachTab extends AbstractEntityTab {
	private static final String[] ATTRIBUTES = {"STR", "DEX", "INT", "SMT", "THF", "ALC", "HP", "SP", "MP"};

	private JXTable skillTable, attributeTable;
	private SkillTableModel skillModel;
	private AttributeTableModel attributeModel;

	public TeachTab(EditorArchiveTab ctx) {
		super(ctx);
	}

	@Override
	public void initComponents() {
		setLayout(new MigLayout("fill, insets 0", "[]", "[][][]"));

		attributeTable = new JXTable();
		TableUtil.disableSearch(attributeTable);
		attributeTable.setColumnFactory(new AttributeTableColumFactory());
		attributeTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		attributeModel = new AttributeTableModel();
		attributeTable.setModel(attributeModel);

		add(SwingUtils.createBoldLabel("Attributes"), "wrap");
		add(new JScrollPane(attributeTable), "grow, push, wrap");
		add(new TableModificationControl<>(ctx, attributeTable, attributeModel, () -> ""), "grow, wrap");

		skillTable = new JXTable();
		TableUtil.disableSearch(skillTable);
		skillTable.setColumnFactory(new SkillTableColumFactory());
		skillTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		skillModel = new SkillTableModel();
		skillTable.setModel(skillModel);

		add(SwingUtils.createBoldLabel("Skills"), "gaptop u, wrap");
		add(new JScrollPane(skillTable), "grow, push, wrap");
		add(new TableModificationControl<>(ctx, skillTable, skillModel, () -> new SkillEntry("", "")), "grow");
	}

	@Override
	public String getTabTitle() {
		return "Teach";
	}

	@Override
	public void loadValues(eCEntity entity) {
		bTObjArray_bCString teachAttributes = entity.getClass(CD.gCNPC_PS.class).property(CD.gCNPC_PS.TeachAttribs);
		attributeModel.clearEntries();
		teachAttributes.getEntries().forEach(s -> attributeModel.addEntry(s.getString()));

		bTObjArray_eCEntityProxy teachSkills = entity.getClass(CD.gCNPC_PS.class).property(CD.gCNPC_PS.TeachSkills);
		skillModel.clearEntries();
		teachSkills.getEntries().forEach(g -> skillModel.addEntry(new SkillEntry(g.getGuid(), "")));
	}

	@Override
	public void saveValues(eCEntity entity) {
		if (attributeTable.isEditing()) {
			attributeTable.getCellEditor().stopCellEditing();
		}

		bTObjArray_bCString teachAttributes = entity.getClass(CD.gCNPC_PS.class).property(CD.gCNPC_PS.TeachAttribs);
		teachAttributes.setNativeEntries(attributeModel.getEntries().stream().filter(s -> !s.isEmpty()).collect(Collectors.toList()));

		if (skillTable.isEditing()) {
			skillTable.getCellEditor().stopCellEditing();
		}

		bTObjArray_eCEntityProxy teachSkills = entity.getClass(CD.gCNPC_PS.class).property(CD.gCNPC_PS.TeachSkills);
		teachSkills.setNativeEntries(skillModel.getEntries().stream().filter(e -> GuidUtil.isValid(e.getRefId()))
				.map(e -> GuidUtil.parseGuid(e.getRefId())).collect(Collectors.toList()));
	}

	@Override
	public boolean isActive(eCEntity entity) {
		return entity.hasClass(CD.gCNPC_PS.class);
	}

	private class AttributeTableModel extends ListTableModel<String> {
		public AttributeTableModel() {
			super("Attribute");
		}

		@Override
		public Object getValueAt(String entry, int col) {
			return entry;
		}

		@Override
		public void setValueAt(Object value, int row, int col) {
			getEntries().set(row, (String) value);
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return true;
		}
	}

	private class AttributeTableColumFactory extends ColumnFactory {
		@Override
		public void configureColumnWidths(JXTable table, TableColumnExt columnExt) {
			columnExt.setEditable(true);
			switch (columnExt.getTitle()) {
				case "Attribute":
					columnExt.setPreferredWidth(150);
					JComboBox<String> cbAttributes = new JComboBox<>(Misc.concat(ATTRIBUTES));
					columnExt.setCellEditor(new DefaultCellEditor(cbAttributes));
					break;
				default:
					super.configureColumnWidths(table, columnExt);
			}
		}
	}

	private class SkillTableModel extends ListTableModel<SkillEntry> {
		public SkillTableModel() {
			super("Reference ID", "Name");
		}

		@Override
		public void addEntry(SkillEntry entry) {
			completeName(entry);
			super.addEntry(entry);
		}

		@Override
		public Object getValueAt(SkillEntry entry, int col) {
			switch (col) {
				case 0:
					return entry.getRefId();
				case 1:
					return entry.getName();
			}
			return null;
		}

		@Override
		public void setValueAt(Object value, int row, int col) {
			SkillEntry entry = getEntries().get(row);
			switch (col) {
				case 0:
					if (!entry.getRefId().equals(value)) {
						ctx.fileChanged();
					}
					entry.setRefId((String) value);
					completeName(entry);
					fireTableRowsUpdated(row, row);
					break;
				case 1:
					entry.setName((String) value);
					completeGuid(entry);
					fireTableRowsUpdated(row, row);
					break;
			}
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return true;
		}

		private void completeName(SkillEntry entry) {
			Optional<TemplateCacheEntry> cacheEntry = Caches.template(ctx).getEntryByGuid(GuidUtil.parseGuid(entry.getRefId()));
			if (cacheEntry.isPresent()) {
				entry.setRefId(cacheEntry.get().getGuid());
				entry.setName(cacheEntry.get().getName());
			} else {
				entry.setName("");
			}
		}

		private void completeGuid(SkillEntry entry) {
			Optional<TemplateCacheEntry> cacheEntry = Caches.template(ctx).getEntryByName(entry.getName());
			if (cacheEntry.isPresent()) {
				if (!entry.getRefId().equals(cacheEntry.get().getGuid())) {
					ctx.fileChanged();
				}
				entry.setRefId(cacheEntry.get().getGuid());
			} else {
				entry.setName("");
			}
		}
	}

	private class SkillTableColumFactory extends ColumnFactory {

		@Override
		public void configureColumnWidths(JXTable table, TableColumnExt columnExt) {
			columnExt.setEditable(true);
			switch (columnExt.getTitle()) {
				case "Reference ID":
					columnExt.setPreferredWidth(250);
					break;
				case "Name":
					columnExt.setPreferredWidth(150);
					UndoableTextField tf = SwingUtils.createUndoTF();
					new TemplateIntelliHints(tf, Caches.template(ctx),
							e -> e.getClasses().contains("gCSkill_PS") || e.getClasses().contains("gCMagic_PS"), false);
					columnExt.setCellEditor(new DefaultCellEditor(tf));
					break;
				default:
					super.configureColumnWidths(table, columnExt);
			}
		}
	}

	private class SkillEntry {
		private String refId;
		private String name;

		public SkillEntry(String refId, String name) {
			this.refId = refId;
			this.name = name;
		}

		public String getRefId() {
			return refId;
		}

		public void setRefId(String refId) {
			this.refId = refId;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

	}
}

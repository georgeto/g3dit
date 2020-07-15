package de.george.g3dit.gui.dialogs;

import java.util.List;
import java.util.Optional;

import javax.swing.DefaultCellEditor;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.table.ColumnFactory;
import org.jdesktop.swingx.table.TableColumnExt;

import de.george.g3dit.cache.Caches;
import de.george.g3dit.cache.TemplateCache.TemplateCacheEntry;
import de.george.g3dit.gui.components.TableModificationControl;
import de.george.g3dit.gui.table.TableUtil;
import de.george.g3dit.tab.EditorAbstractFileTab;
import de.george.g3dit.tab.shared.QualityCellEditor;
import de.george.g3dit.tab.shared.QualityCellRenderer;
import de.george.g3utils.gui.ListTableModel;
import de.george.g3utils.gui.SwingUtils;
import de.george.g3utils.gui.UndoableTextField;
import de.george.g3utils.structure.GuidUtil;
import net.miginfocom.swing.MigLayout;

public class ItemPanel extends JPanel {
	private JXTable table;
	private ItemTableModel model;

	private EditorAbstractFileTab ctx;

	public ItemPanel(EditorAbstractFileTab ctx) {
		this.ctx = ctx;
		setLayout(new MigLayout("fillx, insets 0", "[]", "[grow][]"));

		table = new JXTable();
		TableUtil.disableSearch(table);
		table.setColumnFactory(new ItemTableColumFactory());
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		model = new ItemTableModel();
		table.setModel(model);

		JScrollPane scroll = new JScrollPane(table);
		add(scroll, "width 100%, growy, wrap");

		add(new TableModificationControl<>(ctx, table, model, () -> new InventoryStack("", "", 1, 0)), "width 100%");
	}

	public void clearItems() {
		model.clearEntries();
	}

	public void addItem(InventoryStack stack) {
		model.addEntry(stack);
	}

	public List<InventoryStack> getItems() {
		if (table.isEditing()) {
			table.getCellEditor().stopCellEditing();
		}

		return model.getEntries();
	}

	private class ItemTableModel extends ListTableModel<InventoryStack> {
		public ItemTableModel() {
			super("Reference ID", "Name", "Amount", "Quality");
		}

		@Override
		public void addEntry(InventoryStack entry) {
			completeName(entry);
			super.addEntry(entry);
		}

		@Override
		public Object getValueAt(InventoryStack entry, int col) {
			switch (col) {
				case 0:
					return entry.getRefId();
				case 1:
					return entry.getName();
				case 2:
					return entry.getAmount();
				case 3:
					return entry.getQuality();
			}
			return null;
		}

		@Override
		public void setValueAt(Object value, int row, int col) {
			InventoryStack entry = getEntries().get(row);
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
				case 2:
					try {
						int newAmount = Integer.valueOf(String.valueOf(value));
						if (entry.getAmount() != newAmount) {
							ctx.fileChanged();
						}
						entry.setAmount(newAmount);
					} catch (NumberFormatException e) {
						// Nothing
					}
					break;
				case 3:
					if (entry.getQuality() != (int) value) {
						ctx.fileChanged();
					}
					entry.setQuality((int) value);
					break;
			}
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return true;
		}

		private void completeName(InventoryStack entry) {
			Optional<TemplateCacheEntry> cacheEntry = Caches.template(ctx).getEntryByGuid(GuidUtil.parseGuid(entry.getRefId()));
			if (cacheEntry.isPresent()) {
				entry.setRefId(cacheEntry.get().getGuid());
				entry.setName(cacheEntry.get().getName());
			} else {
				entry.setName("");
			}
		}

		private void completeGuid(InventoryStack entry) {
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

	private class ItemTableColumFactory extends ColumnFactory {

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
					new TemplateIntelliHints(tf, Caches.template(ctx));
					columnExt.setCellEditor(new DefaultCellEditor(tf));
					break;
				case "Amount":
					columnExt.setPreferredWidth(50);
					break;
				case "Quality":
					columnExt.setPreferredWidth(100);
					columnExt.setCellRenderer(new QualityCellRenderer());
					columnExt.setCellEditor(new QualityCellEditor());
					break;
				default:
					super.configureColumnWidths(table, columnExt);
			}
		}
	}

	public static class InventoryStack {
		private String refId;
		private String name;
		private int amount;
		private int quality;

		public InventoryStack(String refId, String name, int amount, int quality) {
			this.refId = refId;
			this.name = name;
			this.amount = amount;
			this.quality = quality;
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

		public int getAmount() {
			return amount;
		}

		public void setAmount(int amount) {
			this.amount = amount;
		}

		public int getQuality() {
			return quality;
		}

		public void setQuality(int quality) {
			this.quality = quality;
		}
	}
}

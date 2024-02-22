package de.george.g3dit.gui.edit.handler;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import ca.odell.glazedlists.GlazedLists;
import de.george.g3dit.EditorContext;
import de.george.g3dit.gui.edit.PropertyPanelDef;
import de.george.g3dit.gui.table.TableUtil;
import de.george.g3dit.gui.table.TableUtil.SortableEventTable;
import de.george.g3dit.util.FileChangeMonitor;
import de.george.g3utils.io.G3Serializable;
import de.george.lrentnode.properties.PropertyInstantiator;
import de.george.lrentnode.properties.bTArray;
import de.george.lrentnode.util.PropertyUtil;

public class GenericArrayPropertyHandler extends TitledPropertyHandler<bTArray<G3Serializable>> {
	private final EditorContext ctx;
	private final Class<G3Serializable> entryType;

	private SortableEventTable<G3Serializable> table;

	public GenericArrayPropertyHandler(PropertyPanelDef def, EditorContext ctx) {
		super(def);
		this.ctx = ctx;
		entryType = createEmptyArray().getEntryType();
	}

	@Override
	protected void addValueComponent(JPanel content) {
		table = TableUtil.createTable(GlazedLists.eventList(null), entryType, def.getTableCoumns());
		if (def.getTableCoumns().length < 2) {
			table.table.setTableHeader(null);
		}
		content.add(new JScrollPane(table.table), "grow, sgx table, wrap");
		FileChangeMonitor changeMonitor = ctx instanceof FileChangeMonitor ? (FileChangeMonitor) ctx : null;
		content.add(table.createModificationControl(changeMonitor, this::createEntry), "growx, sgx table");
	}

	@Override
	protected void load(bTArray<G3Serializable> value) {
		table.setEntries(value.getEntries(PropertyUtil::clone));
	}

	@Override
	protected bTArray<G3Serializable> save() {
		TableUtil.stopEditing(table.table);

		bTArray<G3Serializable> result = createEmptyArray();
		result.setEntries(table.getEntries(), PropertyUtil::clone);
		return result;
	}

	@SuppressWarnings("unchecked")
	private bTArray<G3Serializable> createEmptyArray() {
		return (bTArray<G3Serializable>) def.getAdapter().getDefaultValue();
	}

	private G3Serializable createEntry() {
		return PropertyInstantiator.getPropertyDefaultValue(def.getName(), entryType).get();
	}
}

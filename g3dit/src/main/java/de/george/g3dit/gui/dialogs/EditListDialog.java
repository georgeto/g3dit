package de.george.g3dit.gui.dialogs;

import java.awt.Window;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.JComponent;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import de.george.g3dit.gui.table.TableColumnDef;

public class EditListDialog<T> extends AbstractEditListDialog<T> {
	private EventList<T> entries;
	private Class<T> itemType;
	private Supplier<T> inputNewEntry;
	private Consumer<List<T>> onSave;
	private TableColumnDef[] tableColumns;

	public EditListDialog(Window parent, String title, boolean modal, Collection<T> items, Class<T> itemType, Supplier<T> inputNewEntry,
			Consumer<List<T>> onSave, TableColumnDef... tableColumns) {
		super(parent, title, modal);

		this.entries = GlazedLists.eventList(items);
		this.itemType = itemType;
		this.inputNewEntry = inputNewEntry;
		this.onSave = onSave;
		this.tableColumns = tableColumns;
	}

	@Override
	protected void save() {
		onSave.accept(entries);
		super.save();
	}

	@Override
	public JComponent createContentPanel() {
		return createContentPanel(entries, itemType, inputNewEntry, tableColumns);
	}
}

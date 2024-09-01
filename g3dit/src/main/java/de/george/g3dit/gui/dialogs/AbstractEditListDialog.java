package de.george.g3dit.gui.dialogs;

import java.awt.Window;
import java.util.function.Supplier;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.jidesoft.dialog.ButtonPanel;
import com.teamunify.i18n.I;

import ca.odell.glazedlists.EventList;
import de.george.g3dit.EditorContext;
import de.george.g3dit.gui.table.TableColumnDef;
import de.george.g3dit.gui.table.TableUtil;
import de.george.g3dit.gui.table.TableUtil.SortableEventTable;
import de.george.g3utils.gui.SwingUtils;
import net.miginfocom.swing.MigLayout;

public abstract class AbstractEditListDialog<T> extends ExtStandardDialog {
	private final EditorContext ctx;

	public AbstractEditListDialog(EditorContext ctx, Window parent, String title, boolean modal) {
		super(parent, title, modal);
		this.ctx = ctx;
		setSize(1000, 500);
		setDefaultCancelAction(SwingUtils.createAction(this::cancel));
	}

	protected JComponent createContentPanel(EventList<T> entries, Class<T> itemType, Supplier<T> inputNewEntry,
			TableColumnDef... tableColumns) {
		JPanel panel = new JPanel(new MigLayout("insets 10, fill"));

		SortableEventTable<T> sortableTable = TableUtil.createSortableTable(ctx, entries, itemType, tableColumns);

		panel.add(new JScrollPane(sortableTable.table), "width 100%, growy, push, wrap");

		panel.add(sortableTable.createModificationControl(null, () -> {
			if (inputNewEntry == null)
				return null;

			T newEntry = inputNewEntry.get();
			if (newEntry != null) {
				int index = entries.indexOf(newEntry);
				if (index != -1) {
					sortableTable.table.setRowSelectionInterval(index, index);
					return null;
				}
			}
			return newEntry;
		}), "width 100%,");

		return panel;
	}

	@Override
	public ButtonPanel createButtonPanel() {
		ButtonPanel buttonPanel = newButtonPanel();

		addButton(buttonPanel, SwingUtils.createAction(I.tr("Save"), this::save), ButtonPanel.AFFIRMATIVE_BUTTON);
		addButton(buttonPanel, SwingUtils.createAction(I.tr("Cancel"), this::cancel), ButtonPanel.CANCEL_BUTTON);

		return buttonPanel;
	}

	protected void save() {
		affirm();
	}

	@Override
	public void dispose() {
		super.dispose();
	}
}

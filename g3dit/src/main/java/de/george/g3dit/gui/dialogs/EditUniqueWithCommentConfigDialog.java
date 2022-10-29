package de.george.g3dit.gui.dialogs;

import java.awt.Window;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.ezware.dialog.task.TaskDialogs;
import com.google.common.collect.ImmutableSet;
import com.jidesoft.dialog.ButtonPanel;
import com.teamunify.i18n.I;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import de.george.g3dit.config.JsonSetConfigFile;
import de.george.g3dit.gui.components.TableModificationControl;
import de.george.g3dit.gui.table.TableColumnDef;
import de.george.g3dit.gui.table.TableUtil;
import de.george.g3dit.gui.table.TableUtil.SortableEventTable;
import de.george.g3utils.gui.SwingUtils;
import net.miginfocom.swing.MigLayout;

// TODO: Merge with guid dialog?
public abstract class EditUniqueWithCommentConfigDialog<T> extends ExtStandardDialog {
	private JsonSetConfigFile<T> config;
	private EventList<T> entries;

	public EditUniqueWithCommentConfigDialog(Window parent, String title, JsonSetConfigFile<T> config) {
		super(parent, title, false);
		// Acquire config edit lock
		if (!config.acquireEditLock()) {
			TaskDialogs.error(parent, title, I.tr("Already opened for editing."));
			cancel();
		} else {
			this.config = config;
			setSize(1000, 500);
			setDefaultCancelAction(SwingUtils.createAction(this::cancel));
		}
	}

	private static final TableColumnDef COLUMN_COMMENT = TableColumnDef.withName("Comment").displayName(I.tr("Comment"))
			.sizeExample("[BASE] Closest entity: G3_Desert_Objects_House_Ruins_B_04_LOWPOLY.xcmsh (460)").editable(true).b();

	@Override
	public JComponent createContentPanel() {
		JPanel panel = new JPanel(new MigLayout("insets 10, fill"));

		entries = GlazedLists.eventList(config.getClonedContent());

		SortableEventTable<T> sortableTable = TableUtil.createSortableTable(entries, config.type(), getColumnDef(), COLUMN_COMMENT);

		panel.add(new JScrollPane(sortableTable.table), "width 100%, growy, push, wrap");

		panel.add(new TableModificationControl<>(null, sortableTable.table, sortableTable.sortedSource, () -> {
			T newEntry = inputNewEntry();
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

	private void save() {
		config.updateContent(ImmutableSet.copyOf(entries));
		affirm();
	}

	protected abstract TableColumnDef getColumnDef();

	protected abstract T inputNewEntry();

	@Override
	public void dispose() {
		super.dispose();

		// Free config edit lock
		if (config != null) {
			config.releaseEditLock();
		}
	}
}

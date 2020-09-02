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
import de.george.g3dit.config.GuidWithCommentConfigFile;
import de.george.g3dit.gui.components.TableModificationControl;
import de.george.g3dit.gui.table.TableColumnDef;
import de.george.g3dit.gui.table.TableUtil;
import de.george.g3dit.gui.table.TableUtil.SortableEventTable;
import de.george.g3dit.util.GuidWithComment;
import de.george.g3utils.gui.SwingUtils;
import de.george.g3utils.structure.GuidUtil;
import net.miginfocom.swing.MigLayout;

public class EditGuidWithCommentConfigDialog extends ExtStandardDialog {
	private GuidWithCommentConfigFile config;
	private EventList<GuidWithComment> entries;

	public EditGuidWithCommentConfigDialog(Window parent, String title, GuidWithCommentConfigFile config) {
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

	private static final TableColumnDef COLUMN_GUID = TableColumnDef.withName("Guid").displayName(I.tr("Guid")).editable(false)
			.sizeExample(GuidUtil.randomGUID()).b();

	private static final TableColumnDef COLUMN_COMMENT = TableColumnDef.withName("Comment").displayName(I.tr("Comment"))
			.sizeExample("[BASE] Closest entity: G3_Desert_Objects_House_Ruins_B_04_LOWPOLY.xcmsh (460)").editable(true).b();

	@Override
	public JComponent createContentPanel() {
		JPanel panel = new JPanel(new MigLayout("insets 10, fill"));

		entries = GlazedLists.eventList(config.getClonedContent());

		SortableEventTable<GuidWithComment> sortableTable = TableUtil.createSortableTable(entries, GuidWithComment.class, COLUMN_GUID,
				COLUMN_COMMENT);

		panel.add(new JScrollPane(sortableTable.table), "width 100%, growy, push, wrap");

		panel.add(new TableModificationControl<>(null, sortableTable.table, sortableTable.sortedSource, () -> {
			EnterGuidDialog dialog = new EnterGuidDialog(getOwner(), I.tr("Enter Guid"), I.tr("Ok"), "");
			if (dialog.openAndWasSuccessful()) {
				GuidWithComment newEntry = new GuidWithComment(GuidUtil.parseGuid(dialog.getEnteredGuid()), "");
				int index = entries.indexOf(newEntry);
				if (index == -1) {
					return newEntry;
				} else {
					sortableTable.table.setRowSelectionInterval(index, index);
					return null;
				}
			} else {
				return null;
			}
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

	@Override
	public void dispose() {
		super.dispose();

		// Free config edit lock
		if (config != null) {
			config.releaseEditLock();
		}

	}
}

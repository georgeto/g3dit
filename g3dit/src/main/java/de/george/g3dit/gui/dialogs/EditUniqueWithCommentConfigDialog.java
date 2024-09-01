package de.george.g3dit.gui.dialogs;

import java.awt.Window;

import javax.swing.JComponent;

import com.ezware.dialog.task.TaskDialogs;
import com.google.common.collect.ImmutableSet;
import com.teamunify.i18n.I;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import de.george.g3dit.EditorContext;
import de.george.g3dit.config.JsonSetConfigFile;
import de.george.g3dit.gui.table.TableColumnDef;

// TODO: Merge with guid dialog?
public abstract class EditUniqueWithCommentConfigDialog<T> extends AbstractEditListDialog<T> {
	private JsonSetConfigFile<T> config;
	private EventList<T> entries;

	public EditUniqueWithCommentConfigDialog(EditorContext ctx, Window parent, String title, JsonSetConfigFile<T> config) {
		super(ctx, parent, title, false);
		// Acquire config edit lock
		if (!config.acquireEditLock()) {
			TaskDialogs.error(parent, title, I.tr("Already opened for editing."));
			cancel();
		} else {
			this.config = config;
		}
	}

	private static final TableColumnDef COLUMN_COMMENT = TableColumnDef.withName("Comment").displayName(I.tr("Comment"))
			.sizeExample("[BASE] Closest entity: G3_Desert_Objects_House_Ruins_B_04_LOWPOLY.xcmsh (460)").editable(true).b();

	@Override
	public JComponent createContentPanel() {
		entries = GlazedLists.eventList(config.getClonedContent());
		return createContentPanel(entries, config.type(), this::inputNewEntry, getColumnDef(), COLUMN_COMMENT);
	}

	@Override
	protected void save() {
		config.updateContent(ImmutableSet.copyOf(entries));
		super.save();
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

package de.george.g3dit.gui.dialogs;

import java.awt.Window;

import com.ezware.dialog.task.TaskDialogs;
import com.teamunify.i18n.I;

import de.george.g3dit.config.StringWithCommentConfigFile;
import de.george.g3dit.gui.table.TableColumnDef;
import de.george.g3dit.util.StringWithComment;

public class EditStringWithCommentConfigDialog extends EditUniqueWithCommentConfigDialog<StringWithComment> {
	public EditStringWithCommentConfigDialog(Window parent, String title, StringWithCommentConfigFile config) {
		super(parent, title, config);
	}

	private static final TableColumnDef COLUMN_VALUE = TableColumnDef.withName("Value").displayName(I.tr("Value")).editable(false)
			.sizeExample("G3_Varant_01_MoraSul_Temple_Lights_01.lrentdat").b();

	@Override
	protected TableColumnDef getColumnDef() {
		return COLUMN_VALUE;
	}

	@Override
	protected StringWithComment inputNewEntry() {
		String fileName = TaskDialogs.input(getOwner(), I.tr("Enter file name"), null, "");
		if (fileName != null)
			return new StringWithComment(fileName, "");
		return null;
	}
}

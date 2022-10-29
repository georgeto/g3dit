package de.george.g3dit.gui.dialogs;

import java.awt.Window;

import com.teamunify.i18n.I;

import de.george.g3dit.config.GuidWithCommentConfigFile;
import de.george.g3dit.gui.table.TableColumnDef;
import de.george.g3dit.util.GuidWithComment;
import de.george.g3utils.structure.GuidUtil;

public class EditGuidWithCommentConfigDialog extends EditUniqueWithCommentConfigDialog<GuidWithComment> {
	public EditGuidWithCommentConfigDialog(Window parent, String title, GuidWithCommentConfigFile config) {
		super(parent, title, config);
	}

	private static final TableColumnDef COLUMN_GUID = TableColumnDef.withName("Guid").displayName(I.tr("Guid")).editable(false)
			.sizeExample(GuidUtil.randomGUID()).b();

	@Override
	protected TableColumnDef getColumnDef() {
		return COLUMN_GUID;
	}

	@Override
	protected GuidWithComment inputNewEntry() {
		EnterGuidDialog dialog = new EnterGuidDialog(getOwner(), I.tr("Enter Guid"), I.tr("Ok"), "");
		if (dialog.openAndWasSuccessful())
			return new GuidWithComment(GuidUtil.parseGuid(dialog.getEnteredGuid()), "");
		return null;
	}
}

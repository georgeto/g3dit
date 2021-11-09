package de.george.g3dit.tab.archive.views.entity.dialogs;

import com.teamunify.i18n.I;

import de.george.g3dit.EditorContext;
import de.george.g3dit.gui.dialogs.TemplateNameSearchDialog;

public class SlotDialog extends TemplateNameSearchDialog {
	public SlotDialog(TemplateSearchListener pCallback, EditorContext ctx) {
		super(pCallback, ctx);

		setTitle(I.tr("Load slot from template"));
	}

	@Override
	protected void callListener() {
		if (tpleFile != null) {
			if (callback.templateSearchCallback(tpleFile)) {
				dispose();
			} else {
				lblStatus.setText(I.tr("Template is not suitable for this slot."));
			}
		}
	}

}

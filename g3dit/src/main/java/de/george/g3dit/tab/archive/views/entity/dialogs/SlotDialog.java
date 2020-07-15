package de.george.g3dit.tab.archive.views.entity.dialogs;

import de.george.g3dit.EditorContext;
import de.george.g3dit.gui.dialogs.TemplateNameSearchDialog;

public class SlotDialog extends TemplateNameSearchDialog {
	public SlotDialog(TemplateSearchListener pCallback, EditorContext ctx) {
		super(pCallback, ctx);

		setTitle("Slot aus Template laden");
	}

	@Override
	protected void callListener() {
		if (tpleFile != null) {
			if (callback.templateSearchCallback(tpleFile)) {
				dispose();
			} else {
				lblStatus.setText("Template passt nicht zu diesem Slot.");
			}
		}
	}

}

package de.george.g3dit.gui.dialogs;

import java.awt.Window;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import com.ezware.dialog.task.TaskDialogs;
import com.jidesoft.dialog.ButtonPanel;

import de.george.g3dit.util.Icons;
import de.george.g3utils.gui.JGuidField;
import de.george.g3utils.gui.SwingUtils;
import de.george.g3utils.structure.GuidUtil;
import net.miginfocom.swing.MigLayout;

public class EnterGuidDialog extends ExtStandardDialog {
	private String actionTitle;
	private JGuidField gfGuid;

	private String enteredGuid;
	private String defaultGuid;

	public EnterGuidDialog(Window owner, String title, String actionTitle, String defaultGuid) {
		super(owner, title, true);
		this.actionTitle = actionTitle;
		this.defaultGuid = defaultGuid;

		setSize(350, 130);
	}

	@Override
	public JComponent createContentPanel() {
		JPanel panel = new JPanel(new MigLayout("insets 10, fill", "[grow]10 push[]"));

		gfGuid = new JGuidField();
		gfGuid.setText(defaultGuid);
		panel.add(gfGuid, "grow");

		JButton btnRandomGuid = new JButton(Icons.getImageIcon(Icons.Data.COUNTER));
		btnRandomGuid.setToolTipText("Zufällige Guid generieren");
		btnRandomGuid.addActionListener(a -> gfGuid.setText(GuidUtil.randomGUID(), true));
		panel.add(btnRandomGuid, "width 27!, height 27!");

		return panel;
	}

	@Override
	public ButtonPanel createButtonPanel() {
		ButtonPanel buttonPanel = newButtonPanel();

		Action action = SwingUtils.createAction(actionTitle, () -> {
			String guid = GuidUtil.parseGuid(gfGuid.getText());
			if (guid == null) {
				TaskDialogs.error(EnterGuidDialog.this, "", "Guid ist ungültig.");
				return;
			}

			enteredGuid = guid;
			affirm();
		});

		addButton(buttonPanel, action, ButtonPanel.AFFIRMATIVE_BUTTON);
		addDefaultCancelButton(buttonPanel);

		return buttonPanel;
	}

	public String getEnteredGuid() {
		return enteredGuid;
	}
}

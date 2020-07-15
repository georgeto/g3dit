package de.george.g3dit.gui.dialogs;

import java.awt.Window;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import com.jidesoft.dialog.ButtonPanel;

import net.miginfocom.swing.MigLayout;

public class EnterEnumDialog extends ExtStandardDialog {
	private String actionTitle;
	private JComboBox<String> cbValues;

	private String[] items;
	private String defaultItem;

	public EnterEnumDialog(Window owner, String title, String actionTitle, String[] items, String defaultItem) {
		super(owner, title, true);
		this.actionTitle = actionTitle;
		this.items = items;
		this.defaultItem = defaultItem;

		setSize(350, 120);
	}

	@Override
	public JComponent createContentPanel() {
		JPanel panel = new JPanel(new MigLayout("insets 10, fill", "[grow]"));

		cbValues = new JComboBox<>(items);
		cbValues.setSelectedItem(defaultItem);
		panel.add(cbValues, "grow");

		return panel;
	}

	@Override
	public ButtonPanel createButtonPanel() {
		ButtonPanel buttonPanel = newButtonPanel();

		addDefaultButton(buttonPanel, actionTitle);
		addDefaultCancelButton(buttonPanel);

		return buttonPanel;
	}

	public String getSelectedValue() {
		return (String) cbValues.getSelectedItem();
	}
}

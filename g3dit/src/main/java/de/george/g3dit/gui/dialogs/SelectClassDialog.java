package de.george.g3dit.gui.dialogs;

import java.awt.BorderLayout;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.jidesoft.dialog.ButtonPanel;
import com.jidesoft.swing.CheckBoxList;

import de.george.lrentnode.archive.G3ClassContainer;
import de.george.lrentnode.classes.G3Class;

public class SelectClassDialog extends ExtStandardDialog {

	private G3ClassContainer container;
	private CheckBoxList cbList;

	private String actionTitle;

	public SelectClassDialog(Window owner, String title, String actionTitle, G3ClassContainer container) {
		super(owner, title, true);
		this.container = container;
		this.actionTitle = actionTitle;

		setSize(250, 400);
	}

	@Override
	public JComponent createContentPanel() {
		JPanel panel = new JPanel(new BorderLayout(10, 10));
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

		G3Class[] classes = container.getClasses().toArray(new G3Class[0]);
		cbList = new CheckBoxList(classes);
		JScrollPane scroll = new JScrollPane(cbList);
		panel.add(scroll, BorderLayout.CENTER);

		return panel;
	}

	@Override
	public ButtonPanel createButtonPanel() {
		ButtonPanel buttonPanel = newButtonPanel();

		addDefaultButton(buttonPanel, actionTitle);
		addDefaultCancelButton(buttonPanel);

		return buttonPanel;
	}

	public List<G3Class> getResultClasses() {
		List<G3Class> classes = new ArrayList<>();
		for (Object obj : cbList.getCheckBoxListSelectedValues()) {
			classes.add((G3Class) obj);
		}
		return classes;
	}
}

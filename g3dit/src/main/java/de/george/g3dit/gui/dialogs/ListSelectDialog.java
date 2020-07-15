package de.george.g3dit.gui.dialogs;

import java.awt.Window;
import java.util.Collection;
import java.util.List;

import javax.swing.Action;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.border.EtchedBorder;

import com.jidesoft.dialog.ButtonPanel;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.swing.GlazedListsSwing;
import de.george.g3utils.gui.SwingUtils;

public class ListSelectDialog<T> extends AbstractSelectDialog<T> {
	private JList<T> list;

	@SuppressWarnings("unchecked")
	public ListSelectDialog(Window owner, String title, int selectionType, Collection<T> entries) {
		this(owner, title, selectionType, entries, (ListCellRenderer<T>) new DefaultListCellRenderer());
	}

	public ListSelectDialog(Window owner, String title, int selectionType, Collection<T> entries, ListCellRenderer<T> listCellRenderer) {
		super(owner, title);
		setSize(400, 400);

		list = new JList<>(GlazedListsSwing.eventListModel(GlazedLists.eventList(entries)));
		list.setSelectionMode(selectionType);
		list.setCellRenderer(listCellRenderer);
	}

	@Override
	public List<T> getSelectedEntries() {
		return list.getSelectedValuesList();
	}

	@Override
	public JComponent createContentPanel() {
		list.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		return new JScrollPane(list);
	}

	@Override
	public ButtonPanel createButtonPanel() {
		ButtonPanel buttonPanel = newButtonPanel();

		Action okAction = SwingUtils.createAction("OK", () -> {
			if (!list.getSelectedValuesList().isEmpty()) {
				setDialogResult(RESULT_AFFIRMED);
			}
			dispose();
		});
		okAction.setEnabled(false);

		addButton(buttonPanel, okAction, ButtonPanel.AFFIRMATIVE_BUTTON);
		addDefaultCancelButton(buttonPanel);

		list.addListSelectionListener(e -> okAction.setEnabled(!list.getSelectedValuesList().isEmpty()));

		return buttonPanel;

	}
}

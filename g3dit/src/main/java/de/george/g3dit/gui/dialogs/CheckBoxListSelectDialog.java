package de.george.g3dit.gui.dialogs;

import java.awt.Window;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.border.EtchedBorder;

import com.jidesoft.dialog.ButtonPanel;
import com.jidesoft.swing.CheckBoxList;
import com.teamunify.i18n.I;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.swing.GlazedListsSwing;
import de.george.g3utils.gui.SwingUtils;

public class CheckBoxListSelectDialog<T> extends AbstractSelectDialog<T> {
	private CheckBoxList list;

	public CheckBoxListSelectDialog(Window owner, String title, Collection<T> entries) {
		this(owner, title, entries, new DefaultListCellRenderer());
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public CheckBoxListSelectDialog(Window owner, String title, Collection<T> entries, ListCellRenderer listCellRenderer) {
		super(owner, title);
		setSize(400, 400);

		list = new CheckBoxList(GlazedListsSwing.eventListModel(GlazedLists.eventList(entries)));
		list.setCellRenderer(listCellRenderer);
	}

	public void setSelectedEntries(Collection<T> selected) {
		list.setSelectedObjects(new Vector<>(selected));
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<T> getSelectedEntries() {
		return (List<T>) Arrays.asList(list.getCheckBoxListSelectedValues());
	}

	@Override
	public JComponent createContentPanel() {
		list.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		return new JScrollPane(list);
	}

	@Override
	public ButtonPanel createButtonPanel() {
		ButtonPanel buttonPanel = newButtonPanel();

		Action okAction = SwingUtils.createAction(I.tr("Ok"), () -> {
			if (!list.getCheckBoxListSelectionModel().isSelectionEmpty()) {
				setDialogResult(RESULT_AFFIRMED);
			}
			dispose();
		});
		okAction.setEnabled(false);

		addButton(buttonPanel, okAction, ButtonPanel.AFFIRMATIVE_BUTTON);
		addDefaultCancelButton(buttonPanel);

		list.getCheckBoxListSelectionModel().addListSelectionListener(e -> {
			okAction.setEnabled(!list.getCheckBoxListSelectionModel().isSelectionEmpty());
		});

		return buttonPanel;

	}
}

package de.george.g3dit.gui.components;

import java.util.Collections;

import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.swing.AdvancedListSelectionModel;
import ca.odell.glazedlists.swing.DefaultEventListModel;
import ca.odell.glazedlists.swing.GlazedListsSwing;

public class JEventList<E> extends JList<E> {
	public JEventList(EventList<E> eventList) {
		super(GlazedListsSwing.eventListModel(eventList));
		this.setSelectionModel(GlazedListsSwing.eventSelectionModel(eventList));
	}

	@Override
	public DefaultEventListModel<E> getModel() {
		return (DefaultEventListModel<E>) super.getModel();
	}

	@Override
	public void setModel(ListModel<E> model) {
		if (model instanceof ListEventListener && model instanceof ListModel) {
			super.setModel(model);
		} else {
			throw new IllegalArgumentException("ListModel is required to implement ListEventListener and ListModel.");
		}
	}

	@Override
	protected ListSelectionModel createSelectionModel() {
		return GlazedListsSwing.eventSelectionModel(GlazedLists.eventList(Collections.emptyList()));
	}

	@Override
	@SuppressWarnings("unchecked")
	public AdvancedListSelectionModel<E> getSelectionModel() {
		return (AdvancedListSelectionModel<E>) super.getSelectionModel();
	}

	@Override
	public void setSelectionModel(ListSelectionModel selectionModel) {
		if (selectionModel instanceof AdvancedListSelectionModel) {
			super.setSelectionModel(selectionModel);
		} else {
			throw new IllegalArgumentException("ListSelectionModel is required to implement AdvancedListSelectionModel interface.");
		}
	}

}

package de.george.g3dit.tab.archive.views;

import java.awt.Component;

import javax.swing.JToolBar;
import javax.swing.event.TreeSelectionEvent;

import de.george.g3dit.entitytree.filter.ITreeExtension;
import de.george.lrentnode.archive.eCEntity;

public interface ArchiveView {
	void load(eCEntity entity);

	void save(eCEntity entity);

	void entitySelectionChanged(TreeSelectionEvent e);

	ITreeExtension getTreeExtension();

	@Deprecated
	default JToolBar getToolBaar() {
		return null;
	}

	Component getContent();

	/**
	 * Wird aufgerufen, direkt nachdem zu diesem View gewechselt wurde.
	 */
	default void onEnter() {

	}

	/**
	 * Wird aufgerufen, bevor zu einem anderen View gewechselt wird.
	 */
	default void onLeave() {

	}

	/**
	 * Wird aufgerufen, wenn der EditorTab, der diese TemplateView enthält, geschlossen wird. In
	 * dieser Methode kann die TemplateView Resourcen freigeben.
	 */
	void cleanUp();
}

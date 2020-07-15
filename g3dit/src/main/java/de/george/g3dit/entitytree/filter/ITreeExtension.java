package de.george.g3dit.entitytree.filter;

import java.awt.event.ActionListener;

import javax.swing.JPanel;

import de.george.g3dit.entitytree.TreeRenderer;
import de.george.lrentnode.archive.ArchiveFile;
import de.george.lrentnode.archive.eCEntity;

public interface ITreeExtension {
	public void guiInit(JPanel extensionPanel, ActionListener filterRefresh);

	/**
	 * Prüft ob <code>entity</code> in der Auflistung angezeigt werden soll
	 *
	 * @param entity
	 * @return true, wenn Entity angezeigt werden soll
	 */
	public boolean filterLeave(eCEntity entity);

	public boolean isFilterActive();

	public void renderElement(TreeRenderer element, eCEntity entity, ArchiveFile file);
}

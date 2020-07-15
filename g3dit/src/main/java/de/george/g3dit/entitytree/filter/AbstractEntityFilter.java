package de.george.g3dit.entitytree.filter;

import de.george.g3dit.gui.components.search.SearchFilter;
import de.george.lrentnode.archive.eCEntity;

public abstract class AbstractEntityFilter implements SearchFilter<eCEntity> {
	private boolean keepChilds = false;

	/**
	 * Gibt an ob der Filter mit gültigen Werten initialisiert wurde
	 *
	 * @return
	 */
	@Override
	public abstract boolean isValid();

	/**
	 * Prüft ob <code>entity</code> die Kritierien des Filters erfüllt
	 *
	 * @param entity
	 * @return true, wenn <code>entity</code> die Kriterien erfüllt
	 */
	@Override
	public abstract boolean matches(eCEntity entity);

	public void setKeepChilds(boolean keepChilds) {
		this.keepChilds = keepChilds;
	}

	public boolean keepChilds() {
		return keepChilds;
	}
}

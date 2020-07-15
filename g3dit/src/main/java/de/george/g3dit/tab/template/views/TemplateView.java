package de.george.g3dit.tab.template.views;

import java.awt.Component;

import javax.swing.JToolBar;

import de.george.lrentnode.template.TemplateFile;

public interface TemplateView {
	void load(TemplateFile entity);

	void save(TemplateFile entity);

	@Deprecated
	default JToolBar getToolBaar() {
		return null;
	}

	Component getContent();

	/**
	 * Wird aufgerufen, wenn der EditorTab, der diese TemplateView enthält, geschlossen wird. In
	 * dieser Methode kann die TemplateView Resourcen freigeben.
	 */
	void cleanUp();
}

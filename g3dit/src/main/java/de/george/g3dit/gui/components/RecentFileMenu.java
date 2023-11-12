package de.george.g3dit.gui.components;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class RecentFileMenu extends FileMenu {

	private int itemCount; // how many items in the menu
	private List<Path> recentFiles;

	/**
	 * Create a new instance of RecentFileMenu.
	 *
	 * @param count The number of recent files to store.
	 */
	public RecentFileMenu(String name, int count) {
		super(name);
		itemCount = count;
		// initialize default entries
		recentFiles = new ArrayList<>(count);
	}

	/**
	 * Adds a new entry to the menu. Moves everything "down" one row.
	 *
	 * @param inFilePath The new path to add.
	 */
	public void addEntry(Path inFilePath) {
		// Eintrag entfernen wenn bereits vorhanden
		recentFiles.remove(inFilePath);

		// Datei an den Anfang der Liste setzen
		recentFiles.add(0, inFilePath);

		// Maximal itemCount Einträge in der Liste erlaubt
		while (recentFiles.size() > itemCount) {
			recentFiles.remove(recentFiles.size() - 1);
		}

		generateMenu();
	}

	public List<Path> getFiles() {
		return Collections.unmodifiableList(recentFiles);
	}

	public void setRecentFiles(List<Path> files) {
		recentFiles.clear();
		recentFiles.addAll(files);
	}
}

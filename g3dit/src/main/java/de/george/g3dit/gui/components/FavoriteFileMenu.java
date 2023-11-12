package de.george.g3dit.gui.components;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class FavoriteFileMenu extends FileMenu {

	private List<Path> favoriteFiles;

	public FavoriteFileMenu(String name) {
		super(name);
		// initialize default entries
		favoriteFiles = new ArrayList<>();
	}

	public boolean isFavorite(Path inFilePath) {
		return favoriteFiles.contains(inFilePath);
	}

	/**
	 * Adds a new entry to the menu. Moves everything "down" one row.
	 *
	 * @param inFilePath The new path to add.
	 */
	public void toggleFavorite(Path inFilePath) {
		if (!favoriteFiles.remove(inFilePath))
			favoriteFiles.add(inFilePath);

		generateMenu();
	}

	public List<Path> getFiles() {
		return Collections.unmodifiableList(favoriteFiles);
	}

	public void setFavoriteFiles(List<Path> files) {
		favoriteFiles.clear();
		favoriteFiles.addAll(files);
	}
}

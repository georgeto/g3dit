package de.george.g3dit.gui.components;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JMenuItem;

import com.teamunify.i18n.I;

import de.george.g3dit.EditorContext;
import de.george.g3dit.gui.dialogs.EditListDialog;
import de.george.g3dit.gui.table.TableColumnDef;

public abstract class FavoriteFileMenu extends FileMenu {

	private final EditorContext ctx;
	private List<Path> favoriteFiles;

	public FavoriteFileMenu(String name, EditorContext ctx) {
		super(name);
		this.ctx = ctx;
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

	@Override
	protected void generateMenu() {
		super.generateMenu();

		this.addSeparator();
		JMenuItem miEdit = new JMenuItem(I.tr("Edit..."));
		miEdit.addActionListener(e -> {
			new EditListDialog<>(ctx, ctx.getParentWindow(), "Edit Favorites", true, favoriteFiles, Path.class, null,
					this::setFavoriteFiles, TableColumnDef.withName("FileName").editable(false).b()).open();
		});
		this.add(miEdit);
	}

	public List<Path> getFiles() {
		return Collections.unmodifiableList(favoriteFiles);
	}

	public void setFavoriteFiles(List<Path> files) {
		favoriteFiles.clear();
		favoriteFiles.addAll(files);
		generateMenu();
	}
}

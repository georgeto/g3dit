package de.george.g3dit.gui.components;

import java.nio.file.Path;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.MenuSelectionManager;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import de.george.g3dit.util.PathAliases;
import de.george.g3utils.gui.SwingUtils;
import de.george.g3utils.util.FilesEx;

public abstract class FileMenu extends JMenu {
	private KeyStroke accelerator;

	private PathAliases pathAlias;

	/**
	 * @param name The name of this menu, not displayed but used to store the list of recently used
	 *            file names.
	 */
	public FileMenu(String name) {
		super();
		setText(name);
		pathAlias = PathAliases.empty();
		setEnabled(false);
		addMenuListener(new MenuListener() {
			@Override
			public void menuSelected(MenuEvent e) {
				setupActionMap();
			}

			@Override
			public void menuDeselected(MenuEvent e) {
				clearActionMap();
			}

			@Override
			public void menuCanceled(MenuEvent e) {
				clearActionMap();
			}
		});
	}

	@Override
	public KeyStroke getAccelerator() {
		return accelerator;
	}

	@Override
	public void setAccelerator(KeyStroke keyStroke) {
		KeyStroke oldAccelerator = accelerator;
		accelerator = keyStroke;
		repaint();
		revalidate();
		firePropertyChange("accelerator", oldAccelerator, accelerator);
	}

	private void setupActionMap() {
		for (int i = 0; i < 10 && i < getItemCount(); i++) {
			JMenuItem menuItem = getItem(i);
			if (menuItem == null)
				break;
			KeyStroke keyStroke = KeyStroke.getKeyStroke(String.valueOf(i).charAt(0));
			menuItem.setAccelerator(keyStroke);
			getInputMap(WHEN_IN_FOCUSED_WINDOW).put(keyStroke, "EntryOpen" + i);
			getActionMap().put("EntryOpen" + i, SwingUtils.createAction(() -> {
				if (menuItem.isShowing()) {
					menuItem.doClick(0);
					MenuSelectionManager.defaultManager().clearSelectedPath();
				}
			}));
		}
	}

	private void clearActionMap() {
		for (int i = 0; i < 10; i++) {
			getInputMap(WHEN_IN_FOCUSED_WINDOW).remove(KeyStroke.getKeyStroke(String.valueOf(i).charAt(0)));
			getActionMap().remove("EntryOpen" + i);
		}
	}

	protected void generateMenu() {
		// check if this is disabled
		if (!isEnabled()) {
			setEnabled(true);
		}
		// clear the existing items
		removeAll();
		clearActionMap();
		// add items back to the menu
		List<Path> files = getFiles();
		for (int i = 0; i < files.size(); i++) {
			Path filePath = files.get(i);
			JMenuItem menuItem = new JFileMenuItem();

			String menuName = pathAlias.apply(filePath);

			menuItem.setText(menuName);

			menuItem.setVisible(true);
			menuItem.setToolTipText(FilesEx.getAbsolutePath(filePath));
			menuItem.addActionListener(e -> onSelectFile(filePath));

			this.add(menuItem);
		}
	}

	public void setAliasMap(PathAliases aliasMap) {
		pathAlias = aliasMap;
		generateMenu();
	}

	public abstract List<Path> getFiles();

	private static class JFileMenuItem extends JMenuItem {

		private KeyStroke accelerator;

		@Override
		public void setAccelerator(KeyStroke keyStroke) {
			accelerator = keyStroke;
			repaint();
			revalidate();
		}

		@Override
		public KeyStroke getAccelerator() {
			return accelerator;
		}
	}

	/**
	 * Event that fires when a recent file is selected from the menu. Override this when
	 * implementing.
	 *
	 * @param filePath The file that was selected.
	 */
	public abstract void onSelectFile(Path filePath);
}

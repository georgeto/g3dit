package de.george.g3dit.gui.components;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.MenuSelectionManager;

import com.google.common.collect.Lists;

import de.george.g3dit.util.SettingsHelper;
import de.george.g3dit.util.StringLengthComparator;
import de.george.g3utils.gui.SwingUtils;

public abstract class RecentFileMenu extends JMenu {
	private KeyStroke accelerator;

	private int itemCount; // how many items in the menu
	private List<String> recentFiles;
	private Map<String, String> pathAlias;

	/**
	 * Create a new instance of RecentFileMenu.
	 *
	 * @param name The name of this menu, not displayed but used to store the list of recently used
	 *            file names.
	 * @param count The number of recent files to store.
	 */
	public RecentFileMenu(String name, int count) {
		super();
		setText(name);
		this.setMnemonic('R');
		itemCount = count;
		// initialize default entries
		recentFiles = new ArrayList<>(count);
		pathAlias = new TreeMap<>(new StringLengthComparator());
		setEnabled(false);
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

	/**
	 * Adds a new entry to the menu. Moves everything "down" one row.
	 *
	 * @param inFilePath The new path to add.
	 */
	public void addEntry(String inFilePath) {
		// Eintrag entfernen wenn bereits vorhanden
		Iterator<String> fileIter = recentFiles.iterator();
		while (fileIter.hasNext()) {
			if (fileIter.next().equals(inFilePath)) {
				fileIter.remove();
			}
		}

		// Datei an den Anfang der Liste setzen
		recentFiles.add(0, inFilePath);

		// Maximal itemCount Einträge in der Liste erlaubt
		while (recentFiles.size() > itemCount) {
			recentFiles.remove(recentFiles.size() - 1);
		}

		generateMenu();
	}

	private void clearActionMap() {
		for (int i = 0; i < 10; i++) {
			getActionMap().remove("EntryOpen" + i);
		}
	}

	public void generateMenu() {
		// check if this is disabled
		if (!isEnabled()) {
			setEnabled(true);
		}
		// clear the existing items
		removeAll();
		clearActionMap();
		// add items back to the menu
		for (int i = 0; i < recentFiles.size(); i++) {
			String filePath = recentFiles.get(i);
			JMenuItem menuItem = new JFileMenuItem();

			if (i < 10) {
				KeyStroke keyStroke = KeyStroke.getKeyStroke(String.valueOf(i).charAt(0));
				menuItem.setAccelerator(keyStroke);
				this.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(keyStroke, "EntryOpen" + i);
				getActionMap().put("EntryOpen" + i, SwingUtils.createAction(() -> {
					if (menuItem.isShowing()) {
						menuItem.doClick(0);
						MenuSelectionManager.defaultManager().clearSelectedPath();
					}
				}));

			}

			String menuName = SettingsHelper.applyAliasMap(pathAlias, filePath);

			menuItem.setText(menuName);

			menuItem.setVisible(true);
			menuItem.setToolTipText(filePath);
			menuItem.setActionCommand(filePath);
			menuItem.addActionListener(e -> onSelectFile(e.getActionCommand()));

			this.add(menuItem);
		}
	}

	public void setAliasMap(Map<String, String> aliasMap) {
		pathAlias = aliasMap;
	}

	public List<String> getRecentFiles() {
		return Collections.unmodifiableList(recentFiles);
	}

	public void addRecentFiles(List<String> files) {
		Lists.reverse(files).forEach(this::addEntry);
	}

	private class JFileMenuItem extends JMenuItem {

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
	public abstract void onSelectFile(String filePath);
}

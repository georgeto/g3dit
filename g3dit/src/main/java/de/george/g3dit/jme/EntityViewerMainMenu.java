package de.george.g3dit.jme;

import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuBar;

import org.jdesktop.swingx.JXColorSelectionButton;

import de.george.g3dit.EditorContext;

public class EntityViewerMainMenu extends JMenuBar {
	private EntityViewer entityViewer;
	private EditorContext editorContext;

	public EntityViewerMainMenu(EntityViewer entityViewer, EditorContext editorContext) {
		this.entityViewer = entityViewer;
		this.editorContext = editorContext;

		createMenuData();
	}

	private void createMenuData() {
		JMenu muColor = new JMenu("Farben");
		muColor.setMnemonic(KeyEvent.VK_D);
		add(muColor);

		muColor.add(new JXColorSelectionButton());
	}

}

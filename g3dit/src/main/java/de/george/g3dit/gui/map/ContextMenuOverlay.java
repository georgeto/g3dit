package de.george.g3dit.gui.map;

import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;

import javax.swing.Icon;
import javax.swing.JMenuItem;

import de.george.g3dit.gui.components.JDynamicPopupMenu;
import de.george.g3utils.structure.bCVector;
import de.george.g3utils.structure.bCVector2;
import hu.kazocsaba.imageviewer.ImageMouseClickAdapter;
import hu.kazocsaba.imageviewer.ImageMouseEvent;
import hu.kazocsaba.imageviewer.Overlay;

public class ContextMenuOverlay extends Overlay implements ImageMouseClickAdapter {
	private JDynamicPopupMenu popupMenu = new JDynamicPopupMenu();
	private bCVector lastPosition;
	private MapModel<? extends MapItem> data;

	public ContextMenuOverlay(MapModel<? extends MapItem> data) {
		this.data = data;
		data.addRepaintListener(this::repaint);
	}

	public void addMenuItem(String text, Consumer<bCVector> callback) {
		addMenuItem(text, null, callback);
	}

	public void addMenuItem(String text, Icon icon, Consumer<bCVector> callback) {
		JMenuItem menuItem = new JMenuItem(text, icon);
		menuItem.addActionListener(a -> callback.accept(lastPosition));
		popupMenu.add(menuItem);
	}

	@Override
	public void mouseClicked(ImageMouseEvent e) {
		MouseEvent event = e.getOriginalEvent();
		if (event.getButton() == MouseEvent.BUTTON3) {
			lastPosition = data.pixelsToPosition(new bCVector2(e.getX(), e.getY()));
			popupMenu.show(event.getComponent(), event.getX(), event.getY());
		}
	}

	@Override
	public void paint(Graphics2D g, BufferedImage image, AffineTransform transform) {}
}

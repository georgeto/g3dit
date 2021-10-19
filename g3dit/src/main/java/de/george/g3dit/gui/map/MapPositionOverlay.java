package de.george.g3dit.gui.map;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.function.Supplier;

import de.george.g3utils.gui.SwingUtils;
import de.george.g3utils.structure.bCVector;
import de.george.g3utils.structure.bCVector2;
import hu.kazocsaba.imageviewer.ImageMouseEvent;
import hu.kazocsaba.imageviewer.ImageMouseMotionAdapter;
import hu.kazocsaba.imageviewer.Overlay;

public class MapPositionOverlay extends Overlay implements ImageMouseMotionAdapter {
	private MapModel<? extends MapItem> data;
	private Supplier<Rectangle> viewRectangle;
	private Point lastMousePosition = null;

	public MapPositionOverlay(MapModel<? extends MapItem> data, Supplier<Rectangle> viewRectangle) {
		this.data = data;
		this.viewRectangle = viewRectangle;
	}

	@Override
	public void mouseMoved(ImageMouseEvent e) {
		lastMousePosition = e.getPoint();
		repaint();
	}

	@Override
	public void paint(Graphics2D g, BufferedImage image, AffineTransform transform) {
		bCVector lastPosition = getLastPosition();
		if (lastPosition != null) {
			String text = lastPosition.toMarvinString();

			g.setFont(g.getFont().deriveFont(g.getFont().getSize() * 1.3f));
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setColor(SwingUtils.getAlphaColor(Color.ORANGE, 0.7f));
			FontMetrics fm = g.getFontMetrics();
			Rectangle2D textRect = fm.getStringBounds(text, g);

			Rectangle view = viewRectangle.get();
			Point textLocation = view.getLocation();
			textLocation.translate(view.width, view.height);
			textLocation.translate((int) Math.ceil(-textRect.getWidth() - view.getWidth() * 0.02), (int) Math.ceil(-textRect.getHeight()));

			g.fillRoundRect(textLocation.x, textLocation.y - fm.getAscent(), (int) textRect.getWidth(), (int) textRect.getHeight(), 5, 5);

			g.setColor(Color.BLACK);
			g.drawChars(text.toCharArray(), 0, text.length(), textLocation.x, textLocation.y);
		}
	}

	public bCVector getLastPosition() {
		if (lastMousePosition == null) {
			return null;
		}

		return data.pixelsToPosition(new bCVector2(lastMousePosition.x, lastMousePosition.y));
	}
}

package de.george.g3dit.gui.map;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.function.Function;

import de.george.g3utils.structure.bCVector2;
import hu.kazocsaba.imageviewer.Overlay;

public class MapMarkerOverlay<T extends MapItem> extends Overlay {
	private int size = 5;
	private MapModel<T> data;
	private Function<T, Color> colorProvider;

	public MapMarkerOverlay(MapModel<T> data, Function<T, Color> colorProvider) {
		this.data = data;
		this.colorProvider = colorProvider;
		data.addRepaintListener(this::repaint);
	}

	public void setSize(int newSize) {
		if (newSize < 0) {
			throw new IllegalArgumentException("Negative size");
		}
		size = newSize;
		repaint();
	}

	@Override
	public void paint(Graphics2D g, BufferedImage image, AffineTransform transform) {
		Point2D p2d = new Point2D.Double();
		int currentSize = Math.max(size, (int) Math.ceil(transform.getScaleX() / Math.sqrt(2)));

		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		for (T entity : data.items) {
			g.setColor(colorProvider.apply(entity));
			bCVector2 p = data.positionToPixels(entity.getPosition());
			p2d.setLocation(p.getX() + .5, p.getY() + .5);
			transform.transform(p2d, p2d);
			g.drawOval((int) p2d.getX() - currentSize, (int) p2d.getY() - currentSize, 2 * currentSize + 1, 2 * currentSize + 1);
		}
	}
}

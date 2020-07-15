package de.george.g3dit.gui.map;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import de.george.g3utils.gui.SwingUtils;
import de.george.g3utils.structure.bCVector2;
import hu.kazocsaba.imageviewer.ImageMouseEvent;
import hu.kazocsaba.imageviewer.ImageMouseMotionAdapter;
import hu.kazocsaba.imageviewer.Overlay;

public class PopupOverlay extends Overlay implements ImageMouseMotionAdapter {
	private MapModel<? extends MapItem> data;
	private MapItem entity;

	public PopupOverlay(MapModel<? extends MapItem> data) {
		this.data = data;
		data.addRepaintListener(this::repaint);
	}

	@Override
	public void mouseMoved(ImageMouseEvent e) {
		entity = data.getNearest(e.getX(), e.getY(), 20);
		repaint();
	}

	@Override
	public void paint(Graphics2D g, BufferedImage image, AffineTransform transform) {
		if (entity == null) {
			return;
		}

		Point2D p2d = new Point2D.Double();

		bCVector2 p = data.positionToPixels(entity.getPosition());
		p2d.setLocation(p.getX() + .5, p.getY() + .5);
		transform.transform(p2d, p2d);

		g.setFont(g.getFont().deriveFont(g.getFont().getSize() * 1.3f));
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(SwingUtils.getAlphaColor(Color.ORANGE, 0.7f));
		FontMetrics fm = g.getFontMetrics();
		Rectangle2D textRect = fm.getStringBounds(entity.getTitle(), g);
		g.fillRoundRect((int) p2d.getX(), (int) p2d.getY() - fm.getAscent(), (int) textRect.getWidth(), (int) textRect.getHeight(), 5, 5);

		g.setColor(Color.BLACK);
		g.drawChars(entity.getTitle().toCharArray(), 0, entity.getTitle().length(), (int) p2d.getX(), (int) p2d.getY());
	}
}

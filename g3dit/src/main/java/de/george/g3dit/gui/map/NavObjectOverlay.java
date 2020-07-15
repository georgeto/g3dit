package de.george.g3dit.gui.map;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Optional;

import com.vividsolutions.jts.geom.Geometry;

import de.george.g3dit.EditorContext;
import de.george.g3dit.EntityMap.EntityMapItem;
import de.george.g3dit.cache.Caches;
import de.george.g3dit.cache.NavCache;
import de.george.g3utils.structure.bCVector;
import de.george.navmap.data.NavPath;
import de.george.navmap.data.NavZone;
import de.george.navmap.data.NegZone;
import de.george.navmap.data.Zone;
import de.george.navmap.draw.LiteShape;
import de.george.navmap.sections.NavMap;
import hu.kazocsaba.imageviewer.Overlay;

public class NavObjectOverlay<T extends EntityMapItem> extends Overlay {
	private MapModel<T> data;
	private EditorContext ctx;

	public NavObjectOverlay(MapModel<T> data, EditorContext ctx) {
		this.data = data;
		this.ctx = ctx;
		data.addRepaintListener(this::repaint);
	}

	private AffineTransform worldTransform(AffineTransform imageTransform) {
		AffineTransform worldTransform = data.positionToPixelsTransform();
		worldTransform.preConcatenate(imageTransform);
		return worldTransform;
	}

	private Point2D transformPoint(bCVector point, AffineTransform transform) {
		Point2D p2d = new Point2D.Double();
		p2d.setLocation(point.getX(), point.getZ());
		return transform.transform(p2d, p2d);
	}

	private void drawZone(Graphics2D g, AffineTransform transform, Zone zone) {
		int xPoints[] = new int[zone.getPointCount()];
		int yPoints[] = new int[zone.getPointCount()];
		List<bCVector> worldPoints = zone.getWorldPoints();
		for (int i = 0; i < zone.getPointCount(); i++) {
			Point2D stick = transformPoint(worldPoints.get(i), transform);
			xPoints[i] = (int) stick.getX();
			yPoints[i] = (int) stick.getY();
		}
		g.setComposite(AlphaComposite.SrcOver.derive(0.1f));
		g.fillPolygon(xPoints, yPoints, xPoints.length);
		g.setComposite(AlphaComposite.SrcOver);
		g.drawPolygon(xPoints, yPoints, xPoints.length);
	}

	private void drawShape(Graphics2D g, Shape shape) {
		g.setComposite(AlphaComposite.SrcOver.derive(0.1f));
		g.fill(shape);
		g.setComposite(AlphaComposite.SrcOver);
		g.draw(shape);
	}

	private void drawGeometry(Graphics2D g, AffineTransform transform, Geometry geometry) {
		drawShape(g, new LiteShape(geometry, transform, false));
	}

	private void drawPath(Graphics2D g, AffineTransform transform, NavPath path) {
		Geometry polygon = path.getPolygon();
		if (polygon != null) {
			drawGeometry(g, transform, polygon);
		} else {
			drawPathLine(g, transform, path);
		}
	}

	private void drawPathLine(Graphics2D g, AffineTransform transform, NavPath path) {
		List<bCVector> worldPoints = path.getWorldPoints();
		for (int i = 0; i < worldPoints.size() - 1; i++) {
			Point2D from = transformPoint(worldPoints.get(i), transform);
			Point2D to = transformPoint(worldPoints.get(i + 1), transform);
			g.drawLine((int) from.getX(), (int) from.getY(), (int) to.getX(), (int) to.getY());
		}
	}

	@Override
	public void paint(Graphics2D g, BufferedImage image, AffineTransform transform) {
		// int currentSize = Math.max(1, (int) Math.ceil(transform.getScaleX() / Math.sqrt(2)));

		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		transform = worldTransform(transform);

		NavCache navCache = Caches.nav(ctx);
		NavMap navMap = ctx.getNavMapManager().getNavMap(true);
		for (T entity : data.items) {
			NavZone navZone = navCache.getZoneByGuid(entity.getGuid());
			if (navZone != null) {
				g.setColor(Color.GREEN);
				drawZone(g, transform, navZone);
				continue;
			}

			Optional<NegZone> negZone = navMap.getNegZone(entity.getGuid());
			if (negZone.isPresent()) {
				g.setColor(Color.RED);
				drawZone(g, transform, negZone.get());
				continue;
			}

			Optional<Geometry> negCircle = navMap.getNegCircleConvexHull(entity.getGuid());
			if (negCircle.isPresent()) {
				g.setColor(Color.RED);
				drawGeometry(g, transform, negCircle.get());
				continue;
			}

			NavPath path = navCache.getPathByGuid(entity.getGuid());
			if (path != null) {
				g.setColor(Color.BLUE.brighter());
				drawPath(g, transform, path);
				continue;
			}

			Optional<Geometry> prefPath = navMap.getPrefPathPolygon(entity.getGuid());
			if (prefPath.isPresent()) {
				g.setColor(Color.PINK.darker().darker());
				drawGeometry(g, transform, prefPath.get());
				continue;
			}
		}
	}

}

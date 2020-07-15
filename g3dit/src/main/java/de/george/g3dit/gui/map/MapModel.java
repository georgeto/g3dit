package de.george.g3dit.gui.map;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import de.george.g3utils.structure.bCBox;
import de.george.g3utils.structure.bCVector;
import de.george.g3utils.structure.bCVector2;

public class MapModel<T extends MapItem> {
	protected bCVector2 topLeft;
	protected bCVector2 bottomRight;

	private int imageWidth = -1;
	private int imageHeight = -1;

	protected EventList<T> items;

	private List<Runnable> repaintListeners = new CopyOnWriteArrayList<>();

	public MapModel(EventList<T> items, bCVector2 topLeft, bCVector2 bottomRight, int imageWidth, int imageHeight) {
		this.items = GlazedLists.readOnlyList(items);
		this.topLeft = topLeft.clone();
		this.bottomRight = bottomRight.clone();
		this.imageWidth = imageWidth;
		this.imageHeight = imageHeight;

		items.addListEventListener(e -> repaint());
	}

	public bCVector2 positionToPixels(bCVector position) {
		bCVector2 distance = position.to2D().getInvTranslated(topLeft);
		bCVector2 size = bottomRight.getInvTranslated(topLeft);
		return new bCVector2(distance.getX() / size.getX() * imageWidth, distance.getY() / size.getY() * imageHeight);
	}

	public AffineTransform positionToPixelsTransform() {
		AffineTransform transform = AffineTransform.getScaleInstance(imageWidth / (bottomRight.getX() - topLeft.getX()),
				imageHeight / (bottomRight.getY() - topLeft.getY()));
		transform.translate(-topLeft.getX(), -topLeft.getY());
		return transform;
	}

	public bCVector pixelsToPosition(bCVector2 pixels) {
		// position = (pixels / imageDim) * size + topLeft;
		bCVector2 size = bottomRight.getInvTranslated(topLeft);
		return pixels.getInvScaled(new bCVector2(imageWidth, imageHeight)).scale(size).translate(topLeft).to3D(0);
	}

	public Rectangle areaToPixels(bCBox area) {
		bCVector2 min = positionToPixels(area.getMin());
		Rectangle rect = new Rectangle(new Point((int) min.getX(), (int) min.getY()));
		bCVector2 max = positionToPixels(area.getMax());
		rect.add(new Point2D.Float(max.getX(), max.getY()));
		return rect;
	}

	public EventList<T> getItems() {
		return items;
	}

	public T getNearest(bCVector point, float maxDist) {
		bCVector2 pixels = positionToPixels(point);
		return getNearest((int) pixels.getX(), (int) pixels.getY(), maxDist);
	}

	public T getNearest(int x, int y, float maxDist) {
		bCVector2 eventCoord = new bCVector2(x, y);
		T nearest = null;
		float distNearest = Float.MAX_VALUE;
		for (T item : items) {
			bCVector2 p = positionToPixels(item.getPosition());
			float dist = p.getInvTranslated(eventCoord).length();
			if (dist < distNearest) {
				distNearest = dist;
				nearest = item;
			}
		}

		return distNearest < maxDist ? nearest : null;
	}

	public List<T> getInRectangle(Rectangle rectangle) {
		List<T> in = new ArrayList<>();
		for (T item : items) {
			bCVector2 p = positionToPixels(item.getPosition());
			if (rectangle.contains(new Point2D.Float(p.getX(), p.getY()))) {
				in.add(item);
			}
		}

		return in;
	}

	public void addRepaintListener(Runnable callback) {
		repaintListeners.add(callback);
	}

	public void repaint() {
		repaintListeners.forEach(Runnable::run);
	}
}

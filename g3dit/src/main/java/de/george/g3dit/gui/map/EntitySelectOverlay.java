package de.george.g3dit.gui.map;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import hu.kazocsaba.imageviewer.ImageMouseClickAdapter;
import hu.kazocsaba.imageviewer.ImageMouseEvent;
import hu.kazocsaba.imageviewer.ImageMouseMotionAdapter;
import hu.kazocsaba.imageviewer.Overlay;

public class EntitySelectOverlay<T extends MapItem> extends Overlay implements ImageMouseClickAdapter, ImageMouseMotionAdapter {
	private MapModel<T> data;

	private Point origin;
	private Point current;

	private Consumer<List<T>> listener;

	public EntitySelectOverlay(MapModel<T> data, Consumer<List<T>> listener) {
		this.data = data;
		this.listener = listener;
		data.addRepaintListener(this::repaint);
	}

	public void reset() {
		origin = current = null;
		listener.accept(Collections.emptyList());
		repaint();
	}

	@Override
	public void mousePressed(ImageMouseEvent e) {
		if (e.getOriginalEvent().getButton() == MouseEvent.BUTTON1) {
			origin = e.getPoint();
			current = null;
			repaint();
		}
	}

	@Override
	public void mouseDragged(ImageMouseEvent e) {
		if (origin != null && (e.getOriginalEvent().getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK) == InputEvent.BUTTON1_DOWN_MASK) {
			current = e.getPoint();
			repaint();
			notifyListener();
		}
	}

	private void notifyListener() {
		if (listener == null) {
			return;
		}

		listener.accept(data.getInRectangle(getSelectRectangle()));
	}

	private Rectangle getSelectRectangle() {
		Rectangle selectRect = new Rectangle(origin);
		selectRect.add(current);
		return selectRect;
	}

	@Override
	public void paint(Graphics2D g, BufferedImage image, AffineTransform transform) {
		if (origin == null || current == null) {
			return;
		}

		Rectangle selectRect = getSelectRectangle();
		selectRect = new Rectangle((int) (selectRect.x * transform.getScaleX() + transform.getTranslateX()),
				(int) (selectRect.y * transform.getScaleY() + transform.getTranslateY()), (int) (selectRect.width * transform.getScaleX()),
				(int) (selectRect.height * transform.getScaleY()));
		g.drawRect(selectRect.x, selectRect.y, selectRect.width, selectRect.height);

	}
}

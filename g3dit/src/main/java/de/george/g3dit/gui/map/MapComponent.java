package de.george.g3dit.gui.map;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import ca.odell.glazedlists.EventList;
import de.george.g3utils.gui.SwingUtils;
import de.george.g3utils.structure.bCBox;
import de.george.g3utils.structure.bCVector;
import de.george.g3utils.structure.bCVector2;
import hu.kazocsaba.imageviewer.ImageMouseClickAdapter;
import hu.kazocsaba.imageviewer.ImageMouseClickListener;
import hu.kazocsaba.imageviewer.ImageMouseEvent;
import hu.kazocsaba.imageviewer.ImageMouseMotionListener;
import hu.kazocsaba.imageviewer.ImageViewer;
import hu.kazocsaba.imageviewer.Overlay;
import hu.kazocsaba.imageviewer.ResizeStrategy;

public class MapComponent<T extends MapItem> {
	private ImageViewer viewer;
	private int imageWidth, imageHeight;

	private MapModel<T> model;

	private ContextMenuOverlay contextMenu;

	public MapComponent(EventList<T> items) {
		try {
			BufferedImage mapWorld = ImageIO.read(MapComponent.class.getResourceAsStream("/res/MapWorld.jpg"));
			viewer = new ImageViewer(mapWorld, false);
			imageWidth = mapWorld.getWidth();
			imageHeight = mapWorld.getHeight();
		} catch (IOException e) {
			e.printStackTrace();
		}
		viewer.setResizeStrategy(ResizeStrategy.CUSTOM_ZOOM);
		viewer.setInterpolationType(RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		viewer.setZoomFactor(getDefaultZoom());

		bCVector2 topLeft = new bCVector2(-235453.148493f, 185244.7756627072f);
		bCVector2 bottomRight = new bCVector2(147072.1704486418f, -206017.8211706696f);
		model = new MapModel<>(items, topLeft, bottomRight, imageWidth, imageHeight);

		contextMenu = new ContextMenuOverlay(model);
		addOverlay(contextMenu, 1);
		addOverlay(new PopupOverlay(model), 2);
		addMouseListener(new NavigationMouseAdapter());
		addMouseListener(new SelectZoomAreaMouseAdapter());

		SwingUtils.addKeyStroke(viewer.getComponent(), JComponent.WHEN_IN_FOCUSED_WINDOW, "Reset Zoom",
				KeyStroke.getKeyStroke(KeyEvent.VK_0, InputEvent.CTRL_DOWN_MASK), () -> viewer.setZoomFactor(getDefaultZoom()));

		SwingUtils.addKeyStroke(viewer.getComponent(), JComponent.WHEN_IN_FOCUSED_WINDOW, "Zoom In",
				KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, InputEvent.CTRL_DOWN_MASK),
				() -> viewer.setZoomFactor(viewer.getZoomFactor() * 1.1));

		SwingUtils.addKeyStroke(viewer.getComponent(), JComponent.WHEN_IN_FOCUSED_WINDOW, "Zoom Out",
				KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, InputEvent.CTRL_DOWN_MASK),
				() -> viewer.setZoomFactor(viewer.getZoomFactor() / 1.1));
	}

	public ImageViewer getViewer() {
		return viewer;
	}

	public MapModel<T> getModel() {
		return model;
	}

	public void addMenuItem(String text, Consumer<bCVector> callback) {
		contextMenu.addMenuItem(text, callback);
	}

	public void addMenuItem(String text, Icon icon, Consumer<bCVector> callback) {
		contextMenu.addMenuItem(text, icon, callback);
	}

	/**
	 * @param area Area to show in world coordinates.
	 */
	public void showArea(bCBox area) {
		showArea(model.areaToPixels(area));
	}

	/**
	 * @param area Area to show in image coordinates.
	 */
	public void showArea(Rectangle area) {
		Rectangle viewRect = viewer.getViewRect();

		double zoomFactor = Math.min(viewRect.getWidth() / area.getWidth(), viewRect.getHeight() / area.getHeight());
		viewer.setZoomFactor(zoomFactor);

		area.setRect(area.getX() * zoomFactor, area.getY() * zoomFactor, area.getWidth() * zoomFactor, area.getHeight() * zoomFactor);
		int borderLength = Math.max(area.width, area.height);
		area.grow((borderLength - area.width) / 2, (borderLength - area.height) / 2);
		viewer.scrollRectToVisible(area);

		viewer.setZoomFactor(zoomFactor * 0.9);
	}

	public void addItemClickListener(BiConsumer<T, ImageMouseEvent> listener) {
		viewer.addImageMouseClickListener(new ImageMouseClickAdapter() {
			@Override
			public void mouseClicked(ImageMouseEvent e) {
				T marker = model.getNearest(e.getX(), e.getY(), 20);
				if (marker != null) {
					listener.accept(marker, e);
				}
			}
		});
	}

	public MapMarkerOverlay<T> addMarkerOverlay(Function<T, Color> colorProvider) {
		MapMarkerOverlay<T> overlay = new MapMarkerOverlay<>(model, colorProvider);
		addOverlay(overlay, 1);
		return overlay;
	}

	public EntitySelectOverlay<T> addSelectOverlay(Consumer<List<T>> selectListener) {
		EntitySelectOverlay<T> selectOverlay = new EntitySelectOverlay<>(model, selectListener);
		addOverlay(selectOverlay, 3);
		return selectOverlay;
	}

	public void addOverlay(Overlay overlay, int layer) {
		viewer.addOverlay(overlay, layer);
		addMouseListener(overlay);
	}

	public void addMouseListener(Object listener) {
		if (listener instanceof MouseListener) {
			viewer.addMouseListener((MouseListener) listener);
		}

		if (listener instanceof MouseMotionListener) {
			viewer.addMouseMotionListener((MouseMotionListener) listener);
		}

		if (listener instanceof MouseWheelListener) {
			viewer.addMouseWheelListener((MouseWheelListener) listener);
		}

		if (listener instanceof ImageMouseClickListener) {
			viewer.addImageMouseClickListener((ImageMouseClickListener) listener);
		}

		if (listener instanceof ImageMouseMotionListener) {
			viewer.addImageMouseMotionListener((ImageMouseMotionListener) listener);
		}
	}

	private double getDefaultZoom() {
		Rectangle viewRect = viewer.getViewRect();
		if (viewRect.isEmpty()) {
			return 0.15;
		}

		return Math.min(viewRect.getWidth() / imageWidth, viewRect.getHeight() / imageHeight) * 0.95;
	}

	private final class NavigationMouseAdapter extends MouseAdapter {
		private Point origin;

		@Override
		public void mousePressed(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON3) {
				origin = e.getPoint();
			}
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			if (origin != null && (e.getModifiersEx() & InputEvent.BUTTON3_DOWN_MASK) == InputEvent.BUTTON3_DOWN_MASK) {
				Rectangle viewRect = viewer.getViewRect();
				if (viewRect != null) {
					int deltaX = origin.x - e.getX();
					int deltaY = origin.y - e.getY();
					viewRect.x += deltaX;
					viewRect.y += deltaY;

					viewer.scrollRectToVisible(viewRect);
				}
			}
		}

		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			if (e.isControlDown() && e.getWheelRotation() != 0) {
				boolean zoomIn = e.getWheelRotation() < 0;
				int rotations = Math.abs(e.getWheelRotation());
				double zoomFactor = viewer.getZoomFactor();
				while (rotations-- > 0) {
					if (zoomIn) {
						zoomFactor *= 1.1;
					} else {
						zoomFactor /= 1.1;
					}
				}
				viewer.setZoomFactor(zoomFactor);
			}
		}
	}

	private final class SelectZoomAreaMouseAdapter implements ImageMouseClickAdapter {
		private Point origin;

		@Override
		public void mousePressed(ImageMouseEvent e) {
			if (e.getOriginalEvent().getButton() == MouseEvent.BUTTON1) {
				origin = e.getOriginalEvent().isControlDown() ? e.getPoint() : null;
			}
		}

		@Override
		public void mouseReleased(ImageMouseEvent e) {
			if (origin != null && e.getOriginalEvent().getButton() == MouseEvent.BUTTON1) {
				Rectangle zoomArea = new Rectangle(origin);
				zoomArea.add(e.getPoint());
				origin = null;

				if (zoomArea.getWidth() < 5 || zoomArea.getHeight() < 5) {
					return;
				}

				showArea(zoomArea);
			}
		}
	}
}

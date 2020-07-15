package de.george.g3dit.gui.map;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.george.g3dit.rpc.MonotonicallyOrderedIpc;
import de.george.g3dit.rpc.proto.DTC;
import de.george.g3dit.rpc.proto.G3RemoteControlProtos.EntityRequest;
import de.george.g3dit.rpc.proto.G3RemoteControlProtos.Position;
import de.george.g3dit.rpc.proto.G3RemoteControlProtos.ResponseContainer;
import de.george.g3dit.rpc.zmq.ResponseCallback;
import de.george.g3utils.structure.bCVector;
import de.george.g3utils.structure.bCVector2;
import hu.kazocsaba.imageviewer.Overlay;

public class PlayerPositionOverlay<T extends MapItem> extends Overlay {
	private static final Logger logger = LoggerFactory.getLogger(PlayerPositionOverlay.class);
	private static final String PC_HERO = "PC_Hero";

	private int size = 5;

	private MapModel<T> model;
	private Supplier<Boolean> isActive;
	private MonotonicallyOrderedIpc ipcPlayerPosition = new MonotonicallyOrderedIpc();
	private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();;

	private boolean lastActive = false;
	private boolean validPosition;
	private bCVector worldPosition;

	public PlayerPositionOverlay(MapModel<T> model, Supplier<Boolean> isActive) {
		this.model = model;
		this.isActive = isActive;

		executor.scheduleAtFixedRate(() -> {
			try {
				boolean active = isActive.get();
				if (active) {
					updatePlayerPosition();
				} else if (lastActive != active) {
					SwingUtilities.invokeLater(this::repaint);
				}
				lastActive = active;
			} catch (Exception e) {
				logger.warn("Fehler in updatePosition().", e);
			}
		}, 0, 1000, TimeUnit.MILLISECONDS);
	}

	private void updatePlayerPosition() {
		EntityRequest request = EntityRequest.newBuilder().setName(PC_HERO).build();
		ipcPlayerPosition.sendRequest(request, (s, rc, ud) -> {
			SwingUtilities.invokeLater(() -> {
				if (s == ResponseCallback.Status.Timeout) {
					validPosition = false;
				} else if (rc.getStatus() == ResponseContainer.Status.FAILED) {
					validPosition = false;
				} else {
					Position position = rc.getEntityResponse().getPosition();
					worldPosition = DTC.convert(position.getTranslation());
					validPosition = true;
				}
				repaint();
			});
		});
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

		if (validPosition && isActive.get()) {
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			bCVector2 p = model.positionToPixels(worldPosition);
			Point2D p2d = new Point2D.Double();
			p2d.setLocation(p.getX() + .5, p.getY() + .5);
			transform.transform(p2d, p2d);
			int currentSize = Math.max(size, (int) Math.ceil(transform.getScaleX() / Math.sqrt(2)));
			g.setColor(new Color(255, 100, 0));
			g.fillRect((int) p2d.getX(), (int) p2d.getY(), 2 * currentSize, 2 * currentSize);
			g.setColor(Color.BLACK);
			g.drawRect((int) p2d.getX(), (int) p2d.getY(), 2 * currentSize, 2 * currentSize);
		}
	}
}

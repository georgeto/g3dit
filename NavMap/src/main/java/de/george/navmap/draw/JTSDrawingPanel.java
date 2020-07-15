package de.george.navmap.draw;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

public class JTSDrawingPanel extends JPanel {
	private static final int MARGIN = 5;

	private List<Geometry> geometries = new ArrayList<>();
	private List<Color> colors = new ArrayList<>();
	private AffineTransform geomToScreen;

	public void addGeometry(Geometry geom) {
		addGeometry(geom, Color.BLUE);
	}

	public void addGeometry(Geometry geom, Color color) {
		geometries.add(geom);
		colors.add(color);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		if (!geometries.isEmpty()) {
			setTransform();

			Graphics2D g2d = (Graphics2D) g;
			// Paint polyPaint = new GradientPaint(0, 0, Color.CYAN, 100, 100, Color.MAGENTA, true);

			for (int i = 0; i < geometries.size(); i++) {
				LiteShape shape = new LiteShape(geometries.get(i), geomToScreen, false);

				// if (Geometries.get(geom) == Geometries.POLYGON) {
				// g2d.setPaint(polyPaint);
				// g2d.fill(shape);
				// } else {
				g2d.setPaint(colors.get(i));
				g2d.draw(shape);
				// }
			}
		}
	}

	private void setTransform() {
		Envelope env = getGeometryBounds();
		Rectangle visRect = getVisibleRect();
		Rectangle drawingRect = new Rectangle(visRect.x + MARGIN, visRect.y + MARGIN, visRect.width - 2 * MARGIN,
				visRect.height - 2 * MARGIN);

		double scale = Math.min(drawingRect.getWidth() / env.getWidth(), drawingRect.getHeight() / env.getHeight());
		double xoff = MARGIN - scale * env.getMinX();
		double yoff = MARGIN + env.getMaxY() * scale;
		geomToScreen = new AffineTransform(scale, 0, 0, -scale, xoff, yoff);
	}

	private Envelope getGeometryBounds() {
		Envelope env = new Envelope();
		for (Geometry geom : geometries) {
			Envelope geomEnv = geom.getEnvelopeInternal();
			env.expandToInclude(geomEnv);
		}

		return env;
	}

	public JFrame showFrame(String title) {
		JFrame frame = new JFrame(title);
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.add(this);
		frame.setSize(500, 500);
		frame.setVisible(true);
		return frame;
	}

	public static void draw(String title, Geometry... geom) {
		JTSDrawingPanel panel = new JTSDrawingPanel();
		for (Geometry geo : geom) {
			panel.addGeometry(geo);
		}
		panel.showFrame(title);
	}

	public static void draw(String title, Collection<Geometry> geom) {
		JTSDrawingPanel panel = new JTSDrawingPanel();
		for (Geometry geo : geom) {
			panel.addGeometry(geo);
		}
		panel.showFrame(title);

	}
}

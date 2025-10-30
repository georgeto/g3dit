package de.george.g3dit.jme;

import java.awt.AWTException;
import java.awt.BufferCapabilities;
import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.ImageCapabilities;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jme3.post.SceneProcessor;
import com.jme3.profile.AppProfiler;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.system.awt.PaintMode;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Image.Format;
import com.jme3.util.BufferUtils;
import com.jme3.util.Screenshots;

/**
 * Uses RGBA frame buffer instead of a RGB one.
 */
public class MyAwtPanel extends Canvas implements SceneProcessor {
	private boolean attachAsMain = false;

	private BufferedImage img;
	private FrameBuffer fb;
	private boolean srgb = false;
	private ByteBuffer byteBuf;
	private IntBuffer intBuf;
	private RenderManager rm;
	private PaintMode paintMode;
	private ArrayList<ViewPort> viewPorts = new ArrayList<>();

	// Visibility/drawing vars
	private BufferStrategy strategy;
	private AffineTransformOp transformOp;
	private AtomicBoolean hasNativePeer = new AtomicBoolean(false);
	private AtomicBoolean showing = new AtomicBoolean(false);
	private AtomicBoolean repaintRequest = new AtomicBoolean(false);

	// Reshape vars
	private int newWidth = 1;
	private int newHeight = 1;
	private AtomicBoolean reshapeNeeded = new AtomicBoolean(false);
	private final Object lock = new Object();
	private AppProfiler prof;

	public MyAwtPanel(PaintMode paintMode) {
		this(paintMode, false);
	}

	public MyAwtPanel(PaintMode paintMode, boolean srgb) {
		this.paintMode = paintMode;
		this.srgb = srgb;
		if (paintMode == PaintMode.Accelerated) {
			setIgnoreRepaint(true);
		}

		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				synchronized (lock) {
					int newWidth2 = Math.max(getWidth(), 1);
					int newHeight2 = Math.max(getHeight(), 1);
					if (newWidth != newWidth2 || newHeight != newHeight2) {
						newWidth = newWidth2;
						newHeight = newHeight2;
						reshapeNeeded.set(true);
					}
				}
			}
		});
	}

	@Override
	public void addNotify() {
		super.addNotify();

		synchronized (lock) {
			hasNativePeer.set(true);
		}

		requestFocusInWindow();
	}

	@Override
	public void removeNotify() {
		synchronized (lock) {
			hasNativePeer.set(false);
		}

		super.removeNotify();
	}

	@Override
	public void paint(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		synchronized (lock) {
			g2d.drawImage(img, transformOp, 0, 0);
		}
	}

	public boolean checkVisibilityState() {
		if (!hasNativePeer.get()) {
			if (strategy != null) {
				// strategy.dispose();
				strategy = null;
			}
			return false;
		}

		boolean currentShowing = isShowing();
		showing.set(currentShowing);
		return currentShowing;
	}

	public void repaintInThread() {
		// Convert screenshot.
		byteBuf.clear();
		rm.getRenderer().readFrameBuffer(fb, byteBuf);

		synchronized (lock) {
			// All operations on img must be synchronized
			// as it is accessed from EDT.
			Screenshots.convertScreenShot2(intBuf, img);
			repaint();
		}
	}

	public void drawFrameInThread() {
		// Convert screenshot.
		byteBuf.clear();
		rm.getRenderer().readFrameBuffer(fb, byteBuf);
		Screenshots.convertScreenShot2(intBuf, img);

		synchronized (lock) {
			// All operations on strategy should be synchronized (?)
			if (strategy == null) {
				try {
					createBufferStrategy(1, new BufferCapabilities(new ImageCapabilities(true), new ImageCapabilities(true),
							BufferCapabilities.FlipContents.UNDEFINED));
				} catch (AWTException ex) {
					Logger.getLogger(MyAwtPanel.class.getName()).log(Level.WARNING, "OGL: Failed to create buffer strategy.", ex);
				}
				strategy = getBufferStrategy();
			}

			// Draw screenshot.
			do {
				do {
					Graphics2D g2d = (Graphics2D) strategy.getDrawGraphics();
					if (g2d == null) {
						Logger.getLogger(MyAwtPanel.class.getName()).log(Level.WARNING, "OGL: DrawGraphics was null.");
						return;
					}

					g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);

					g2d.drawImage(img, transformOp, 0, 0);
					g2d.dispose();
					strategy.show();
				} while (strategy.contentsRestored());
			} while (strategy.contentsLost());
		}
	}

	public boolean isActiveDrawing() {
		return paintMode != PaintMode.OnRequest && showing.get();
	}

	public void attachTo(boolean overrideMainFramebuffer, ViewPort... vps) {
		if (viewPorts.size() > 0) {
			for (ViewPort vp : viewPorts) {
				vp.setOutputFrameBuffer(null);
			}
			viewPorts.get(viewPorts.size() - 1).removeProcessor(this);
		}

		viewPorts.addAll(Arrays.asList(vps));
		viewPorts.get(viewPorts.size() - 1).addProcessor(this);

		attachAsMain = overrideMainFramebuffer;
	}

	@Override
	public void initialize(RenderManager rm, ViewPort vp) {
		if (this.rm == null) {
			// First time called in OGL thread
			this.rm = rm;
			reshapeInThread(1, 1);
		}
	}

	private void reshapeInThread(int width, int height) {
		byteBuf = BufferUtils.ensureLargeEnough(byteBuf, width * height * 4);
		intBuf = byteBuf.asIntBuffer();

		if (fb != null) {
			fb.dispose();
			fb = null;
		}

		fb = new FrameBuffer(width, height, 1);
		fb.setDepthBuffer(Format.Depth);
		fb.setColorBuffer(Format.RGBA8);
		fb.setSrgb(srgb);

		if (attachAsMain) {
			rm.getRenderer().setMainFrameBufferOverride(fb);
		}

		synchronized (lock) {
			img = new BufferedImage(width, height, BufferedImage.TYPE_INT_BGR);
		}

		// synchronized (lock){
		// img = (BufferedImage) getGraphicsConfiguration().createCompatibleImage(width, height);
		// }
		AffineTransform tx = AffineTransform.getScaleInstance(1, -1);
		tx.translate(0, -img.getHeight());
		transformOp = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);

		for (ViewPort vp : viewPorts) {
			if (!attachAsMain) {
				vp.setOutputFrameBuffer(fb);
			}
			vp.getCamera().resize(width, height, true);

			// NOTE: Hack alert. This is done ONLY for custom framebuffers.
			// Main framebuffer should use RenderManager.notifyReshape().
			for (SceneProcessor sp : vp.getProcessors()) {
				sp.reshape(vp, width, height);
			}
		}
	}

	@Override
	public boolean isInitialized() {
		return fb != null;
	}

	@Override
	public void preFrame(float tpf) {}

	@Override
	public void postQueue(RenderQueue rq) {}

	@Override
	public void invalidate() {
		// For "PaintMode.OnDemand" only.
		repaintRequest.set(true);
	}

	void onFrameEnd() {
		if (reshapeNeeded.getAndSet(false)) {
			reshapeInThread(newWidth, newHeight);
		} else {
			if (!checkVisibilityState()) {
				return;
			}

			switch (paintMode) {
				case Accelerated:
					drawFrameInThread();
					break;
				case Repaint:
					repaintInThread();
					break;
				case OnRequest:
					if (repaintRequest.getAndSet(false)) {
						repaintInThread();
					}
					break;
			}
		}
	}

	@Override
	public void postFrame(FrameBuffer out) {
		if (!attachAsMain && out != fb) {
			throw new IllegalStateException("Why did you change the output framebuffer?");
		}

		// onFrameEnd();
	}

	@Override
	public void reshape(ViewPort vp, int w, int h) {}

	@Override
	public void cleanup() {}

	@Override
	public void setProfiler(AppProfiler profiler) {
		prof = profiler;
	}
}

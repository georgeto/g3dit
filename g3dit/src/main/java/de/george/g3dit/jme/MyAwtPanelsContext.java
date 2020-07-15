package de.george.g3dit.jme;

import java.util.ArrayList;

import com.jme3.input.JoyInput;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.TouchInput;
import com.jme3.input.awt.AwtKeyInput;
import com.jme3.input.awt.AwtMouseInput;
import com.jme3.opencl.Context;
import com.jme3.renderer.Renderer;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeContext;
import com.jme3.system.JmeSystem;
import com.jme3.system.SystemListener;
import com.jme3.system.Timer;
import com.jme3.system.awt.PaintMode;

/**
 * Uses MyAwtPanel instead of AwtPanel.
 */
public class MyAwtPanelsContext implements JmeContext {

	protected JmeContext actualContext;
	protected AppSettings settings = new AppSettings(true);
	protected SystemListener listener;
	protected ArrayList<MyAwtPanel> panels = new ArrayList<>();
	protected MyAwtPanel inputSource;

	protected AwtMouseInput mouseInput = new AwtMouseInput();
	protected AwtKeyInput keyInput = new AwtKeyInput();

	protected boolean lastThrottleState = false;

	private class MyAwtPanelsListener implements SystemListener {

		@Override
		public void initialize() {
			initInThread();
		}

		@Override
		public void reshape(int width, int height) {
			throw new IllegalStateException();
		}

		@Override
		public void update() {
			updateInThread();
		}

		@Override
		public void requestClose(boolean esc) {
			// shouldn't happen
			throw new IllegalStateException();
		}

		@Override
		public void gainFocus() {
			// shouldn't happen
			throw new IllegalStateException();
		}

		@Override
		public void loseFocus() {
			// shouldn't happen
			throw new IllegalStateException();
		}

		@Override
		public void handleError(String errorMsg, Throwable t) {
			listener.handleError(errorMsg, t);
		}

		@Override
		public void destroy() {
			destroyInThread();
		}
	}

	public void setInputSource(MyAwtPanel panel) {
		if (!panels.contains(panel)) {
			throw new IllegalArgumentException();
		}

		inputSource = panel;
		mouseInput.setInputSource(panel);
		keyInput.setInputSource(panel);
	}

	@Override
	public Type getType() {
		return Type.OffscreenSurface;
	}

	@Override
	public void setSystemListener(SystemListener listener) {
		this.listener = listener;
	}

	@Override
	public AppSettings getSettings() {
		return settings;
	}

	@Override
	public Renderer getRenderer() {
		return actualContext.getRenderer();
	}

	@Override
	public MouseInput getMouseInput() {
		return mouseInput;
	}

	@Override
	public KeyInput getKeyInput() {
		return keyInput;
	}

	@Override
	public JoyInput getJoyInput() {
		return null;
	}

	@Override
	public TouchInput getTouchInput() {
		return null;
	}

	@Override
	public Timer getTimer() {
		return actualContext.getTimer();
	}

	@Override
	public boolean isCreated() {
		return actualContext != null && actualContext.isCreated();
	}

	@Override
	public boolean isRenderable() {
		return actualContext != null && actualContext.isRenderable();
	}

	@Override
	public Context getOpenCLContext() {
		return actualContext.getOpenCLContext();
	}

	public MyAwtPanelsContext() {}

	public MyAwtPanel createPanel(PaintMode paintMode) {
		MyAwtPanel panel = new MyAwtPanel(paintMode);
		panels.add(panel);
		return panel;
	}

	public MyAwtPanel createPanel(PaintMode paintMode, boolean srgb) {
		MyAwtPanel panel = new MyAwtPanel(paintMode, srgb);
		panels.add(panel);
		return panel;
	}

	private void initInThread() {
		listener.initialize();
	}

	private void updateInThread() {
		// Check if throttle required
		boolean needThrottle = true;

		for (MyAwtPanel panel : panels) {
			if (panel.isActiveDrawing()) {
				needThrottle = false;
				break;
			}
		}

		if (lastThrottleState != needThrottle) {
			lastThrottleState = needThrottle;
		}

		if (needThrottle) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException ex) {
			}
		}

		listener.update();

		for (MyAwtPanel panel : panels) {
			panel.onFrameEnd();
		}
	}

	private void destroyInThread() {
		listener.destroy();
	}

	@Override
	public void setSettings(AppSettings settings) {
		this.settings.copyFrom(settings);
		this.settings.setRenderer(AppSettings.LWJGL_OPENGL3);
		if (actualContext != null) {
			actualContext.setSettings(settings);
		}
	}

	@Override
	public void create(boolean waitFor) {
		if (actualContext != null) {
			throw new IllegalStateException("Already created");
		}

		actualContext = JmeSystem.newContext(settings, Type.OffscreenSurface);
		actualContext.setSystemListener(new MyAwtPanelsListener());
		actualContext.create(waitFor);
	}

	@Override
	public void destroy(boolean waitFor) {
		if (actualContext == null) {
			throw new IllegalStateException("Not created");
		}

		// destroy parent context
		actualContext.destroy(waitFor);
	}

	@Override
	public void setTitle(String title) {
		actualContext.setTitle(title);
	}

	@Override
	public void setAutoFlushFrames(boolean enabled) {
		actualContext.setAutoFlushFrames(enabled);
	}

	@Override
	public void restart() {
		actualContext.restart();
	}

}

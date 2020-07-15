package de.george.g3dit.jme;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jme3.app.LostFocusBehavior;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeContext;
import com.jme3.system.JmeContext.Type;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Image.Format;

import de.george.g3dit.EditorContext;

public class EntityRenderer extends BaseEntityViewer {
	private static final Logger logger = LoggerFactory.getLogger(EntityRenderer.class);

	private FrameBuffer fb;

	public EntityRenderer(EditorContext editorContext) {
		super(editorContext);
	}

	private static EntityRenderer instance;

	public static EntityRenderer getInstance(EditorContext editorContext) {
		if (instance == null) {
			instance = new EntityRenderer(editorContext);
		}

		return instance;
	}

	@Override
	protected void assureAppRunning() {
		JmeContext context = app.getContext();
		if (context == null || !context.isCreated()) {
			AppSettings settings = new AppSettings(true);
			settings.setWidth(640);
			settings.setHeight(480);
			settings.setFullscreen(false);

			// No need to swap the glfw buffers, we are using our own frame buffer.
			settings.setSwapBuffers(false);

			// Support transparent background for screenshots
			settings.setAlphaBits(8);
			settings.setBitsPerPixel(24);
			settings.setFrameRate(60);
			settings.setSamples(4);

			app.setShowSettings(false);
			app.setLostFocusBehavior(LostFocusBehavior.Disabled);
			app.setSettings(settings);
			app.setDisplayFps(false);
			app.setDisplayStatView(false);

			ExecutorService executor = Executors.newSingleThreadExecutor();
			executor.submit(() -> app.start(Type.OffscreenSurface));
			executor.shutdown();
			app.awaitInited();
			appTask(() -> updateOffscreenFrameBuffer(settings.getWidth(), settings.getHeight()));
			// Only enabled when rendering a screenshot
			app.setPaused(true);

			applySettings();
		}
	}

	/**
	 * Main entry point, all the other methods of {@link EntityRenderer} must only be called from
	 * within the {@code sceneComposer} callback.
	 *
	 * @param outFile Path to save the rendered image to.
	 * @param format Image format of the rendered image, {@code png} or {@code jpg}.
	 * @param sceneComposer Composes the scene to be rendered.
	 * @return Whether screenshot creation was successful.
	 */
	@SafeVarargs
	public final synchronized boolean renderScene(File outFile, String format, Consumer<EntityRenderer>... sceneComposers) {
		assureAppRunning();
		for (Consumer<EntityRenderer> sceneComposer : sceneComposers) {
			sceneComposer.accept(this);
		}

		// Unpause app to render the screenshot
		app.setPaused(false);

		// Request and wait for screenshot
		CompletableFuture<Boolean> complete = new CompletableFuture<>();
		appTask(() -> app.makeScreenshot(outFile, format, complete));
		boolean success = false;
		try {
			success = complete.get();
		} catch (InterruptedException | ExecutionException e) {
			logger.warn("Waiting for screenshot failed.");
		}

		// Pause app until the next screenshot is requested
		app.setPaused(true);

		return success;
	}

	public void setResolution(int width, int height) {
		appTask(() -> {
			JmeContext context = app.getContext();
			AppSettings settings = context.getSettings();
			if (width != settings.getWidth() || height != settings.getHeight()) {
				settings.setResolution(width, height);
				// DON'T: context.restart();
				// For some reason restarting the context fucks up glScissor() or something
				// similiar. Whatever, the effect is that either the resulting render is stretched
				// or incomplete.
				// We have to provide our own offscreen frame buffer anyways, as the one provided
				// by glfw is limited to the maximum resolution of the monitor. Therefore the
				// resolution of the context (glfw window) does not matter.
				// context.restart();

				app.reshape(width, height);

				// Resize frame buffer
				updateOffscreenFrameBuffer(width, height);
			}
		});
	}

	/**
	 * Provide a custom offscreen frame buffer override, as the one provided by
	 * LwjglOffscreenSurface is limited to the maximum resolution of the monitor (implementation is
	 * based on glfw).
	 *
	 * @param width Width
	 * @param height Height
	 */
	private void updateOffscreenFrameBuffer(int width, int height) {
		if (fb != null) {
			fb.dispose();
			fb = null;
		}

		fb = new FrameBuffer(width, height, 1);
		fb.setDepthBuffer(Format.Depth);
		fb.setColorBuffer(Format.RGBA8);

		// Just assume that the buffer is not larger than the maximum supported by the gpu.
		app.getRenderManager().getRenderer().setMainFrameBufferOverride(fb);
	}
}

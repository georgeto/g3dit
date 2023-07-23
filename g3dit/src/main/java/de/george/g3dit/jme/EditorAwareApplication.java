package de.george.g3dit.jme;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.scene.Spatial.CullHint;

import de.george.g3dit.jme.asset.SwitchedMaterialKey;
import de.george.g3dit.jme.asset.SwitchedModelKey;

public abstract class EditorAwareApplication extends SimpleApplication {
	private CountDownLatch appIsInited = new CountDownLatch(1);

	protected DirectionalLight directionalLight;
	protected AmbientLight ambientLight;

	protected ExtScreenshotAppState screenShotState;

	private BitmapText stateText;
	private boolean showStateText;

	public EditorAwareApplication(AssetManager assetManager) {
		this.assetManager = assetManager;
	}

	@Override
	public void initialize() {
		super.initialize();

		directionalLight = new DirectionalLight(new Vector3f(0f, -0.7f, -1.0f).normalizeLocal(), ColorRGBA.White);
		rootNode.addLight(directionalLight);
		ambientLight = new AmbientLight(new ColorRGBA(0.6f, 0.6f, 0.6f, 1.0f));
		rootNode.addLight(ambientLight);

		screenShotState = new ExtScreenshotAppState();
		inputManager.addMapping("ScreenShot", new KeyTrigger(KeyInput.KEY_F1));
		stateManager.attach(screenShotState);

		stateText = new BitmapText(guiFont);
		stateText.setCullHint(CullHint.Always);
		guiNode.attachChild(stateText);
		showStateText = false;
		inputManager.addMapping("Toggle Info", new KeyTrigger(KeyInput.KEY_F2));
		inputManager.addListener((ActionListener) (name, isPressed, tpf) -> {
			if (isPressed) {
				showStateText = !showStateText;
				stateText.setCullHint(showStateText ? CullHint.Never : CullHint.Always);
			}
		}, "Toggle Info");

		appIsInited.countDown();
	}

	public void awaitInited() {
		try {
			appIsInited.await();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean isPaused() {
		return paused;
	}

	public void setPaused(boolean paused) {
		this.paused = paused;
	}

	public Spatial loadModel(String name, int materialSwitch) {
		return assetManager.loadModel(new SwitchedModelKey(name, materialSwitch));
	}

	public Material loadMaterial(String name, int materialSwitch) {
		return assetManager.loadAsset(new SwitchedMaterialKey(name, materialSwitch));
	}

	public void setAmbientLightColor(ColorRGBA color) {
		ambientLight.setColor(color);
	}

	public void setLightDirection(float inclination, float azimuth) {
		// Convert spherical coordinates to cartesian coordinates
		Vector3f directionalLightDirection = new Vector3f(FastMath.cos(inclination) * FastMath.cos(azimuth), FastMath.sin(inclination),
				FastMath.cos(inclination) * FastMath.sin(azimuth)).negateLocal();
		directionalLight.setDirection(directionalLightDirection);
	}

	public void setLightColor(ColorRGBA color) {
		directionalLight.setColor(color);
	}

	public void setScreenhotFilePath(Path screenshotFolder) {
		screenShotState.setFilePath(screenshotFolder);
	}

	public void makeScreenshot(Path outFile) {
		makeScreenshot(outFile, "png", null);
	}

	public void makeScreenshot(Path outFile, String format, CompletableFuture<Boolean> complete) {
		screenShotState.takeScreenshot(outFile, format, complete);
	}

	@Override
	public void update() {
		super.update();
		List<String> stateTextLines = getStateText();
		stateText.setLocalTranslation(0, stateText.getLineHeight() * stateTextLines.size(), 0);
		stateText.setText(String.join("\n", stateTextLines));
	}

	protected List<String> getStateText() {
		return Collections.emptyList();
	}
}

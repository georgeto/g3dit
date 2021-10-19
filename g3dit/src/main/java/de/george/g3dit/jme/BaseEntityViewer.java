package de.george.g3dit.jme;

import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.scene.Spatial;

import de.george.g3dit.EditorContext;
import de.george.g3dit.settings.EditorOptions;
import de.george.g3dit.settings.SettingsUpdatedEvent;
import de.george.g3utils.util.Pair;
import de.george.lrentnode.archive.G3ClassContainer;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.util.EntityUtil;
import de.george.lrentnode.util.NPCUtil;

public abstract class BaseEntityViewer {
	private static final Logger logger = LoggerFactory.getLogger(BaseEntityViewer.class);

	protected final EntityViewerApp app;
	protected final EditorContext editorContext;

	public BaseEntityViewer(EditorContext editorContext) {
		this.editorContext = editorContext;
		editorContext.eventBus().register(this);

		app = new EntityViewerApp(AssetManagerProvider.getAssetManager(editorContext));
	}

	@Subscribe
	public void onSettingsUpdated(SettingsUpdatedEvent event) {
		applySettings();
	}

	protected abstract void assureAppRunning();

	public void applySettings() {
		ColorRGBA backgroundColor = new ColorRGBA();
		backgroundColor.fromIntARGB(editorContext.getOptionStore().get(EditorOptions.D3View.BACKGROUND_COLOR).getRGB());
		float ambientLightIntensity = editorContext.getOptionStore().get(EditorOptions.D3View.AMBIENT_LIGHT_INTENSITY);
		float directionalLightIntensity = editorContext.getOptionStore().get(EditorOptions.D3View.DIRECTIONAL_LIGHT_INTENSITY);
		float directionalLightInclination = editorContext.getOptionStore().get(EditorOptions.D3View.DIRECTIONAL_LIGHT_INCLINATION)
				* FastMath.DEG_TO_RAD;
		float directionalLightAzimuth = editorContext.getOptionStore().get(EditorOptions.D3View.DIRECTIONAL_LIGHT_AZIMUTH)
				* FastMath.DEG_TO_RAD;
		float horizontalRotation = editorContext.getOptionStore().get(EditorOptions.D3View.HORIZONTAL_ROTATION);
		float verticalRotation = editorContext.getOptionStore().get(EditorOptions.D3View.VERTICAL_ROTATION);
		float distance = editorContext.getOptionStore().get(EditorOptions.D3View.DISTANCE);
		String screenshotFolder = editorContext.getOptionStore().get(EditorOptions.D3View.SCREENSHOT_FOLDER);
		appTask(() -> {
			app.getViewPort().setBackgroundColor(backgroundColor);
			app.setAmbientLightColor(new ColorRGBA(ambientLightIntensity, ambientLightIntensity, ambientLightIntensity, 1.0f));
			app.setLightDirection(directionalLightInclination, directionalLightAzimuth);
			app.setLightColor(new ColorRGBA(directionalLightIntensity, directionalLightIntensity, directionalLightIntensity, 1.0f));
			app.setScreenhotFilePath(screenshotFolder);
			app.setHorizontalRotation(horizontalRotation * FastMath.DEG_TO_RAD);
			app.setVerticalRotation(verticalRotation * FastMath.DEG_TO_RAD);
			app.setRelativeDistance(distance);
		});
	}

	public void showContainer(G3ClassContainer entity) {
		assureAppRunning();
		appTask(app::clearEntities);
		addContainer(entity);
	}

	public void showEntity(eCEntity entity) {
		assureAppRunning();
		appTask(app::clearEntities);
		addContainer(entity);
		if (NPCUtil.isNPC(entity)) {
			for (eCEntity wearable : entity.getChilds()) {
				addContainer(wearable);
			}
		}
	}

	public void showMesh(String mesh, int materialSwitch) {
		assureAppRunning();
		appTask(app::clearEntities);
		addMesh(mesh, materialSwitch);
	}

	private void addContainer(G3ClassContainer container) {
		Pair<String, Integer> meshAndMaterialSwitch = EntityUtil.getMeshAndMaterialSwitch(container).orElse(null);
		if (meshAndMaterialSwitch != null) {
			int materialSwitch = meshAndMaterialSwitch.el1();
			String mesh = EntityUtil.cleanAnimatedMeshName(meshAndMaterialSwitch.el0());
			addMesh(mesh, materialSwitch);
		}
	}

	private void addMesh(String mesh, int materialSwitch) {
		if (mesh.toLowerCase().endsWith(".xcmsh") || mesh.toLowerCase().endsWith(".xlmsh") || mesh.endsWith(".xact")) {
			appTask(() -> {
				Spatial entityMesh = app.loadModel(mesh, materialSwitch);
				app.addEntity(entityMesh);
			});
		}
	}

	public void appTask(Consumer<EntityViewerApp> task) {
		app.enqueue(() -> {
			task.accept(app);
			return null;
		});
	}

	protected void appTask(Runnable runnable) {
		app.enqueue(() -> {
			runnable.run();
			return null;
		});
	}

	protected void waitForAppTask(Runnable runnable) {
		try {
			app.enqueue(() -> {
				runnable.run();
				return null;
			}).get();
		} catch (InterruptedException | ExecutionException e) {
			logger.warn("Waiting for app task failed.", e);
		}
	}

}

package de.george.g3dit.jme;

import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.scene.Spatial;
import com.jme3.system.JmeContext;

import de.george.g3dit.EditorContext;
import de.george.g3dit.settings.EditorOptions;
import de.george.g3utils.structure.bCMatrix;
import de.george.g3utils.structure.bCQuaternion;
import de.george.g3utils.structure.bCVector;
import de.george.g3utils.util.Pair;
import de.george.lrentnode.archive.G3ClassContainer;
import de.george.lrentnode.util.EntityUtil;

public class NodeViewer {
	private NodeViewerApp app;
	private EditorContext editorContext;

	public NodeViewer(EditorContext editorContext) {
		this.editorContext = editorContext;
		app = new NodeViewerApp(AssetManagerProvider.getAssetManager(editorContext));
	}

	private static NodeViewer instance;
	private JmeAppFrame<NodeViewerApp> viewerFrame;

	public static NodeViewer getInstance(EditorContext editorContext) {
		if (instance == null) {
			instance = new NodeViewer(editorContext);
		}

		return instance;
	}

	protected void assureAppRunning() {
		JmeContext context = app.getContext();
		if (context == null || !context.isCreated()) {
			if (viewerFrame == null) {
				viewerFrame = new JmeAppFrame<>();
				viewerFrame.initApp(app);
				viewerFrame.startApp();
				applySettings();
			}

			if (viewerFrame.getFrame() == null) {
				// viewerFrame.setMenuBar(new EntityViewerMainMenu(this, editorContext));
				viewerFrame.createFrame();
			}
		} else {
			viewerFrame.show();
		}
	}

	public void applySettings() {
		ColorRGBA backgroundColor = new ColorRGBA();
		backgroundColor.fromIntARGB(editorContext.getOptionStore().get(EditorOptions.D3View.BACKGROUND_COLOR).getRGB());
		float ambientLightIntensity = editorContext.getOptionStore().get(EditorOptions.D3View.AMBIENT_LIGHT_INTENSITY);
		float directionalLightIntensity = editorContext.getOptionStore().get(EditorOptions.D3View.DIRECTIONAL_LIGHT_INTENSITY);
		float directionalLightInclination = editorContext.getOptionStore().get(EditorOptions.D3View.DIRECTIONAL_LIGHT_INCLINATION)
				* FastMath.DEG_TO_RAD;
		float directionalLightAzimuth = editorContext.getOptionStore().get(EditorOptions.D3View.DIRECTIONAL_LIGHT_AZIMUTH)
				* FastMath.DEG_TO_RAD;

		String screenshotFolder = editorContext.getOptionStore().get(EditorOptions.D3View.SCREENSHOT_FOLDER);
		appTask(() -> {
			app.getViewPort().setBackgroundColor(backgroundColor);
			app.setAmbientLightColor(new ColorRGBA(ambientLightIntensity, ambientLightIntensity, ambientLightIntensity, 1.0f));
			app.setLightDirection(directionalLightInclination, directionalLightAzimuth);
			app.setLightColor(new ColorRGBA(directionalLightIntensity, directionalLightIntensity, directionalLightIntensity, 1.0f));
			app.setScreenhotFilePath(screenshotFolder);
		});
	}

	public void addContainer(G3ClassContainer container, bCMatrix worldMatrix) {
		assureAppRunning();
		Pair<String, Integer> meshAndMaterialSwitch = EntityUtil.getMeshAndMaterialSwitch(container).orElse(null);
		if (meshAndMaterialSwitch != null) {
			int materialSwitch = meshAndMaterialSwitch.el1();
			String mesh = EntityUtil.cleanAnimatedMeshName(meshAndMaterialSwitch.el0());
			addMesh(mesh, materialSwitch, worldMatrix);
		}
	}

	private void addMesh(String mesh, int materialSwitch, bCMatrix worldMatrix) {
		if (mesh.toLowerCase().endsWith(".xcmsh") || mesh.toLowerCase().endsWith(".xlmsh") || mesh.endsWith(".xact")) {
			appTask(() -> {
				Spatial entityMesh = app.loadModel(mesh, materialSwitch);

				bCVector translation = worldMatrix.getTranslation();
				entityMesh.setLocalTranslation(translation.getX(), translation.getY(), translation.getZ());
				bCQuaternion worldRotation = new bCQuaternion(worldMatrix);
				entityMesh.setLocalRotation(
						new Quaternion(worldRotation.getX(), worldRotation.getY(), worldRotation.getZ(), worldRotation.getW()));
				bCVector scaling = worldMatrix.getPureScaling();
				entityMesh.setLocalScale(scaling.getX(), scaling.getY(), scaling.getZ());

				app.getEntitiesNode().attachChild(entityMesh);
			});
		}
	}

	public void centerCamera() {
		appTask(app::centerCamera);
	}

	protected void appTask(Runnable runnable) {
		app.enqueue(() -> {
			runnable.run();
			return null;
		});
	}
}

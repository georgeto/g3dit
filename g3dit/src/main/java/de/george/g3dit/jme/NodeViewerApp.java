package de.george.g3dit.jme;

import com.jme3.asset.AssetManager;
import com.jme3.bounding.BoundingBox;
import com.jme3.collision.CollisionResults;
import com.jme3.input.ChaseCamera;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.debug.WireBox;

/**
 * Sample 2 - How to use nodes as handles to manipulate objects in the scene. You can rotate,
 * translate, and scale objects by manipulating their parent nodes. The Root Node is special: Only
 * what is attached to the Root Node appears in the scene.
 */
public class NodeViewerApp extends EditorAwareApplication {
	private Node entitiesNode;

	private ChaseCamera pivotCam;

	private boolean pickModeEnabled = false;
	private Spatial selectedEntity;
	private Geometry boundingBoxGeometry;

	public NodeViewerApp(AssetManager assetManager) {
		super(assetManager);
	}

	@Override
	public void simpleInitApp() {
		initCameras();
		initKeys();

		rootNode.scale(1f, 1f, -1f);
		entitiesNode = new Node("entities");
		rootNode.attachChild(entitiesNode);
	}

	private void initCameras() {
		getCamera().setFrustumFar(111111f);

		// Init fly cam
		getFlyByCamera().setDragToRotate(true);
		getFlyByCamera().setMoveSpeed(9000f);

		// Init pivot cam
		pivotCam = new ChaseCamera(cam, inputManager);
		pivotCam.setDefaultDistance(1000f);
		pivotCam.setMaxDistance(10000f);
		pivotCam.setZoomSensitivity(100f);
		pivotCam.setMinVerticalRotation(-FastMath.HALF_PI);
		pivotCam.setMaxVerticalRotation(FastMath.HALF_PI);
		pivotCam.setEnabled(false);
	}

	private void initKeys() {
		inputManager.addMapping("TogglePickMode", new KeyTrigger(KeyInput.KEY_LSHIFT));
		inputManager.addMapping("ToggleCamera", new KeyTrigger(KeyInput.KEY_C));
		inputManager.addListener(actionListener, "TogglePickMode", "ToggleCamera");

		inputManager.addMapping("PickTarget", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
		inputManager.addListener(analogListener, "PickTarget");
	}

	private ActionListener actionListener = (name, keyPressed, tpf) -> {
		if (name.equals("TogglePickMode")) {
			pickModeEnabled = keyPressed;
			getFlyByCamera().setEnabled(!pickModeEnabled);
		} else if (name.equals("ToggleCamera") && !keyPressed) {
			if (!pickModeEnabled && selectedEntity != null) {
				boolean pivotCamEnabled = flyCam.isEnabled();
				flyCam.setEnabled(!pivotCamEnabled);
				pivotCam.setEnabled(pivotCamEnabled);
				selectedEntity.addControl(pivotCam);
			}
		}
	};

	private AnalogListener analogListener = (name, intensity, tpf) -> {
		if (name.equals("PickTarget") && pickModeEnabled) {
			// Reset results list.
			CollisionResults results = new CollisionResults();
			// Convert screen click to 3d position
			Vector2f click2d = inputManager.getCursorPosition();
			Vector3f click3d = cam.getWorldCoordinates(new Vector2f(click2d.x, click2d.y), 0f).clone();
			Vector3f dir = cam.getWorldCoordinates(new Vector2f(click2d.x, click2d.y), 1f).subtractLocal(click3d).normalizeLocal();
			// Aim the ray from the clicked spot forwards.
			Ray ray = new Ray(click3d, dir);
			// Collect intersections between ray and all nodes in results list.
			entitiesNode.collideWith(ray, results);
			if (results.size() > 0) {
				// The closest result is the target that the player picked:
				Geometry target2 = results.getClosestCollision().getGeometry();
				Node parent = target2.getParent();
				if (parent != entitiesNode) {
					updateSelectedEntity(parent);
				} else {
					updateSelectedEntity(target2);
				}

			}
		} // else if ...
	};

	private void updateSelectedEntity(Spatial selectedEntity) {
		this.selectedEntity = selectedEntity;

		// Remove existing bounding box
		if (boundingBoxGeometry != null) {
			rootNode.detachChild(boundingBoxGeometry);
		}

		// Create bounding box
		BoundingBox boundingBox = (BoundingBox) selectedEntity.getWorldBound();
		boundingBoxGeometry = WireBox.makeGeometry(boundingBox);
		Material boundingBoxMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		boundingBoxMaterial.setColor("Color", ColorRGBA.Green);
		boundingBoxGeometry.setMaterial(boundingBoxMaterial);

		// Relative to transform of root node
		boundingBoxGeometry.getLocalTransform().combineWithParent(rootNode.getWorldTransform());

		// Attach bounding box
		rootNode.attachChild(boundingBoxGeometry);
	}

	public void centerCamera() {
		BoundingBox worldBound = (BoundingBox) entitiesNode.getWorldBound();
		getCamera().setLocation(worldBound.getCenter());
	}

	public Node getEntitiesNode() {
		return entitiesNode;
	}
}

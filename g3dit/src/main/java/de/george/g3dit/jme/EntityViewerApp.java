package de.george.g3dit.jme;

import com.jme3.asset.AssetManager;
import com.jme3.bounding.BoundingBox;
import com.jme3.input.ChaseCamera;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

public class EntityViewerApp extends EditorAwareApplication implements ActionListener {
	private Node entitiesNode;

	private ChaseCamera pivotCam;

	private float defaultHorizontalRotation = 0.0f;
	private float defaultVerticalRotation = 0.0f;
	private float defaultRelativeDistance = 4.0f;
	private float defaultAbsoluteDistance = Float.NaN;

	public EntityViewerApp(AssetManager assetManager) {
		super(assetManager);
	}

	@Override
	public void simpleInitApp() {
		// see AbstractG3MeshLoader#isLeftHanded()
		rootNode.scale(1f, 1f, -1f);
		entitiesNode = new Node("entities");
		rootNode.attachChild(entitiesNode);

		initCameras();
		initKeys();
	}

	private void initCameras() {
		getCamera().setFrustumFar(111111f);

		// Disable fly cam
		getFlyByCamera().setDragToRotate(true);
		getFlyByCamera().setEnabled(false);

		// Init pivot cam
		pivotCam = new ChaseCamera(cam, inputManager);
		pivotCam.setDefaultHorizontalRotation(defaultHorizontalRotation);
		pivotCam.setMinVerticalRotation(-FastMath.HALF_PI + 0.01f * FastMath.DEG_TO_RAD);
		pivotCam.setMaxVerticalRotation(FastMath.HALF_PI - 0.01f * FastMath.DEG_TO_RAD);
		pivotCam.setDefaultVerticalRotation(defaultVerticalRotation);
		entitiesNode.addControl(pivotCam);
	}

	private void initKeys() {
		inputManager.addMapping("Reset Camera", new KeyTrigger(KeyInput.KEY_D));
		inputManager.addListener(this, "Reset Camera");
	}

	@Override
	public void onAction(String name, boolean isPressed, float tpf) {
		switch (name) {
			case "Reset Camera":
				if (!isPressed) {
					pivotCam.setDefaultHorizontalRotation(defaultHorizontalRotation);
					pivotCam.setDefaultVerticalRotation(defaultVerticalRotation);
					pivotCam.setDefaultDistance(pivotCam.getMaxDistance() / 80.0f * defaultRelativeDistance);
				}

				break;
		}
	}

	public void clearEntities() {
		entitiesNode.detachAllChildren();
	}

	public void addEntity(Spatial entity) {
		entitiesNode.attachChild(entity);

		BoundingBox worldBound = (BoundingBox) entitiesNode.getWorldBound();
		if (worldBound != null) {
			Vector3f lookAtOffset = worldBound.getCenter().subtract(entitiesNode.getWorldTranslation());
			pivotCam.setLookAtOffset(lookAtOffset);

			if (Float.isNaN(defaultAbsoluteDistance)) {
				float extent = worldBound.getExtent(null).length();
				pivotCam.setDefaultDistance(extent * defaultRelativeDistance);
				pivotCam.setMaxDistance(extent * 80f);
				pivotCam.setZoomSensitivity(extent * 0.5f);
			}
		}
	}

	public void setRelativeDistance(float distance) {
		defaultAbsoluteDistance = Float.NaN;
		defaultRelativeDistance = distance;
		pivotCam.setDefaultDistance(pivotCam.getMaxDistance() / 80.0f * defaultRelativeDistance);
	}

	public void setAbsoluteDistance(float distance) {
		defaultRelativeDistance = Float.NaN;
		defaultAbsoluteDistance = distance;
		pivotCam.setMaxDistance(defaultAbsoluteDistance * 2);
		pivotCam.setDefaultDistance(defaultAbsoluteDistance);
	}

	public void setHorizontalRotation(float rotation) {
		defaultHorizontalRotation = rotation;
		pivotCam.setDefaultHorizontalRotation(rotation);
	}

	public void setVerticalRotation(float rotation) {
		defaultVerticalRotation = Math.min(pivotCam.getMaxVerticalRotation(), Math.max(pivotCam.getMinVerticalRotation(), rotation));
		pivotCam.setDefaultVerticalRotation(defaultVerticalRotation);
	}

	public void setHorizontalRotationDeg(float rotation) {
		setHorizontalRotation(rotation * FastMath.DEG_TO_RAD);
	}

	public void setVerticalRotationDeg(float rotation) {
		setVerticalRotation(rotation * FastMath.DEG_TO_RAD);
	}
}

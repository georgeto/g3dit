package de.george.g3dit.jme;

import java.util.Arrays;
import java.util.List;

import com.jme3.asset.AssetManager;
import com.jme3.bounding.BoundingBox;
import com.jme3.input.ChaseCamera;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

public class EntityViewerApp extends EditorAwareApplication implements ActionListener {
	private static final float RELATIVE_MAX_DISTANCE = 80.0f;

	private Node entitiesNode;

	private ChaseCamera pivotCam;

	private float defaultHorizontalRotation = 0.0f;
	private float defaultVerticalRotation = 0.0f;
	private float defaultRelativeDistance = 4.0f;
	private float defaultAbsoluteDistance = Float.NaN;

	private boolean invertRotation = false;

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
		flyCam.setDragToRotate(true);
		flyCam.setMoveSpeed(400.0f);
		flyCam.setEnabled(false);

		// Init pivot cam
		pivotCam = new ChaseCamera(cam, inputManager);
		pivotCam.setDefaultHorizontalRotation(defaultHorizontalRotation);
		pivotCam.setMinVerticalRotation(-FastMath.HALF_PI + 0.01f * FastMath.DEG_TO_RAD);
		pivotCam.setMaxVerticalRotation(FastMath.HALF_PI - 0.01f * FastMath.DEG_TO_RAD);
		pivotCam.setDefaultVerticalRotation(defaultVerticalRotation);
		entitiesNode.addControl(pivotCam);
	}

	private void initKeys() {
		inputManager.addMapping("Reset Camera", new KeyTrigger(KeyInput.KEY_R));
		inputManager.addMapping("Toggle Camera", new KeyTrigger(KeyInput.KEY_C));
		inputManager.addMapping("Rotate X", new KeyTrigger(KeyInput.KEY_F));
		inputManager.addMapping("Rotate Y", new KeyTrigger(KeyInput.KEY_G));
		inputManager.addMapping("Rotate Z", new KeyTrigger(KeyInput.KEY_H));
		inputManager.addMapping("Reset Rotation", new KeyTrigger(KeyInput.KEY_J));
		inputManager.addMapping("Invert Rotation", new KeyTrigger(KeyInput.KEY_LSHIFT));
		inputManager.addListener(this, "Reset Camera", "Toggle Camera", "Rotate X", "Rotate Y", "Rotate Z", "Reset Rotation",
				"Invert Rotation");
	}

	@Override
	public void onAction(String name, boolean isPressed, float tpf) {
		if (name.equals("Invert Rotation")) {
			invertRotation = isPressed;
			return;
		} else if (isPressed) {
			return;
		}

		switch (name) {
			case "Reset Camera":
				pivotCam.setDefaultHorizontalRotation(defaultHorizontalRotation);
				pivotCam.setDefaultVerticalRotation(defaultVerticalRotation);
				pivotCam.setDefaultDistance(pivotCam.getMaxDistance() / RELATIVE_MAX_DISTANCE * defaultRelativeDistance);
				break;

			case "Toggle Camera":
				boolean flyCamEnabled = flyCam.isEnabled();
				flyCam.setEnabled(!flyCamEnabled);
				pivotCam.setEnabled(flyCamEnabled);
				break;

			case "Rotate X":
				entitiesNode.rotate(getRotationStep(), 0, 0);
				break;

			case "Rotate Y":
				entitiesNode.rotate(0, getRotationStep(), 0);
				break;

			case "Rotate Z":
				entitiesNode.rotate(0, 0, getRotationStep());
				break;

			case "Reset Rotation":
				setObjectRotation(0, 0, 0);
				break;
		}
	}

	private float getRotationStep() {
		return (invertRotation ? -1 : 1) * 10.0f * FastMath.DEG_TO_RAD;
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
				pivotCam.setMaxDistance(extent * RELATIVE_MAX_DISTANCE);
				pivotCam.setZoomSensitivity(extent * 0.5f);
			}
		}
	}

	public void setRelativeDistance(float distance) {
		defaultAbsoluteDistance = Float.NaN;
		defaultRelativeDistance = distance;
		pivotCam.setDefaultDistance(pivotCam.getMaxDistance() / RELATIVE_MAX_DISTANCE * defaultRelativeDistance);
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

	public void setObjectRotation(float x, float y, float z) {
		entitiesNode.setLocalRotation(new Quaternion().fromAngles(x, y, z));
	}

	public void setObjectRotationDeg(float x, float y, float z) {
		setObjectRotation(x * FastMath.DEG_TO_RAD, y * FastMath.DEG_TO_RAD, z * FastMath.DEG_TO_RAD);
	}

	@Override
	protected List<String> getStateText() {
		float[] objectRotation = new float[3];
		entitiesNode.getLocalRotation().toAngles(objectRotation);

		String cameraDistanceType;
		float cameraDistance;
		if (Float.isNaN(defaultAbsoluteDistance)) {
			cameraDistanceType = "relative";
			cameraDistance = pivotCam.getDistanceToTarget() / (pivotCam.getMaxDistance() / RELATIVE_MAX_DISTANCE);
		} else {
			cameraDistanceType = "absolute";
			cameraDistance = pivotCam.getDistanceToTarget();
		}

		return Arrays.asList(
				String.format("Camera rotation: %.2f / %.2f", pivotCam.getHorizontalRotation() * FastMath.RAD_TO_DEG,
						pivotCam.getVerticalRotation() * FastMath.RAD_TO_DEG),
				String.format("Camera distance: %.2f (%s)", cameraDistance, cameraDistanceType),
				String.format("Object rotation: %.2f / %.2f / %.2f", objectRotation[0] * FastMath.RAD_TO_DEG,
						objectRotation[1] * FastMath.RAD_TO_DEG, objectRotation[2] * FastMath.RAD_TO_DEG));
	}
}

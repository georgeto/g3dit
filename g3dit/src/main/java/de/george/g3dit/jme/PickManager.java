package de.george.g3dit.jme;

import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Spatial;

import de.george.g3dit.jme.SceneComposerToolController.TransformationType;

public interface PickManager {
	public static final Quaternion PLANE_XY = new Quaternion().fromAngleAxis(0, new Vector3f(1, 0, 0));
	public static final Quaternion PLANE_YZ = new Quaternion().fromAngleAxis(-FastMath.PI / 2, new Vector3f(0, 1, 0));// YAW090
	public static final Quaternion PLANE_XZ = new Quaternion().fromAngleAxis(FastMath.PI / 2, new Vector3f(1, 0, 0)); // PITCH090

	void reset();

	void initiatePick(Spatial selectedSpatial, Quaternion planeXy, TransformationType transformType, Camera camera, Vector2f screenCoord);

	boolean updatePick(Camera camera, Vector2f screenCoord);

	Vector3f getTranslation();

	Vector3f getTranslation(Vector3f constraintAxis);

}

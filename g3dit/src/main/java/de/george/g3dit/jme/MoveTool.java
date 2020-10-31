/*
 * To change this template, choose Tools | Templates and open the template in the editor.
 */
package de.george.g3dit.jme;

import com.jme3.asset.AssetManager;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

import de.george.g3dit.jme.undoredo.AbstractUndoableSceneEdit;

/**
 * Move an object. When created, it generates a quad that will lie along a plane that the user
 * selects for moving on. When the mouse is over the axisMarker, it will highlight the plane that it
 * is over: XY,XZ,YZ. When clicked and then dragged, the selected object will move along that plane.
 *
 * @author Brent Owens
 */
public class MoveTool extends SceneEditTool {

	private Vector3f pickedMarker;
	private Vector3f constraintAxis; // used for one axis move
	private boolean wasDragging = false;
	private Vector3f startPosition;
	private Vector3f lastPosition;
	private PickManager pickManager;

	public MoveTool() {
		axisPickType = SceneEditTool.AxisMarkerPickType.axisAndPlane;
		setOverrideCameraControl(true);

	}

	@Override
	public void activate(AssetManager manager, Node toolNode, Node onTopToolNode, Spatial selectedSpatial,
			SceneComposerToolController toolController) {
		super.activate(manager, toolNode, onTopToolNode, selectedSpatial, toolController);
		pickManager = toolController.getPickManager();
		displayPlanes();
		displayCones();
	}

	@Override
	public void actionPrimary(Vector2f screenCoord, boolean pressed, Node rootNode, Object dataObject) {
		if (!pressed) {
			setDefaultAxisMarkerColors();
			pickedMarker = null; // mouse released, reset selection
			constraintAxis = Vector3f.UNIT_XYZ; // no constraint
			if (wasDragging) {
				actionPerformed(new MoveUndo(toolController.getSelectedSpatial(), startPosition, lastPosition));
				wasDragging = false;
			}
			pickManager.reset();
		} else {
			if (toolController.getSelectedSpatial() == null) {
				return;
			}

			if (pickedMarker == null) {
				pickedMarker = pickAxisMarker(camera, screenCoord, axisPickType);
				if (pickedMarker == null) {
					return;
				}

				if (pickedMarker.equals(QUAD_XY)) {
					pickManager.initiatePick(toolController.getSelectedSpatial(), PickManager.PLANE_XY, getTransformType(), camera,
							screenCoord);
				} else if (pickedMarker.equals(QUAD_XZ)) {
					pickManager.initiatePick(toolController.getSelectedSpatial(), PickManager.PLANE_XZ, getTransformType(), camera,
							screenCoord);
				} else if (pickedMarker.equals(QUAD_YZ)) {
					pickManager.initiatePick(toolController.getSelectedSpatial(), PickManager.PLANE_YZ, getTransformType(), camera,
							screenCoord);
				} else if (pickedMarker.equals(ARROW_X)) {
					pickManager.initiatePick(toolController.getSelectedSpatial(), PickManager.PLANE_XY, getTransformType(), camera,
							screenCoord);
					constraintAxis = Vector3f.UNIT_X; // move only X
				} else if (pickedMarker.equals(ARROW_Y)) {
					pickManager.initiatePick(toolController.getSelectedSpatial(), PickManager.PLANE_YZ, getTransformType(), camera,
							screenCoord);
					constraintAxis = Vector3f.UNIT_Y; // move only Y
				} else if (pickedMarker.equals(ARROW_Z)) {
					pickManager.initiatePick(toolController.getSelectedSpatial(), PickManager.PLANE_XZ, getTransformType(), camera,
							screenCoord);
					constraintAxis = Vector3f.UNIT_Z; // move only Z
				}
				startPosition = toolController.getSelectedSpatial().getLocalTranslation().clone();
				wasDragging = true;
			}
		}
	}

	@Override
	public void actionSecondary(Vector2f screenCoord, boolean pressed, Node rootNode, Object dataObject) {
		if (pressed) {
			cancel();
		}
	}

	@Override
	public void mouseMoved(Vector2f screenCoord, Node rootNode, Object currentDataObject) {

		if (pickedMarker == null) {
			highlightAxisMarker(camera, screenCoord, axisPickType);
		} else {
			pickedMarker = null;
			pickManager.reset();
		}
	}

	@Override
	public void draggedPrimary(Vector2f screenCoord, boolean pressed, Node rootNode, Object currentDataObject) {
		if (!pressed) {
			setDefaultAxisMarkerColors();
			pickedMarker = null; // mouse released, reset selection
			constraintAxis = Vector3f.UNIT_XYZ; // no constraint
			if (wasDragging) {
				actionPerformed(new MoveUndo(toolController.getSelectedSpatial(), startPosition, lastPosition));
				wasDragging = false;
			}
			pickManager.reset();
		} else if (wasDragging == true) {
			if (!pickManager.updatePick(camera, screenCoord)) {
				return;
			}
			Vector3f diff = Vector3f.ZERO;
			if (pickedMarker.equals(QUAD_XY) || pickedMarker.equals(QUAD_XZ) || pickedMarker.equals(QUAD_YZ)) {
				diff = pickManager.getTranslation();

			} else if (pickedMarker.equals(ARROW_X) || pickedMarker.equals(ARROW_Y) || pickedMarker.equals(ARROW_Z)) {
				diff = pickManager.getTranslation(constraintAxis);
			}
			Vector3f position;
			Spatial parent = toolController.getSelectedSpatial().getParent();
			if (parent != null) {
				position = startPosition.add(parent.getWorldRotation().inverse().mult(diff));
			} else {
				position = startPosition.add(diff);
			}
			lastPosition = position;
			toolController.getSelectedSpatial().setLocalTranslation(position);
			updateToolsTransformation();
		}
	}

	@Override
	public void draggedSecondary(Vector2f screenCoord, boolean pressed, Node rootNode, Object currentDataObject) {
		if (pressed) {
			cancel();
		}
	}

	private void cancel() {
		if (wasDragging) {
			wasDragging = false;
			toolController.getSelectedSpatial().setLocalTranslation(startPosition);
			setDefaultAxisMarkerColors();
			pickedMarker = null; // mouse released, reset selection
			constraintAxis = Vector3f.UNIT_XYZ; // no constraint
			pickManager.reset();
		}
	}

	protected class MoveUndo extends AbstractUndoableSceneEdit {

		private Spatial spatial;
		private Vector3f before = new Vector3f(), after = new Vector3f();

		MoveUndo(Spatial spatial, Vector3f before, Vector3f after) {
			this.spatial = spatial;
			this.before.set(before);
			if (after != null) {
				this.after.set(after);
			}
		}

		@Override
		public void sceneUndo() {
			spatial.setLocalTranslation(before);
		}

		@Override
		public void sceneRedo() {
			spatial.setLocalTranslation(after);
		}

		public void setAfter(Vector3f after) {
			this.after.set(after);
		}
	}
}

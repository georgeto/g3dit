/*
 * To change this template, choose Tools | Templates and open the template in the editor.
 */
package de.george.g3dit.jme;

import org.openide.util.Lookup;

import com.jme3.asset.AssetManager;
import com.jme3.input.event.KeyInputEvent;
import com.jme3.math.Vector2f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

/**
 * @author Brent Owens
 */
public class SceneComposerToolController extends SceneToolController {

	private ShortcutManager scm;

	private Node rootNode;
	private SceneEditTool editTool;
	private SceneEditorController editorController;
	private ViewPort overlayView;
	private Node onTopToolsNode;

	private boolean snapToGrid = false;
	private boolean snapToScene = false;
	private boolean selectTerrain = false;
	private boolean selectGeometries = false;
	private TransformationType transformationType = TransformationType.local;

	public enum TransformationType {
		local,
		global,
		camera
	}

	public SceneComposerToolController(NodeViewerApp app, final Node toolsNode, AssetManager manager, Node rootNode) {
		super(app, toolsNode, manager);
		this.rootNode = rootNode;
		setShowGrid(showGrid);
		scm = app.getShortcutManager();
	}

	public SceneComposerToolController(NodeViewerApp app, AssetManager manager) {
		super(app, manager);
		scm = app.getShortcutManager();
	}

	public void setEditorController(SceneEditorController editorController) {
		this.editorController = editorController;
	}

	public void createOnTopToolNode() {
		// a node in a viewport that will always render on top
		onTopToolsNode = new Node("OverlayNode");
		overlayView = app.getOverlayView();
		app.enqueue(() -> {
			overlayView.attachScene(onTopToolsNode);
			return null;
		});
	}

	@Override
	public void cleanup() {
		super.cleanup();
		editorController = null;
		app.enqueue(() -> {
			overlayView.detachScene(onTopToolsNode);
			onTopToolsNode.detachAllChildren();
			return null;
		});
	}

	@Override
	public void update(float tpf) {
		super.update(tpf);
		if (editTool != null) {
			editTool.doUpdateToolsTransformation();
		}
		if (onTopToolsNode != null) {
			onTopToolsNode.updateLogicalState(tpf);
			onTopToolsNode.updateGeometricState();
		}
	}

	@Override
	public void render(RenderManager rm) {
		super.render(rm);
	}

	public boolean isEditToolEnabled() {
		return editTool != null;
	}

	/**
	 * If the current tool overrides camera zoom/pan controls
	 *
	 * @return
	 */
	public boolean isOverrideCameraControl() {
		if (editTool != null) {
			return editTool.isOverrideCameraControl();
		} else {
			return false;
		}
	}

	/**
	 * Scene composer edit tool activated. Pass in null to remove tools.
	 *
	 * @param sceneEditTool pass in null to hide any existing tool markers
	 */
	public void showEditTool(final SceneEditTool sceneEditTool) {
		app.enqueue(() -> {
			doEnableEditTool(sceneEditTool);
			return null;
		});
	}

	private void doEnableEditTool(SceneEditTool sceneEditTool) {
		if (editTool != null) {
			editTool.hideMarker();
		}
		editTool = sceneEditTool;
		editTool.activate(manager, toolsNode, onTopToolsNode, selected, this);
	}

	public void selectedSpatialTransformed() {
		if (editTool != null) {
			editTool.updateToolsTransformation();
		}
	}

	public void setSelected(Spatial selected) {
		this.selected = selected;
	}

	public void setNeedsSave(boolean needsSave) {
		editorController.setNeedsSave(needsSave);
	}

	/**
	 * Primary button activated, send command to the tool for appropriate action.
	 *
	 * @param mouseLoc
	 * @param pressed
	 * @param camera
	 */
	public void doEditToolActivatedPrimary(Vector2f mouseLoc, boolean pressed, Camera camera) {
		if (scm.isActive()) {
			scm.getActiveShortcut().setCamera(camera);
			scm.getActiveShortcut().actionPrimary(mouseLoc, pressed, rootNode, editorController.getCurrentDataObject());
		} else if (editTool != null) {
			editTool.setCamera(camera);
			editTool.actionPrimary(mouseLoc, pressed, rootNode, editorController.getCurrentDataObject());
		}
	}

	/**
	 * Secondary button activated, send command to the tool for appropriate action.
	 *
	 * @param mouseLoc
	 * @param pressed
	 * @param camera
	 */
	public void doEditToolActivatedSecondary(Vector2f mouseLoc, boolean pressed, Camera camera) {
		ShortcutManager scm = Lookup.getDefault().lookup(ShortcutManager.class);

		if (scm.isActive()) {
			scm.getActiveShortcut().setCamera(camera);
			scm.getActiveShortcut().actionSecondary(mouseLoc, pressed, rootNode, editorController.getCurrentDataObject());
		} else if (editTool != null) {
			editTool.setCamera(camera);
			editTool.actionSecondary(mouseLoc, pressed, rootNode, editorController.getCurrentDataObject());
		}
	}

	public void doEditToolMoved(Vector2f mouseLoc, Camera camera) {
		ShortcutManager scm = Lookup.getDefault().lookup(ShortcutManager.class);

		if (scm.isActive()) {
			scm.getActiveShortcut().setCamera(camera);
			scm.getActiveShortcut().mouseMoved(mouseLoc, rootNode, editorController.getCurrentDataObject());
		} else if (editTool != null && editorController != null) {
			editTool.setCamera(camera);
			editTool.mouseMoved(mouseLoc, rootNode, editorController.getCurrentDataObject());
		}
	}

	public void doEditToolDraggedPrimary(Vector2f mouseLoc, boolean pressed, Camera camera) {
		ShortcutManager scm = Lookup.getDefault().lookup(ShortcutManager.class);

		if (scm.isActive()) {
			scm.getActiveShortcut().setCamera(camera);
			scm.getActiveShortcut().draggedPrimary(mouseLoc, pressed, rootNode, editorController.getCurrentDataObject());
		} else if (editTool != null) {
			editTool.setCamera(camera);
			editTool.draggedPrimary(mouseLoc, pressed, rootNode, editorController.getCurrentDataObject());
		}
	}

	public void doEditToolDraggedSecondary(Vector2f mouseLoc, boolean pressed, Camera camera) {
		ShortcutManager scm = Lookup.getDefault().lookup(ShortcutManager.class);

		if (scm.isActive()) {
			scm.getActiveShortcut().setCamera(null);
			scm.getActiveShortcut().draggedSecondary(mouseLoc, pressed, rootNode, editorController.getCurrentDataObject());
		} else if (editTool != null) {
			editTool.setCamera(camera);
			editTool.draggedSecondary(mouseLoc, pressed, rootNode, editorController.getCurrentDataObject());
		}
	}

	public void doKeyPressed(KeyInputEvent kie) {
		ShortcutManager scm = Lookup.getDefault().lookup(ShortcutManager.class);

		if (scm.isActive()) {
			scm.doKeyPressed(kie);
		} else if (scm.activateShortcut(kie)) {
			scm.getActiveShortcut().activate(manager, toolsNode, onTopToolsNode, selected, this);
		} else if (editTool != null) {
			editTool.keyPressed(kie);
		}
	}

	public boolean isSnapToGrid() {
		return snapToGrid;
	}

	public void setSnapToGrid(boolean snapToGrid) {
		this.snapToGrid = snapToGrid;
	}

	public void setSnapToScene(boolean snapToScene) {
		this.snapToScene = snapToScene;
	}

	public boolean isSnapToScene() {
		return snapToScene;
	}

	public boolean isSelectTerrain() {
		return selectTerrain;
	}

	public void setSelectTerrain(boolean selectTerrain) {
		this.selectTerrain = selectTerrain;
	}

	public boolean isSelectGeometries() {
		return selectGeometries;
	}

	public void setSelectGeometries(boolean selectGeometries) {
		this.selectGeometries = selectGeometries;
	}

	public void setTransformationType(String type) {
		if (type != null) {
			if (type.equals("Local")) {
				setTransformationType(TransformationType.local);
			} else if (type.equals("Global")) {
				setTransformationType(TransformationType.global);
			} else if (type.equals("Camera")) {
				setTransformationType(TransformationType.camera);
			}
		}
	}

	/**
	 * @param type the transformationType to set
	 */
	public void setTransformationType(TransformationType type) {
		if (type != transformationType) {
			transformationType = type;
			if (editTool != null) {
				// update the transform type of the tool
				editTool.setTransformType(transformationType);
			}
		}
	}

	/**
	 * @return the transformationType
	 */
	public TransformationType getTransformationType() {
		return transformationType;
	}

	public Node getRootNode() {
		return rootNode;
	}

	public PickManager getPickManager() {
		// TODO Auto-generated method stub
		return null;
	}
}

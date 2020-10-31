package de.george.g3dit.jme;

public interface SceneEditorController {

	void setNeedsSave(boolean needsSave);

	Object getCurrentDataObject();

	void setToolController(SceneComposerToolController toolController);

}

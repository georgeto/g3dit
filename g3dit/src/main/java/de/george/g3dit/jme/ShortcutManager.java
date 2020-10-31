package de.george.g3dit.jme;

import com.jme3.input.event.KeyInputEvent;

public interface ShortcutManager {

	boolean isActive();

	SceneEditTool getActiveShortcut();

	void doKeyPressed(KeyInputEvent kie);

	boolean activateShortcut(KeyInputEvent kie);

}

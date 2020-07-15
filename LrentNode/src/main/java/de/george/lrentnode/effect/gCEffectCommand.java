package de.george.lrentnode.effect;

import de.george.g3utils.io.G3FileReader;
import de.george.lrentnode.classes.G3Class;

public class gCEffectCommand extends G3Class {

	public gCEffectCommand(String className, G3FileReader reader) {
		super(className, reader);
	}

	public gCEffectCommand(String className, int classVersion) {
		super(className, classVersion);
	}

	// public abstract int getCommandType();
}

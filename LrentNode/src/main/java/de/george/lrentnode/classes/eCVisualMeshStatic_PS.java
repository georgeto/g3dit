package de.george.lrentnode.classes;

import de.george.g3utils.io.G3FileReader;

public class eCVisualMeshStatic_PS extends eCVisualMeshBase_PS {
	public eCVisualMeshStatic_PS(String className, G3FileReader reader) {
		super(className, reader);
	}

	@Override
	protected void readPostClassVersion(G3FileReader reader) {
		if (classVersion == 3 || classVersion == 4) {
			throw new UnsupportedOperationException("Version 3 oder 4 werden nicht unterstützt.");
		}
		super.readPostClassVersion(reader);
	}
}

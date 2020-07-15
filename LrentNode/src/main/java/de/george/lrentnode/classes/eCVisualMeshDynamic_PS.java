package de.george.lrentnode.classes;

import de.george.g3utils.io.G3FileReader;

public class eCVisualMeshDynamic_PS extends eCVisualMeshBase_PS {
	public eCVisualMeshDynamic_PS(String className, G3FileReader reader) {
		super(className, reader);
	}

	@Override
	protected void readPostClassVersion(G3FileReader reader) {
		if (classVersion <= 12) {
			reader.readBool();
			reader.readBool();
		}
		super.readPostClassVersion(reader);
	}
}

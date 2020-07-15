package de.george.lrentnode.classes;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.properties.bCString;

public class eCVisualMeshBase_PS extends eCEntityPropertySet {
	public eCVisualMeshBase_PS(String className, G3FileReader reader) {
		super(className, reader);
	}

	@Override
	protected void readPostClassVersion(G3FileReader reader) {
		if (classVersion <= 12) {
			reader.readBool();
			reader.readBool();
		}
		reader.readUnsignedShort();
		super.readPostClassVersion(reader);

		if (classVersion < 50) {
			property(CD.eCVisualMeshBase_PS.ResourceFileName).setString(this.<bCString>property("ResourceFilePath").getString());
		}
	}

	@Override
	protected void writePostClassVersion(G3FileWriter writer) {
		writer.writeUnsignedShort(1);
		super.writePostClassVersion(writer);
	}
}

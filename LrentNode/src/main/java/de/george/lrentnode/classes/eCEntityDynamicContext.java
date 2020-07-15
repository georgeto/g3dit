package de.george.lrentnode.classes;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.structure.bCBox;
import de.george.lrentnode.classes.desc.CD;

public class eCEntityDynamicContext extends G3Class {
	private boolean enabled;
	private float visualLoDFactor;
	private float objectCullFactor;

	public eCEntityDynamicContext(String className, G3FileReader reader) {
		super(className, reader);
	}

	@Override
	protected void readPostClassVersion(G3FileReader reader) {
		enabled = reader.readBool();
		visualLoDFactor = reader.readFloat();
		objectCullFactor = reader.readFloat();
		reader.skip(24);
	}

	@Override
	protected void writePostClassVersion(G3FileWriter writer) {
		bCBox box = property(CD.eCContextBase.ContextBox);
		writer.writeBool(enabled).writeFloat(visualLoDFactor).writeFloat(objectCullFactor).write(box);
	}
}

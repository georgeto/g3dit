package de.george.lrentnode.classes;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.structure.bCBox;
import de.george.lrentnode.classes.desc.CD;

public class eCGeometrySpatialContext extends G3Class {
	private boolean enabled;
	private boolean hybridContext;

	public eCGeometrySpatialContext(String className, G3FileReader reader) {
		super(className, reader);
	}

	@Override
	protected void readPostClassVersion(G3FileReader reader) {
		enabled = reader.readBool();
		hybridContext = reader.readBool();
		reader.skip(24);
	}

	@Override
	protected void writePostClassVersion(G3FileWriter writer) {
		bCBox box = property(CD.eCContextBase.ContextBox);
		writer.writeBool(enabled).writeBool(hybridContext).write(box);
	}
}

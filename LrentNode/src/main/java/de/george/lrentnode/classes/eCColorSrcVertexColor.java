package de.george.lrentnode.classes;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;

public final class eCColorSrcVertexColor extends eCColorSrcBase {
	public eCColorSrcVertexColor(String className, G3FileReader reader) {
		super(className, reader);
	}

	@Override
	protected void readPostClassVersion(G3FileReader reader) {
		super.readPostClassVersion(reader);
	}

	@Override
	protected void writePostClassVersion(G3FileWriter writer) {
		super.writePostClassVersion(writer);
	}
}

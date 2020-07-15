package de.george.lrentnode.structures;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.io.G3Serializable;

public class eCTexCoordSrcProxy implements G3Serializable {
	private int vertexTexCoord;
	private bCGuid guid;

	public eCTexCoordSrcProxy(int colorComponent, bCGuid guid) {
		vertexTexCoord = colorComponent;
		this.guid = guid;
	}

	@Override
	public void read(G3FileReader reader) {
		vertexTexCoord = reader.readInt();
		guid = reader.read(bCGuid.class);
	}

	@Override
	public void write(G3FileWriter writer) {
		writer.writeInt(vertexTexCoord);
		writer.write(guid);
	}
}

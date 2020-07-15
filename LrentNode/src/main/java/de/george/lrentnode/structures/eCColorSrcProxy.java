package de.george.lrentnode.structures;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.io.G3Serializable;

public class eCColorSrcProxy implements G3Serializable {
	private int colorComponent;
	private bCGuid guid;

	public eCColorSrcProxy(int colorComponent, bCGuid guid) {
		this.colorComponent = colorComponent;
		this.guid = guid;
	}

	@Override
	public void read(G3FileReader reader) {
		colorComponent = reader.readInt();
		guid = reader.read(bCGuid.class);
	}

	@Override
	public void write(G3FileWriter writer) {
		writer.writeInt(colorComponent);
		writer.write(guid);
	}

	public int getColorComponent() {
		return colorComponent;
	}

	public void setColorComponent(int colorComponent) {
		this.colorComponent = colorComponent;
	}

	public bCGuid getGuid() {
		return guid;
	}

	public void setGuid(bCGuid guid) {
		this.guid = guid;
	}
}

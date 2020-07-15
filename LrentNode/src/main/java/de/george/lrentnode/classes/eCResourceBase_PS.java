package de.george.lrentnode.classes;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;

public abstract class eCResourceBase_PS extends G3Class {
	private int size;

	public eCResourceBase_PS(String className, G3FileReader reader) {
		super(className, reader);
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	@Override
	protected void readPostClassVersion(G3FileReader reader) {
		int resourceVersion = reader.readShort();

		if (resourceVersion >= 0x17) {
			size = reader.readInt();
		}

		if (resourceVersion < 0x1E) {
			if (reader.readUnsignedShort() > 1) {
				reader.skip(1);
			}
		}
	}

	@Override
	protected void writePostClassVersion(G3FileWriter writer) {
		writer.writeUnsignedShort(0x1E).writeInt(size);
	}
}

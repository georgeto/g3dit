package de.george.lrentnode.properties;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.io.G3Serializable;
import de.george.lrentnode.enums.G3Enums.G3Enum;

public class SerializableEnumWrapper<T extends G3Enum> implements G3Serializable {
	private int enumValue;

	@Override
	public void read(G3FileReader reader) {
		enumValue = reader.readInt();
	}

	@Override
	public void write(G3FileWriter writer) {
		writer.writeInt(enumValue);
	}
}

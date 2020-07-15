package de.george.lrentnode.properties;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.io.G3Serializable;
import de.george.lrentnode.enums.G3Enums.G3Enum;

public class bTPropertyContainer<T extends G3Enum> implements G3Serializable {
	private int enumValue;

	public bTPropertyContainer() {
		this(0);
	}

	public bTPropertyContainer(int enumValue) {
		this.enumValue = enumValue;
	}

	@Override
	public void read(G3FileReader reader) {
		reader.readShort(); // Version
		enumValue = reader.readInt();
	}

	@Override
	public void write(G3FileWriter writer) {
		writer.writeUnsignedShort(1); // Version
		writer.writeInt(enumValue);
	}

	public int getEnumValue() {
		return enumValue;
	}

	public void setEnumValue(int enumValue) {
		this.enumValue = enumValue;
	}

	@Override
	public String toString() {
		return Integer.toString(enumValue);
	}
}

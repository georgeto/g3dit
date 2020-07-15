package de.george.lrentnode.properties;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.io.G3Serializable;

public final class ClassProperty<T extends G3Serializable> {
	private String propName;
	private String propClassType;
	private int magicValue;

	private T data;

	public ClassProperty(G3FileReader reader) {
		propName = reader.readEntry();
		propClassType = reader.readEntry();
		magicValue = reader.readShort();
		data = PropertyInstantiator.getPropertyInstance(propName, propClassType);
		data.read(reader, reader.readInt());
	}

	public ClassProperty(String name, String type, T value) {
		propName = name;
		propClassType = type;
		data = value;
		magicValue = 30;// 0x1E00
	}

	public T getValue() {
		return data;
	}

	public void setValue(T data) {
		this.data = data;
	}

	public String getName() {
		return propName;
	}

	public String getType() {
		return propClassType;
	}

	public boolean nameEqual(String name) {
		return propName.equals(name);
	}

	public void write(G3FileWriter writer) {
		writer.writeEntry(propName);
		writer.writeEntry(propClassType);
		writer.writeUnsignedShort(magicValue);
		int sizeOffset = writer.getSize();
		writer.writeInt(-1);
		writer.write(data);
		writer.replaceInt(writer.getSize() - sizeOffset - 4, sizeOffset);
	}

	@Override
	public String toString() {
		return getType() + " " + getName() + " = " + getValue();
	}
}

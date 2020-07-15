package de.george.lrentnode.properties;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;

public class eCPropertySetProxy extends eCEntityProxy {
	private String propertySetName;

	public eCPropertySetProxy() {
		this(null, null);
	}

	public eCPropertySetProxy(String guid, String propertySetName) {
		super(guid);
		this.propertySetName = propertySetName;
	}

	@Override
	public void read(G3FileReader reader) {
		reader.readShort(); // Version
		propertySetName = reader.readBool() ? reader.readEntry() : null;
		super.read(reader);
	}

	@Override
	public void write(G3FileWriter writer) {
		writer.writeUnsignedShort(1); // Version
		if (propertySetName != null) {
			writer.writeBool(true).writeEntry(propertySetName);
		} else {
			writer.writeBool(false);
		}
		super.write(writer);
	}

	public String getPropertySetName() {
		return propertySetName;
	}

	public void setPropertySetName(String propertySetName) {
		this.propertySetName = propertySetName;
	}

	@Override
	public String toString() {
		return super.toString() + "." + propertySetName;
	}
}

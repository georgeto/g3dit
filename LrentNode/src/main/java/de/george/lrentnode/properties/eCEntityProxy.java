package de.george.lrentnode.properties;

import com.google.common.base.Strings;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.io.G3Serializable;

public class eCEntityProxy implements G3Serializable {
	private String guid;

	public eCEntityProxy() {
		this(null);
	}

	public eCEntityProxy(String guid) {
		this.guid = guid;
	}

	@Override
	public void read(G3FileReader reader) {
		reader.readShort(); // Version
		if (reader.readBool()) {
			guid = reader.readGUID();
		} else {
			guid = null;
		}
	}

	@Override
	public void write(G3FileWriter writer) {
		writer.writeUnsignedShort(1); // Version
		if (guid != null) {
			writer.writeBool(true).write(guid);
		} else {
			writer.writeBool(false);
		}
	}

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	@Override
	public eCEntityProxy clone() {
		return new eCEntityProxy(guid);
	}

	@Override
	public String toString() {
		return Strings.nullToEmpty(guid);
	}
}

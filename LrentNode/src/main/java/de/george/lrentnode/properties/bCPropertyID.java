package de.george.lrentnode.properties;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.io.G3Serializable;

public class bCPropertyID implements G3Serializable {
	private static final String INVALID_GUID = "0000000000000000000000000000000000000000";

	private String guid;

	public bCPropertyID() {
		this(INVALID_GUID);
	}

	public bCPropertyID(String guid) {
		this.guid = guid;
	}

	@Override
	public void read(G3FileReader reader) {
		guid = reader.readGUID();
	}

	@Override
	public void write(G3FileWriter writer) {
		if (guid != null) {
			writer.write(guid);
		} else {
			writer.write(INVALID_GUID);
		}
	}

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	@Override
	public String toString() {
		return String.valueOf(guid);
	}
}

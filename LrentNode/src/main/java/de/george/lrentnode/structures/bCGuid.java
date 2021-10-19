package de.george.lrentnode.structures;

import java.util.Objects;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.io.G3Serializable;
import de.george.g3utils.structure.GuidUtil;

public class bCGuid implements G3Serializable {
	private String guid;
	public int valid; // eigentlich ein value, aber es wird das komplette dword geschrieben

	public bCGuid() {
		setGuid(null);
	}

	public bCGuid(String guid, int valid) {
		this.guid = guid;
		this.valid = valid;
	}

	@Override
	public void read(G3FileReader reader) {
		guid = reader.read(16);
		valid = reader.readInt();
	}

	@Override
	public void write(G3FileWriter writer) {
		writer.write(guid);
		writer.writeInt(valid);
	}

	public boolean isValid() {
		return (valid & 0xFF) != 0;
	}

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		valid = GuidUtil.isValid(guid) ? 1 : 0;
		this.guid = guid;
	}

	@Override
	public String toString() {
		return "guid=" + guid + ", valid=" + valid;
	}

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof bCGuid castOther)) {
			return false;
		}
		return Objects.equals(guid, castOther.guid) && isValid() == castOther.isValid();
	}

	@Override
	public int hashCode() {
		return Objects.hash(guid, isValid());
	}
}

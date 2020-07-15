package de.george.lrentnode.properties;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.io.G3Serializable;

public class bCString implements G3Serializable {
	private String string;

	public bCString() {
		this("");
	}

	public bCString(String string) {
		this.string = string;
	}

	@Override
	public void read(G3FileReader reader) {
		string = reader.readEntry();
	}

	@Override
	public void write(G3FileWriter writer) {
		writer.writeEntry(string);
	}

	public String getString() {
		return string;
	}

	public void setString(String value) {
		string = value;
	}

	@Override
	public String toString() {
		return string;
	}
}

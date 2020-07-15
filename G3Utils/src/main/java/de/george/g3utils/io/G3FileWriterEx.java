package de.george.g3utils.io;

import de.george.g3utils.structure.Stringtable;

public class G3FileWriterEx extends G3FileWriter {

	protected Stringtable stringtable = new Stringtable();

	public G3FileWriterEx() {
		super();
	}

	public G3FileWriterEx(int initialSize) {
		super(initialSize);
	}

	public G3FileWriterEx(byte[] data) {
		super(data);
	}

	public G3FileWriterEx(String hex) {
		super(hex);
	}

	public G3FileWriterEx(String hex, int size) {
		super(hex, size);
	}

	public G3FileWriterEx(StringBuffer hex) {
		super(hex);
	}

	public Stringtable getStringtable() {
		return stringtable;
	}

	public void setStringtable(Stringtable table) {
		stringtable = table;
	}

	public String getEntry(int position) {
		return stringtable.getEntry(position);
	}

	public int getEntryIndex(String entry) {
		int value = stringtable.getPosition(entry);
		if (value == -1) {
			value = stringtable.addEntry(entry);
		}
		return value;
	}

	@Override
	public G3FileWriter writeEntry(String entry) {
		writeUnsignedShort(getEntryIndex(entry));
		return this;
	}

	public void writeStringtable() {
		// Stringtable schreiben
		write("01");
		writeInt(stringtable.getEntryCount());
		for (int i = 0; i < stringtable.getEntryCount(); i++) {
			String entry = stringtable.getEntry(i);
			writeUnsignedShort(entry.length());
			writeString(entry);
		}
	}
}

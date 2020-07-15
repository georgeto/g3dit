package de.george.g3utils.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;

import de.george.g3utils.structure.Stringtable;

public class G3FileReaderEx extends G3FileReader {
	protected Stringtable table;

	public G3FileReaderEx(String hex) {
		super(hex);
	}

	public G3FileReaderEx(byte[] bytes) {
		super(bytes);
	}

	public G3FileReaderEx(File file) throws IOException {
		super(file);
	}

	public G3FileReaderEx(InputStream is) throws IOException {
		super(is);
	}

	public G3FileReaderEx(FileChannel fileChannel) throws IOException {
		super(fileChannel);
	}

	public void readStringtable(int deadbeefOffset) {
		readStringtable(deadbeefOffset, true);
	}

	public void readStringtable(int deadbeefOffset, boolean savePosition) {
		int savePos = getPos();

		Stringtable stringtable = new Stringtable();
		seek(deadbeefOffset);

		// Stringtable ist aktiv
		if (readBool()) {
			int stEntryCount = readInt();
			for (int i = 0; i < stEntryCount; i++) {
				stringtable.addEntry(readString(readShort()));
			}
		}

		setStringtable(stringtable);

		if (savePosition) {
			seek(savePos);
		}
	}

	public void setStringtable(Stringtable table) {
		this.table = table;
	}

	public Stringtable getStringtable() {
		return table;
	}

	public String getEntry(int position) {
		return table.getEntry(position);
	}

	@Override
	public String readEntry() {
		return table.getEntry(readShort());
	}
}

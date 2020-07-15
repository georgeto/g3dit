package de.george.lrentnode.classes;

import java.util.ArrayList;
import java.util.List;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.io.G3Serializable;
import de.george.g3utils.structure.bCVector;

public class gCMap_PS extends G3Class {
	public List<MapMarker> markers;

	public gCMap_PS(String className, G3FileReader reader) {
		super(className, reader);
	}

	@Override
	protected void readPostClassVersion(G3FileReader reader) {
		int markerCount = reader.readInt();
		int oldVersionDetect = reader.readInt();
		if (oldVersionDetect != 0x14) {
			reader.skip(markerCount * oldVersionDetect);
			markers = new ArrayList<>();
		} else {
			markers = reader.readList(MapMarker.class, markerCount);
		}
	}

	@Override
	protected void writePostClassVersion(G3FileWriter writer) {
		writer.writeInt(markers.size()).writeInt(0x14);
		writer.write(markers);
	}

	public static class MapMarker implements G3Serializable {
		public String name;
		public bCVector position;
		public boolean active;

		public MapMarker(String name, bCVector position, boolean active) {
			this.name = name;
			this.position = position;
			this.active = active;
		}

		@Override
		public void read(G3FileReader reader) {
			name = reader.readEntry();
			position = reader.readVector();
			active = reader.readBool();
		}

		@Override
		public void write(G3FileWriter writer) {
			writer.writeEntry(name).writeVector(position).writeBool(active);
		}
	}
}

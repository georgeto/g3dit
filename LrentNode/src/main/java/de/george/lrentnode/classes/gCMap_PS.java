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

	public List<MapMarker> getMarkers() {
		return markers;
	}

	public void setMarkers(List<MapMarker> markers) {
		this.markers = markers;
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

		public MapMarker() {
			this("", bCVector.nullVector(), false);
		}

		public MapMarker(String name, bCVector position, boolean active) {
			this.name = name;
			this.position = position;
			this.active = active;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public bCVector getPosition() {
			return position;
		}

		public void setPosition(bCVector position) {
			this.position = position;
		}

		public boolean isActive() {
			return active;
		}

		public void setActive(boolean active) {
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

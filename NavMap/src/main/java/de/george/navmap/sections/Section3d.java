package de.george.navmap.sections;

import java.util.List;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.io.G3Serializable;
import de.george.g3utils.structure.bCVector;
import de.george.lrentnode.properties.eCPropertySetProxy;
import de.george.navmap.data.NavPath;

/**
 * Aufzählung aller NavPaths, bestehend aus Guid und den Intersections mit den beiden verbundenen
 * NavZones.
 */
public class Section3d implements G3Serializable {
	public List<Section3dEntry> navPaths;

	@Override
	public void read(G3FileReader reader) {
		navPaths = reader.readPrefixedList(Section3dEntry.class);
	}

	@Override
	public void write(G3FileWriter writer) {
		writer.writePrefixedList(navPaths);
	}

	public void writeText(StringBuilder builder) {
		builder.append("Section 3d");
		builder.append("\nAnzahl der NavPaths: " + navPaths.size());
		for (int i = 0; i < navPaths.size(); i++) {
			Section3dEntry entry = navPaths.get(i);
			builder.append("\nNavPath " + i);
			builder.append("\n" + entry.guid);
			builder.append("\nCluster: " + entry.clusterIndex);
			builder.append("\nzoneACenter: " + entry.zoneACenter);
			builder.append("\nzoneAMargin1: " + entry.zoneAMargin1);
			builder.append("\nzoneAMargin2: " + entry.zoneAMargin2);
			builder.append("\nzoneBCenter: " + entry.zoneBCenter);
			builder.append("\nzoneBMargin1: " + entry.zoneBMargin1);
			builder.append("\nzoneBMargin2: " + entry.zoneBMargin2);
		}
	}

	public Section3dEntry getNavPath(String guid) {
		for (Section3dEntry entry : navPaths) {
			if (entry.guid.equals(guid)) {
				return entry;
			}
		}
		return null;
	}

	public int getNavPathIndex(String guid) {
		for (int i = 0; i < navPaths.size(); i++) {
			Section3dEntry entry = navPaths.get(i);
			if (entry.guid.equalsIgnoreCase(guid)) {
				return i;
			}
		}
		return -1;
	}

	public boolean addNavPath(NavPath navPath, int clusterIndex) {
		return navPaths.add(new Section3dEntry(navPath.guid, clusterIndex, navPath.zoneAIntersection.zoneIntersectionCenter,
				navPath.zoneAIntersection.zoneIntersectionMargin1, navPath.zoneAIntersection.zoneIntersectionMargin2,
				navPath.zoneBIntersection.zoneIntersectionCenter, navPath.zoneBIntersection.zoneIntersectionMargin1,
				navPath.zoneBIntersection.zoneIntersectionMargin2));
	}

	public boolean updateNavPath(NavPath navPath) {
		Section3dEntry entry = getNavPath(navPath.guid);

		if (entry == null) {
			return false;
		}

		entry.zoneACenter = navPath.zoneAIntersection.zoneIntersectionCenter;
		entry.zoneAMargin1 = navPath.zoneAIntersection.zoneIntersectionMargin1;
		entry.zoneAMargin2 = navPath.zoneAIntersection.zoneIntersectionMargin2;
		entry.zoneBCenter = navPath.zoneBIntersection.zoneIntersectionCenter;
		entry.zoneBMargin1 = navPath.zoneBIntersection.zoneIntersectionMargin1;
		entry.zoneBMargin2 = navPath.zoneBIntersection.zoneIntersectionMargin2;
		return true;
	}

	public boolean removeNavPath(String navPath) {
		for (Section3dEntry entry : navPaths) {
			if (entry.guid.equals(navPath)) {
				navPaths.remove(entry);
				return true;
			}
		}
		return false;
	}

	public static class Section3dEntry implements G3Serializable {
		public String guid;
		public int clusterIndex;
		public bCVector zoneACenter, zoneAMargin1, zoneAMargin2, zoneBCenter, zoneBMargin1, zoneBMargin2;

		public Section3dEntry(String guid, int clusterIndex, bCVector zoneACenter, bCVector zoneAMargin1, bCVector zoneAMargin2,
				bCVector zoneBCenter, bCVector zoneBMargin1, bCVector zoneBMargin2) {
			this.guid = guid;
			this.clusterIndex = clusterIndex;
			this.zoneACenter = zoneACenter;
			this.zoneAMargin1 = zoneAMargin1;
			this.zoneAMargin2 = zoneAMargin2;
			this.zoneBCenter = zoneBCenter;
			this.zoneBMargin1 = zoneBMargin1;
			this.zoneBMargin2 = zoneBMargin2;
		}

		@Override
		public void read(G3FileReader reader) {
			guid = reader.read(eCPropertySetProxy.class).getGuid();
			clusterIndex = reader.readInt();
			zoneACenter = reader.readVector();
			zoneAMargin1 = reader.readVector();
			zoneAMargin2 = reader.readVector();
			zoneBCenter = reader.readVector();
			zoneBMargin1 = reader.readVector();
			zoneBMargin2 = reader.readVector();
		}

		@Override
		public void write(G3FileWriter writer) {
			writer.write(new eCPropertySetProxy(guid, "gCNavPath_PS"));
			writer.writeInt(clusterIndex);
			writer.writeVector(zoneACenter);
			writer.writeVector(zoneAMargin1);
			writer.writeVector(zoneAMargin2);
			writer.writeVector(zoneBCenter);
			writer.writeVector(zoneBMargin1);
			writer.writeVector(zoneBMargin2);
		}
	}

}

package de.george.navmap.sections;

import java.util.ArrayList;
import java.util.List;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.io.G3Serializable;
import de.george.g3utils.util.Misc;
import de.george.lrentnode.properties.eCPropertySetProxy;
import de.george.navmap.data.NegCircle;
import de.george.navmap.data.NegZone;
import de.george.navmap.data.PrefPath;
import de.george.navmap.sections.Section3c.NegCircleOverlaps;

/**
 * Gruppierung der NavObjekte aus 2,3a,3b1 (Zuordnung zu NavZones)
 */
public class Section3b2 implements G3Serializable {
	public List<NavZoneWithObjects> navZones = new ArrayList<>();

	public int getEntryIndexByGuid(String guid) {
		for (int i = 0; i < navZones.size(); i++) {
			NavZoneWithObjects entry = navZones.get(i);
			if (entry.zoneGuid.equalsIgnoreCase(guid)) {
				return i;
			}
		}
		return -1;
	}

	public NavZoneWithObjects getEntryByGuid(String guid) {
		for (NavZoneWithObjects entry : navZones) {
			if (entry.zoneGuid.equalsIgnoreCase(guid)) {
				return entry;
			}
		}
		return null;
	}

	public void writeText(StringBuilder builder) {
		builder.append("Section 3b2");
		builder.append("\nAnzahl der NavZones: " + navZones.size());
		for (int i = 0; i < navZones.size(); i++) {
			NavZoneWithObjects zone = navZones.get(i);
			builder.append("\nNavZone " + i);
			builder.append("\n" + zone.zoneGuid);
			builder.append("\nCluster: " + zone.clusterIndex);
			builder.append("\nAnzahl enthaltener NegZones: " + zone.iNegZones.size() + "\n");
			zone.iNegZones.forEach(index -> builder.append(index + ", "));
			builder.append("\nAnzahl enthaltener NegCircles: " + zone.iNegCircles.size() + "\n");
			zone.iNegCircles.forEach(index -> builder.append(index + ", "));
			builder.append("\nAnzahl enthaltener PrefPaths: " + zone.iPrefPaths.size() + "\n");
			zone.iPrefPaths.forEach(index -> builder.append(index + ", "));
		}
	}

	public void addNavZone(String guid, int clusterIndex) {
		navZones.add(new NavZoneWithObjects(guid, clusterIndex));
	}

	public void removeNavZone(String guid, NavMap navMap) {
		NavZoneWithObjects zone = getEntryByGuid(guid);

		for (int index : zone.iPrefPaths) {
			PrefPath prefPath = navMap.sec3b1.getPrefPath(index);
			prefPath.setZoneGuid(NavMap.INVALID_ZONE_ID);
		}

		for (int index : zone.iNegZones) {
			NegZone negZone = navMap.sec2.getNegZone(index);
			negZone.setZoneGuid(NavMap.INVALID_ZONE_ID);
		}

		for (int index : zone.iNegCircles) {
			NegCircle negCircle = navMap.sec3a.getNegCircle(index);
			negCircle.zoneGuids.remove(zone.zoneGuid);

			NegCircleOverlaps overlaps = navMap.sec3c.entries.get(index);
			overlaps.entries.removeIf(o -> o.zoneGuid.equals(zone.zoneGuid));
		}

		navZones.remove(zone);
	}

	public void addNegCircle(NegCircle negCircle, int index) {
		for (String areaId : negCircle.zoneGuids) {
			NavZoneWithObjects navZone = getEntryByGuid(areaId);
			if (navZone != null) {
				navZone.iNegCircles.add(index);
			}
		}
	}

	public void removeNegCircle(NegCircle negCircle, int circleIndex, boolean displaceIndexes) {
		// Sektion3b2
		for (String guid : negCircle.zoneGuids) {
			NavZoneWithObjects zone = getEntryByGuid(guid);
			if (zone == null) {
				// Es können auch NavPaths unter den zoneGuids sein, diese haben allerdings
				// keine Auflistung aller
				// NegCircles die sie schneiden
				continue;
			}
			zone.iNegCircles.remove(Integer.valueOf(circleIndex));
		}

		// Indizes der NegCircles in Sektion 3b2 (Verknüpfung mit NavZones) anpassen
		if (displaceIndexes) {
			for (NavZoneWithObjects zone : navZones) {
				Misc.removeRangeAndDisplace(zone.iNegCircles, circleIndex, 1);
			}
		}
	}

	@Override
	public void read(G3FileReader reader) {
		navZones = reader.readPrefixedList(NavZoneWithObjects.class);
	}

	@Override
	public void write(G3FileWriter writer) {
		writer.writePrefixedList(navZones);
	}

	public static class NavZoneWithObjects implements G3Serializable {
		public String zoneGuid;
		public int clusterIndex;
		public List<Integer> iNegZones;
		public List<Integer> iNegCircles;
		public List<Integer> iPrefPaths;

		public NavZoneWithObjects(String zoneGuid, int clusterIndex) {
			this(zoneGuid, clusterIndex, new ArrayList<Integer>(), new ArrayList<Integer>(), new ArrayList<Integer>());
		}

		public NavZoneWithObjects(String zoneGuid, int clusterIndex, List<Integer> iNegZones, List<Integer> iNegCircles,
				List<Integer> iPrefPaths) {
			this.zoneGuid = zoneGuid;
			this.clusterIndex = clusterIndex;
			this.iNegZones = iNegZones;
			this.iNegCircles = iNegCircles;
			this.iPrefPaths = iPrefPaths;
		}

		@Override
		public void read(G3FileReader reader) {
			zoneGuid = reader.read(eCPropertySetProxy.class).getGuid();
			clusterIndex = reader.readInt();
			iNegZones = reader.readPrefixedList(G3FileReader::readInt);
			iNegCircles = reader.readPrefixedList(G3FileReader::readInt);
			iPrefPaths = reader.readPrefixedList(G3FileReader::readInt);
		}

		@Override
		public void write(G3FileWriter writer) {
			writer.write(new eCPropertySetProxy(zoneGuid, "gCNavZone_PS"));
			writer.writeInt(clusterIndex);
			writer.writePrefixedList(iNegZones, G3FileWriter::writeInt);
			writer.writePrefixedList(iNegCircles, G3FileWriter::writeInt);
			writer.writePrefixedList(iPrefPaths, G3FileWriter::writeInt);
		}
	}
}

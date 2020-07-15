package de.george.navmap.sections;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.common.base.Joiner;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.io.G3Serializable;
import de.george.g3utils.util.Misc;
import de.george.navmap.data.NegCircle;
import de.george.navmap.sections.Section3b2.NavZoneWithObjects;
import one.util.streamex.StreamEx;

/**
 * NavObjekte aus 3a) werden untereinander gruppiert (welche NegCircles schneiden sich?)
 */
public class Section3c implements G3Serializable {
	public List<NegCircleOverlaps> entries;

	@Override
	public void read(G3FileReader reader) {
		entries = reader.readPrefixedList(NegCircleOverlaps.class);
	}

	@Override
	public void write(G3FileWriter writer) {
		writer.writePrefixedList(entries);
	}

	public void writeText(StringBuilder builder) {
		builder.append("Section 3c");
		builder.append("\nNegCircle Anzahl: " + entries.size());
		for (NegCircleOverlaps entry : entries) {
			builder.append("\nIndex: " + entry.index);
			builder.append("\nAnzahl der beteiligten Zonen: " + entry.entries.size());
			for (NegCircleOverlapsInZone subEntry : entry.entries) {
				builder.append("\nZoneGuid: " + subEntry.zoneGuid);
				builder.append("\nAnzahl der überschneidenden NegCircle: " + subEntry.circleIndexes.size());
				builder.append("\n" + Joiner.on(" ").join(subEntry.circleIndexes));
			}
		}
	}

	public void addNegCircle(NegCircle negCircle, int circleIndex, NavMap navMap) {
		// NegCircleOverlaps für NegCircle erstellen
		NegCircleOverlaps circleOverlaps = new NegCircleOverlaps(circleIndex, new ArrayList<>());
		entries.add(circleIndex, circleOverlaps);

		for (NavZoneWithObjects zone : navMap.sec3b2.navZones) {
			// Zonen die den NegCircle enhalten
			if (negCircle.zoneGuids.contains(zone.zoneGuid)) {
				NegCircleOverlapsInZone circleOverlapsInZone = new NegCircleOverlapsInZone(zone.zoneGuid);

				// Für jeden Index der Zone auf Überschneidung prüfen
				for (int otherCircleIndex : zone.iNegCircles) {
					// Keine Selbstüberschneidung
					if (circleIndex == otherCircleIndex) {
						continue;
					}

					if (negCircle.intersects(navMap.sec3a.getNegCircle(otherCircleIndex))) {
						circleOverlapsInZone.circleIndexes.add(otherCircleIndex);

						// Eintrag bei Partner hinzufügen
						addNegCircleOverlapInZone(circleIndex, zone.zoneGuid, otherCircleIndex);
					}
				}

				// Wenn es Überschneidungen in dieser Zone gibt
				if (circleOverlapsInZone.circleIndexes.size() > 0) {
					circleOverlaps.entries.add(circleOverlapsInZone);
				}
			}
		}
	}

	public void removeNegCircle(int circleIndex, boolean displaceIndexes) {
		NegCircleOverlaps circleOverlaps = entries.get(circleIndex);
		if (circleOverlaps.index != circleIndex) {
			throw new IllegalStateException("Section3c Index Unequal Error: " + circleIndex + " - " + circleOverlaps.index);
		}

		// Intersection in other Zones
		for (NegCircleOverlapsInZone circleOverlapsInZone : circleOverlaps.entries) {
			for (int otherCircleIndex : circleOverlapsInZone.circleIndexes) {
				if (otherCircleIndex == circleIndex) {
					throw new IllegalStateException("Intersecting with myself: " + circleIndex + " - " + otherCircleIndex);
				}

				// NegCircle mit dem geschnitten wird
				NegCircleOverlaps otherCircleOverlaps = entries.get(otherCircleIndex);
				if (otherCircleOverlaps.index != otherCircleIndex) {
					throw new IllegalStateException(
							"Section3c Index Unequal Error 2: " + otherCircleIndex + " - " + otherCircleOverlaps.index);
				}

				Iterator<NegCircleOverlapsInZone> iter = otherCircleOverlaps.entries.iterator();
				while (iter.hasNext()) {
					NegCircleOverlapsInZone otherCircleOverlapsInZone = iter.next();
					if (circleOverlapsInZone.zoneGuid.equalsIgnoreCase(otherCircleOverlapsInZone.zoneGuid)) {
						otherCircleOverlapsInZone.circleIndexes.remove(Integer.valueOf(circleIndex));
						if (otherCircleOverlapsInZone.circleIndexes.size() == 0) {
							iter.remove();
						}
						break;
					}
				}
			}
		}

		entries.remove(circleOverlaps);

		if (displaceIndexes) {
			// Indizes der NegCircles in Sektion 3c (Verknüpfung untereinander) anpassen
			for (NegCircleOverlaps entry : entries) {
				if (entry.index > circleIndex) {
					entry.index -= 1;
				}
				for (NegCircleOverlapsInZone subEntry : entry.entries) {
					Misc.removeRangeAndDisplace(subEntry.circleIndexes, circleIndex, 1);
				}
			}
		}
	}

	private void addNegCircleOverlapInZone(int circleIndex, String zoneGuid, int otherCircleIndex) {
		NegCircleOverlaps otherCircleOverlaps = entries.get(otherCircleIndex);
		NegCircleOverlapsInZone otherCircleOverlapsInZone = StreamEx.of(otherCircleOverlaps.entries)
				.findFirst(e -> e.zoneGuid.equals(zoneGuid)).orElse(null);

		if (otherCircleOverlapsInZone == null) {
			otherCircleOverlapsInZone = new NegCircleOverlapsInZone(zoneGuid);
			otherCircleOverlaps.entries.add(otherCircleOverlapsInZone);
		}

		otherCircleOverlapsInZone.circleIndexes.add(circleIndex);
	}

	/**
	 * Repräsentiert einen NegCircle, enthält Überschneidungen mit anderen NegCirclen
	 */
	public static class NegCircleOverlaps implements G3Serializable {
		public int index;
		public List<NegCircleOverlapsInZone> entries;

		public NegCircleOverlaps(int index, List<NegCircleOverlapsInZone> entries) {
			this.index = index;
			this.entries = entries;
		}

		@Override
		public void read(G3FileReader reader) {
			index = reader.readInt();
			entries = reader.readPrefixedList(NegCircleOverlapsInZone.class);
		}

		@Override
		public void write(G3FileWriter writer) {
			writer.writeInt(index);
			writer.writePrefixedList(entries);
		}
	}

	/**
	 * Repräsentiert die Überschneidungen mit anderen NegCircles in einer NavZone
	 */
	public static class NegCircleOverlapsInZone implements G3Serializable {
		public String zoneGuid;
		public List<Integer> circleIndexes;

		public NegCircleOverlapsInZone(String zoneGuid) {
			this(zoneGuid, new ArrayList<>());
		}

		public NegCircleOverlapsInZone(String zoneGuid, List<Integer> circleIndexes) {
			this.zoneGuid = zoneGuid;
			this.circleIndexes = circleIndexes;
		}

		@Override
		public void read(G3FileReader reader) {
			zoneGuid = reader.readGUID();
			circleIndexes = reader.readPrefixedList(G3FileReader::readInt);
		}

		@Override
		public void write(G3FileWriter writer) {
			writer.write(zoneGuid);
			writer.writePrefixedList(circleIndexes, G3FileWriter::writeInt);
		}
	}

}

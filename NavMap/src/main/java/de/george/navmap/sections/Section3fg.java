package de.george.navmap.sections;

import java.util.ArrayList;
import java.util.List;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.io.G3Serializable;
import de.george.g3utils.util.Pair;
import de.george.navmap.data.NavObject;
import de.george.navmap.data.NavPath;

/**
 * Indizierung für Sektion 3h) (NavZones + NavPaths)
 * <p>
 * Jedem NavPath werden seine beiden IntersectionCenter Einträge aus Section3h zugeordnet. <br>
 * Jeder NavZone werden der passende IntersectionCenter ihrer NavPaths zugeordnet.
 */
public class Section3fg implements G3Serializable {
	public List<NavObject> navZones;
	public List<NavObject> navPaths;

	@Override
	public void read(G3FileReader reader) {
		navZones = new ArrayList<>();
		navPaths = new ArrayList<>();
		reader.readPrefixedList(NavObject.class, navObject -> {
			if (navObject.isNavPath) {
				navPaths.add(navObject);
			} else {
				navZones.add(navObject);
			}
		});
	}

	@Override
	public void write(G3FileWriter writer) {
		writer.writeListPrefix();
		writer.writeInt(navZones.size() + navPaths.size());
		writer.write(navPaths);
		writer.write(navZones);
	}

	public void writeText(StringBuilder builder) {
		builder.append("Section 3fg");
		builder.append("\nAnzahl der NavPaths: " + navPaths.size());
		for (int i = 0; i < navPaths.size(); i++) {
			NavObject path = navPaths.get(i);
			builder.append("\nNavPath " + i);
			builder.append("\n" + path.guid);
			builder.append("\nAnzahl der Intersections: " + path.indexes.size() + "\n");
			path.indexes.forEach(index -> builder.append(index + ", "));
		}

		builder.append("\n\nAnzahl der NavZones: " + navZones.size());
		for (int i = 0; i < navZones.size(); i++) {
			NavObject zone = navZones.get(i);
			builder.append("\nNavZone " + i);
			builder.append("\n" + zone.guid);
			builder.append("\nAnzahl der Intersections: " + zone.indexes.size() + "\n");
			zone.indexes.forEach(index -> builder.append(index + ", "));
		}
	}

	public void addNavZone(String guid) {
		navZones.add(new NavObject(guid, false, new ArrayList<Integer>()));
	}

	public void addNavPath(NavPath navPath, NavMap navMap) {
		Pair<Integer, Integer> pair = navMap.sec3h.getNavPathLinkIndexPair(navPath.guid, false);
		int indexA = pair.el0();
		int indexB = pair.el1();

		// NavPath Einträge erstellen
		List<Integer> indexes = new ArrayList<>();
		indexes.add(indexA);
		indexes.add(indexB);
		navPaths.add(new NavObject(navPath.guid, true, indexes));

		// Pfad in die NavZones eintragen
		NavObject zoneA = getZone(navPath.zoneAGuid);
		zoneA.addIndex(indexA);
		NavObject zoneB = getZone(navPath.zoneBGuid);
		zoneB.addIndex(indexB);
	}

	public void removeNavPath(String navPath) {
		navPaths.remove(getPath(navPath));
	}

	public NavObject removeNavZone(String guid) {
		NavObject zoneObject = getZone(guid);
		navZones.remove(zoneObject);
		return zoneObject;
	}

	public NavObject getZone(String guid) {
		for (NavObject zone : navZones) {
			if (zone.guid.equals(guid)) {
				return zone;
			}
		}
		return null;
	}

	public NavObject getPath(String guid) {
		for (NavObject path : navPaths) {
			if (path.guid.equals(guid)) {
				return path;
			}
		}
		return null;
	}
}

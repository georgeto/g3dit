package de.george.navmap.sections;

import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.teamunify.i18n.I;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.io.G3Serializable;
import de.george.g3utils.structure.bCVector;
import de.george.g3utils.util.Misc;
import de.george.g3utils.util.Pair;
import de.george.navmap.data.NavObject;
import de.george.navmap.data.NavPath;
import de.george.navmap.data.NavPathLink;

/**
 * Verknüpfung von NavZones und NavPaths mit Hilfe der Indizees aus 3f)
 * <p>
 * Beide IntersectionCenter eines NavPaths, sind einer der verbundenen NavZones zugeordnet. <br>
 * Die Zuordnung geschieht über die Guids von NavPath und NavZone.
 * <p>
 * Der Index der Zuordnungen wird in Section3fg und Section4 verwendet.
 */
public class Section3h implements G3Serializable {
	private static final Logger logger = LoggerFactory.getLogger(Section3h.class);

	public List<NavPathLink> navPathLinks;

	@Override
	public void read(G3FileReader reader) {
		navPathLinks = reader.readPrefixedList(NavPathLink.class);
	}

	@Override
	public void write(G3FileWriter writer) {
		writer.writePrefixedList(navPathLinks);
	}

	public void writeText(StringBuilder builder) {
		builder.append("Section 3h");
		builder.append("\nAnzahl der Links: " + navPathLinks.size());
		for (int i = 0; i < navPathLinks.size(); i++) {
			NavPathLink path = navPathLinks.get(i);
			builder.append("\nLink " + i);
			builder.append("\nNavZone: " + path.zoneGuid);
			builder.append("\nNavPath: " + path.pathGuid);
			builder.append("\nIntersection: " + path.intersection);
		}
	}

	public int addNavPathLink(String pathGuid, String zoneGuid, bCVector intersection) {
		navPathLinks.add(new NavPathLink(intersection, zoneGuid, pathGuid));
		return navPathLinks.size() - 1;
	}

	public NavPathLink getNavPathLink(int index) {
		return navPathLinks.get(index);
	}

	public int getNavPathLinkIndex(NavPathLink link) {
		return navPathLinks.indexOf(link);
	}

	public NavPathLink getNavPathLink(String pathGuid, String zoneGuid) {
		Objects.requireNonNull(pathGuid);
		Objects.requireNonNull(zoneGuid);
		for (NavPathLink link : navPathLinks) {
			if (pathGuid.equals(link.pathGuid) && zoneGuid.equals(link.zoneGuid)) {
				return link;
			}
		}
		return null;
	}

	public Pair<NavPathLink, NavPathLink> getNavPathLinkPair(String pathGuid, boolean noThrow) {
		NavPathLink link1 = null, link2 = null;
		for (NavPathLink link : navPathLinks) {
			if (link.pathGuid.equals(pathGuid)) {
				if (link1 == null) {
					link1 = link;
				} else {
					link2 = link;
					break;
				}
			}
		}

		if (!noThrow && (link1 == null || link2 == null)) {
			throw new IllegalArgumentException(I.trf("Ungültiger NavPath Eintrag in Section3h: {0}", pathGuid));
		}

		return new Pair<>(link1, link2);
	}

	public Pair<Integer, Integer> getNavPathLinkIndexPair(String pathGuid, boolean noThrow) {
		Integer link1 = -1, link2 = -1;
		for (int i = 0; i < navPathLinks.size(); i++) {
			NavPathLink link = navPathLinks.get(i);
			if (link.pathGuid.equals(pathGuid)) {
				if (link1 == -1) {
					link1 = i;
				} else {
					link2 = i;
					break;
				}
			}
		}

		if (!noThrow && (link1 == -1 || link2 == -1)) {
			throw new IllegalArgumentException(I.trf("Ungültiger NavPath Eintrag in Section3h: {0}", pathGuid));
		}

		return new Pair<>(link1, link2);
	}

	public void addNavPath(NavPath navPath) {
		// Eintragen in Sektion 3h
		addNavPathLink(navPath.guid, navPath.zoneAGuid,
				navPath.zoneAIntersection.zoneIntersectionCenter.getTransformed(navPath.getWorldMatrix()));
		addNavPathLink(navPath.guid, navPath.zoneBGuid,
				navPath.zoneBIntersection.zoneIntersectionCenter.getTransformed(navPath.getWorldMatrix()));
	}

	/**
	 * Aktualisiert einen NavPath und passt außerdem Verweise auf Section3h in Section3fg an.
	 *
	 * @param navPath
	 * @param navMap
	 */
	public void updateNavPath(NavPath navPath, NavMap navMap) {
		Pair<NavPathLink, NavPathLink> pair = getNavPathLinkPair(navPath.guid, false);

		updateLink(pair.el0(), navPath.zoneAIntersection.zoneIntersectionCenter.getTransformed(navPath.getWorldMatrix()),
				navPath.zoneAGuid, navMap);
		updateLink(pair.el1(), navPath.zoneBIntersection.zoneIntersectionCenter.getTransformed(navPath.getWorldMatrix()),
				navPath.zoneBGuid, navMap);
	}

	private void updateLink(NavPathLink link, bCVector intersection, String zoneGuid, NavMap navMap) {
		int linkIndex = getNavPathLinkIndex(link);

		removeLinkFromZone(link, linkIndex, navMap);

		link.intersection = intersection;
		link.zoneGuid = zoneGuid;

		addLinkToZone(link, linkIndex, navMap);
	}

	/**
	 * Löscht einen NavPath und passt außerdem Verweise auf Section3h in Section3fg an.
	 *
	 * @param navPath
	 * @param navMap
	 */
	public void removeNavPath(String navPath, NavMap navMap) {
		Pair<NavPathLink, NavPathLink> pair = getNavPathLinkPair(navPath, true);
		removeNavPathLink(pair.el0(), navMap);
		removeNavPathLink(pair.el1(), navMap);
	}

	private void removeNavPathLink(NavPathLink link, NavMap navMap) {
		if (link == null) {
			return;
		}

		int index = getNavPathLinkIndex(link);

		removeLinkFromZone(link, index, navMap);

		for (NavObject zone : navMap.sec3fg.navZones) {
			Misc.removeRangeAndDisplace(zone.indexes, index, 1);
		}

		for (NavObject path : navMap.sec3fg.navPaths) {
			Misc.removeRangeAndDisplace(path.indexes, index, 1);
		}

		navPathLinks.remove(link);
	}

	private void addLinkToZone(NavPathLink link, int linkIndex, NavMap navMap) {
		if (link.zoneGuid != null) {
			NavObject zone = navMap.sec3fg.getZone(link.zoneGuid);
			if (zone != null) {
				zone.addIndex(linkIndex);
			} else {
				logger.warn("NavPath {} has intersection ({}) with zone {} that does not exist.", link.pathGuid, link.intersection,
						link.zoneGuid);
			}
		} else {
			logger.warn("NavPath {} has no zone registered for intersection ({}).", link.pathGuid, link.intersection);
		}
	}

	private void removeLinkFromZone(NavPathLink link, int linkIndex, NavMap navMap) {
		if (link.zoneGuid != null) {
			NavObject oldZone = navMap.sec3fg.getZone(link.zoneGuid);
			if (oldZone != null) {
				oldZone.removeIndex(linkIndex);
			} else {
				logger.warn("NavPath {} had intersection ({}) with zone {} that does not exist.", link.pathGuid, link.intersection,
						link.zoneGuid);
			}
		} else {
			logger.warn("NavPath {} had no zone registered for intersection ({}).", link.pathGuid, link.intersection);
		}
	}

	public void removeNavZone(NavObject zoneObject) {
		for (int linkIndex : zoneObject.indexes) {
			NavPathLink link = getNavPathLink(linkIndex);
			link.zoneGuid = null;
		}
	}
}

package de.george.navmap.sections;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.teamunify.i18n.I;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.io.G3Serializable;
import de.george.g3utils.util.Pair;
import de.george.navmap.data.NavObject;
import de.george.navmap.data.NavPath;
import de.george.navmap.data.NavPathLink;
import one.util.streamex.StreamEx;

/**
 * Wegfindungsnetz (die Struktur orientiert sich an Sektion 3h))
 * <p>
 * Zu jedem IntersectionCenter aus Sektion 3h, wird die Verbindung zu seinem Partner Stick und die
 * Verbindungen zu allen anderen IntersectionCenters seiner Zone aufgelistet.
 */
public class Section4 implements G3Serializable {
	private static final Logger logger = LoggerFactory.getLogger(Section4.class);

	public List<Waypoint> waypoints;

	@Override
	public void read(G3FileReader reader) {
		waypoints = reader.readPrefixedList(Waypoint.class);
	}

	@Override
	public void write(G3FileWriter writer) {
		writer.writePrefixedList(waypoints);
	}

	public void writeText(StringBuilder builder) {
		builder.append("Section 4");
		builder.append("\nAnzahl der Waypoints: " + waypoints.size());
		for (int i = 0; i < waypoints.size(); i++) {
			Waypoint wayPoint = waypoints.get(i);
			builder.append("\nWayPoint " + i);
			builder.append("\nAnzahl der Verbindungen: " + wayPoint.cons.size());
			wayPoint.cons.forEach(c -> builder.append("\n" + c.distance + " " + (c.backRoad ? "tru" : "fls") + " Ix_" + c.index));
		}
	}

	public int addWaypoint() {
		waypoints.add(new Waypoint(new ArrayList<WaypointConnection>()));
		return waypoints.size() - 1;
	}

	public Waypoint getWaypoint(int index) {
		return waypoints.get(index);
	}

	/**
	 * Muss für jeden NavPath zweimal aufgerufen werden, d.h. für jede Intersection einmal.
	 *
	 * @param navPath
	 * @param index
	 * @param neighbourIndex
	 * @param zone
	 * @param navMap
	 */
	private void processIntersection(NavPath navPath, String zoneGuid, int index, int neighbourIndex, NavMap navMap) {
		NavPathLink path = navMap.sec3h.getNavPathLink(index);
		Waypoint inter = getWaypoint(index);

		// Distanz zum zweiten Stick des Paths, hier die richtigen Koordinaten und nicht die
		// IntersectionCenter
		inter.addConnection(new WaypointConnection(navPath.getLength(), true, neighbourIndex));

		NavObject zone = navMap.sec3fg.getZone(zoneGuid);
		// Wenn keine Verbindung zu einer NavZone besteht, gibt es nur die Verbindung zum zweiten
		// Stick.
		if (zone == null) {
			return;
		}

		// Distanz zwischen 2 verschiedenen NavPaths -> Distanz IntersectionCenter zu
		// IntersectionCenter
		for (Integer destIndex : zone.indexes) {
			// Verbindung zu sich selbst macht keinen Sinn
			if (destIndex == index) {
				continue;
			}

			NavPathLink destPath = navMap.sec3h.getNavPathLink(destIndex);
			Waypoint destInter = getWaypoint(destIndex);

			float distance = path.intersection.getRelative(destPath.intersection).length();
			// Verbindung von processedPath zu destPath
			inter.addConnection(new WaypointConnection(distance, false, destIndex));

			// Nur wenn destPath nicht die Partner Intersection ist, da sonst doppelte EInträge
			// entständen
			if (destIndex != neighbourIndex) {
				// Verbindung von destPath zu processedPath
				destInter.addConnection(new WaypointConnection(distance, false, index));
			}

		}
	}

	/**
	 * Fügt einen NavPath hinzu, es muss für diesen bereits ein Eintrag in Section3h bestehen.
	 *
	 * @param navPath
	 * @param navMap
	 */
	public void addNavPath(NavPath navPath, NavMap navMap) {
		Pair<Integer, Integer> pair = navMap.sec3h.getNavPathLinkIndexPair(navPath.guid, false);
		int indexA = pair.el0();
		int indexB = pair.el1();

		if (addWaypoint() != indexA || addWaypoint() != indexB) {
			throw new IllegalArgumentException(I.tr("Section4 does not have the same number of entries as Section3h!"));
		}

		processIntersection(navPath, navPath.zoneAGuid, indexA, indexB, navMap);
		processIntersection(navPath, navPath.zoneBGuid, indexB, indexA, navMap);
	}

	/**
	 * Aktualisiert einen NavPath, dessen Position und verbundene Zonen sich geändert haben.
	 *
	 * @param navPath
	 * @param navMap
	 */
	public void updateNavPath(NavPath navPath, NavMap navMap) {
		Pair<Integer, Integer> pair = navMap.sec3h.getNavPathLinkIndexPair(navPath.guid, false);
		int indexA = pair.el0();
		int indexB = pair.el1();

		freeWaypoint(indexA, false);
		freeWaypoint(indexB, false);
		processIntersection(navPath, navPath.zoneAGuid, indexA, indexB, navMap);
		processIntersection(navPath, navPath.zoneBGuid, indexB, indexA, navMap);
	}

	/**
	 * Löscht einen NavPath (d.h. seine beiden Waypoints). Der NavPath muss zuerst in Section4
	 * gelöscht werden und erst anschließend in Section3h.
	 *
	 * @param navPath
	 * @param navMap
	 */
	public void removeNavPath(String navPath, NavMap navMap) {
		Pair<Integer, Integer> pair = navMap.sec3h.getNavPathLinkIndexPair(navPath, true);
		List<Integer> indices = StreamEx.of(pair.el0(), pair.el1()).filter(i -> i != -1).reverseSorted().toList();

		for (int index : indices) {
			freeWaypoint(index, false);
		}

		// Hinteren zuerst löschen, da sonst Index nicht mehr stimmt
		for (int index : indices) {
			waypoints.remove(index);

			for (Waypoint waypoint : waypoints) {
				for (WaypointConnection connection : waypoint.cons) {
					if (connection.index > index) {
						connection.index -= 1;
					}
				}
			}
		}
	}

	/**
	 * Löscht eine NavZone (d.h. aktualisiert die Waypoints der angeschlossenen NavPaths).
	 */
	public void removeNavZone(NavObject zoneObject) {
		for (int linkIndex : zoneObject.indexes) {
			freeWaypoint(linkIndex, true);
		}
	}

	/**
	 * Löscht alle Verbindungen in denen der Waypoint {@code index} involviert ist.
	 *
	 * @param index Index des Waypoints
	 */
	private void freeWaypoint(int index, boolean preserveBackRoad) {
		Waypoint inter = getWaypoint(index);

		for (WaypointConnection con : inter.cons) {
			// Partnerverbindung nur einseitig löschen
			if (con.backRoad) {
				continue;
			}

			Waypoint destInter = getWaypoint(con.index);
			if (!destInter.removeConnection(index)) {
				logger.warn("Connection from {} to {} could not be deleted.", con.index, index);
			}
		}

		inter.clearConnections(preserveBackRoad);
	}

	/**
	 * Schnittpunkt von NavZone und NavPath, dient als Wegpunkt für die Navigation von Zone zu Zone.
	 */
	public static class Waypoint implements G3Serializable {
		public List<WaypointConnection> cons;

		public Waypoint(List<WaypointConnection> cons) {
			this.cons = cons;
		}

		public void clearConnections(boolean preserveBackRoad) {
			if (!preserveBackRoad) {
				cons.clear();
			} else {
				cons.removeIf(connection -> !connection.backRoad);
			}
		}

		/**
		 * Fügt eine Verbindung, an der passenden Stelle, in die Liste der Verbindungen dieses
		 * Waypoints ein.
		 *
		 * @param connection
		 */
		public void addConnection(WaypointConnection connection) {
			if (connection.backRoad) {
				if (cons.size() > 0 && cons.get(0).backRoad) {
					throw new IllegalArgumentException(I.trf(
							"Insertion of partner connection to {0, number} failed: A partner connection to Intersection {1, number} already exists.",
							connection.index, cons.get(0).index));
				}
				cons.add(0, connection);
			} else {
				for (int i = 0; i < cons.size(); i++) {
					WaypointConnection con = cons.get(i);

					if (con.backRoad) {
						continue;
					}

					if (con.index == connection.index) {
						throw new IllegalArgumentException(I.trf(
								"Insertion of connection to {0, number} failed: A connection to Intersection {1, number} already exists.",
								connection.index, con.index));
					}

					if (con.index > connection.index) {
						cons.add(i, connection);
						return;
					}
				}

				cons.add(connection);
			}
		}

		/**
		 * Gibt die Verbindung zum Waypoint {@code destIndex} zurück.
		 * <p>
		 * Die Partner Verbindung wird nicht berücksichtigt, da es möglich ist, dass der
		 * Partner-Waypoint ebenfalls in der Zone liegt. Dann gibt es zwei Wege von diesen Waypoint
		 * zu seinem Partner.
		 *
		 * @param destIndex
		 * @return {@code null}, wenn Verbindung nicht gefunden wurde
		 */
		public WaypointConnection getConnection(int destIndex) {
			for (WaypointConnection con : cons) {
				if (!con.backRoad && con.index == destIndex) {
					return con;
				}
			}
			return null;
		}

		/**
		 * Löscht die Verbindung zun Waypoint {@code destIndex}.
		 * <p>
		 * Die Partner Verbindung wird nicht berücksichtigt, da es möglich ist, dass der
		 * Partner-Waypoint ebenfalls in der Zone liegt. Dann gibt es zwei Wege von diesen Waypoint
		 * zu seinem Partner.
		 *
		 * @param destIndex
		 * @return {@code false}, wenn Verbindung nicht gefunden wurde
		 */
		public boolean removeConnection(int destIndex) {
			Iterator<WaypointConnection> iter = cons.iterator();
			while (iter.hasNext()) {
				WaypointConnection con = iter.next();
				if (!con.backRoad && con.index == destIndex) {
					iter.remove();
					return true;
				}
			}
			return false;
		}

		@Override
		public void read(G3FileReader reader) {
			reader.skipListPrefix();
			cons = reader.readList(WaypointConnection.class, reader.readUnsignedShort());
		}

		@Override
		public void write(G3FileWriter writer) {
			writer.writeListPrefix();
			writer.writeUnsignedShort(cons.size());
			writer.write(cons);
		}
	}

	/**
	 * Verbindet zwei Waypoints.
	 */
	public static class WaypointConnection implements G3Serializable {

		public float distance; // Wegstrecke zum Stick mit dem Index
		public boolean backRoad; // Weg zum eigenen BruderStick
		public int index; // Section 3h Index

		public WaypointConnection(float distance, boolean backRoad, int index) {
			this.distance = distance;
			this.backRoad = backRoad;
			this.index = index;
		}

		@Override
		public void read(G3FileReader reader) {
			distance = reader.readFloat();
			backRoad = reader.readBool();
			reader.skip(3); // Padding
			index = reader.readInt();

		}

		private static final byte[] PADDING = new byte[] {(byte) 0xF0, (byte) 0xFE, 0x3f};

		@Override
		public void write(G3FileWriter writer) {
			writer.writeFloat(distance);
			writer.writeBool(backRoad);
			writer.write(PADDING); // Padding
			writer.writeInt(index);
		}

	}
}

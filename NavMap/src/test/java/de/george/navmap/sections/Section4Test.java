package de.george.navmap.sections;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.george.navmap.data.NavObject;
import de.george.navmap.data.NavPathLink;
import de.george.navmap.sections.Section4.Waypoint;
import de.george.navmap.sections.Section4.WaypointConnection;

public class Section4Test extends NavMapTest {
	private static Logger logger = LoggerFactory.getLogger(Section4Test.class);

	@Test
	public void test() {
		Assert.assertEquals(navMap.sec4.waypoints.size(), navMap.sec3h.navPathLinks.size());

		for (int n = 0; n < navMap.sec4.waypoints.size(); n++) {
			Waypoint entry = navMap.sec4.getWaypoint(n);

			NavPathLink link = navMap.sec3h.getNavPathLink(n);

			if (entry.cons.size() == 0) {
				logger.warn("{}: {} hat keine Entries", n, link.pathGuid);
				Assert.assertNull(n + ": Hat keine Entries, aber eine Zone", link.zoneGuid);
				continue;
			}

			// Partner Verbindungen
			WaypointConnection toPartner = entry.cons.get(0);
			Assert.assertTrue(n + ": Erste Verbindung geht nicht zum Parnter", toPartner.backRoad);
			Assert.assertEquals(n + ": Mehr als eine Partnerverbindung", 1, entry.cons.stream().filter(c -> c.backRoad).count());

			// NavPath Objekt
			NavObject path = navMap.sec3fg.getPath(link.pathGuid);
			Assert.assertNotNull(n + ": NavPath Objekt existiert nicht.", path);
			Assert.assertEquals(2, path.indexes.size());
			Assert.assertTrue(path.indexes.contains(n));
			Assert.assertTrue(path.indexes.contains(toPartner.index));

			// Es existiert keine Zone
			if (link.zoneGuid == null) {
				logger.info("{} hat keine Zone: {}", n, link.intersection.toMarvinString());
				// Keine Zone -> Nur Partnerverbindung
				Assert.assertEquals(1, entry.cons.size());
			} else {
				// NavZone Objekt
				NavObject zone = navMap.sec3fg.getZone(link.zoneGuid);
				Assert.assertNotNull(n + ": Zone " + link.zoneGuid + " konnte nicht gefunden werden.", zone);

				// Anzahl der Verbindungen und Anzahl der IntersectionCenter der Zone müssen
				// übereinstimmen
				Assert.assertEquals(entry.cons.size(), zone.indexes.size());
				Assert.assertTrue(n + ": Zone " + link.zoneGuid + " enthält diesen IntersectionCenter nicht", zone.indexes.contains(n));
				for (WaypointConnection subEntry : entry.cons) {
					if (!subEntry.backRoad) {
						Assert.assertTrue(n + ": Zone " + link.zoneGuid + " enthält den IntersectionCenter " + subEntry.index + " nicht",
								zone.indexes.contains(subEntry.index));
					}
				}

				// Parter überprüfen
				WaypointConnection fromPartner = navMap.sec4.getWaypoint(toPartner.index).cons.get(0);
				Assert.assertEquals(n, fromPartner.index);
				Assert.assertTrue(fromPartner.backRoad);
				Assert.assertEquals(toPartner.distance, fromPartner.distance, 0.01f);

				// Verbindungen überprüfem
				for (int c = 1; c < entry.cons.size(); c++) {
					WaypointConnection toInter = entry.cons.get(c);

					boolean found = false;
					for (WaypointConnection fromInter : navMap.sec4.getWaypoint(toInter.index).cons) {
						if (!fromInter.backRoad && fromInter.index == n) {
							Assert.assertEquals(toInter.distance, fromInter.distance, 0.01f);
							found = true;
							break;
						}
					}

					Assert.assertTrue("Verbindung von " + n + " <-> " + toInter.index + " besteht nur in eine Richtung.", found);
				}
			}
		}
	}
}

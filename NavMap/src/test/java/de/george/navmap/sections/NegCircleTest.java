package de.george.navmap.sections;

import java.io.IOException;
import java.util.Iterator;

import org.junit.Assert;
import org.junit.Test;

import de.george.navmap.data.NegCircle;
import de.george.navmap.sections.Section3b2.NavZoneWithObjects;
import de.george.navmap.sections.Section3c.NegCircleOverlaps;
import de.george.navmap.sections.Section3c.NegCircleOverlapsInZone;
import one.util.streamex.StreamEx;

public class NegCircleTest extends NavMapTest {

	@Test
	public void testIntersectionsInPath() throws IOException {
		for (NegCircleOverlaps entry : navMap.sec3c.entries) {

			Assert.assertTrue(entry.index + ": Hat mind. zwei SubEntries für gleiche Zone.",
					entry.entries.size() == entry.entries.stream().map(e -> e.zoneGuid).distinct().count());

			NegCircle circle = navMap.sec3a.getNegCircle(entry.index);
			Iterator<NegCircleOverlapsInZone> iter = entry.entries.iterator();
			while (iter.hasNext()) {
				NegCircleOverlapsInZone subEntry = iter.next();

				NavZoneWithObjects zone = navMap.sec3b2.getEntryByGuid(subEntry.zoneGuid);
				Assert.assertNotNull(zone);

				// Assert.assertTrue(subEntry.circleIndexes.size() > 0);

				Assert.assertTrue(entry.index + ": Intersection in Zone " + subEntry.zoneGuid
						+ ", ohne dass Zone in Sektion 3b2 den Circle enthält.", zone.iNegCircles.contains(entry.index));
				//@foff
				/*if(!circle.zoneGuids.contains(subEntry.zoneGuid)) {
					System.out.println(entry.index + ": Intersection in Zone " + subEntry.zoneGuid + ", ohne dass Zone in Sektion 3a eingetragen ist.");

					Assert.assertEquals("6373705FC0500141BD014E617630300100000000", subEntry.zoneGuid);
					Assert.assertTrue(circle.zoneGuids.contains("4CF269F6B82E1B44BD0A803C5C2D83CB00000000"));

					circle.zoneGuids.remove("4CF269F6B82E1B44BD0A803C5C2D83CB00000000");
					circle.zoneGuids.add("6373705FC0500141BD014E617630300100000000");
				}*/
				//@fon

				Assert.assertTrue(
						entry.index + ": Intersection in Zone " + subEntry.zoneGuid + ", ohne dass Zone in Sektion 3a eingetragen ist.",
						circle.zoneGuids.contains(subEntry.zoneGuid));
				Assert.assertTrue(entry.index + ": Schneidet sich mit Circles, die nicht in der Zone " + subEntry.zoneGuid + " liegen.",
						zone.iNegCircles.containsAll(subEntry.circleIndexes));

				//@foff
				/*
				if(!zone.iNegCircles.containsAll(subEntry.circleIndexes))
					System.out.println(entry.index + ": Schneidet sich mit Circles, die nicht in der Zone " + subEntry.zoneGuid + " liegen.");
				*/
				//@fon

				for (Integer index : subEntry.circleIndexes) {
					NegCircleOverlaps partnerSec3c = navMap.sec3c.entries.get(index);
					Assert.assertTrue(
							entry.index + ": Schneidet sich mit Circle " + index + "' in Zone '" + subEntry.zoneGuid
									+ "', aber Partner hat keinen SubEntry für diese.",
							partnerSec3c.entries.stream().anyMatch(e -> e.zoneGuid.equals(subEntry.zoneGuid)));

					Assert.assertTrue(
							entry.index + ": Schneidet sich mit Circle " + index + "', aber Partner hat keinen Schnitt eingetragen.",
							StreamEx.of(partnerSec3c.entries).findFirst(e -> e.zoneGuid.equals(subEntry.zoneGuid)).get().circleIndexes
									.contains(entry.index));
				}
			}
		}
	}

}

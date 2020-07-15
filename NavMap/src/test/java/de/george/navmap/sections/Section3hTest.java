package de.george.navmap.sections;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.george.g3utils.util.Pair;
import de.george.navmap.data.NavObject;
import de.george.navmap.data.NavPathLink;
import de.george.navmap.sections.Section3d.Section3dEntry;

public class Section3hTest extends NavMapTest {
	private static Logger logger = LoggerFactory.getLogger(Section3hTest.class);

	@Test
	public void test() {
		// -2 aufgrund von haltlosem NavPath...
		Assert.assertEquals(navMap.sec3d.navPaths.size() * 2 - 2, navMap.sec3h.navPathLinks.size());

		for (Section3dEntry entry : navMap.sec3d.navPaths) {
			NavObject path = navMap.sec3fg.getPath(entry.guid);
			Pair<NavPathLink, NavPathLink> navPathLinkPair = navMap.sec3h.getNavPathLinkPair(entry.guid, true);

			Assert.assertNotNull(entry.guid + " hat keinen ersten Link", navPathLinkPair.el0());

			if (navPathLinkPair.el1() == null) {
				Assert.assertNull(navPathLinkPair.el0().zoneGuid);
				Assert.assertEquals(1, path.indexes.size());
				logger.info(navPathLinkPair.el0().intersection.toString());
			} else {
				Assert.assertEquals(2, path.indexes.size());
			}

		}
	}
}

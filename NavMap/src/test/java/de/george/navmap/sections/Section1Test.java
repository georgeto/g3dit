package de.george.navmap.sections;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.george.g3utils.structure.bCPoint;
import de.george.g3utils.structure.bCRect;
import de.george.lrentnode.properties.eCPropertySetProxy;
import de.george.navmap.data.NavPath;
import de.george.navmap.data.NavZone;

public class Section1Test {
	private static final Logger logger = LoggerFactory.getLogger(Section1Test.class);

	private NavMap navMap;
	private Collection<NavZone> navZones;
	private Collection<NavPath> navPaths;

	public Section1Test(NavMap navMap, Collection<NavZone> navZones, Collection<NavPath> navPaths) {
		this.navMap = navMap;
		this.navZones = navZones;
		this.navPaths = navPaths;
	}

	private Map<String, bCRect> buildValidationSet() {
		Map<String, bCRect> validationSet = new HashMap<>();

		List<eCPropertySetProxy>[][] navRegions = navMap.sec1.navGrid;
		for (int x = 0; x < navRegions.length; x++) {
			for (int z = 0; z < navRegions[x].length; z++) {
				for (eCPropertySetProxy entry : navRegions[x][z]) {
					bCPoint point = new bCPoint(x, z);
					validationSet.compute(entry.getPropertySetName() + " " + entry.getGuid(),
							(k, v) -> (v != null ? v : new bCRect()).merge(point));
				}
			}
		}
		return validationSet;
	}

	public void testCalcBoundary() {
		Map<String, bCRect> validationSet = buildValidationSet();
		for (NavZone navZone : navZones) {
			bCRect boundary = navMap.sec1.calcNavZoneBoundary(navZone);
			bCRect rect = validationSet.remove("gCNavZone_PS " + navZone.getGuid());
			if (rect == null) {
				logger.info("NICHT in NavMap vorhanden: NavZone " + navZone.getGuid());
			} else if (!boundary.equal(rect)) {
				logger.info("NavZone " + navZone.getGuid());
				logger.info(boundary.toString());
				logger.info(rect.toString());
			}
		}
		for (NavPath navPath : navPaths) {
			bCRect boundary = navMap.sec1.calcNavPathBoundary(navPath);
			bCRect rect = validationSet.remove("gCNavPath_PS " + navPath.guid);
			if (rect == null) {
				logger.info("NICHT in NavMap vorhanden: NavPath " + navPath.guid);
			} else if (!boundary.equal(rect)) {
				logger.info("NavPath " + navPath.guid);
				logger.info(boundary.toString());
				logger.info(rect.toString());
			}
		}

		validationSet.forEach((k, v) -> logger.info("Übrig: " + k));
	}

	public void testGridBoundary() {
		for (NavZone navZone : navZones) {
			if (!navMap.sec1.isInGrid(navZone)) {
				logger.info("NavZone: NICHT innerhalb des Grids " + navZone.getGuid());
			}
		}
		for (NavPath navPath : navPaths) {
			if (!navMap.sec1.isInGrid(navPath)) {
				logger.info("NavPath: NICHT innerhalb des Grids " + navPath.guid);
			}
		}
	}
}

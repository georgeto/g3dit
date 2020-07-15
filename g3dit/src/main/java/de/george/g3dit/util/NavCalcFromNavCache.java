package de.george.g3dit.util;

import de.george.g3dit.cache.NavCache;
import de.george.g3utils.structure.bCVector;
import de.george.navmap.data.NavPath;
import de.george.navmap.data.NavZone;
import de.george.navmap.sections.NavMap;
import de.george.navmap.util.NavCalc;

/**
 * Use the NavMap only to lookup the NegZones and NegCircles for NavZones.
 */
public class NavCalcFromNavCache extends NavCalc {
	private final Iterable<NavZone> zones;
	private final Iterable<NavPath> paths;

	public NavCalcFromNavCache(NavMap navMap, NavCache cache) {
		super(navMap, cache::getZone, cache::getPath);
		zones = cache.getZones();
		paths = cache.getPaths();
	}

	@Override
	public Iterable<NavZone> listZones(bCVector position) {
		return zones;
	}

	@Override
	public Iterable<NavPath> listPaths(bCVector position) {
		return paths;
	}

	@Override
	public Iterable<NavZone> getNavZones() {
		return zones;
	}
}

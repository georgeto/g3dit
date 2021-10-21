package de.george.navmap.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import de.george.g3utils.structure.bCMatrix;
import de.george.g3utils.structure.bCMotion;
import de.george.g3utils.structure.bCVector;
import de.george.g3utils.structure.bCVector2;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.properties.eCPropertySetProxy;
import de.george.navmap.data.NavPath;
import de.george.navmap.data.NavPath.ZonePathIntersection;
import de.george.navmap.data.NavZone;
import de.george.navmap.data.NegCircle;
import de.george.navmap.data.NegZone;
import de.george.navmap.data.Zone;
import de.george.navmap.sections.NavMap;
import one.util.streamex.StreamEx;

public class NavCalc {
	public static class NavArea {
		public String areaId;
		public boolean isNavPath;

		public NavArea(String areaId, boolean isNavPath) {
			this.areaId = areaId;
			this.isNavPath = isNavPath;
		}

		@Override
		public boolean equals(final Object other) {
			if (!(other instanceof NavArea castOther)) {
				return false;
			}
			return Objects.equals(areaId, castOther.areaId) && Objects.equals(isNavPath, castOther.isNavPath);
		}

		@Override
		public int hashCode() {
			return Objects.hash(areaId, isNavPath);
		}

		@Override
		public String toString() {
			return String.format("[%s] %s", isNavPath ? "NavPath" : "NavZone", areaId);
		}

		public static Optional<NavArea> fromPropertySetProxy(eCPropertySetProxy area) {
			if (area == null || area.getGuid() == null || area.getPropertySetName() == null) {
				return Optional.empty();
			}

			boolean isNavPath;
			if (area.getPropertySetName().equals("gCNavZone_PS")) {
				isNavPath = false;
			} else if (area.getPropertySetName().equals("gCNavPath_PS")) {
				isNavPath = true;
			} else {
				return Optional.empty();
			}
			return Optional.of(new NavArea(area.getGuid(), isNavPath));
		}

		public static eCPropertySetProxy toPropertySetProxy(Optional<NavArea> area) {
			return new eCPropertySetProxy(area.map(a -> a.areaId).orElse(null),
					area.map(a -> a.isNavPath ? "gCNavPath_PS" : "gCNavZone_PS").orElse(null));
		}
	}

	private Function<String, Optional<NavZone>> lookupNavZone;
	private Function<String, Optional<NavPath>> lookupNavPath;
	private NavMap navMap;

	public NavCalc(NavMap navMap, Function<String, Optional<NavZone>> lookupNavZone, Function<String, Optional<NavPath>> lookupNavPath) {
		this.navMap = navMap;
		this.lookupNavZone = lookupNavZone;
		this.lookupNavPath = lookupNavPath;
	}

	public Iterable<NavZone> listZones(bCVector position) {
		return StreamEx.of(navMap.getCell(position)).flatMap(List::stream).filter(p -> p.getPropertySetName().equals("gCNavZone_PS"))
				.map(eCPropertySetProxy::getGuid).map(lookupNavZone).filter(Optional::isPresent).map(Optional::get);
	}

	public Iterable<NavPath> listPaths(bCVector position) {
		return StreamEx.of(navMap.getCell(position)).flatMap(List::stream).filter(p -> p.getPropertySetName().equals("gCNavPath_PS"))
				.map(eCPropertySetProxy::getGuid).map(lookupNavPath).filter(Optional::isPresent).map(Optional::get);
	}

	private List<NavZone> cachedNavZones = null;

	public Iterable<NavZone> getNavZones() {
		if (cachedNavZones != null) {
			return cachedNavZones;

		}
		return navMap.getNavZones().map(lookupNavZone).filter(Optional::isPresent).map(Optional::get);
	}

	public void cacheNavZones() {
		cachedNavZones = ((StreamEx<NavZone>) getNavZones()).toList();
	}

	public void uncacheNavZones() {
		cachedNavZones = null;
	}

	public static boolean isInZoneRadius(Zone zone, bCVector position) {
		// Optimization: Check if position is outside the zone radius
		bCVector2 worldRadiusOffset2d = zone.getWorldRadiusOffset().to2D();
		float positionSquareRadius = worldRadiusOffset2d.getInvTranslated(position.to2D()).getSquareMagnitude();
		return positionSquareRadius <= zone.getRadius() * zone.getRadius();
	}

	public static boolean isCircleInZoneRadius(NavZone navZone, bCVector position, float radius) {
		// Optimization: Check if position is outside the zone radius
		bCVector2 worldRadiusOffset2d = navZone.getWorldRadiusOffset().to2D();
		float positionSquareRadius = worldRadiusOffset2d.getInvTranslated(position.to2D()).getSquareMagnitude();
		return positionSquareRadius <= navZone.getRadius() * navZone.getRadius() + radius * radius;
	}

	public final static bCVector[] CIRCLE_RADIUS_TRANSFORMS = {new bCVector(1.0f, 0.0f, 0.0f), new bCVector(-1.0f, 0.0f, 0.0f),
			new bCVector(0.0f, 0.0f, 1.0f), new bCVector(0.0f, 0.0f, -1.0f), new bCVector(0.70719999f, 0.0f, 0.70719999f),
			new bCVector(-0.70719999f, 0.0f, -0.70719999f), new bCVector(-0.70719999f, 0.0f, 0.70719999f),
			new bCVector(0.70719999f, 0.0f, -0.70719999f)};

	private boolean isInZone(NavZone navZone, bCVector position, float radius, boolean dontConsiderNavPath,
			boolean excludeWhenInInternaNegZone) {

		for (int i = -1; i < CIRCLE_RADIUS_TRANSFORMS.length; i++) {
			bCVector adjustedPosition;
			if (i == -1) {
				adjustedPosition = position;
			} else {
				adjustedPosition = CIRCLE_RADIUS_TRANSFORMS[i].getScaled(radius).translate(position);
			}

			Optional<NavArea> result = getZone(adjustedPosition, dontConsiderNavPath, excludeWhenInInternaNegZone, -1.0f);
			if (result.isPresent() && result.get().areaId.equals(navZone.getGuid())) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Get single zone for a point.
	 * <p>
	 * Example call for NegZone <br>
	 * {@code getZone(negZone.worldMatrix.GetWorldPosition(), true, false, negZone.radius)}
	 * <p>
	 * Example call for NavPath <br>
	 * {@code ZoneA = getZone(navPath.worldMatrix * navPath.points[0], true, false, -1.0f)} <br>
	 * {@code ZoneB = getZone(navPath.worldMatrix * navPath.points[navPath.points.size() - 1], true, false, -1.0f)}
	 * <p>
	 * Example call for PrefPath <br>
	 * {@code Zone = getZone(prefPath.worldMatrix.GetWorldPosition(), true, false, -1.0f)} <br>
	 */
	public Optional<NavArea> getZone(bCVector position, boolean dontConsiderNavPath, boolean excludeWhenInInternaNegZone,
			float minZoneRadius) {

		NavArea result = null;

		boolean matchedAreaLinked = false;
		NavZone matchedZone = null;
		for (NavZone navZone : listZones(position)) {
			if (minZoneRadius > 0.0 && minZoneRadius >= navZone.getRadius()) {
				continue;
			}

			NavZone.CalcResult heightResult = navZone.calcAbsHeightDiffFlagAndPrioFlag(position);
			if ((heightResult.isAreaLinked || !matchedAreaLinked && heightResult.isInZone)
					&& (matchedZone == null || navZone.getRadius() < matchedZone.getRadius())) {

				if (isInZoneRadius(navZone, position) && navZone.test3DPointInZone(position)) {
					result = new NavArea(navZone.getGuid(), false);
					matchedZone = navZone;
					matchedAreaLinked = heightResult.isAreaLinked;
				}
			}
		}

		boolean isInInternalNegZone = false;
		if (!dontConsiderNavPath) {
			if (matchedZone != null) {
				isInInternalNegZone = matchedZone.test3DPointInInternalNegZone(position, navMap.getNegZonesForZone(matchedZone.getGuid()));

				// Matched NavZone counts as linked for NavPath, if the point is not inside one of
				// its NegZones or if the NavZone is really linked.
				matchedAreaLinked = matchedAreaLinked || !isInInternalNegZone;

			}
			// Linked NavZone has always priority over NavPath.
			if (matchedZone == null || isInInternalNegZone || !matchedAreaLinked) {
				for (NavPath navPath : listPaths(position)) {
					NavPath.CalcResult heightResult = navPath.calcAbsHeightDiffFlagAndPrioFlag(position);
					// NavZone has always priority over NavPath, unless NavPath is linked or NavZone
					// is not linked and the point lies in one of its NegZones!
					if (heightResult.isAreaLinked || !matchedAreaLinked && heightResult.isInPath) {
						if (navPath.test3DPointOnPath(position)) {
							result = new NavArea(navPath.guid, true);
							// We take the first matching NavPath we find.
							break;
						}
					}
				}
			}
		}

		// When "exclude internal neg zone" and the matched area is a NavZone
		if (excludeWhenInInternaNegZone && matchedZone != null && !result.isNavPath) {
			// Reuse result from NavPath calculation or do the test.
			if (isInInternalNegZone || dontConsiderNavPath
					&& matchedZone.test3DPointInInternalNegZone(position, navMap.getNegZonesForZone(matchedZone.getGuid()))) {
				matchedZone = null;
				result = null;
			}
		}

		return Optional.ofNullable(result);
	}

	/**
	 * Get all zones for a point and radius (CollisionCircle).
	 * <p>
	 * Example call for CollisionCircle <br>
	 * {@code getZone(negCircle.worldMatrix * negCircle.Offset[0], 0.9300000071525574f * negCircle.Radius[0], false, false)}
	 */
	public List<NavArea> getZone(bCVector position, float radius, boolean dontConsiderNavPath, boolean excludeWhenInInternaNegZone) {
		List<NavArea> result = new ArrayList<>();

		for (NavZone navZone : listZones(position)) {
			if (isCircleInZoneRadius(navZone, position, radius)
					&& isInZone(navZone, position, radius, true, excludeWhenInInternaNegZone)) {
				result.add(new NavArea(navZone.getGuid(), false));
			}
		}

		if (!dontConsiderNavPath) {
			getNavPathsForCircle(position, radius, result);
		}

		return result;
	}

	private void getNavPathsForCircle(bCVector position, float radius, List<NavArea> result) {
		for (int i = -1; i < CIRCLE_RADIUS_TRANSFORMS.length; i++) {
			bCVector adjustedPosition;
			if (i == -1) {
				adjustedPosition = position;
			} else {
				adjustedPosition = CIRCLE_RADIUS_TRANSFORMS[i].getScaled(radius).translate(position);
			}

			for (NavPath navPath : listPaths(adjustedPosition)) {
				NavPath.CalcResult heightResult = navPath.calcAbsHeightDiffFlagAndPrioFlag(adjustedPosition);
				if (heightResult.isAreaLinked || heightResult.isInPath) {
					if (navPath.test3DPointOnPath(adjustedPosition)) {
						if (result.stream().noneMatch(p -> p.areaId.equals(navPath.guid))) {
							result.add(new NavArea(navPath.guid, true));
						}
					}
				}
			}
		}
	}

	/**
	 * Get all zones for two points and radius (CollisionCircle).
	 * <p>
	 * Example call for CollisionCircle <br>
	 * {@code getZone(negCircle.worldMatrix * negCircle.Offset[0], 0.9300000071525574f * negCircle.Radius[0], negCircle.worldMatrix * negCircle.Offset[1], 0.9300000071525574f * negCircle.Radius[1] false, false)}
	 */
	public List<NavArea> getZone(bCVector positionA, float radiusA, bCVector positionB, float radiusB, boolean dontConsiderNavPath,
			boolean excludeWhenInInternaNegZone) {
		List<NavArea> result = new ArrayList<>();

		for (NavZone navZone : getNavZones()) {

			bCVector position;
			float radius;

			if (isCircleInZoneRadius(navZone, positionA, radiusA)) {
				position = positionA;
				radius = radiusA;
			} else if (isCircleInZoneRadius(navZone, positionB, radiusB)) {
				position = positionB;
				radius = radiusB;
			} else {
				continue;
			}

			if (isInZone(navZone, position, radius, true, excludeWhenInInternaNegZone)) {
				result.add(new NavArea(navZone.getGuid(), false));
			}
		}

		if (!dontConsiderNavPath) {
			getNavPathsForCircle(positionA, radiusA, result);
			getNavPathsForCircle(positionB, radiusB, result);
		}

		return result;
	}

	/**
	 * Get zone for entity. When an entity has the gCNavOffset_PS property set, it is handled
	 * special.
	 * <p>
	 * Example call for all interact objects (without NPCS & items) <br>
	 * Entities with gCInteraction_PS, but without gCNavigation_PS and gCItem_PS
	 */
	public Optional<NavArea> getZone(eCEntity entity, boolean dontConsiderNavPath, boolean excludeWhenInInternaNegZone) {
		if (!entity.hasClass(CD.gCNavOffset_PS.class) || entity.getProperty(CD.gCNavOffset_PS.OffsetCircle).isBool()) {
			return getZone(entity.getWorldPosition(), dontConsiderNavPath, excludeWhenInInternaNegZone, -1.0f);
		}

		List<bCMotion> offsetPose = entity.getProperty(CD.gCNavOffset_PS.OffsetPose).getEntries();
		if (offsetPose.size() >= 1) {
			for (bCMotion offset : offsetPose) {
				bCVector WorldOffsetPose = offset.getPosition().getTransformed(entity.getWorldMatrix());
				Optional<NavArea> area = getZone(WorldOffsetPose, dontConsiderNavPath, excludeWhenInInternaNegZone, -1.0f);
				if (area.isPresent()) {
					return area;
				}
			}
			return Optional.empty();
		} else {
			return getZone(entity.getWorldPosition(), dontConsiderNavPath, excludeWhenInInternaNegZone, -1.0f);
		}
	}

	/**
	 * Gets the NavZone for a NegZone. More robust implementation than the one used by Gothic 3.
	 * <p>
	 * Instead of only checking the world position of the NegZone, this implementation detects a
	 * zone for every stick. The zone that contains tha majority of the sticks wins.
	 *
	 * @param negZone
	 * @return
	 */
	public Optional<NavArea> getZone(NegZone negZone) {
		// Gothic 3 implementation (but we don't have the worldMatrix)
		// NavArea navZone = getZone(negZone.worldMatrix.getWorldPosition(), true, false,
		// negZone.getRadius()).orElse(null);

		Multiset<NavArea> zoneVoting = HashMultiset.create();
		getZone(negZone.getWorldRadiusOffset(), true, false, negZone.getRadius()).ifPresent(zoneVoting::add);

		for (bCVector stick : negZone.getWorldPoints()) {
			getZone(stick, true, false, -1.0f).ifPresent(zoneVoting::add);

		}

		boolean tie = false;
		NavArea bestZone = null;
		for (NavArea zone : zoneVoting.elementSet()) {
			if (bestZone == null || zoneVoting.count(zone) >= zoneVoting.count(bestZone)) {
				tie = zoneVoting.count(zone) == zoneVoting.count(bestZone);
				bestZone = zone;
			}
		}

		return bestZone != null && !tie ? Optional.of(bestZone) : Optional.empty();
	}

	public String getNavZoneGuid(NegZone negZone) {
		return getZone(negZone).filter(a -> !a.isNavPath).map(a -> a.areaId).orElse(NavMap.INVALID_ZONE_ID);
	}

	/**
	 * @return true if the point does not collide with any NegCircles
	 */
	public boolean testPointAgainstNegCircles(String navZone, bCVector point) {
		for (NegCircle negCircle : navMap.getNegCirclesForZone(navZone)) {
			if (negCircle.intersectsPoint(point)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @return true if the point does not collide with any NegZones
	 */
	public boolean testPointAgainstNegZones(String navZone, bCVector point) {
		for (NegZone negZone : navMap.getNegZonesForZone(navZone)) {
			if (isInZoneRadius(negZone, point) && negZone.test3DPointInZone(point)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @param start
	 * @param end
	 * @param worldMatrix
	 * @param navZone If {@code null} the intersection 0/0/0// is returned.
	 * @return
	 */
	private bCVector calcIntersection(bCVector start, bCVector end, bCMatrix worldMatrix, NavZone navZone) {
		if (navZone == null) {
			return bCVector.nullVector();
		}

		bCVector worldStart = start.getTransformed(worldMatrix);
		bCVector worldEnd = end.getTransformed(worldMatrix);

		double initialDistance = Math.max(worldEnd.getInvTranslated(worldStart).length() - 0.1000000014901161d, 0.0d);
		double distance = initialDistance;
		Optional<Float> navZoneIntersect = navZone.intersectsStretch(worldStart, worldEnd, true);
		if (navZoneIntersect.isPresent() && navZoneIntersect.get() < distance) {
			distance = navZoneIntersect.get();
		}

		for (NegZone negZone : navMap.getNegZonesForZone(navZone.getGuid())) {
			Optional<Float> negZoneIntersect = negZone.intersectsStretch(worldStart, worldEnd, true);
			if (negZoneIntersect.isPresent() && negZoneIntersect.get() < distance) {
				distance = negZoneIntersect.get();
			}
		}

		if (distance < initialDistance) {
			bCVector stretch = end.getInvTranslated(start).normalize(); // NormalizeSafe
			return start.getTranslated(stretch.getScaled((float) (distance - 1.0)));
		} else {
			// No intersection.
			return start.clone();
		}
	}

	public enum IntersectZone {
		ZoneA,
		ZoneB
	}

	public Optional<String> detectZoneForNavPath(NavPath navPath, IntersectZone zone) {
		if (navPath.getPoints().size() < 2) {
			return Optional.empty();
		}

		int index = zone == IntersectZone.ZoneA ? 0 : navPath.getPoints().size() - 1;
		return getZone(navPath.getWorldPoint(index), true, false, -1.0f).map(a -> a.areaId);
	}

	public ZonePathIntersection calcZonePathIntersection(List<bCVector> points, List<Float> radius, bCMatrix worldMatrix, String zoneGuid,
			IntersectZone zone) {
		NavZone navZone = lookupNavZone.apply(zoneGuid).orElse(null);

		int startIndex = zone == IntersectZone.ZoneA ? 0 : points.size() - 1;
		int endIndex = zone == IntersectZone.ZoneA ? 1 : points.size() - 2;
		bCVector start = points.get(startIndex);
		bCVector end = points.get(endIndex);
		float radiusStart = radius.get(startIndex);
		float radiusEnd = radius.get(endIndex);
		bCVector zoneIntersectionCenter = calcIntersection(start, end, worldMatrix, navZone);

		bCVector dir = end.getInvTranslated(start).normalize();
		bCVector dirMargin = dir.getCrossProduct(new bCVector(0.0f, 1.0f, 0.0f)).normalize();

		bCVector startMargin1 = start.getTranslated(dirMargin.getScaled(radiusStart));
		bCVector endMargin1 = end.getTranslated(dirMargin.getScaled(radiusEnd));
		bCVector zoneIntersectionMargin1 = calcIntersection(startMargin1, endMargin1, worldMatrix, navZone);

		bCVector startMargin2 = start.getInvTranslated(dirMargin.getScaled(radiusStart));
		bCVector endMargin2 = end.getInvTranslated(dirMargin.getScaled(radiusEnd));
		bCVector zoneIntersectionMargin2 = calcIntersection(startMargin2, endMargin2, worldMatrix, navZone);

		return new ZonePathIntersection(zoneIntersectionCenter, zoneIntersectionMargin1, zoneIntersectionMargin2);
	}

	/**
	 * @param path
	 * @param zone Calc intersection for ZoneA if {@code true}, otherwise ZoneB
	 * @return
	 */
	public ZonePathIntersection calcZonePathIntersection(NavPath path, IntersectZone zone) {
		return calcZonePathIntersection(path.getPoints(), path.getRadius(), path.getWorldMatrix(),
				zone == IntersectZone.ZoneA ? path.zoneAGuid : path.zoneBGuid, zone);

	}
}

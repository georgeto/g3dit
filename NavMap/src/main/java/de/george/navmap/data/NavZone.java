package de.george.navmap.data;

import java.util.List;
import java.util.stream.Collectors;

import de.george.g3utils.structure.bCMatrix;
import de.george.g3utils.structure.bCVector;
import de.george.g3utils.structure.bCVector2;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.classes.G3Class;
import de.george.lrentnode.classes.desc.CD;

public class NavZone extends Zone {
	private float topTolerance;
	private float bottomTolerance;
	private boolean linkInnerArea;
	private boolean linkInnerTopArea;
	private boolean linkInnerBottomArea;

	// For caching
	private transient boolean zoneHeightsValid;
	private transient float meanY = 0.0f;
	// Both positive
	private transient float minYOffset = -1.0f;
	private transient float maxYOffset = -1.0f;

	private transient boolean worldRadiusOffsetValid;
	private transient bCVector worldRadiusOffset;

	public NavZone(String guid, List<bCVector> stickList, bCMatrix worldMatrix, float radius, bCVector radiusOffset, boolean ccw,
			float topTolerance, float bottomTolerance, boolean linkInnerArea, boolean linkInnerTopArea, boolean linkInnerBottomArea) {
		super(guid, stickList, worldMatrix, radius, radiusOffset, ccw);
		this.topTolerance = topTolerance;
		this.bottomTolerance = bottomTolerance;
		this.linkInnerArea = linkInnerArea;
		this.linkInnerTopArea = linkInnerTopArea;
		this.linkInnerBottomArea = linkInnerBottomArea;

		// Cache WorldRadiusOffset
		getWorldRadiusOffset();
	}

	public static NavZone fromArchiveEntity(eCEntity entity) {
		G3Class navZone = entity.getClass(CD.gCNavZone_PS.class);

		if (navZone == null) {
			return null;
		}

		float radius = navZone.property(CD.gCNavZone_PS.Radius).getFloat();
		bCVector radiusOffset = navZone.property(CD.gCNavZone_PS.RadiusOffset).clone();
		bCMatrix worldMatrix = entity.getWorldMatrix();
		List<bCVector> sticks = navZone.property(CD.gCNavZone_PS.Point).getEntries(v -> v.getTransformed(worldMatrix));
		boolean ccw = navZone.property(CD.gCNavZone_PS.ZoneIsCCW).isBool();
		float topTolerance = navZone.property(CD.gCNavZone_PS.TopToleranz).getFloat();
		float bottomTolerance = navZone.property(CD.gCNavZone_PS.BottomToleranz).getFloat();
		boolean linkInnerArea = navZone.property(CD.gCNavZone_PS.LinkInnerArea).isBool();
		boolean linkInnerTopArea = navZone.property(CD.gCNavZone_PS.LinkInnerTopArea).isBool();
		boolean linkInnerBottomArea = navZone.property(CD.gCNavZone_PS.LinkInnerBottomArea).isBool();

		return new NavZone(entity.getGuid(), sticks, worldMatrix, radius, radiusOffset, ccw, topTolerance, bottomTolerance, linkInnerArea,
				linkInnerTopArea, linkInnerBottomArea);
	}

	private synchronized boolean calcZoneHeights(boolean force) {
		// Don't do unnecessary calculations in a multi-threaded environment.
		if (zoneHeightsValid && !force) {
			return true;
		}

		float meanY = 0.0f;
		float minYOffset = 0.0f;
		float maxYOffset = 0.0f;

		boolean result = false;
		if (!points.isEmpty()) {
			meanY = points.get(0).getY();
			minYOffset = points.get(0).getY();
			maxYOffset = points.get(0).getY();

			for (int i = 1; i < points.size(); i++) {
				bCVector stick = points.get(i);
				meanY += stick.getY();
				if (minYOffset > stick.getY()) {
					minYOffset = stick.getY();
				}
				if (maxYOffset < stick.getY()) {
					maxYOffset = stick.getY();
				}
			}

			meanY /= points.size();
			minYOffset = meanY - minYOffset;
			maxYOffset = maxYOffset - meanY;
			result = true;
		}

		this.meanY = meanY;
		this.minYOffset = minYOffset;
		this.maxYOffset = maxYOffset;
		zoneHeightsValid = true;
		return result;
	}

	public static class CalcResult {
		public boolean isInZone;
		public boolean isAreaLinked;
	}

	public CalcResult calcAbsHeightDiffFlagAndPrioFlag(bCVector position) {
		CalcResult result = new CalcResult();

		if (!zoneHeightsValid) {
			calcZoneHeights(false);
		}

		float minZoneY = meanY - minYOffset - bottomTolerance;
		float maxZoneY = meanY + maxYOffset + topTolerance;

		if (position.getY() <= maxZoneY && position.getY() >= minZoneY) {
			result.isInZone = true;
			result.isAreaLinked = linkInnerArea;
		} else {
			result.isInZone = false;
			if (position.getY() < minZoneY) {
				result.isAreaLinked = linkInnerBottomArea;
			} else {
				result.isAreaLinked = linkInnerTopArea;
			}
		}

		return result;
	}

	@Override
	public NavZone clone() {
		return new NavZone(getGuid(), getPoints().stream().map(bCVector::clone).collect(Collectors.toList()), getWorldMatrix(),
				getRadius(), getRadiusOffset().clone(), isCcw(), topTolerance, bottomTolerance, linkInnerArea, linkInnerTopArea,
				linkInnerBottomArea);
	}

	public boolean test3DPointInInternalNegZone(bCVector point, Iterable<NegZone> negZones) {
		bCVector2 point2d = point.to2D();
		for (NegZone negZone : negZones) {
			float distance = negZone.getWorldRadiusOffset().to2D().invTranslate(point2d).length();
			if (distance <= negZone.getRadius() && negZone.test3DPointInZone(point)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public bCVector getWorldRadiusOffset() {
		if (!worldRadiusOffsetValid) {
			worldRadiusOffset = radiusOffset.getTransformed(worldMatrix);
			worldRadiusOffsetValid = true;
		}
		return worldRadiusOffset;
	}

	@Override
	public List<bCVector> getWorldPoints() {
		return getPoints();
	}
}

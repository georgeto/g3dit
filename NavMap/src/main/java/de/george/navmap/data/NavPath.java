package de.george.navmap.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.math.Vector2D;
import com.vividsolutions.jts.operation.polygonize.Polygonizer;
import com.vividsolutions.jts.util.GeometricShapeFactory;

import de.george.g3utils.structure.bCInfCylinder;
import de.george.g3utils.structure.bCInfDoubleCylinder;
import de.george.g3utils.structure.bCMatrix;
import de.george.g3utils.structure.bCVector;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.classes.G3Class;
import de.george.lrentnode.classes.desc.CD;
import de.george.navmap.util.GeoUtil;

public class NavPath {
	public String guid, zoneAGuid, zoneBGuid;
	private bCMatrix worldMatrix;
	// Relative ZoneIntersections
	public ZonePathIntersection zoneAIntersection, zoneBIntersection;
	// Relative Positionen der Sticks
	private List<bCVector> points;
	private List<Float> radius;

	private float topTolerance;
	private float bottomTolerance;
	private boolean linkInnerArea;
	private boolean linkInnerTopArea;
	private boolean linkInnerBottomArea;

	// For caching
	private transient boolean invertedWorldMatrixValid;
	private transient bCMatrix invertedWorldMatrix;

	private transient boolean pathHeightsValid;
	private transient float meanY = 0.0f;
	// Both positive
	private transient float minYOffset = -1.0f;
	private transient float maxYOffset = -1.0f;

	public NavPath(String guid, List<bCVector> sticks, List<Float> radius, bCMatrix worldMatrix) {
		this.guid = guid;
		points = sticks;
		this.radius = radius;
		this.worldMatrix = worldMatrix;
	}

	public NavPath(String guid, List<bCVector> sticks, List<Float> radius, bCMatrix worldMatrix, float topTolerance, float bottomTolerance,
			boolean linkInnerArea, boolean linkInnerTopArea, boolean linkInnerBottomArea) {
		this(guid, sticks, radius, worldMatrix);
		this.topTolerance = topTolerance;
		this.bottomTolerance = bottomTolerance;
		this.linkInnerArea = linkInnerArea;
		this.linkInnerTopArea = linkInnerTopArea;
		this.linkInnerBottomArea = linkInnerBottomArea;
	}

	public String getGuid() {
		return guid;
	}

	public List<bCVector> getPoints() {
		return points;
	}

	public bCVector getWorldPoint(int index) {
		return points.get(index).getTransformed(worldMatrix);
	}

	public List<bCVector> getWorldPoints() {
		return points.stream().map(v -> v.getTransformed(worldMatrix)).collect(Collectors.toList());
	}

	public List<Float> getRadius() {
		return radius;
	}

	public float getLength() {
		float sum = 0;
		for (int i = 1; i < points.size(); i++) {
			sum += points.get(i - 1).getRelative(points.get(i)).length();
		}
		return sum;
	}

	public bCMatrix getWorldMatrix() {
		return worldMatrix;
	}

	public bCMatrix getInvertedWorldMatrix() {
		if (!invertedWorldMatrixValid) {
			invertedWorldMatrix = worldMatrix.getInverted();
			invertedWorldMatrixValid = true;
		}
		return invertedWorldMatrix;
	}

	public static NavPath fromArchiveEntity(eCEntity entity) {
		G3Class pathClass = entity.getClass(CD.gCNavPath_PS.class);

		if (pathClass == null) {
			return null;
		}

		bCMatrix worldMatrix = entity.getWorldMatrix();

		List<bCVector> points = pathClass.property(CD.gCNavPath_PS.Point).getEntries(bCVector::clone);
		List<Float> radius = pathClass.property(CD.gCNavPath_PS.Radius).getNativeEntries();
		float topTolerance = pathClass.property(CD.gCNavPath_PS.TopToleranz).getFloat();
		float bottomTolerance = pathClass.property(CD.gCNavPath_PS.BottomToleranz).getFloat();
		boolean linkInnerArea = pathClass.property(CD.gCNavPath_PS.LinkInnerArea).isBool();
		boolean linkInnerTopArea = pathClass.property(CD.gCNavPath_PS.LinkInnerTopArea).isBool();
		boolean linkInnerBottomArea = pathClass.property(CD.gCNavPath_PS.LinkInnerBottomArea).isBool();

		NavPath navPath = new NavPath(entity.getGuid(), points, radius, worldMatrix, topTolerance, bottomTolerance, linkInnerArea,
				linkInnerTopArea, linkInnerBottomArea);

		navPath.zoneAGuid = pathClass.property(CD.gCNavPath_PS.ZoneAEntityID).getGuid();
		navPath.zoneBGuid = pathClass.property(CD.gCNavPath_PS.ZoneBEntityID).getGuid();

		navPath.zoneAIntersection = new ZonePathIntersection();
		navPath.zoneAIntersection.zoneIntersectionMargin1 = pathClass.property(CD.gCNavPath_PS.ZoneAIntersectionMargin1);
		navPath.zoneAIntersection.zoneIntersectionMargin2 = pathClass.property(CD.gCNavPath_PS.ZoneAIntersectionMargin2);
		navPath.zoneAIntersection.zoneIntersectionCenter = pathClass.property(CD.gCNavPath_PS.ZoneAIntersectionCenter);

		navPath.zoneBIntersection = new ZonePathIntersection();
		navPath.zoneBIntersection.zoneIntersectionMargin1 = pathClass.property(CD.gCNavPath_PS.ZoneBIntersectionMargin1);
		navPath.zoneBIntersection.zoneIntersectionMargin2 = pathClass.property(CD.gCNavPath_PS.ZoneBIntersectionMargin2);
		navPath.zoneBIntersection.zoneIntersectionCenter = pathClass.property(CD.gCNavPath_PS.ZoneBIntersectionCenter);

		return navPath;
	}

	public void toArchiveEntity(eCEntity entity) {
		G3Class pathClass = entity.getClass(CD.gCNavPath_PS.class);
		pathClass.setPropertyData(CD.gCNavPath_PS.ZoneAIntersectionMargin1, zoneAIntersection.zoneIntersectionMargin1);
		pathClass.setPropertyData(CD.gCNavPath_PS.ZoneAIntersectionMargin2, zoneAIntersection.zoneIntersectionMargin2);
		pathClass.setPropertyData(CD.gCNavPath_PS.ZoneAIntersectionCenter, zoneAIntersection.zoneIntersectionCenter);
		pathClass.setPropertyData(CD.gCNavPath_PS.ZoneBIntersectionMargin1, zoneBIntersection.zoneIntersectionMargin1);
		pathClass.setPropertyData(CD.gCNavPath_PS.ZoneBIntersectionMargin2, zoneBIntersection.zoneIntersectionMargin2);
		pathClass.setPropertyData(CD.gCNavPath_PS.ZoneBIntersectionCenter, zoneBIntersection.zoneIntersectionCenter);

		pathClass.property(CD.gCNavPath_PS.ZoneAEntityID).setGuid(zoneAGuid);
		pathClass.property(CD.gCNavPath_PS.ZoneBEntityID).setGuid(zoneBGuid);
	}

	@Override
	public NavPath clone() {
		NavPath clone = new NavPath(guid, points.stream().map(bCVector::clone).collect(Collectors.toList()), new ArrayList<>(radius),
				worldMatrix.clone(), topTolerance, bottomTolerance, linkInnerArea, linkInnerTopArea, linkInnerBottomArea);
		clone.zoneAGuid = zoneAGuid;
		clone.zoneAIntersection = zoneAIntersection.clone();
		clone.zoneBGuid = zoneBGuid;
		clone.zoneBIntersection = zoneBIntersection.clone();
		return clone;
	}

	public static class ZonePathIntersection {
		public bCVector zoneIntersectionCenter;
		public bCVector zoneIntersectionMargin1;
		public bCVector zoneIntersectionMargin2;

		public ZonePathIntersection() {}

		public ZonePathIntersection(bCVector zoneIntersectionCenter, bCVector zoneIntersectionMargin1, bCVector zoneIntersectionMargin2) {
			this.zoneIntersectionCenter = zoneIntersectionCenter;
			this.zoneIntersectionMargin1 = zoneIntersectionMargin1;
			this.zoneIntersectionMargin2 = zoneIntersectionMargin2;
		}

		public void transform(bCMatrix mat) {
			zoneIntersectionCenter.transform(mat);
			zoneIntersectionMargin1.transform(mat);
			zoneIntersectionMargin2.transform(mat);
		}

		@Override
		public String toString() {
			return "ZonePathIntersection [zoneIntersectionCenter=" + zoneIntersectionCenter + ", zoneIntersectionMargin1="
					+ zoneIntersectionMargin1 + ", zoneIntersectionMargin2=" + zoneIntersectionMargin2 + "]";
		}

		@Override
		public ZonePathIntersection clone() {
			ZonePathIntersection zpi = new ZonePathIntersection();
			zpi.zoneIntersectionCenter = zoneIntersectionCenter.clone();
			zpi.zoneIntersectionMargin1 = zoneIntersectionMargin1.clone();
			zpi.zoneIntersectionMargin2 = zoneIntersectionMargin2.clone();
			return zpi;
		}
	}

	private synchronized boolean calcPathHeights(boolean force) {
		// Don't do unnecessary calculations in a multi-threaded environment.
		if (pathHeightsValid && !force) {
			return true;
		}

		float meanY = 0.0f;
		float minYOffset = 0.0f;
		float maxYOffset = 0.0f;

		boolean result = false;
		if (!points.isEmpty()) {
			List<bCVector> worldPoints = getWorldPoints();
			meanY = worldPoints.get(0).getY();
			minYOffset = worldPoints.get(0).getY();
			maxYOffset = worldPoints.get(0).getY();

			for (int i = 1; i < worldPoints.size(); i++) {
				bCVector stick = worldPoints.get(i);
				meanY += stick.getY();
				if (minYOffset > stick.getY()) {
					minYOffset = stick.getY();
				}
				if (maxYOffset < stick.getY()) {
					maxYOffset = stick.getY();
				}
			}

			meanY /= worldPoints.size();
			minYOffset = meanY - minYOffset;
			maxYOffset = maxYOffset - meanY;
			result = true;
		}

		this.meanY = meanY;
		this.minYOffset = minYOffset;
		this.maxYOffset = maxYOffset;
		pathHeightsValid = true;

		return result;
	}

	public static class CalcResult {
		public boolean isInPath;
		public boolean isAreaLinked;
	}

	public CalcResult calcAbsHeightDiffFlagAndPrioFlag(bCVector position) {
		CalcResult result = new CalcResult();

		if (!pathHeightsValid) {
			calcPathHeights(false);
		}

		float minZoneY = meanY - minYOffset - bottomTolerance;
		float maxZoneY = meanY + maxYOffset + topTolerance;

		if (position.getY() <= maxZoneY && position.getY() >= minZoneY) {
			result.isInPath = true;
			result.isAreaLinked = linkInnerArea;
		} else {
			result.isInPath = false;
			if (position.getY() < minZoneY) {
				result.isAreaLinked = linkInnerBottomArea;
			} else {
				result.isAreaLinked = linkInnerTopArea;
			}
		}

		return result;
	}

	public boolean test3DPointOnPath(bCVector point) {
		point = point.getTransformed(getInvertedWorldMatrix());

		if (points.size() > 1) {
			int containingSegment = -1;
			for (int i = 0; i + 1 < points.size(); i++) {
				bCInfDoubleCylinder segment = new bCInfDoubleCylinder(radius.get(i), points.get(i), radius.get(i + 1), points.get(i + 1));
				if (segment.contains(point)) {
					containingSegment = i;
					break;
				}
			}

			// No segement contains the point
			if (containingSegment == -1) {
				return false;
			}

			// The intersecting segment is the first segment of the path, so we have to
			// test whether the point lies in ZoneA.
			if (containingSegment == 0 && zoneAGuid != null) {
				if (!zoneAIntersection.zoneIntersectionMargin1.equals(zoneAIntersection.zoneIntersectionMargin2)) {
					bCVector vecM1M2 = zoneAIntersection.zoneIntersectionMargin2
							.getInvTranslated(zoneAIntersection.zoneIntersectionMargin1).setY(0);
					bCVector vecM1PathPoint = points.get(1).getInvTranslated(zoneAIntersection.zoneIntersectionMargin1).setY(0);
					bCVector vecM1Point = point.getInvTranslated(zoneAIntersection.zoneIntersectionMargin1).setY(0);

					// Ensure that it the vector is only on the path and not in the zone (on the
					// same side of MarginA as the rest of path, therefore not in the zone).
					if (vecM1M2.getCrossProduct(vecM1PathPoint).getY() * vecM1M2.getCrossProduct(vecM1Point).getY() < 0) {
						// Point lies in ZoneA
						return false;
					}
				}
			}

			// The intersecting segment is the last segment of the path, so we have to
			// test whether the point lies in ZoneA.
			if (containingSegment == points.size() - 2 && zoneBGuid != null) {
				if (!zoneBIntersection.zoneIntersectionMargin1.equals(zoneBIntersection.zoneIntersectionMargin2)) {
					bCVector vecM1M2 = zoneBIntersection.zoneIntersectionMargin2
							.getInvTranslated(zoneBIntersection.zoneIntersectionMargin1).setY(0);
					bCVector vecM1PathPoint = points.get(points.size() - 2).getInvTranslated(zoneBIntersection.zoneIntersectionMargin1)
							.setY(0);
					bCVector vecM1Point = point.getInvTranslated(zoneBIntersection.zoneIntersectionMargin1).setY(0);

					// Ensure that it the vector is only on the path and not in the zone (on the
					// same side of MarginB as the rest of path, therefore not in the zone).
					if (vecM1M2.getCrossProduct(vecM1PathPoint).getY() * vecM1M2.getCrossProduct(vecM1Point).getY() < 0) {
						// Point lies in ZoneB
						return false;
					}
				}
			}

			// Point lies on path
			return true;
		} else if (points.size() == 1) {
			// Degenerative NavPath with one point
			return new bCInfCylinder(radius.get(0), points.get(0)).contains(point);
		} else {
			return false;
		}
	}

	private Geometry linearString, polygon;
	private boolean invalidPolygon = false;

	public Geometry getLinearString() {
		if (linearString == null) {
			linearString = createLinearString();
		}
		return linearString;
	}

	public Geometry getPolygon() {
		if (polygon == null && !invalidPolygon) {
			polygon = createPolygon();
		}
		return polygon;
	}

	private Geometry createLinearString() {
		return GeoUtil.FACTORY.createLineString(GeoUtil.create2DCoordinateArray(getWorldPoints()));
	}

	@SuppressWarnings("unchecked")
	private Geometry createPolygon() {
		if (points.size() < 2) {
			return null;
		}

		try {
			GeometricShapeFactory shapeFactory = new GeometricShapeFactory();

			List<Geometry> geometries = new ArrayList<>();

			List<bCVector> worldPoints = getWorldPoints();
			bCVector lastStick = worldPoints.get(0);
			float lastRadius = radius.get(0);
			Coordinate lastStickPostion = GeoUtil.to2DCoordinate(lastStick);
			for (int i = 1; i < worldPoints.size(); i++) {
				bCVector curStick = worldPoints.get(i);
				float curRadius = radius.get(i);
				Coordinate curStickPositon = GeoUtil.to2DCoordinate(curStick);

				Vector2D g12 = new Vector2D(lastStickPostion, curStickPositon).normalize();
				LinearRing pathRect = GeoUtil.createLinearRing(
						g12.rotateByQuarterCircle(-1).multiply(lastRadius).translate(lastStickPostion),
						g12.rotateByQuarterCircle(1).multiply(lastRadius).translate(lastStickPostion),
						g12.rotateByQuarterCircle(1).multiply(curRadius).translate(curStickPositon),
						g12.rotateByQuarterCircle(-1).multiply(curRadius).translate(curStickPositon));

				geometries.add(pathRect);

				if (i < worldPoints.size() - 1) {
					shapeFactory.setCentre(curStickPositon);
					shapeFactory.setSize(curRadius * 2);
					geometries.add(shapeFactory.createCircle().getExteriorRing());
				}

				lastStick = curStick;
				lastStickPostion = curStickPositon;
			}

			Polygonizer polygonizer = new Polygonizer();
			polygonizer.add(geometries);

			return GeoUtil.FACTORY.createGeometryCollection(((Collection<Geometry>) polygonizer.getPolygons()).toArray(new Geometry[0]))
					.union();
		} catch (Exception e) {
			invalidPolygon = true;
			return null;
		}
	}
}

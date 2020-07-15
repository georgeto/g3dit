package de.george.navmap.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.math.Vector2D;
import com.vividsolutions.jts.operation.polygonize.Polygonizer;
import com.vividsolutions.jts.util.GeometricShapeFactory;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.io.G3Serializable;
import de.george.g3utils.structure.GuidUtil;
import de.george.g3utils.structure.bCVector;
import de.george.navmap.util.GeoUtil;

public class PrefPath implements G3Serializable {
	private static final Logger logger = LoggerFactory.getLogger(PrefPath.class);

	// Wird nicht in NavMap persistiert, dient lediglich zur Identifizierung
	private String virtualGuid;

	private List<bCVector> points;
	private List<Float> pointRadius;
	private float radius;
	private bCVector radiusOffset;
	private String zoneGuid;

	private Geometry linearString, polygon;
	private boolean invalidPolygon = false;

	public PrefPath(List<bCVector> points, List<Float> pointRadius, String zoneGuid) {
		setPoints(points, pointRadius);
		this.zoneGuid = zoneGuid;
		virtualGuid = GuidUtil.randomGUID();
	}

	public PrefPath(List<bCVector> points, List<Float> pointRadius, float radius, bCVector radiusOffset, String zoneGuid) {
		this(points, pointRadius, radius, radiusOffset, zoneGuid, GuidUtil.randomGUID());
	}

	private PrefPath(List<bCVector> points, List<Float> pointRadius, float radius, bCVector radiusOffset, String zoneGuid,
			String virtualGuid) {
		this.points = points;
		this.pointRadius = pointRadius;
		this.radius = radius;
		this.radiusOffset = radiusOffset;
		this.zoneGuid = zoneGuid;
		this.virtualGuid = virtualGuid;
	}

	public List<bCVector> getPoints() {
		return points;
	}

	public List<Float> getPointRadius() {
		return pointRadius;
	}

	public float getRadius() {
		return radius;
	}

	public bCVector getRadiusOffset() {
		return radiusOffset;
	}

	public String getZoneGuid() {
		return zoneGuid;
	}

	public void setZoneGuid(String zoneGuid) {
		this.zoneGuid = zoneGuid;
	}

	public void setPoints(List<bCVector> points, List<Float> pointRadius) {
		this.points = points;
		this.pointRadius = pointRadius;

		radiusOffset = bCVector.averageVector(points);
		radius = 0;
		for (int i = 0; i < this.points.size(); i++) {
			radius = Math.max(radiusOffset.getRelative(this.points.get(i)).length() + this.pointRadius.get(i), radius);
		}

		linearString = null;
		polygon = null;
	}

	public String getVirtualGuid() {
		return virtualGuid;
	}

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
		return GeoUtil.FACTORY.createLineString(GeoUtil.create2DCoordinateArray(points));
	}

	@SuppressWarnings("unchecked")
	private Geometry createPolygon() {
		if (points.size() < 1 || points.size() != pointRadius.size()) {
			return null;
		}

		try {
			GeometricShapeFactory shapeFactory = new GeometricShapeFactory();

			if (points.size() == 1) {
				shapeFactory.setCentre(GeoUtil.to2DCoordinate(points.get(0)));
				shapeFactory.setSize(pointRadius.get(0) * 2);
				return shapeFactory.createCircle();
			}

			List<Geometry> geometries = new ArrayList<>();

			Iterator<bCVector> stickIterator = points.iterator();
			Iterator<Float> pointRadiusIterator = pointRadius.iterator();
			bCVector lastStick = stickIterator.next();
			float lastStickRadius = pointRadiusIterator.next();
			Coordinate lastStickPostion = GeoUtil.to2DCoordinate(lastStick);
			while (stickIterator.hasNext()) {
				bCVector curStick = stickIterator.next();
				float curStickRadius = pointRadiusIterator.next();
				Coordinate curStickPositon = GeoUtil.to2DCoordinate(curStick);

				Vector2D g12 = new Vector2D(lastStickPostion, curStickPositon).normalize();
				LinearRing pathRect = GeoUtil.createLinearRing(
						g12.rotateByQuarterCircle(-1).multiply(lastStickRadius).translate(lastStickPostion),
						g12.rotateByQuarterCircle(1).multiply(lastStickRadius).translate(lastStickPostion),
						g12.rotateByQuarterCircle(1).multiply(curStickRadius).translate(curStickPositon),
						g12.rotateByQuarterCircle(-1).multiply(curStickRadius).translate(curStickPositon));

				geometries.add(pathRect);

				if (stickIterator.hasNext() && pointRadiusIterator.hasNext()) {
					shapeFactory.setCentre(curStickPositon);
					shapeFactory.setSize(curStickRadius * 2);
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
			logger.warn("Für PrefPath {} - {} konnte kein Polygon erstellt werden.", virtualGuid, radiusOffset.toMarvinString());
			return null;
		}
	}

	@Override
	public PrefPath clone() {
		return new PrefPath(points.stream().map(p -> p.clone()).collect(Collectors.toList()), new ArrayList<>(pointRadius), radius,
				radiusOffset.clone(), zoneGuid, virtualGuid);
	}

	@Override
	public void read(G3FileReader reader) {
		virtualGuid = GuidUtil.randomGUID();
		points = reader.readPrefixedList(bCVector.class);
		pointRadius = reader.readPrefixedList(G3FileReader::readFloat);
		radius = reader.readFloat();
		radiusOffset = reader.readVector();
		zoneGuid = reader.readGUID();
	}

	@Override
	public void write(G3FileWriter writer) {
		writer.writePrefixedList(points);
		writer.writePrefixedList(pointRadius, G3FileWriter::writeFloat);
		writer.writeFloat(radius);
		writer.writeVector(radiusOffset);
		writer.write(zoneGuid);
	}
}

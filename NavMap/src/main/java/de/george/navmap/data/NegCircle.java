package de.george.navmap.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.vividsolutions.jts.algorithm.ConvexHull;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.math.Vector2D;
import com.vividsolutions.jts.util.GeometricShapeFactory;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.io.G3Serializable;
import de.george.g3utils.structure.bCInfCylinder;
import de.george.g3utils.structure.bCInfDoubleCylinder;
import de.george.g3utils.structure.bCMatrix;
import de.george.g3utils.structure.bCVector;
import de.george.lrentnode.enums.G3Enums.gENavObstacleType;
import de.george.navmap.util.GeoUtil;

public class NegCircle implements G3Serializable {
	private static final Logger logger = LoggerFactory.getLogger(NegCircle.class);

	public List<bCVector> circleOffsets;
	public List<Float> circleRadius;
	public List<String> zoneGuids; // Enthält auch NavPaths...
	public String circleGuid;
	public float objectY;
	public int obstacleType;
	private Geometry hull = null;

	public NegCircle(String circleGuid) {
		this(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), circleGuid, 0.0f);
	}

	public NegCircle(List<bCVector> circleOffsets, List<Float> circleRadius, List<String> zoneGuids, String circleGuid, float objectY) {
		this.circleOffsets = circleOffsets;
		this.circleRadius = circleRadius;
		this.zoneGuids = zoneGuids;
		this.circleGuid = circleGuid;
		this.objectY = objectY;
		obstacleType = gENavObstacleType.gENavObstacleType_Obstacle;
	}

	public String getCircleGuid() {
		return circleGuid;
	}

	public bCVector getCenter() {
		return bCVector.averageVector(circleOffsets);
	}

	public Geometry getConvexHull() {
		if (hull == null) {
			hull = createConvexHull();
		}
		return hull;
	}

	private Geometry createConvexHull() {
		if (circleOffsets.size() == 0) {
			return null;
		}

		// NegCircle besteht aus einem Element
		if (circleOffsets.size() == 1) {
			GeometricShapeFactory factory = new GeometricShapeFactory();
			factory.setCentre(GeoUtil.to2DCoordinate(circleOffsets.get(0)));
			factory.setSize(circleRadius.get(0) * 2);
			return factory.createCircle();
		}

		// NegCircle besteht aus zwei Elementen
		if (circleOffsets.size() == 2) {
			GeometryFactory factory = new GeometryFactory();
			GeometricShapeFactory shapeFactory = new GeometricShapeFactory();
			shapeFactory.setNumPoints(50);

			Coordinate[] offsets = {GeoUtil.to2DCoordinate(circleOffsets.get(0)), GeoUtil.to2DCoordinate(circleOffsets.get(1))};
			// Kreis 1
			shapeFactory.setCentre(offsets[0]);
			shapeFactory.setSize(circleRadius.get(0) * 2);
			LineString circle1 = shapeFactory.createCircle().getExteriorRing();

			// Kreis 2
			shapeFactory.setCentre(offsets[1]);
			shapeFactory.setSize(circleRadius.get(1) * 2);
			LineString circle2 = shapeFactory.createCircle().getExteriorRing();

			// Schnittgeraden für die Kreise
			Vector2D gerade1 = new Vector2D(offsets[0], offsets[1]).rotateByQuarterCircle(1).normalize();
			Vector2D gerade2 = new Vector2D(offsets[0], offsets[1]).rotateByQuarterCircle(-1).normalize();
			LineString inter1 = factory.createLineString(new Coordinate[] {gerade1.multiply(circleRadius.get(0) + 1).translate(offsets[0]),
					gerade2.multiply(circleRadius.get(0) + 1).translate(offsets[0])});
			LineString inter2 = factory.createLineString(new Coordinate[] {gerade1.multiply(circleRadius.get(1) + 1).translate(offsets[1]),
					gerade2.multiply(circleRadius.get(1) + 1).translate(offsets[1])});

			// Schnittpunkte Kreis 1
			Coordinate[] schnitt1 = circle1.intersection(inter1).getCoordinates();

			// Schnittpunkte Kreis 1
			Coordinate[] schnitt2 = circle2.intersection(inter2).getCoordinates();

			if (schnitt1.length != 2 || schnitt2.length != 2) {
				logger.warn("Malformed NegCircle: {}", circleGuid);
				return null;
			}

			// Äußere LineStrings
			LineString außen1 = factory.createLineString(new Coordinate[] {schnitt1[0], schnitt2[0]});
			LineString außen2 = factory.createLineString(new Coordinate[] {schnitt1[1], schnitt2[1]});

			GeometryCollection colletion = factory.createGeometryCollection(new Geometry[] {circle1, außen1, circle2, außen2});
			ConvexHull hull = new ConvexHull(colletion);

			return hull.getConvexHull();
		}

		return null;
	}

	private bCInfCylinder toInfCylinder() {
		return new bCInfCylinder(circleRadius.get(0), circleOffsets.get(0));
	}

	private bCInfDoubleCylinder toDoubleInfCylinder() {
		return new bCInfDoubleCylinder(circleRadius.get(0), circleOffsets.get(0), circleRadius.get(1), circleOffsets.get(1));
	}

	public boolean intersects(NegCircle circle) {
		if (circleOffsets.size() == 1) {
			return circle.intersects(toInfCylinder());
		} else if (circleOffsets.size() == 2) {
			return circle.intersects(toDoubleInfCylinder());
		} else {
			return false;
		}
	}

	public boolean intersects(bCInfCylinder cylinder) {
		if (circleOffsets.size() == 1) {
			return cylinder.intersects(toInfCylinder());
		} else if (circleOffsets.size() == 2) {
			return cylinder.intersects(toDoubleInfCylinder());
		} else {
			return false;
		}
	}

	public boolean intersects(bCInfDoubleCylinder cylinder) {
		if (circleOffsets.size() == 1) {
			return cylinder.intersects(toInfCylinder());
		} else if (circleOffsets.size() == 2) {
			return cylinder.intersects(toDoubleInfCylinder());
		} else {
			return false;
		}
	}

	public boolean intersectsPoint(bCVector point) {
		if (circleOffsets.size() == 1) {
			return toInfCylinder().contains(point);
		} else if (circleOffsets.size() == 2) {
			return toDoubleInfCylinder().contains(point);
		} else {
			return false;
		}
	}

	@Override
	public NegCircle clone() {
		NegCircle cloned = new NegCircle(circleOffsets.stream().map(bCVector::clone).collect(Collectors.toList()),
				new ArrayList<>(circleRadius), new ArrayList<>(zoneGuids), circleGuid, objectY);
		cloned.obstacleType = obstacleType;
		return cloned;
	}

	public static class NegCirclePrototype {
		public String mesh;
		public List<bCVector> circleOffsets;
		public List<Float> circleRadius;

		@JsonCreator
		public NegCirclePrototype(@JsonProperty("mesh") String mesh, @JsonProperty("circleOffsets") List<bCVector> circleOffsets,
				@JsonProperty("circleRadius") List<Float> circleRadius) {
			this.mesh = mesh;
			this.circleOffsets = circleOffsets;
			this.circleRadius = circleRadius;
		}

		public NegCircle toNegCircle(bCMatrix matrix, String guid) {
			// Transform offsets
			List<bCVector> negCircleOffsets = new ArrayList<>();
			for (bCVector vector : circleOffsets) {
				negCircleOffsets.add(vector.getTransformed(matrix));
			}

			// Apply scale
			float uniformScaling = matrix.getPureScaling().getX();
			List<Float> negcircleRadius = circleRadius.stream().map(f -> f * uniformScaling).collect(Collectors.toList());

			return new NegCircle(negCircleOffsets, negcircleRadius, new ArrayList<String>(), guid, matrix.getTranslation().getY());
		}
	}

	@Override
	public void read(G3FileReader reader) {
		circleOffsets = reader.readPrefixedList(bCVector.class);
		circleRadius = reader.readPrefixedList(G3FileReader::readFloat);
		obstacleType = reader.readInt();
		zoneGuids = reader.readPrefixedList(G3FileReader::readGUID);
		circleGuid = reader.readGUID();
		objectY = reader.readFloat();
	}

	@Override
	public void write(G3FileWriter writer) {
		writer.writePrefixedList(circleOffsets);
		writer.writePrefixedList(circleRadius, G3FileWriter::writeFloat);
		writer.writeInt(obstacleType);
		writer.writePrefixedList(zoneGuids, G3FileWriter::write);
		writer.write(circleGuid);
		writer.writeFloat(objectY);
	}
}

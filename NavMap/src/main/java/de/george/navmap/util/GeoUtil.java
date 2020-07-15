package de.george.navmap.util;

import java.util.Arrays;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.util.GeometricShapeFactory;

import de.george.g3utils.structure.bCVector;

public class GeoUtil {
	public static final GeometryFactory FACTORY = new GeometryFactory();
	public static final GeometricShapeFactory SHAPE_FACTORY = new GeometricShapeFactory();

	public static Coordinate[] create2DCoordinateArray(double... coords) {
		if (coords.length == 0 || coords.length % 2 != 0) {
			return new Coordinate[0];
		}
		Coordinate[] result = new Coordinate[coords.length / 2];
		for (int i = 0; i < coords.length; i += 2) {
			result[i / 2] = new Coordinate(coords[i], coords[i + 1]);
		}
		return result;
	}

	public static Coordinate[] create2DCoordinateArray(bCVector... coords) {
		return create2DCoordinateArray(Arrays.asList(coords));
	}

	public static Coordinate[] create2DCoordinateArray(List<bCVector> coords) {
		Coordinate[] result = new Coordinate[coords.size()];
		for (int i = 0; i < coords.size(); i++) {
			result[i] = new Coordinate(coords.get(i).getX(), coords.get(i).getZ());
		}
		return result;
	}

	public static LinearRing createLinearRing(Coordinate... coords) {
		Coordinate[] ringCoords = Arrays.copyOf(coords, coords.length + 1);
		ringCoords[coords.length] = ringCoords[0];
		return FACTORY.createLinearRing(ringCoords);
	}

	public static Polygon createCircle(float radius, bCVector center) {
		GeometricShapeFactory shapeFactory = new GeometricShapeFactory();
		shapeFactory.setHeight(radius * 2);
		shapeFactory.setWidth(radius * 2);
		shapeFactory.setCentre(new Coordinate(center.getX(), center.getZ()));
		return shapeFactory.createCircle();
	}

	public static bCVector coordinatesToVector(Coordinate coord) {
		return new bCVector(coord.x, 0, coord.y);
	}

	public static Coordinate to2DCoordinate(bCVector vector) {
		return new Coordinate(vector.getX(), vector.getZ());
	}
}

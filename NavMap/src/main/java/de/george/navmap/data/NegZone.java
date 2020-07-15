package de.george.navmap.data;

import java.util.List;
import java.util.stream.Collectors;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.io.G3Serializable;
import de.george.g3utils.structure.bCVector;

public class NegZone extends Zone implements G3Serializable {

	private String zoneGuid;

	public NegZone(String guid, String zoneGuid, List<bCVector> sticks, float radius, bCVector radiusOffset, boolean ccw) {
		super(guid, sticks, null, radius, radiusOffset, ccw);
		this.zoneGuid = zoneGuid;
	}

	public String getZoneGuid() {
		return zoneGuid;
	}

	public void setZoneGuid(String zoneGuid) {
		this.zoneGuid = zoneGuid;
	}

	@Override
	public NegZone clone() {
		return new NegZone(getGuid(), zoneGuid, getPoints().stream().map(bCVector::clone).collect(Collectors.toList()), getRadius(),
				getRadiusOffset().clone(), isCcw());
	}

	@Override
	public void read(G3FileReader reader) {
		points = reader.readPrefixedList(bCVector.class);
		radius = reader.readFloat();
		radiusOffset = reader.readVector();
		ccw = reader.readBool();
		zoneGuid = reader.readGUID();
		guid = reader.readGUID();
	}

	@Override
	public void write(G3FileWriter writer) {
		writer.writePrefixedList(points);
		writer.writeFloat(radius);
		writer.writeVector(radiusOffset);
		writer.writeBool(ccw);
		writer.write(zoneGuid);
		writer.write(guid);
	}

	@Override
	public bCVector getWorldRadiusOffset() {
		return radiusOffset;
	}

	@Override
	public List<bCVector> getWorldPoints() {
		return getPoints();
	}
}

package de.george.navmap.data;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.io.G3Serializable;
import de.george.g3utils.structure.bCVector;
import de.george.lrentnode.properties.eCPropertySetProxy;

public class NavPathLink implements G3Serializable {
	public bCVector intersection;
	public String zoneGuid;
	public String pathGuid;

	public NavPathLink(bCVector intersection, String zoneGuid, String pathGuid) {
		this.intersection = intersection;
		this.zoneGuid = zoneGuid;
		this.pathGuid = pathGuid;
	}

	@Override
	public void read(G3FileReader reader) {
		intersection = reader.readVector();
		zoneGuid = reader.read(eCPropertySetProxy.class).getGuid();
		pathGuid = reader.read(eCPropertySetProxy.class).getGuid();
	}

	@Override
	public void write(G3FileWriter writer) {
		writer.writeVector(intersection);
		writer.write(zoneGuid != null ? new eCPropertySetProxy(zoneGuid, "gCNavZone_PS") : new eCPropertySetProxy(null, null));
		writer.write(new eCPropertySetProxy(pathGuid, "gCNavPath_PS"));
	}
}

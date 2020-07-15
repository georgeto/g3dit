package de.george.navmap.sections;

import java.util.List;

import com.google.common.collect.Iterables;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.io.G3Serializable;
import de.george.navmap.data.PrefPath;

/**
 * Auflistung aller PrefPaths
 */
public class Section3b1 implements G3Serializable {
	public List<PrefPath> prefPaths;

	public PrefPath getPrefPath(int index) {
		return prefPaths.get(index);
	}

	public PrefPath getPrefPath(String virtualGuid) {
		return Iterables.find(prefPaths, p -> p.getVirtualGuid().equals(virtualGuid), null);
	}

	public int getPrefPathIndex(String virtualGuid) {
		return Iterables.indexOf(prefPaths, p -> p.getVirtualGuid().equals(virtualGuid));
	}

	@Override
	public void read(G3FileReader reader) {
		prefPaths = reader.readPrefixedList(PrefPath.class);
	}

	@Override
	public void write(G3FileWriter writer) {
		writer.writePrefixedList(prefPaths);
	}

	public void writeText(StringBuilder builder) {
		builder.append("Section 3b1");
		builder.append("\nPrefPath Anzahl: " + prefPaths.size());
		for (int i = 0; i < prefPaths.size(); i++) {
			builder.append("\nPrefPath " + i);
			PrefPath prefPath = prefPaths.get(i);
			builder.append("\nNavZone-Guid: " + prefPath.getZoneGuid());
			builder.append("\nRadiusOffset: " + prefPath.getRadiusOffset());
			builder.append("\nRadius: " + prefPath.getRadius());
			builder.append("\nPoint Anzahl: " + prefPath.getPoints().size());
			prefPath.getPoints().forEach(p -> builder.append("\n" + p));
			builder.append("\nPointRadius Anzahl: " + prefPath.getPointRadius().size());
			prefPath.getPointRadius().forEach(r -> builder.append("\n" + r));
		}
	}

}

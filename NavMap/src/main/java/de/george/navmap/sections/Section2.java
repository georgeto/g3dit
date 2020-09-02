package de.george.navmap.sections;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.teamunify.i18n.I;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.io.G3Serializable;
import de.george.navmap.data.NegZone;

/**
 * Auflistung aller NegZones
 */
public class Section2 implements G3Serializable {
	private List<NegZone> negZones;
	private Map<String, NegZone> guidMapping;

	public List<NegZone> getNegZones() {
		return negZones;
	}

	public int getNegZoneCount() {
		return negZones.size();
	}

	public int addNegZone(NegZone negZone) {
		if (guidMapping.containsKey(negZone.getGuid())) {
			throw new IllegalArgumentException(I.trf("Section2 enthält bereits eine NegZone mit der Guid '{0}'.", negZone.getGuid()));
		}
		negZones.add(negZone);
		guidMapping.put(negZone.getGuid(), negZone);
		return getNegZoneCount() - 1;
	}

	public NegZone getNegZone(int index) {
		return negZones.get(index);
	}

	public NegZone getNegZone(String guid) {
		return guidMapping.get(guid);
	}

	public int getNegZoneIndex(String guid) {
		return negZones.indexOf(getNegZone(guid));
	}

	public void updateNegZone(int index, NegZone negZone) {
		if (!negZone.getGuid().equals(getNegZone(index).getGuid())) {
			throw new IllegalArgumentException(I.tr("Guid der NegZone darf sich nicht ändern."));
		}
		negZones.set(index, negZone);
		guidMapping.put(negZone.getGuid(), negZone);
	}

	public void removeNegZone(int index) {
		NegZone negZone = negZones.remove(index);
		guidMapping.remove(negZone.getGuid());
	}

	@Override
	public void read(G3FileReader reader) {
		negZones = reader.readPrefixedList(NegZone.class);
		guidMapping = negZones.stream().collect(Collectors.toMap(NegZone::getGuid, Function.identity()));
	}

	@Override
	public void write(G3FileWriter writer) {
		writer.writePrefixedList(negZones);
	}

	public void writeText(StringBuilder builder) {
		builder.append("Section 2");
		builder.append("\nNegZone Anzahl: " + negZones.size());
		for (int i = 0; i < negZones.size(); i++) {
			builder.append("\nNegZone " + i);
			NegZone negZone = negZones.get(i);
			builder.append("\nNegZone-Guid: " + negZone.getGuid());
			builder.append("\nNavZone-Guid: " + negZone.getZoneGuid());
			builder.append("\nRadiusOffset: " + negZone.getRadiusOffset());
			builder.append("\nRadius: " + negZone.getRadius());
			builder.append("\nCCW: " + negZone.isCcw());
			builder.append("\nStick Anzahl: " + negZone.getPointCount());
			negZone.getPoints().forEach(s -> builder.append("\n" + s));

		}
	}
}

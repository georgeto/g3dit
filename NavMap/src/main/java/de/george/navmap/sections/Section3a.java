package de.george.navmap.sections;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.teamunify.i18n.I;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.io.G3Serializable;
import de.george.navmap.data.NegCircle;

/**
 * Auflistung aller NegCircles
 */
public class Section3a implements G3Serializable {
	private List<NegCircle> negCircles;
	private Map<String, NegCircle> guidMapping;

	@Override
	public void read(G3FileReader reader) {
		negCircles = reader.readPrefixedList(NegCircle.class);
		guidMapping = negCircles.stream().collect(Collectors.toMap(n -> n.circleGuid, Function.identity()));
	}

	@Override
	public void write(G3FileWriter writer) {
		writer.writePrefixedList(negCircles);
	}

	public void writeText(StringBuilder builder) {
		builder.append("Section 3a");
		builder.append("\nAnzahl der NegCircles: " + negCircles.size());
		for (int i = 0; i < negCircles.size(); i++) {
			NegCircle circle = negCircles.get(i);
			builder.append("\nNegCircle " + i);
			builder.append("\nCircle-Guid:" + circle.circleGuid);
			builder.append("\nObjectY:" + circle.objectY);
			builder.append("\nObstacleType:" + circle.obstacleType);
			builder.append("\nAnzahl der CircleOffsets:" + circle.circleOffsets.size());
			circle.circleOffsets.forEach(o -> builder.append("\n" + o));
			builder.append("\nAnzahl der CircleRadius:" + circle.circleRadius.size());
			circle.circleRadius.forEach(r -> builder.append("\n" + r));
			builder.append("\nAnzahl der beteiligten Zonen:" + circle.zoneGuids.size());
			circle.zoneGuids.forEach(z -> builder.append("\n" + z));
		}
	}

	public List<NegCircle> getNegCircles() {
		return negCircles;
	}

	public int getCircleCount() {
		return negCircles.size();
	}

	public int addNegCircle(NegCircle negCircle) {
		if (guidMapping.containsKey(negCircle.circleGuid)) {
			throw new IllegalArgumentException(
					I.trf("Section3a enthält bereits einen NegCircle mit der Guid '{0}'.", negCircle.circleGuid));
		}
		negCircles.add(negCircle);
		guidMapping.put(negCircle.circleGuid, negCircle);
		return getCircleCount() - 1;
	}

	public NegCircle getNegCircle(int index) {
		return negCircles.get(index);
	}

	public NegCircle getNegCircle(String guid) {
		return guidMapping.get(guid);
	}

	public int getNegCircleIndex(String guid) {
		return negCircles.indexOf(getNegCircle(guid));
	}

	public int updateNegCircle(NegCircle negCircle) {
		if (!guidMapping.containsKey(negCircle.circleGuid)) {
			throw new IllegalArgumentException(I.trf("Section3a enthält keinen NegCircle mit der Guid '{0}'.", negCircle.circleGuid));
		}

		int circleIndex = getNegCircleIndex(negCircle.circleGuid);
		negCircles.set(circleIndex, negCircle);
		guidMapping.put(negCircle.circleGuid, negCircle);
		return circleIndex;
	}

	public void removeNegCircle(int index) {
		NegCircle circle = negCircles.remove(index);
		guidMapping.remove(circle.circleGuid);
	}
}

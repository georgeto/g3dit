package de.george.navmap.util;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.util.EntityUtil;
import de.george.navmap.data.NegCircle;
import de.george.navmap.data.NegCircle.NegCirclePrototype;
import de.george.navmap.util.NavCalc.NavArea;

public class NegCircleCalc {
	private Map<String, NegCirclePrototype> circlePrototypes;

	public void setNegCirclePrototypes(List<NegCirclePrototype> prototypes) {
		circlePrototypes = prototypes.stream().collect(Collectors.toMap(p -> p.mesh, p -> p));
	}

	public NegCirclePrototype getNegCirclePrototype(eCEntity entity) {
		NegCirclePrototype proto = circlePrototypes.get(entity.getName());
		if (proto == null) {
			String mesh = EntityUtil.getMesh(entity).orElse(null);
			if (mesh == null) {
				mesh = EntityUtil.getTreeMesh(entity).orElse(null);
			}
			if (mesh != null) {
				proto = circlePrototypes.get(mesh);
			}
		}

		return proto;
	}

	public boolean hasNegCirclePrototype(eCEntity entity) {
		return getNegCirclePrototype(entity) != null;
	}

	public NegCircle createNegCircleFromEntity(eCEntity entity) {
		NegCirclePrototype base = getNegCirclePrototype(entity);
		return base.toNegCircle(entity.getWorldMatrix(), entity.getGuid());
	}

	public NegCircle createNegCircleFromEntity(eCEntity entity, NavCalc navCalc) {
		NegCircle negCircle = createNegCircleFromEntity(entity);
		negCircle.zoneGuids = calcAssignedAreas(negCircle, navCalc).stream().map(area -> area.areaId).collect(Collectors.toList());
		return negCircle;
	}

	public static List<NavArea> calcAssignedAreas(NegCircle negCircle, NavCalc navCalc) {
		List<NavArea> assignedAreas;
		if (negCircle.circleOffsets.size() <= 0) {
			assignedAreas = Collections.emptyList();
		} else if (negCircle.circleOffsets.size() == 1) {
			assignedAreas = navCalc.getZone(negCircle.circleOffsets.get(0), (float) (negCircle.circleRadius.get(0) * 0.9300000071525574),
					false, false);
		} else {
			assignedAreas = navCalc.getZone(negCircle.circleOffsets.get(0), (float) (negCircle.circleRadius.get(0) * 0.9300000071525574),
					negCircle.circleOffsets.get(1), (float) (negCircle.circleRadius.get(1) * 0.9300000071525574), false, false);
		}
		return assignedAreas;
	}
}

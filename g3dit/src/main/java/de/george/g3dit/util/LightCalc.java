package de.george.g3dit.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import de.george.g3dit.cache.LightCache;
import de.george.g3dit.cache.LightCache.LightSource;
import de.george.g3utils.structure.bCVector;
import de.george.lrentnode.archive.G3ClassContainer;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.classes.G3Class;
import de.george.lrentnode.classes.eCIlluminated_PS;
import de.george.lrentnode.classes.eCIlluminated_PS.StaticLight;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.enums.G3Enums.eEStaticLighingType;
import de.george.lrentnode.util.EntityUtil;

public class LightCalc {

	public static enum StaticLightedResult {
		Valid,
		MeshNotStatic,
		WrongLightingType
	}

	public static StaticLightedResult isStaticLighted(G3ClassContainer entity) {
		// Beleuchtungstyp auf eEStaticLighingType_Instance umstellen
		G3Class mesh = EntityUtil.getStaticMeshClass(entity);
		if (mesh == null) {
			// Bäume werden auch statisch beleuchtet
			if (!EntityUtil.getTreeMesh(entity).isPresent()) {
				return StaticLightedResult.MeshNotStatic;
			}
		} else if (mesh.property(CD.eCVisualMeshBase_PS.StaticLightingType)
				.getEnumValue() != eEStaticLighingType.eEStaticLighingType_Instance) {
			return StaticLightedResult.WrongLightingType;
		}
		return StaticLightedResult.Valid;
	}

	/**
	 * Berechnet die Beleuchtung für <code>entity</code>, trägt diese in
	 * <code>eCIlluminated_PS</code> ein und ändert eventuell den Beleuchtungsmodus der Entity
	 *
	 * @param entity Zu beleuchtende Entity
	 * @return -3 wenn StaticLightingType ungleich Instance, -2 wenn kein statisches Mesh, -1 wenn
	 *         <code>entity</code> keine eCIlluminated_PS Klasse hat, ansonsten die Anzahl der
	 *         Lichtquellen durch die <code>entity</code> beleuchtet wird
	 */
	public static int calcLighting(eCEntity entity, LightCache lightCache) {
		if (!entity.hasClass(CD.eCIlluminated_PS.class)) {
			return -1;
		}

		switch (isStaticLighted(entity)) {
			case MeshNotStatic:
				return -2;
			case WrongLightingType:
				return -3;
			default:
				break;
		}

		eCIlluminated_PS illuminated = entity.getClass(CD.eCIlluminated_PS.class);
		illuminated.lights.getLights().clear();

		float radius = entity.getWorldTreeSphere().getRadius();
		Map<Float, LightSource> lightSources = getNearestLightSources(entity.getWorldPosition(), radius, lightCache);

		for (Entry<Float, LightSource> lightEntry : lightSources.entrySet()) {
			LightSource light = lightEntry.getValue();
			float colorScale = 2.75f / Math.max(light.color.getRed(), Math.max(light.color.getGreen(), light.color.getBlue()))
					* lightEntry.getKey();
			StaticLight staticLight = new StaticLight();
			staticLight.color = new bCVector(light.color.getRed() * colorScale, light.color.getGreen() * colorScale,
					light.color.getBlue() * colorScale);
			staticLight.position = light.position;
			if (light.intensity != null) {
				staticLight.intensity = light.intensity;
			} else {
				staticLight.intensity = getIntensity(light.name);
			}
			illuminated.lights.getLights().add(staticLight);
		}

		return lightSources.size();
	}

	/**
	 * Sucht die der Positon am nähesten gelegenen Lichtquellen
	 *
	 * @param position Position des zu beleuchtenden Objekts
	 * @param radius Radius des zu beleuchtenden Objekts
	 * @return Map mit dem LichtMultiplikator als <code>key</code> und der Lichtquelle als
	 *         <code>value</code>
	 */
	public static Map<Float, LightSource> getNearestLightSources(bCVector position, float radius, LightCache lightCache) {
		Map<Float, LightSource> nearestLights = new HashMap<>();
		for (LightSource light : lightCache.getEntries()) {
			float dist = position.getRelative(light.position).length();
			float lightModifier = 1 - Math.max(0, dist - radius) / light.range;
			if (lightModifier < 0) {
				continue;
			}

			if (nearestLights.size() == 4) {
				float lowestModifier = 1;
				for (float pLightModifier : nearestLights.keySet()) {
					lowestModifier = Math.min(lowestModifier, pLightModifier);
				}
				if (lightModifier > lowestModifier) {
					nearestLights.remove(lowestModifier);
					nearestLights.put(lightModifier, light);
				}

			} else {
				nearestLights.put(lightModifier, light);
			}
		}
		return nearestLights;
	}

	/**
	 * Gibt die häufigste für <code>name</code> genutzte Intensität zurück
	 *
	 * @param name Name bzw. Meshname der Lichtquelle
	 * @return Intensität der Lichtquelle
	 */
	public static String getIntensity(String name) {
		String meshName = name;
		if (!meshName.endsWith(".xcmsh")) {
			meshName += ".xcmsh";
		}
		return switch (meshName) {
			case "G3_Object_Fire_Chandelier_01.xcmsh" -> "BD378635";
			case "G3_Object_Candle_01.xcmsh" -> "DC693A37";
			case "G3_Object_Candle_02.xcmsh" -> "F5F40837";
			case "G3_Object_Candle_03.xcmsh" -> "F5F40837";
			case "G3_Object_Crystal_01.xcmsh" -> "FB9BEE34";
			case "G3_Object_Crystal_02.xcmsh" -> "FB9BEE34";
			case "G3_Object_Fire_Cage_01.xcmsh" -> "FB9BEE35";
			case "G3_Object_Fire_Latern_01.xcmsh" -> "BD378636";
			case "G3_Object_Fire_Pelvis_01.xcmsh" -> "BD378635";
			case "G3_Object_Fire_Pelvis_02.xcmsh" -> "BD378635";
			case "G3_Object_Fireplace_03.xcmsh" -> "BBC4B935";
			case "G3_Object_Fireplace_04.xcmsh" -> "FB9BEE35";
			case "G3_Object_Fireplace_Large_01.xcmsh" -> "BD378635";
			case "G3_Object_Interact_Campfire_01.xcmsh" -> "FB9BEE35";
			case "G3_Object_Interact_Campfire_02.xcmsh" -> "FB9BEE35";
			case "G3_Object_Interact_Campfire_03.xcmsh" -> "FB9BEE35";
			case "G3_Object_Interact_Campfire_04.xcmsh" -> "BD378635";
			case "G3_Object_Latern_Oil_01.xcmsh" -> "17B7D136";
			case "G3_Object_Tablestuff_Poor_Double_01.xcmsh" -> "F5F40837";
			case "G3_Object_Tablestuff_Poor_Double_02.xcmsh" -> "F5F40837";
			case "G3_Object_Tablestuff_Poor_Single_01.xcmsh" -> "F5F40837";
			case "G3_Object_Tablestuff_Poor_Single_02.xcmsh" -> "F5F40837";
			case "G3_Object_Tablestuff_Rich_Double_01.xcmsh" -> "F5F40837";
			case "G3_Object_Tablestuff_Rich_Double_02.xcmsh" -> "F5F40837";
			case "G3_Object_Tablestuff_Rich_Single_01.xcmsh" -> "F5F40837";
			case "G3_Object_Tablestuff_Rich_Single_02.xcmsh" -> "F5F40837";
			case "G3_Object_Torch_01.xcmsh" -> "FB9BEE35";
			case "G3_Object_Torch_02.xcmsh" -> "F5D85D36";
			case "G3_Object_Varant_Firebasin_01.xcmsh" -> "FB9BEE35";
			case "G3_Object_Varant_Oillamp_01.xcmsh" -> "F5F40837";
			case "G3_Object_Varant_Oillamp_02.xcmsh" -> "17B7D136";
			case "G3_Object_Varant_Oillamp_03.xcmsh" -> "17B7D136";
			default -> "FB9BEE35";
		};
	}
}

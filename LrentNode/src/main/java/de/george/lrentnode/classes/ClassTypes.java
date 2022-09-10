package de.george.lrentnode.classes;

import java.lang.reflect.Constructor;
import java.util.concurrent.ConcurrentHashMap;

import de.george.g3utils.io.G3FileReader;
import de.george.lrentnode.effect.gCEffectCommand;
import de.george.lrentnode.effect.gCEffectCommandPlaySound;

public class ClassTypes {
	private static ConcurrentHashMap<String, Class<?>> classMap = new ConcurrentHashMap<>();
	private static ConcurrentHashMap<String, Constructor<?>> consMap = new ConcurrentHashMap<>();

	private static void add(Class<?> clazz, String... types) {
		for (String type : types) {
			classMap.putIfAbsent(type, clazz);
			try {
				consMap.putIfAbsent(type, clazz.getConstructor(String.class, G3FileReader.class));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	public static G3Class getClassInstance(String className, G3FileReader reader) {
		Constructor<?> constructor = consMap.get(className);
		if (constructor != null) {
			try {
				return (G3Class) constructor.newInstance(className, reader);
			} catch (Exception e) {
				if (e.getSuppressed().length > 0) {
					throw new RuntimeException(e.getSuppressed()[0]);
				}
				throw new RuntimeException(e);

			}
		}
		return new DefaultClass(className, reader);
	}

	static {
		add(eCRigidBody_PS.class, "eCRigidBody_PS");
		add(gCParty_PS.class, "gCParty_PS");
		add(gCEnclave_PS.class, "gCEnclave_PS");
		add(gCAnchor_PS.class, "gCAnchor_PS");
		add(eCCollisionShape_PS.class, "eCCollisionShape_PS");
		add(eCCollisionShape.class, "eCCollisionShape");
		add(gCInventory_PS.class, "gCInventory_PS");
		add(eCIlluminated_PS.class, "eCIlluminated_PS");
		add(eCVisualAnimation_PS.class, "eCVisualAnimation_PS");
		add(eCVisualMeshDynamic_PS.class, "eCVisualMeshDynamic_PS", "eCVisualMeshStatic_PS");
		add(gCItem_PS.class, "gCItem_PS");
		add(eCEntityDynamicContext.class, "eCEntityDynamicContext");
		add(eCGeometrySpatialContext.class, "eCGeometrySpatialContext");
		add(eCEntityPropertySet.class, "eCSpeedTree_PS", "eCSpeedTreeWind_PS");
		add(eCVegetation_PS.class, "eCVegetation_PS");
		add(eCVegetation_Mesh.class, "eCVegetation_Mesh");
		add(gCMap_PS.class, "gCMap_PS");
		add(eCColorSrcBlend.class, "eCColorSrcBlend");
		add(eCColorSrcCombiner.class, "eCColorSrcCombiner");
		add(eCColorSrcConstant.class, "eCColorSrcConstant");
		add(eCColorSrcCubeSampler.class, "eCColorSrcCubeSampler");
		add(eCColorSrcSampler.class, "eCColorSrcSampler");
		add(eCColorSrcVertexColor.class, "eCColorSrcVertexColor");
		add(eCResourceShaderMaterial_PS.class, "eCResourceShaderMaterial_PS");
		add(eCShaderSkin.class, "eCShaderSkin");
		add(eCShaderDefault.class, "eCShaderDefault");
		add(eCShaderWater.class, "eCShaderWater");
		add(eCShaderParticle.class, "eCShaderParticle");
		add(eCShaderLeaf.class, "eCShaderLeaf");
		add(eCResourceMeshLoD_PS.class, "eCResourceMeshLoD_PS");
		add(eCResourceCollisionMesh_PS.class, "eCResourceCollisionMesh_PS");
		add(eCParticle_PS.class, "eCParticle_PS");
		add(eCSpringAndDamperEffector.class, "eCSpringAndDamperEffector");
		add(eCResourceMeshComplex_PS.class, "eCResourceMeshComplex_PS");
		add(gCEffectCommandPlaySound.class, "gCEffectCommandPlaySound");
		add(gCEffectCommand.class, "gCEffectCommandEarthquake", "gCEffectCommandKillEntity", "gCEffectCommandMusicTrigger",
				"gCEffectCommandPlayVoice", "gCEffectCommandSpawnEntity", "gCEffectCommandTriggerEntity");
		add(gCProjectile_PS.class, "gCProjectile_PS");
		add(eCPhysicsScene_PS.class, "eCPhysicsScene_PS");
		add(gCCameraAI_PS.class, "gCCameraAI_PS");
	}
}

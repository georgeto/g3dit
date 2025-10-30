package de.george.lrentnode.enums;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class G3Enums {
	private static final Logger logger = LoggerFactory.getLogger(G3Enums.class);

	public static class G3Enum {}

	@SuppressWarnings("unchecked")
	public static Class<? extends G3Enum> byName(String name) {
		for (Class<?> clazz : G3Enums.class.getDeclaredClasses()) {
			if (classToEnumName(clazz).equals(name)) {
				return (Class<? extends G3Enum>) clazz;
			}
		}
		return null;
	}

	public static Class<? extends G3Enum> byG3Type(String name) {
		return byName(name.replaceFirst(".*<enum", "").replaceFirst(">.*", "").trim());
	}

	public static String[] asArray(Class<? extends G3Enum> clazz) {
		return asArray(clazz, true);
	}

	public static String[] asArray(Class<? extends G3Enum> clazz, boolean stripped) {
		Field[] fields = clazz.getFields();
		String[] array = new String[clazz.getFields().length];

		String className = classToEnumName(clazz);
		for (int i = 0; i < fields.length; i++) {
			array[i] = stripped ? fields[i].getName().replace(className + "_", "") : fields[i].getName();
		}

		return array;
	}

	public static List<String> asList(Class<? extends G3Enum> clazz) {
		return asList(clazz, true);
	}

	public static List<String> asList(Class<? extends G3Enum> clazz, boolean stripped) {
		List<String> list = new ArrayList<>(clazz.getFields().length);

		String className = classToEnumName(clazz);
		for (Field field : clazz.getFields()) {
			list.add(stripped ? field.getName().replace(className + "_", "") : field.getName());
		}

		return list;
	}

	public static String asString(Class<? extends G3Enum> clazz, int number) {
		return asString(clazz, number, true);
	}

	public static String asString(Class<? extends G3Enum> clazz, int number, boolean stripped) {
		try {
			for (Field field : clazz.getFields()) {
				if (field.getInt(null) == number) {
					return stripped ? field.getName().replace(classToEnumName(clazz) + "_", "") : field.getName();
				}
			}
		} catch (Exception e) {
			logger.warn("Failed to convert enum {} with value {} to string.", clazz, number, e);
		}
		return null;
	}

	public static int asInt(Class<? extends G3Enum> clazz, String name) {
		return asInt(clazz, name, true);
	}

	public static int asInt(Class<? extends G3Enum> clazz, String name, boolean stripped) {
		try {
			Field field = clazz.getField(stripped ? classToEnumName(clazz) + "_" + name : name);
			return field.getInt(null);
		} catch (Exception e) {
			logger.warn("Failed to convert enum {} with name {} to int.", clazz, name, e);
		}
		return -1;
	}

	public static String classToEnumName(Class<?> clazz) {
		return clazz.getSimpleName().replaceAll(".*\\$", "");
	}

	// gCArmorSet_PS.ModAttrib1
	// gCArmorSet_PS.ModAttrib2
	// gCArmorSet_PS.ModAttrib3
	// gCArmorSet_PS.ModAttrib4
	// gCItem_PS.ModAttrib1
	// gCItem_PS.ModAttrib2
	// gCItem_PS.ModAttrib3
	// gCItem_PS.ModAttrib4
	// gCItem_PS.ModAttrib5
	// gCItem_PS.ModAttrib6

	public static class EAttribModOperation extends G3Enum {
		public static final int EAttribModOperation_AddToVal = 0;
		public static final int EAttribModOperation_AddToMax = 1;
		public static final int EAttribModOperation_AddPercentageToVal = 2;
		public static final int EAttribModOperation_AddPercentageToMax = 3;
		public static final int EAttribModOperation_SetVal = 4;
		public static final int EAttribModOperation_SetMax = 5;
		public static final int EAttribModOperation_SetValToPercentage = 6;
		public static final int EAttribModOperation_SetMaxToPercentage = 7;
		public static final int EAttribModOperation_SetValToMax = 8;
	}

	// gCItem_PS.ReqAttrib1
	// gCItem_PS.ReqAttrib2
	// gCItem_PS.ReqAttrib3
	// gCItem_PS.ReqAttrib4

	public static class EAttribReqOperation extends G3Enum {
		public static final int EAttribReqOperation_ValGreaterEqual = 0;
		public static final int EAttribReqOperation_MaxGreaterEqual = 1;
	}

	// eCVegetationBrush_PS.ColorNoiseTurbulence
	// eCVegetationBrush_PS.ProbabilityNoiseTurbulence

	public static class bENoiseTurbulence extends G3Enum {
		public static final int bENoiseTurbulence_FractalSum = 0;
		public static final int bENoiseTurbulence_FractalAbsSum = 1;
	}

	// gCAmbientSequencer.Climate
	// gCAmbientSound.Climate

	public static class eEAmbientClimate extends G3Enum {
		public static final int eEAmbientClimate_Mediterranean = 0;
		public static final int eEAmbientClimate_Arctic = 1;
		public static final int eEAmbientClimate_Desert = 2;
		public static final int eEAmbientClimate_Cave = 3;
	}

	// gCAmbientSequencer.DayTime
	// gCAmbientSound.DayTime

	public static class eEAmbientDayTime extends G3Enum {
		public static final int eEAmbientDayTime_Morning = 0;
		public static final int eEAmbientDayTime_Day = 1;
		public static final int eEAmbientDayTime_Evening = 2;
		public static final int eEAmbientDayTime_Night = 3;
	}

	// eCSpeedTree_PS.AmbientEnvironment
	// gCAmbientSequencer.Environment
	// gCAmbientSound.Environment

	public static class eEAmbientEnvironment extends G3Enum {
		public static final int eEAmbientEnvironment_Forest = 0;
		public static final int eEAmbientEnvironment_Meadow = 1;
		public static final int eEAmbientEnvironment_Plain = 2;
		public static final int eEAmbientEnvironment_Mountain = 3;
		public static final int eEAmbientEnvironment_Coast = 4;
		public static final int eEAmbientEnvironment_Town = 5;
	}

	// eCAudioEmitter_PS.SpawningMode

	public static class eEAudioEmitterMode extends G3Enum {
		public static final int eEAudioEmitterMode_Once = 0;
		public static final int eEAudioEmitterMode_Loop = 1;
		public static final int eEAudioEmitterMode_Repeat = 2;
	}

	// eCCollisionShape_PS.Group
	// eCVisualAnimation_PS.SkeletonShapeGroup

	public static class eECollisionGroup extends G3Enum {
		public static final int eECollisionGroup_Undefined = 0;
		public static final int eECollisionGroup_Static = 1;
		public static final int eECollisionGroup_Dynamic = 2;
		public static final int eECollisionGroup_Player = 3;
		public static final int eECollisionGroup_NPC = 4;
		public static final int eECollisionGroup_Item_Equipped = 5;
		public static final int eECollisionGroup_Item_World = 6;
		public static final int eECollisionGroup_Item_Attack = 7;
		public static final int eECollisionGroup_Interactable = 8;
		public static final int eECollisionGroup_Trigger = 9;
		public static final int eECollisionGroup_Zone = 10;
		public static final int eECollisionGroup_Camera = 11;
		public static final int eECollisionGroup_Tree = 12;
		public static final int eECollisionGroup_DownCharacter = 13;
		public static final int eECollisionGroup_PlayerTrigger = 14;
		public static final int eECollisionGroup_ObjectTrigger = 15;
		public static final int eECollisionGroup_Ghost = 16;
		public static final int eECollisionGroup_Mover = 17;
		public static final int eECollisionGroup_RagDoll = 18;
	}

	// eCCollisionShape.Type

	public static class eECollisionShapeType extends G3Enum {
		public static final int eECollisionShapeType_None = 0;
		public static final int eECollisionShapeType_TriMesh = 1;
		public static final int eECollisionShapeType_Plane = 2;
		public static final int eECollisionShapeType_Box = 3;
		public static final int eECollisionShapeType_Capsule = 4;
		public static final int eECollisionShapeType_Sphere = 5;
		public static final int eECollisionShapeType_ConvexHull = 6;
		public static final int eECollisionShapeType_Point = 7;
	}

	// eCParticle_PS.CollisionSound

	public static class eECollisionSound extends G3Enum {
		public static final int eECollisionSound_Disabled = 0;
		public static final int eECollisionSound_LinearLocal = 1;
		public static final int eECollisionSound_LinearGlobal = 2;
		public static final int eECollisionSound_Random = 3;
	}

	// eCColorSrcCombiner.CombinerType

	public static class eEColorSrcCombinerType extends G3Enum {
		public static final int eEColorSrcCombinerType_Add = 0;
		public static final int eEColorSrcCombinerType_Subtract = 1;
		public static final int eEColorSrcCombinerType_Multiply = 2;
		public static final int eEColorSrcCombinerType_Max = 3;
		public static final int eEColorSrcCombinerType_Min = 4;
	}

	// eCColorSrcSampler.TexRepeatU
	// eCColorSrcSampler.TexRepeatV

	public static class eEColorSrcSampleTexRepeat extends G3Enum {
		public static final int eEColorSrcSampleTexRepeat_Wrap = 0;
		public static final int eEColorSrcSampleTexRepeat_Clamp = 1;
		public static final int eEColorSrcSampleTexRepeat_Mirror = 2;
	}

	// eCColorSrcSampler.SwitchRepeat

	public static class eEColorSrcSwitchRepeat extends G3Enum {
		public static final int eEColorSrcSwitchRepeat_Repeat = 0;
		public static final int eEColorSrcSwitchRepeat_Clamp = 1;
		public static final int eEColorSrcSwitchRepeat_PingPong = 2;
	}

	// eCParticle_PS.CoordinateSystem

	public static class eECoordinateSystem extends G3Enum {
		public static final int eECoordinateSystem_Independent = 0;
		public static final int eECoordinateSystem_Relative = 1;
	}

	// eCIlluminated_PS.DirectionalShadowType

	public static class eEDirectionalShadowType extends G3Enum {
		public static final int eEDirectionalShadowType_Terrain = 0;
		public static final int eEDirectionalShadowType_Building = 1;
		public static final int eEDirectionalShadowType_Object = 2;
	}

	// eCPointLight_PS.Effect

	public static class eEDynamicLightEffect extends G3Enum {
		public static final int eEDynamicLightEffect_Steady = 0;
		public static final int eEDynamicLightEffect_Pulse = 1;
		public static final int eEDynamicLightEffect_Blink = 2;
		public static final int eEDynamicLightEffect_Flicker = 3;
		public static final int eEDynamicLightEffect_Strobe = 4;
	}

	// eCEventFilter_PS.FireEventWhenDamaged
	// eCEventFilter_PS.FireEventWhenIntersects
	// eCEventFilter_PS.FireEventWhenTouched
	// eCEventFilter_PS.FireEventWhenTriggered
	// eCEventFilter_PS.FireEventWhenUntouched
	// eCEventFilter_PS.FireEventWhenUntriggered
	// eCTrigger_PS.LastEvent
	// eCTrigger_PS.RunningEventType

	public static class eEEventType extends G3Enum {
		public static final int eEEventType_None = 0;
		public static final int eEEventType_Trigger = 1;
		public static final int eEEventType_Untrigger = 2;
		public static final int eEEventType_Touch = 3;
		public static final int eEEventType_Untouch = 4;
		public static final int eEEventType_Intersect = 5;
		public static final int eEEventType_Damage = 6;
	}

	// eCParticle_PS.FacingDirection

	public static class eEFacingDirection extends G3Enum {
		public static final int eEFacingDirection_FacingCamera = 0;
		public static final int eEFacingDirection_AlongMovementFacingCamera = 1;
		public static final int eEFacingDirection_SpecifiedNormal = 2;
		public static final int eEFacingDirection_AlongMovementFacingNormal = 3;
		public static final int eEFacingDirection_PerpendicularToMovement = 4;
	}

	// eCStrip_PS.FinalBlend

	public static class eEFinalBlend extends G3Enum {
		public static final int eEFinalBlend_Overwrite = 0;
		public static final int eEFinalBlend_AlphaBlend = 1;
		public static final int eEFinalBlend_AlphaModulate = 2;
		public static final int eEFinalBlend_Modulate = 3;
		public static final int eEFinalBlend_Translucent = 4;
		public static final int eEFinalBlend_Darken = 5;
		public static final int eEFinalBlend_Brighten = 6;
		public static final int eEFinalBlend_Invisible = 7;
	}

	// eCForceField_PS.DirMode

	public static class eEForceFieldDirMode extends G3Enum {
		public static final int eEForceFieldDirMode_Entity_AtVec = 0;
		public static final int eEForceFieldDirMode_ShapeCenterOutside = 1;
		public static final int eEForceFieldDirMode_ShapeCenterInside = 2;
		public static final int eEForceFieldDirMode_EntityCenterOutside = 3;
		public static final int eEForceFieldDirMode_EntityCenterInside = 4;
	}

	// eCForceField_PS.DistanceScale

	public static class eEForceFieldDistanceScale extends G3Enum {
		public static final int eEForceFieldDistanceScale_One = 0;
		public static final int eEForceFieldDistanceScale_Linear = 1;
		public static final int eEForceFieldDistanceScale_Ease = 2;
		public static final int eEForceFieldDistanceScale_Exp = 3;
	}

	// eCForceField_PS.Type

	public static class eEForceFieldType extends G3Enum {
		public static final int eEForceFieldType_AddForce = 0;
		public static final int eEForceFieldType_ChangeVelocity = 1;
	}

	// eCJointDesc.Flag

	public static class eEJointFlag extends G3Enum {
		public static final int eEJointFlag_None = 0;
		public static final int eEJointFlag_CollisionEnabled = 1;
		public static final int eEJointFlag_Visualization = 2;
	}

	// eCSphericalJointDesc.JointProjectionMode

	public static class eEJointProjectionMode extends G3Enum {
		public static final int eEJointProjectionMode_NONE = 0;
		public static final int eEJointProjectionMode_POINT_MINDIST = 1;
	}

	// eCParticle_PS.LightingStyle

	public static class eELightingStyle extends G3Enum {
		public static final int eELightingStyle_Disabled = 0;
		public static final int eELightingStyle_Flat = 1;
		public static final int eELightingStyle_Particle = 2;
		public static final int eELightingStyle_System = 3;
	}

	// eCVisualMeshBase_PS.LightmapAmbientOcclusion

	public static class eELightmapAmbientOcclusion extends G3Enum {
		public static final int eELightmapAmbientOcclusion_None = 0;
		public static final int eELightmapAmbientOcclusion_PerTriangle = 1;
		public static final int eELightmapAmbientOcclusion_PerVertex = 2;
	}

	// eCVisualMeshBase_PS.LightmapType

	public static class eELightmapType extends G3Enum {
		public static final int eELightmapType_PerVertex = 0;
		public static final int eELightmapType_Mixed = 1;
	}

	// eCParticle_PS.StartLocation

	public static class eELocationShape extends G3Enum {
		public static final int eELocationShape_Box = 0;
		public static final int eELocationShape_Sphere = 1;
	}

	// eCCollisionShape_PS.Range

	public static class eEPhysicRangeType extends G3Enum {
		public static final int eEPhysicRangeType_World = 0;
		public static final int eEPhysicRangeType_ProcessingRange = 1;
		public static final int eEPhysicRangeType_VisibilityRange = 2;
	}

	// eCTrigger_PS.RecognizesPSType
	// gCEffectCommandModifyEntity.PropertySet

	public static class eEPropertySetType extends G3Enum {
		public static final int eEPropertySetType_Unknown = 0;
		public static final int eEPropertySetType_Base = 1;
		public static final int eEPropertySetType_MeshVisual = 2;
		public static final int eEPropertySetType_DummyVisual = 3;
		public static final int eEPropertySetType_Advanced = 4;
		public static final int eEPropertySetType_Navigation = 5;
		public static final int eEPropertySetType_DynamicCollisionCircle = 6;
		public static final int eEPropertySetType_CollisionCircle = 7;
		public static final int eEPropertySetType_NavZone = 8;
		public static final int eEPropertySetType_NegZone = 9;
		public static final int eEPropertySetType_NavPath = 10;
		public static final int eEPropertySetType_PrefPath = 11;
		public static final int eEPropertySetType_NavHelper = 12;
		public static final int eEPropertySetType_RigidBody = 13;
		public static final int eEPropertySetType_CollisionShape = 14;
		public static final int eEPropertySetType_DynamicLight = 15;
		public static final int eEPropertySetType_DirectionalLight = 16;
		public static final int eEPropertySetType_PointLight = 17;
		public static final int eEPropertySetType_SpotLight = 18;
		public static final int eEPropertySetType_Hemisphere = 19;
		public static final int eEPropertySetType_MovementBase = 20;
		public static final int eEPropertySetType_CharacterMovement = 21;
		public static final int eEPropertySetType_CharacterControl = 22;
		public static final int eEPropertySetType_CharacterSensor = 23;
		public static final int eEPropertySetType_CameraAI = 24;
		public static final int eEPropertySetType_Editor = 25;
		public static final int eEPropertySetType_AIHelper = 26;
		public static final int eEPropertySetType_AIHelperFreePoint = 27;
		public static final int eEPropertySetType_AIHelperSpawnPoint = 28;
		public static final int eEPropertySetType_AIHelperPlayerStart = 29;
		public static final int eEPropertySetType_NPC = 30;
		public static final int eEPropertySetType_Inventory = 31;
		public static final int eEPropertySetType_Clock = 32;
		public static final int eEPropertySetType_Effect = 33;
		public static final int eEPropertySetType_ParticleEffect = 34;
		public static final int eEPropertySetType_EventFilter = 35;
		public static final int eEPropertySetType_EventDebugger = 36;
		public static final int eEPropertySetType_EventLogic = 37;
		public static final int eEPropertySetType_EventScript = 38;
		public static final int eEPropertySetType_Trigger = 39;
		public static final int eEPropertySetType_TriggerList = 40;
		public static final int eEPropertySetType_TriggerCombiner = 41;
		public static final int eEPropertySetType_Skydome = 42;
		public static final int eEPropertySetType_EditorVisual = 43;
		public static final int eEPropertySetType_EngineCaps = 44;
		public static final int eEPropertySetType_ScriptRoutine = 45;
		public static final int eEPropertySetType_SpeedTree = 46;
		public static final int eEPropertySetType_SpeedTreeWind = 47;
		public static final int eEPropertySetType_Item = 48;
		public static final int eEPropertySetType_Interaction = 49;
		public static final int eEPropertySetType_TouchDamage = 50;
		public static final int eEPropertySetType_Damage = 51;
		public static final int eEPropertySetType_DamageReceiver = 52;
		public static final int eEPropertySetType_QuestManager = 53;
		public static final int eEPropertySetType_Quest = 54;
		public static final int eEPropertySetType_InfoManager = 55;
		public static final int eEPropertySetType_Info = 56;
		public static final int eEPropertySetType_InfoScript = 57;
		public static final int eEPropertySetType_InfoScriptLine = 58;
		public static final int eEPropertySetType_Focus = 59;
		public static final int eEPropertySetType_PlayerMemory = 60;
		public static final int eEPropertySetType_NavOffset = 61;
		public static final int eEPropertySetType_Enclave = 62;
		public static final int eEPropertySetType_Arena = 63;
		public static final int eEPropertySetType_Area = 64;
		public static final int eEPropertySetType_Vegetation = 65;
		public static final int eEPropertySetType_VegetationBrush = 66;
		public static final int eEPropertySetType_VegetationRubber = 67;
		public static final int eEPropertySetType_Dialog = 68;
		public static final int eEPropertySetType_Lock = 69;
		public static final int eEPropertySetType_Door = 70;
		public static final int eEPropertySetType_StaticLight = 71;
		public static final int eEPropertySetType_StaticPointLight = 72;
		public static final int eEPropertySetType_StaticSpotLight = 73;
		public static final int eEPropertySetType_Illuminated = 74;
		public static final int eEPropertySetType_Statistics = 75;
		public static final int eEPropertySetType_PhysicScene = 76;
		public static final int eEPropertySetType_Party = 77;
		public static final int eEPropertySetType_Anchor = 78;
		public static final int eEPropertySetType_Letter = 79;
		public static final int eEPropertySetType_Book = 80;
		public static final int eEPropertySetType_Map = 81;
		public static final int eEPropertySetType_LinkContainer = 82;
		public static final int eEPropertySetType_Projectile = 83;
		public static final int eEPropertySetType_Sound = 84;
		public static final int eEPropertySetType_Strip = 85;
		public static final int eEPropertySetType_ForceField = 86;
		public static final int eEPropertySetType_Particle = 87;
		public static final int eEPropertySetType_ParticleSystem = 88;
		public static final int eEPropertySetType_ParticleEmitter = 89;
		public static final int eEPropertySetType_SpriteEmitter = 90;
		public static final int eEPropertySetType_BeamEmitter = 91;
		public static final int eEPropertySetType_SparkEmitter = 92;
		public static final int eEPropertySetType_MeshEmitter = 93;
		public static final int eEPropertySetType_AIZone = 94;
		public static final int eEPropertySetType_AudioEmitter = 95;
		public static final int eEPropertySetType_Effect2 = 96;
		public static final int eEPropertySetType_LightStreaks = 97;
		public static final int eEPropertySetType_WeatherZone = 98;
		public static final int eEPropertySetType_Precipitation = 99;
		public static final int eEPropertySetType_Animation = 100;
		public static final int eEPropertySetType_Magic = 101;
		public static final int eEPropertySetType_Skill = 102;
		public static final int eEPropertySetType_Mover = 103;
		public static final int eEPropertySetType_Teleporter = 104;
		public static final int eEPropertySetType_ArmorSet = 105;
		// public static final int eEPropertySetType_??? = 106;
		public static final int eEPropertySetType_Flock = 107;
		public static final int eEPropertySetType_TreasureSet = 108;
		public static final int eEPropertySetType_Recipe = 109;
		public static final int eEPropertySetType_WaterZone = 110;
		public static final int eEPropertySetType_DistanceTrigger = 111;
		public static final int eEPropertySetType_TimeZone = 112;
	}

	// eCRigidBody_PS.BodyFlag

	public static class eERigidbody_Flag extends G3Enum {
		// ???
	}

	// eCParticle_PS.RotationFrom

	public static class eERotationFrom extends G3Enum {
		public static final int eERotationFrom_None = 0;
		public static final int eERotationFrom_Entity = 1;
		public static final int eERotationFrom_Offset = 2;
		public static final int eERotationFrom_Normal = 3;
	}

	// eCShaderBase.BlendMode

	public static class eEShaderMaterialBlendMode extends G3Enum {
		public static final int eEShaderMaterialBlendMode_Normal = 0;
		public static final int eEShaderMaterialBlendMode_Masked = 1;
		public static final int eEShaderMaterialBlendMode_AlphaBlend = 2;
		public static final int eEShaderMaterialBlendMode_Modulate = 3;
		public static final int eEShaderMaterialBlendMode_AlphaModulate = 4;
		public static final int eEShaderMaterialBlendMode_Translucent = 5;
		public static final int eEShaderMaterialBlendMode_Darken = 6;
		public static final int eEShaderMaterialBlendMode_Brighten = 7;
		public static final int eEShaderMaterialBlendMode_Invisible = 8;
	}

	// eCShaderDefault.TransformationType

	public static class eEShaderMaterialTransformation extends G3Enum {
		public static final int eEShaderMaterialTransformation_Default = 0;
		public static final int eEShaderMaterialTransformation_Instanced = 1;
		public static final int eEShaderMaterialTransformation_Skinned = 2;
		public static final int eEShaderMaterialTransformation_Tree_Branches = 3;
		public static final int eEShaderMaterialTransformation_Tree_Fronds = 4;
		public static final int eEShaderMaterialTransformation_Tree_Leafs = 5;
		public static final int eEShaderMaterialTransformation_Billboard = 6;
	}

	// eCShaderBase.MaxShaderVersion

	public static class eEShaderMaterialVersion extends G3Enum {
		public static final int eEShaderMaterialVersion_1_1 = 0;
		public static final int eEShaderMaterialVersion_1_4 = 1;
		public static final int eEShaderMaterialVersion_2_0 = 2;
		public static final int eEShaderMaterialVersion_3_0 = 3;
	}

	// eCCollisionShape.ShapeAABBAdapt

	public static class eEShapeAABBAdapt extends G3Enum {
		public static final int eEShapeAABBAdapt_None = 0;
		public static final int eEShapeAABBAdapt_LocalNode = 1;
		public static final int eEShapeAABBAdapt_LocalTree = 2;
	}

	// eCCollisionShape.Group
	// eCVisualAnimation_PS.BoneShapeGroup

	public static class eEShapeGroup extends G3Enum {
		public static final int eEShapeGroup_Undefined = 0;
		public static final int eEShapeGroup_Static = 1;
		public static final int eEShapeGroup_Dynamic = 2;
		public static final int eEShapeGroup_Shield = 3;
		public static final int eEShapeGroup_MeleeWeapon = 4;
		public static final int eEShapeGroup_Projectile = 5;
		public static final int eEShapeGroup_Movement = 6;
		public static final int eEShapeGroup_WeaponTrigger = 7;
		public static final int eEShapeGroup_ParadeSphere = 8;
		public static final int eEShapeGroup_Tree_Trunk = 9;
		public static final int eEShapeGroup_Tree_Branches = 10;
		public static final int eEShapeGroup_Camera = 11;
		public static final int eEShapeGroup_Movement_ZoneNPC = 12;
		public static final int eEShapeGroup_HeightRepulsor = 13;
		public static final int eEShapeGroup_Cloth = 14;
		public static final int eEShapeGroup_PhysicalBodyPart = 15;
		public static final int eEShapeGroup_KeyframedBodyPart = 16;
		public static final int eEShapeGroup_Camera_Obstacle = 17;
		public static final int eEShapeGroup_Projectile_Level = 18;
		public static final int eEShapeGroup_Trigger = 19;
		public static final int eEShapeGroup_Door = 20;
	}

	// eCCollisionShape.Material
	// eCResourceShaderMaterial_PS.PhysicMaterial
	// eCVisualAnimation_PS.BoneShapeMaterial

	public static class eEShapeMaterial extends G3Enum {
		public static final int eEShapeMaterial_None = 0;
		public static final int eEShapeMaterial_Wood = 1;
		public static final int eEShapeMaterial_Metal = 2;
		public static final int eEShapeMaterial_Water = 3;
		public static final int eEShapeMaterial_Stone = 4;
		public static final int eEShapeMaterial_Earth = 5;
		public static final int eEShapeMaterial_Ice = 6;
		public static final int eEShapeMaterial_Leather = 7;
		public static final int eEShapeMaterial_Clay = 8;
		public static final int eEShapeMaterial_Glass = 9;
		public static final int eEShapeMaterial_Flesh = 10;
		public static final int eEShapeMaterial_Snow = 11;
		public static final int eEShapeMaterial_Debris = 12;
		public static final int eEShapeMaterial_Foliage = 13;
		public static final int eEShapeMaterial_Magic = 14;
		public static final int eEShapeMaterial_Grass = 15;
		public static final int eEShapeMaterial_SpringAndDamper1 = 16;
		public static final int eEShapeMaterial_SpringAndDamper2 = 17;
		public static final int eEShapeMaterial_SpringAndDamper3 = 18;
		public static final int eEShapeMaterial_Damage = 19;
		public static final int eEShapeMaterial_Sand = 20;
		public static final int eEShapeMaterial_Movement = 21;
	}

	// eCResourceSound_PS.LoopMode

	public static class eESoundLoopMode extends G3Enum {
		public static final int eESoundLoopMode_Off = 0;
		public static final int eESoundLoopMode_Normal = 1;
		public static final int eESoundLoopMode_PingPong = 2;
	}

	// eCParticle_PS.SpawningSound

	public static class eESpawningSound extends G3Enum {
		public static final int eESpawningSound_Disabled = 0;
		public static final int eESpawningSound_LinearLocal = 1;
		public static final int eESpawningSound_LinearGlobal = 2;
		public static final int eESpawningSound_Random = 3;
	}

	// eCIlluminated_PS.StaticIlluminated

	public static class eEStaticIlluminated extends G3Enum {
		public static final int eEStaticIlluminated_Static = 0;
		public static final int eEStaticIlluminated_Dynamic = 1;
	}

	// eCVisualMeshBase_PS.StaticLightingType

	public static class eEStaticLighingType extends G3Enum {
		public static final int eEStaticLighingType_Lightmap = 0;
		/**
		 * Standard für eCVisualMeshDynamic_PS ?!
		 */
		public static final int eEStaticLighingType_Instance = 1;
	}

	// eCStrip_PS.Spawning

	public static class eEStripSpawning extends G3Enum {
		public static final int eEStripSpawning_Continuous = 0;
		public static final int eEStripSpawning_Movement = 1;
		public static final int eEStripSpawning_Timed = 2;
	}

	// eCTexCoordSrcOscillator.OscillatorTypeU
	// eCTexCoordSrcOscillator.OscillatorTypeV

	public static class eETexCoordSrcOscillatorType extends G3Enum {
		public static final int eETexCoordSrcOscillatorType_Pan = 0;
		public static final int eETexCoordSrcOscillatorType_Stretch = 1;
		public static final int eETexCoordSrcOscillatorType_StretchRepeat = 2;
		public static final int eETexCoordSrcOscillatorType_Jitter = 3;
	}

	// eCTexCoordSrcRotator.RotationType

	public static class eETexCoordSrcRotatorType extends G3Enum {
		public static final int eETexCoordSrcRotatorType_Once = 0;
		public static final int eETexCoordSrcRotatorType_Constant = 1;
		public static final int eETexCoordSrcRotatorType_Oscillate = 2;
	}

	// eCParticle_PS.DrawStyle

	public static class eETextureDrawStyle extends G3Enum {
		public static final int eETextureDrawStyle_Regular = 0;
		public static final int eETextureDrawStyle_AlphaBlend = 1;
		public static final int eETextureDrawStyle_Modulated = 2;
		public static final int eETextureDrawStyle_Translucent = 3;
		public static final int eETextureDrawStyle_AlphaModulate = 4;
		public static final int eETextureDrawStyle_Darken = 5;
		public static final int eETextureDrawStyle_Brighten = 6;
		public static final int eETextureDrawStyle_Invisible = 7;
	}

	// eCTriggerList_PS.ProcessType

	public static class eETriggerListProcessType extends G3Enum {
		public static final int eETriggerListProcessType_All = 0;
		public static final int eETriggerListProcessType_NextOne = 1;
		public static final int eETriggerListProcessType_Rand_One = 2;
	}

	// eCVegetationBrush_PS.ColorFunction

	public static class eEVegetationBrushColorFunction extends G3Enum {
		// public static final int eEVegetationBrushColorFunction_??? = 0;
		public static final int eEVegetationBrushColorFunction_Random = 1;
		public static final int eEVegetationBrushColorFunction_PerlinNoise = 2;
		public static final int eEVegetationBrushColorFunction_PerlinNoise_Improved = 3;
		public static final int eEVegetationBrushColorFunction_EbertNoise = 4;
		public static final int eEVegetationBrushColorFunction_PeacheyNoise = 5;
		public static final int eEVegetationBrushColorFunction_PeacheyNoise_Gradient = 6;
		public static final int eEVegetationBrushColorFunction_PeacheyNoise_GradientValue = 7;
		public static final int eEVegetationBrushColorFunction_PeacheyNoise_SparseConvolusion = 8;
		public static final int eEVegetationBrushColorFunction_PeacheyNoise_ValueConvolusion = 9;
	}

	// eCVegetationBrush_PS.BrushMode

	public static class eEVegetationBrushMode extends G3Enum {
		public static final int eEVegetationBrushMode_Place = 0;
		public static final int eEVegetationBrushMode_Remove = 1;
	}

	// eCVegetationBrush_PS.BrushPlacement

	public static class eEVegetationBrushPlace extends G3Enum {
		public static final int eEVegetationBrushPlace_DistanceSelf = 0;
		public static final int eEVegetationBrushPlace_DistanceOther = 1;
		public static final int eEVegetationBrushPlace_RemoveOther = 2;
	}

	// eCVegetationBrush_PS.ProbabilityFunction

	public static class eEVegetationBrushProbabilityFunction extends G3Enum {
		public static final int eEVegetationBrushProbabilityFunction_None = 0;
		public static final int eEVegetationBrushProbabilityFunction_Shape = 1;
		public static final int eEVegetationBrushProbabilityFunction_PerlinNoise = 2;
		public static final int eEVegetationBrushProbabilityFunction_PerlinNoise_Improved = 3;
		public static final int eEVegetationBrushProbabilityFunction_EbertNoise = 4;
		public static final int eEVegetationBrushProbabilityFunction_PeacheyNoise = 5;
		public static final int eEVegetationBrushProbabilityFunction_PeacheyNoise_Gradient = 6;
		public static final int eEVegetationBrushProbabilityFunction_PeacheyNoise_GradientValue = 7;
		public static final int eEVegetationBrushProbabilityFunction_PeacheyNoise_SparseConvolusion = 8;
		public static final int eEVegetationBrushProbabilityFunction_PeacheyNoise_ValueConvolusion = 9;
	}

	// eCVegetationBrush_PS.BrushShape

	public static class eEVegetationBrushShape extends G3Enum {
		public static final int eEVegetationBrushShape_Circle = 0;
		public static final int eEVegetationBrushShape_Rect = 1;
		public static final int eEVegetationBrushShape_Single = 2;
	}

	// eCVegetation_Mesh.MeshShading

	public static class eEVegetationMeshShading extends G3Enum {
		public static final int eEVegetationMeshShading_MeshNormal = 0;
		public static final int eEVegetationMeshShading_EntryOrientation = 1;
	}

	// eCParticle_PS.VelocityDirectionFrom

	public static class eEVelocityDirectionFrom extends G3Enum {
		public static final int eEVelocityDirectionFrom_None = 0;
		public static final int eEVelocityDirectionFrom_StartPositionAndOwner = 1;
		public static final int eEVelocityDirectionFrom_OwnerAndStartPosition = 2;
	}

	// eCWeatherZone_PS.AmbientBackLightOverwrite
	// eCWeatherZone_PS.AmbientGeneralOverwrite
	// eCWeatherZone_PS.AmbientIntensityOverwrite
	// eCWeatherZone_PS.CloudColorOverwrite
	// eCWeatherZone_PS.CloudThicknessOverwrite
	// eCWeatherZone_PS.FogColorOverwrite
	// eCWeatherZone_PS.FogEndOverwrite
	// eCWeatherZone_PS.FogStartOverwrite
	// eCWeatherZone_PS.HazeColorOverwrite
	// eCWeatherZone_PS.LightDiffuseOverwrite
	// eCWeatherZone_PS.LightIntensityOverwrite
	// eCWeatherZone_PS.LightSpecularOverwrite
	// eCWeatherZone_PS.SkyColorOverwrite

	public static class eEWeatherZoneOverwrite extends G3Enum {
		public static final int eEWeatherZoneOverwrite_None = 0;
		public static final int eEWeatherZoneOverwrite_Overwrite = 1;
		public static final int eEWeatherZoneOverwrite_Modulate = 2;
		public static final int eEWeatherZoneOverwrite_Add = 3;
	}

	// eCWeatherZone_PS.Shape

	public static class eEWeatherZoneShape extends G3Enum {
		public static final int eEWeatherZoneShape_2D_Circle = 0;
		public static final int eEWeatherZoneShape_2D_Rect = 1;
		public static final int eEWeatherZoneShape_3D_Sphere = 2;
		public static final int eEWeatherZoneShape_3D_Box = 3;
	}

	// gCScriptRoutine_PS.AIMode

	public static class gEAIMode extends G3Enum {
		public static final int gEAIMode_Routine = 0;
		public static final int gEAIMode_Reaction = 1;
		// public static final int gEAIMode_??? = 2;
		public static final int gEAIMode_Combat = 3;
		public static final int gEAIMode_Flee = 4;
		public static final int gEAIMode_Arena = 5;
		public static final int gEAIMode_Talk = 6;
		// public static final int gEAIMode_??? = 7;
		public static final int gEAIMode_Down = 8;
		public static final int gEAIMode_Dead = 9;
		public static final int gEAIMode_Abandoned = 10;
		public static final int gEAIMode_PlayerParty = 11;
	}

	// gCScriptRoutine_PS.Action

	public static class gEAction extends G3Enum {
		public static final int gEAction_None = 0;
		public static final int gEAction_Attack = 1;
		public static final int gEAction_PowerAttack = 2;
		public static final int gEAction_QuickAttack = 3;
		public static final int gEAction_QuickAttackR = 4;
		public static final int gEAction_QuickAttackL = 5;
		public static final int gEAction_SimpleWhirl = 6;
		public static final int gEAction_TurnLeft = 7;
		public static final int gEAction_TurnRight = 8;
		public static final int gEAction_SprintAttack = 9;
		public static final int gEAction_WhirlAttack = 10;
		public static final int gEAction_PierceAttack = 11;
		public static final int gEAction_JumpAttack = 12;
		public static final int gEAction_RamAttack = 13;
		public static final int gEAction_HackAttack = 14;
		public static final int gEAction_FinishingAttack = 15;
		public static final int gEAction_Parade = 16;
		public static final int gEAction_ParadeR = 17;
		public static final int gEAction_ParadeL = 18;
		public static final int gEAction_ExitParade = 19;
		public static final int gEAction_QuickParadeStumble = 20;
		public static final int gEAction_ParadeStumble = 21;
		public static final int gEAction_ParadeStumbleR = 22;
		public static final int gEAction_ParadeStumbleL = 23;
		public static final int gEAction_HeavyParadeStumble = 24;
		public static final int gEAction_QuickStumble = 25;
		public static final int gEAction_Stumble = 26;
		public static final int gEAction_StumbleR = 27;
		public static final int gEAction_StumbleL = 28;
		public static final int gEAction_SitKnockDown = 29;
		public static final int gEAction_GetUpAttack = 30;
		public static final int gEAction_GetUpParade = 31;
		public static final int gEAction_LieKnockDown = 32;
		public static final int gEAction_LieKnockOut = 33;
		public static final int gEAction_PierceStumble = 34;
		public static final int gEAction_Die = 35;
		public static final int gEAction_LieDead = 36;
		public static final int gEAction_LiePiercedKO = 37;
		public static final int gEAction_LiePiercedDead = 38;
		public static final int gEAction_AbortAttack = 39;
		public static final int gEAction_Aim = 40;
		public static final int gEAction_Shoot = 41;
		public static final int gEAction_Reload = 42;
		public static final int gEAction_Cock = 43;
		public static final int gEAction_Cast = 44;
		public static final int gEAction_PowerCast = 45;
		public static final int gEAction_MagicParade = 46;
		public static final int gEAction_QuickCast = 47;
		public static final int gEAction_Summon = 48;
		public static final int gEAction_Heal = 49;
		public static final int gEAction_Wait = 50;
		public static final int gEAction_JumpBack = 51;
		public static final int gEAction_Fwd = 52;
		public static final int gEAction_Back = 53;
		public static final int gEAction_Left = 54;
		public static final int gEAction_Right = 55;
		public static final int gEAction_Move = 56;
		public static final int gEAction_Jump = 57;
		public static final int gEAction_Evade = 58;
		public static final int gEAction_Slide = 59;
		public static final int gEAction_Fall = 60;
		public static final int gEAction_Dive = 61;
		public static final int gEAction_COMBATACTIONS_END = 62;
		public static final int gEAction_Use = 63;
		public static final int gEAction_Equip = 64;
		public static final int gEAction_Reach = 65;
		public static final int gEAction_Take = 66;
		public static final int gEAction_Drop = 67;
		public static final int gEAction_Hold = 68;
		public static final int gEAction_HoldLeft = 69;
		public static final int gEAction_HoldRight = 70;
		public static final int gEAction_Transfer = 71;
		public static final int gEAction_FlameSword = 72;
		public static final int gEAction_Fry = 73;
		public static final int gEAction_Eat = 74;
		public static final int gEAction_Drink = 75;
		public static final int gEAction_Stand = 76;
		public static final int gEAction_Sneak = 77;
		public static final int gEAction_Kneel = 78;
		public static final int gEAction_SitGround = 79;
		public static final int gEAction_SitStool = 80;
		public static final int gEAction_SitBench = 81;
		public static final int gEAction_SitThrone = 82;
		public static final int gEAction_SleepBed = 83;
		public static final int gEAction_SleepGround = 84;
		public static final int gEAction_OrcDrum = 85;
		public static final int gEAction_Waterpipe = 86;
		public static final int gEAction_Anvil = 87;
		public static final int gEAction_OrcAnvil = 88;
		public static final int gEAction_Forge = 89;
		public static final int gEAction_CoolWeapon = 90;
		public static final int gEAction_DrinkWaterBarrel = 91;
		public static final int gEAction_GrindStone = 92;
		public static final int gEAction_Cauldron = 93;
		public static final int gEAction_Barbecue = 94;
		public static final int gEAction_Alchemy = 95;
		public static final int gEAction_Bookstand = 96;
		public static final int gEAction_Stoneplate = 97;
		public static final int gEAction_PickOre = 98;
		public static final int gEAction_OpenChest = 99;
		public static final int gEAction_Lockpick = 100;
		public static final int gEAction_OpenDoor = 101;
		public static final int gEAction_ExitInteract = 102;
		public static final int gEAction_PickGround = 103;
		public static final int gEAction_DigGround = 104;
		public static final int gEAction_Repair = 105;
		public static final int gEAction_SawLog = 106;
		public static final int gEAction_Lumberjack = 107;
		public static final int gEAction_RakeField = 108;
		public static final int gEAction_CleanFloor = 109;
		public static final int gEAction_TiltOrcBoulder = 110;
		public static final int gEAction_HoldOrcBoulder = 111;
		public static final int gEAction_LiftOrcBoulder = 112;
		public static final int gEAction_Fan = 113;
		public static final int gEAction_Dance = 114;
		public static final int gEAction_OrcDance = 115;
		public static final int gEAction_FieldWork = 116;
		public static final int gEAction_EatGround = 117;
		public static final int gEAction_DrinkWater = 118;
		public static final int gEAction_ArmsCrossed = 119;
		public static final int gEAction_HandsOnHips = 120;
		public static final int gEAction_Warn = 121;
		public static final int gEAction_Ambient = 122;
		public static final int gEAction_Think = 123;
		public static final int gEAction_Pee = 124;
		public static final int gEAction_Say = 125;
		public static final int gEAction_Listen = 126;
		public static final int gEAction_WatchFight = 127;
		public static final int gEAction_Cheer = 128;
		public static final int gEAction_SuperCheer = 129;
		public static final int gEAction_Applaud = 130;
		public static final int gEAction_QuickUse0 = 131;
		public static final int gEAction_QuickUse1 = 132;
		public static final int gEAction_QuickUse2 = 133;
		public static final int gEAction_QuickUse3 = 134;
		public static final int gEAction_QuickUse4 = 135;
		public static final int gEAction_QuickUse5 = 136;
		public static final int gEAction_QuickUse6 = 137;
		public static final int gEAction_QuickUse7 = 138;
		public static final int gEAction_QuickUse8 = 139;
		public static final int gEAction_QuickUse9 = 140;
		public static final int gEAction_QuickUseH = 141;
		public static final int gEAction_QuickUseM = 142;
		public static final int gEAction_QuickUseS = 143;
		public static final int gEAction_Count = 145;

	}

	// gCScriptRoutine_PS.AmbientAction

	public static class gEAmbientAction extends G3Enum {
		public static final int gEAmbientAction_Ambient = 0;
		public static final int gEAmbientAction_Listen = 1;
		public static final int gEAmbientAction_Count = 2;
	}

	// gCAnchor_PS.AnchorType

	public static class gEAnchorType extends G3Enum {
		public static final int gEAnchorType_Local = 0;
		public static final int gEAnchorType_Roam = 1;
		public static final int gEAnchorType_Patrol = 2;
		public static final int gEAnchorType_Event = 3;
	}

	// gCScriptRoutine_PS.AniState

	public static class gEAniState extends G3Enum {
		public static final int gEAniState_Dummy0 = 0;
		public static final int gEAniState_Dummy1 = 1;
		public static final int gEAniState_Stand = 2;
		public static final int gEAniState_Sneak = 3;
		public static final int gEAniState_Parade = 4;
		public static final int gEAniState_Kneel = 5;
		public static final int gEAniState_SitGround = 6;
		public static final int gEAniState_SitStool = 7;
		public static final int gEAniState_SitBench = 8;
		public static final int gEAniState_SitThrone = 9;
		public static final int gEAniState_SleepBed = 10;
		public static final int gEAniState_SleepGround = 11;
		public static final int gEAniState_TiltOrcBoulder = 12;
		public static final int gEAniState_HoldOrcBoulder = 13;
		public static final int gEAniState_LiftOrcBoulder = 14;
		public static final int gEAniState_SitKnockDown = 15;
		public static final int gEAniState_LieKnockDown = 16;
		public static final int gEAniState_LieKnockOut = 17;
		public static final int gEAniState_LieStraightDead = 18;
		public static final int gEAniState_LieDead = 19;
		public static final int gEAniState_LiePiercedKO = 20;
		public static final int gEAniState_LiePiercedDead = 21;
		public static final int gEAniState_TalkStand = 22;
		public static final int gEAniState_TalkSitGround = 23;
		public static final int gEAniState_TalkSitStool = 24;
		public static final int gEAniState_TalkSitBench = 25;
		public static final int gEAniState_TalkSitThrone = 26;
		public static final int gEAniState_Wade = 27;
		public static final int gEAniState_Swim = 28;
		public static final int gEAniState_Dive = 29;
		public static final int gEAniState_Count = 30;
	}

	// gCArena_PS.Status

	public static class gEArenaStatus extends G3Enum {
		public static final int gEArenaStatus_None = 0;
		public static final int gEArenaStatus_Running = 1;
	}

	// gCNPC_PS.AttackReason
	// gCNPC_PS.LastPlayerAR

	public static class gEAttackReason extends G3Enum {
		public static final int gEAttackReason_None = 0;
		public static final int gEAttackReason_LeaveZone = 1;
		public static final int gEAttackReason_Livestock = 2;
		public static final int gEAttackReason_StopThief = 3;
		public static final int gEAttackReason_Theft = 4;
		public static final int gEAttackReason_UseInteractObject = 5;
		public static final int gEAttackReason_ReactToWeapon = 6;
		public static final int gEAttackReason_Angry = 7;
		public static final int gEAttackReason_StopFight = 8;
		public static final int gEAttackReason_ReactToDamage = 9;
		public static final int gEAttackReason_GateGuard = 10;
		public static final int gEAttackReason_Intruder = 11;
		public static final int gEAttackReason_Enemy = 12;
		public static final int gEAttackReason_Murder = 13;
		public static final int gEAttackReason_Prey = 14;
		public static final int gEAttackReason_DriveAway = 15;
		public static final int gEAttackReason_PlayerCommand = 16;
		public static final int gEAttackReason_Duel = 17;
		public static final int gEAttackReason_Arena = 18;
		public static final int gEAttackReason_Revolution = 19;
	}

	// gCNPC_PS.AttitudeToPlayer2

	public static class gEAttitude extends G3Enum {
		public static final int gEAttitude_None = 0;
		public static final int gEAttitude_Friendly = 1;
		public static final int gEAttitude_Neutral = 2;
		public static final int gEAttitude_Angry = 3;
		public static final int gEAttitude_Hostile = 4;
		public static final int gEAttitude_Predator = 5;
		public static final int gEAttitude_Panic = 6;
	}

	// gCNPC_PS.Bearing

	public static class gEBearing extends G3Enum {
		public static final int gEBearing_None = 0;
		public static final int gEBearing_Babe = 1;
		public static final int gEBearing_Zombie = 2;
	}

	// gCMagic_PS.CastType

	public static class gECastType extends G3Enum {
		public static final int gECastType_Immediate = 0;
		public static final int gECastType_Power = 1;
		public static final int gECastType_Regular = 2;
	}

	// gCCharacterControl_PS.ControlFOR

	public static class gECharacterControlFOR extends G3Enum {
		public static final int gECharacterControlFOR_Camera = 0;
		public static final int gECharacterControlFOR_Target = 1;
	}

	// gCNPC_PS.Class

	public static class gEClass extends G3Enum {
		public static final int gEClass_None = 0;
		public static final int gEClass_Mage = 1;
		public static final int gEClass_Paladin = 2;
		public static final int gEClass_Warrior = 3;
		public static final int gEClass_Ranger = 4;
		public static final int gEClass_EMPTY_A = 5;
		public static final int gEClass_EMPTY_B = 6;
		public static final int gEClass_EMPTY_C = 7;
		public static final int gEClass_EMPTY_D = 8;
		public static final int gEClass_EMPTY_E = 9;
		public static final int gEClass_EMPTY_F = 10;
		public static final int gEClass_EMPTY_G = 11;
	}

	// gCDamageReceiver_PS.DamageType
	// gCDamage_PS.DamageType

	public static class gEDamageType extends G3Enum {
		public static final int gEDamageType_None = 0;
		public static final int gEDamageType_Impact = 1;
		public static final int gEDamageType_Blade = 2;
		public static final int gEDamageType_Missile = 3;
		public static final int gEDamageType_Fire = 4;
		public static final int gEDamageType_Ice = 5;
		public static final int gEDamageType_Lightning = 6;
		public static final int gEDamageType_Physics = 7;
	}

	// eCParticleSystemBase_PS.DirectionMode

	public static class gEDirectionMode extends G3Enum {
		// public static final int gEDirectionMode_None = ???;
		// public static final int gEDirectionMode_Direction = ???;
		// public static final int gEDirectionMode_Target = ???;
	}

	public static class gEDirection extends G3Enum {
		public static final int gEDirection_None = 0;
		public static final int gEDirection_Fwd = 1;
		public static final int gEDirection_Back = 2;
		public static final int gEDirection_Left = 3;
		public static final int gEDirection_Right = 4;
		public static final int gEDirection_FwdLeft = 5;
		public static final int gEDirection_FwdRight = 6;
		public static final int gEDirection_BackLeft = 7;
		public static final int gEDirection_BackRight = 8;
	}

	// gCDoor_PS.Status

	public static class gEDoorStatus extends G3Enum {
		public static final int gEDoorStatus_Open = 0;
		public static final int gEDoorStatus_Closed = 1;
	}

	// gCEffectCommandSpawnEntity.Link

	public static class gEEffectLink extends G3Enum {
		public static final int gEEffectLink_Independent = 0;
		public static final int gEEffectLink_TargetEntity = 1;
		public static final int gEEffectLink_TargetBone = 2;
	}

	// eCParticleSystemBase_PS.EmitterType

	public static class gEEmitterType extends G3Enum {
		// public static final int gEEmitterType_Point = ???;
		// public static final int gEEmitterType_Line = ???;
		// public static final int gEEmitterType_Box = ???;
		// public static final int gEEmitterType_Circle = ???;
		// public static final int gEEmitterType_Sphere = ???;
		// public static final int gEEmitterType_Mesh = ???;
	}

	// gCEnclave_PS.Status

	public static class gEEnclaveStatus extends G3Enum {
		public static final int gEEnclaveStatus_Routine = 0;
		public static final int gEEnclaveStatus_Attack = 1;
		public static final int gEEnclaveStatus_Flee = 2;
	}

	// gCDynamicLayer.EntityType

	public static class gEEntityType extends G3Enum {
		public static final int gEEntityType_Game = 0;
		public static final int gEEntityType_Temporary = 1;
	}

	// eCParticleSystemBase_PS.FOR
	// eCParticleSystemBase_PS.TFOR

	public static class gEFOR extends G3Enum {
		// public static final int gEFOR_World = ???;
		// public static final int gEFOR_Object = ???;
	}

	// gCFocus_PS.CurrentMode

	public static class gEFocus extends G3Enum {
		public static final int gEFocus_None = 0;
		public static final int gEFocus_Hard = 1;
		public static final int gEFocus_Soft = 2;
		public static final int gEFocus_Shape = 3;
	}

	// gCFocus_PS.FocusLookAtMode

	public static class gEFocusLookAt extends G3Enum {
		public static final int gEFocusLookAt_Camera = 0;
		public static final int gEFocusLookAt_Entity = 1;
		public static final int gEFocusLookAt_Keys = 2;
	}

	// gCFocus_PS.FocusLookAtKeysFOR

	public static class gEFocusLookAtKeysFOR extends G3Enum {
		public static final int gEFocusLookAtKeysFOR_Camera = 0;
		public static final int gEFocusLookAtKeysFOR_Entity = 1;
	}

	// gCInteraction_PS.FocusNameType

	public static class gEFocusNameType extends G3Enum {
		public static final int gEFocusNameType_Skeleton = 0;
		public static final int gEFocusNameType_Entity = 1;
		public static final int gEFocusNameType_Bone = 2;
		public static final int gEFocusNameType_Disable = 3;
	}

	// gCInteraction_PS.FocusPri

	public static class gEFocusPriority extends G3Enum {
		public static final int gEFocusPriority_None = 0;
		public static final int gEFocusPriority_Normal = 1;
		public static final int gEFocusPriority_High = 2;
	}

	// gCNPC_PS.Gender

	public static class gEGender extends G3Enum {
		public static final int gEGender_Male = 0;
		public static final int gEGender_Female = 1;
	}

	// gCGeometryLayer.GeometryType

	public static class gEGeometryType extends G3Enum {
		public static final int gEGeometryType_Spatial = 0;
		public static final int gEGeometryType_Culling = 1;
	}

	// gCNPC_PS.GuardStatus

	public static class gEGuardStatus extends G3Enum {
		public static final int gEGuardStatus_Active = 0;
		public static final int gEGuardStatus_FirstWarnGiven = 1;
		public static final int gEGuardStatus_SecondWarnGiven = 2;
		public static final int gEGuardStatus_Inactive = 3;
		public static final int gEGuardStatus_Behind = 4;
	}

	// gCScriptRoutine_PS.HitDirection

	public static class gEHitDirection extends G3Enum {
		public static final int gEHitDirection_Left = 0;
		public static final int gEHitDirection_Right = 1;
	}

	// gCInfo_PS.CondType

	public static class gEInfoCondType extends G3Enum {
		public static final int gEInfoCondType_Crime = 0;
		public static final int gEInfoCondType_Fight = 1;
		public static final int gEInfoCondType_Hello = 2;
		public static final int gEInfoCondType_General = 3;
		public static final int gEInfoCondType_Overtime = 4;
		public static final int gEInfoCondType_Open = 5;
		public static final int gEInfoCondType_Activator = 6;
		public static final int gEInfoCondType_Running = 7;
		public static final int gEInfoCondType_Delivery = 8;
		public static final int gEInfoCondType_PartDelivery = 9;
		public static final int gEInfoCondType_Success = 10;
		public static final int gEInfoCondType_DoCancel = 11;
		public static final int gEInfoCondType_Failed = 12;
		public static final int gEInfoCondType_Cancelled = 13;
		public static final int gEInfoCondType_Join = 14;
		public static final int gEInfoCondType_Dismiss = 15;
		public static final int gEInfoCondType_Teach = 16;
		public static final int gEInfoCondType_Trade = 17;
		public static final int gEInfoCondType_PickPocket = 18;
		public static final int gEInfoCondType_Ready = 19;
		public static final int gEInfoCondType_Lost = 20;
		public static final int gEInfoCondType_Reactivator = 21;
		public static final int gEInfoCondType_Won = 22;
		public static final int gEInfoCondType_MasterThief = 23;
		public static final int gEInfoCondType_EnclaveFriend = 24;
		public static final int gEInfoCondType_PoliticalFriend = 25;
		public static final int gEInfoCondType_FirstWarn = 26;
		public static final int gEInfoCondType_SecondWarn = 27;
		public static final int gEInfoCondType_MobJoin = 28;
		public static final int gEInfoCondType_SlaveJoin = 29;
		public static final int gEInfoCondType_LongTimeNoSee = 30;
		public static final int gEInfoCondType_EnclaveCrime = 31;
		public static final int gEInfoCondType_PoliticalCrime = 32;
		public static final int gEInfoCondType_MobDismiss = 33;
		public static final int gEInfoCondType_Wait = 34;
		public static final int gEInfoCondType_Heal = 35;
		public static final int gEInfoCondType_NothingToSay = 50;
		public static final int gEInfoCondType_End = 51;
		public static final int gEInfoCondType_Back = 52;
	}

	// NOTE: the enum member names are guesswork!
	// (used the strings from NPCStatusToString)

	public static class gEInfoNPCStatus extends G3Enum {
		public static final int gEInfoNPCStatus_Alive = 0;
		public static final int gEInfoNPCStatus_UnHarmed = 1;
		public static final int gEInfoNPCStatus_Defeated = 2;
		public static final int gEInfoNPCStatus_Dead = 3;
		public static final int gEInfoNPCStatus_TalkedToPlayer = 4;
		public static final int gEInfoNPCStatus_NotTalkedToPlayer = 5;
	}

	// gCInfo_PS.Type

	public static class gEInfoType extends G3Enum {
		public static final int gEInfoType_Refuse = 0;
		public static final int gEInfoType_Important = 1;
		public static final int gEInfoType_News = 2;
		public static final int gEInfoType_Info = 3;
		public static final int gEInfoType_Parent = 4;
		public static final int gEInfoType_Comment = 5;
	}

	// gCItem_PS.Category

	public static class gEItemCategory extends G3Enum {
		public static final int gEItemCategory_None = 0;
		public static final int gEItemCategory_Weapon = 1;
		public static final int gEItemCategory_Armor = 2;
		public static final int gEItemCategory_Artefact = 3;
		public static final int gEItemCategory_Alchemy = 4;
		public static final int gEItemCategory_Empty_A = 5;
		public static final int gEItemCategory_Misc = 6;
		public static final int gEItemCategory_Written = 7;
		public static final int gEItemCategory_Empty_B = 8;
		public static final int gEItemCategory_Spellbook = 9;
		public static final int gEItemCategory_Skill = 10;
		public static final int gEItemCategory_Count = 11;
	}

	// gCNPC_PS.LastFightAgainstPlayer

	public static class gELastFightAgainstPlayer extends G3Enum {
		public static final int gELastFightAgainstPlayer_None = 0;
		public static final int gELastFightAgainstPlayer_Lost = 1;
		public static final int gELastFightAgainstPlayer_Won = 2;
		public static final int gELastFightAgainstPlayer_Cancel = 3;
		public static final int gELastFightAgainstPlayer_Running = 4;
		public static final int gELastFightAgainstPlayer_Friendly = 5;
	}

	// gCLock_PS.Status

	public static class gELockStatus extends G3Enum {
		public static final int gELockStatus_Locked = 0;
		public static final int gELockStatus_Unlocked = 1;
	}

	// gCMagicBarrier_PS.Shape

	public static class gEMagicBarrierShape extends G3Enum {
		public static final int gEMagicBarrierShape_EntitySphere = 0;
		public static final int gEMagicBarrierShape_EntityBox = 1;
		public static final int gEMagicBarrierShape_Mesh = 2;
	}

	// eCParticleSystemBase_PS.AlphaDestination
	// eCParticleSystemBase_PS.AlphaSource

	public static class gEMasterBlending extends G3Enum {
		// public static final int gEMasterBlending_InvSrcAlpha = ???;
		// public static final int gEMasterBlending_SrcAlpha = ???;
	}

	// eCParticleSystemBase_PS.AlphaCompareFunction

	public static class gEMasterCmpFunc extends G3Enum {
		// public static final int gEMasterCmpFunc_Less = ???;
	}

	// gCDistanceTrigger_PS.MaxDistType

	public static class gEMaxDistType extends G3Enum {
		public static final int gEMaxDistType_Manual = 0;
		public static final int gEMaxDistType_NavZone = 1;
		public static final int gEMaxDistType_CollisionShape = 2;
	}

	// gCMover_PS.MoverBehavior

	public static class gEMoverBehavior extends G3Enum {
		public static final int gEMoverBehavior_1STATE_PLAYONCE = 0;
		public static final int gEMoverBehavior_2STATE_TOGGLE = 1;
		public static final int gEMoverBehavior_2STATE_TRIGGER_CONTROL = 2;
	}

	// gCMover_PS.MoverState

	public static class gEMoverState extends G3Enum {
		public static final int gEMoverState_OPEN = 0;
		public static final int gEMoverState_OPENING = 1;
		public static final int gEMoverState_CLOSED = 2;
		public static final int gEMoverState_CLOSING = 3;
	}

	// gCMover_PS.MoverTouchBehavior

	public static class gEMoverTouchBehavior extends G3Enum {
		public static final int gEMoverTouchBehavior_NONE = 0;
		public static final int gEMoverTouchBehavior_TOGGLE = 1;
		public static final int gEMoverTouchBehavior_WAIT = 2;
	}

	// gCEffectCommandMusicTrigger.Entrance
	// gCMusicTrigger.Entrance

	public static class gEMusicFragmentPosition extends G3Enum {
		public static final int gEMusicFragmentPosition_Begin = 0;
		public static final int gEMusicFragmentPosition_Current = 1;
		// public static final int gEMusicFragmentPosition_??? = 2;
		public static final int gEMusicFragmentPosition_RandomEntrance = 3;
	}

	// gCMusicLink.Type

	public static class gEMusicLink extends G3Enum {
		public static final int gEMusicLink_Exit = 0;
		public static final int gEMusicLink_Entrance = 1;
		public static final int gEMusicLink_Loop = 2;
		public static final int gEMusicLink_Generic = 3;
	}

	// gCMusicSequencer.FragmentSequence
	// gCMusicSequencer.TransitionSequence

	public static class gEMusicSequence extends G3Enum {
		// public static final int gEMusicSequence_Sequential = ???;
		// public static final int gEMusicSequence_Shuffle = ???;
		// public static final int gEMusicSequence_Random = ???;
	}

	// gCEffectCommandMusicTrigger.Transition
	// gCMusicTrigger.Transition

	public static class gEMusicTransition extends G3Enum {
		public static final int gEMusicTransition_None = 0;
		public static final int gEMusicTransition_MatchingConnector = 1;
		public static final int gEMusicTransition_SpecifiedConnector = 2;
		public static final int gEMusicTransition_SpecifiedTransition = 3;
	}

	// gCMusicTransition.Timing

	public static class gEMusicTransitionTiming extends G3Enum {
		public static final int gEMusicTransitionTiming_Centered = 0;
		public static final int gEMusicTransitionTiming_OverCurrent = 1;
		public static final int gEMusicTransitionTiming_OverNext = 2;
		public static final int gEMusicTransitionTiming_SpecifiedOffset = 3;
	}

	// gCEffectCommandMusicTrigger.Exit
	// gCMusicTrigger.Exit

	public static class gEMusicTriggerTime extends G3Enum {
		public static final int gEMusicTriggerTime_Immediately = 0;
		public static final int gEMusicTriggerTime_NextExit = 1;
		public static final int gEMusicTriggerTime_NextPriorityExit = 2;
		public static final int gEMusicTriggerTime_EndOfFragment = 3;
	}

	// gCNPC_PS.Type

	public static class gENPCType extends G3Enum {
		public static final int gENPCType_Personal = 0;
		public static final int gENPCType_Enclave = 1;
		public static final int gENPCType_Political = 2;
		public static final int gENPCType_Friend = 3;
	}

	// gCCollisionCircle_PS.Type

	public static class gENavObstacleType extends G3Enum {
		public static final int gENavObstacleType_Obstacle = 0;
		public static final int gENavObstacleType_Climbable = 1;
	}

	// gCParty_PS.PartyMemberType

	public static class gEPartyMemberType extends G3Enum {
		public static final int gEPartyMemberType_None = 0;
		public static final int gEPartyMemberType_Party = 1;
		public static final int gEPartyMemberType_Mob = 2;
		public static final int gEPartyMemberType_Slave = 3;
		public static final int gEPartyMemberType_Controlled = 4;
		public static final int gEPartyMemberType_Summoned = 5;
	}

	// gCEnclave_PS.KnownPlayerCrime
	// gCNPC_PS.LastPlayerCrime

	public static class gEPlayerCrime extends G3Enum {
		public static final int gEPlayerCrime_None = 0;
		public static final int gEPlayerCrime_Livestock = 1;
		public static final int gEPlayerCrime_Attack = 2;
		public static final int gEPlayerCrime_Theft = 3;
		public static final int gEPlayerCrime_Murder = 4;
		public static final int gEPlayerCrime_Count = 5;
	}

	// gCEnclave_PS.PoliticalAlignment
	// gCNPC_PS.PoliticalAlignment
	// gCQuest_PS.PoliticalSuccess

	public static class gEPoliticalAlignment extends G3Enum {
		public static final int gEPoliticalAlignment_None = 0;
		public static final int gEPoliticalAlignment_Orc = 1;
		public static final int gEPoliticalAlignment_Nrd = 2;
		public static final int gEPoliticalAlignment_Reb = 3;
		public static final int gEPoliticalAlignment_Mid = 4;
		public static final int gEPoliticalAlignment_Ass = 5;
		public static final int gEPoliticalAlignment_Nom = 6;
		public static final int gEPoliticalAlignment_Out = 7;
		public static final int gEPoliticalAlignment_Slave = 8;
		public static final int gEPoliticalAlignment_Pirate = 9;
		public static final int gEPoliticalAlignment_Citizen = 10;
		public static final int gEPoliticalAlignment_Mid_Torn = 11;
		public static final int gEPoliticalAlignment_Partisan = 12;
		public static final int gEPoliticalAlignment_Bakaresh = 13;
		public static final int gEPoliticalAlignment_Gonzalez = 14;
	}

	// gCProjectile_PS.PathStyle

	public static class gEProjectilePath extends G3Enum {
		public static final int gEProjectilePath_Line = 0;
		public static final int gEProjectilePath_Curve = 1;
		public static final int gEProjectilePath_Physics = 2;
		public static final int gEProjectilePath_Missile = 3;
	}

	// gCProjectile_PS.TouchBehavior

	public static class gEProjectileTouchBehavior extends G3Enum {
		public static final int gEProjectileTouchBehavior_KillSelf = 0;
		public static final int gEProjectileTouchBehavior_Reflect = 1;
		public static final int gEProjectileTouchBehavior_Stop = 2;
		public static final int gEProjectileTouchBehavior_MaterialDependant = 3;
	}

	// gCQuest_PS.status

	public static class gEQuestStatus extends G3Enum {
		public static final int gEQuestStatus_Open = 0;
		public static final int gEQuestStatus_Running = 1;
		public static final int gEQuestStatus_Success = 2;
		public static final int gEQuestStatus_Failed = 3;
		public static final int gEQuestStatus_Obsolete = 4;
		public static final int gEQuestStatus_Cancelled = 5;
		public static final int gEQuestStatus_Lost = 6;
		public static final int gEQuestStatus_Won = 7;
	}

	// gCQuest_PS.Type

	public static class gEQuestType extends G3Enum {
		public static final int gEQuestType_HasItems = 0;
		public static final int gEQuestType_Report = 1;
		public static final int gEQuestType_Kill = 2;
		public static final int gEQuestType_Defeat = 3;
		public static final int gEQuestType_DriveAway = 4;
		public static final int gEQuestType_Arena = 5;
		public static final int gEQuestType_BringNpc = 6;
		public static final int gEQuestType_FollowNpc = 7;
		public static final int gEQuestType_EnterArea = 8;
		// public static final int gEQuestType_??? = 9;
		public static final int gEQuestType_FreeEnclave = 10;
		public static final int gEQuestType_Plunder = 11;
		public static final int gEQuestType_Sparring = 12;
		public static final int gEQuestType_SpellCast = 13;
	}

	// gCAIZone_PS.SecurityLevel

	public static class gESecurityLevel extends G3Enum {
		public static final int gESecurityLevel_None = 0;
		public static final int gESecurityLevel_Public = 1;
		public static final int gESecurityLevel_Enclave = 2;
		public static final int gESecurityLevel_Political = 3;
	}

	// gCSessionEditor.State

	public static class gESessionEditorState extends G3Enum {
		public static final int gESessionEditorState_None = 0;
		public static final int gESessionEditorState_FreeFly = 1;
		public static final int gESessionEditorState_Steering = 2;
		public static final int gESessionEditorState_SteeringWithCam = 3;
		public static final int gESessionEditorState_Navigation = 4;
		public static final int gESessionEditorState_Physics = 5;
		public static final int gESessionEditorState_Property = 6;
	}

	// gCCharacterControl_PS.PressedKey

	public static class gESessionKey extends G3Enum {
		public static final int gESessionKey_None = 0;
		public static final int gESessionKey_Forward = 1;
		public static final int gESessionKey_Backward = 2;
		public static final int gESessionKey_StrafeLeft = 3;
		public static final int gESessionKey_StrafeRight = 4;
		public static final int gESessionKey_TurnPlayerRight = 5;
		public static final int gESessionKey_TurnPlayerLeft = 6;
		public static final int gESessionKey_TurnUp = 7;
		public static final int gESessionKey_TurnDown = 8;
		public static final int gESessionKey_RotateCamRight = 9;
		public static final int gESessionKey_RotateCamLeft = 10;
		public static final int gESessionKey_Walk = 11;
		public static final int gESessionKey_WalkToggle = 12;
		public static final int gESessionKey_Up = 13;
		public static final int gESessionKey_Down = 14;
		public static final int gESessionKey_Use1 = 15;
		public static final int gESessionKey_Use2 = 16;
		public static final int gESessionKey_Plus = 17;
		public static final int gESessionKey_Minus = 18;
		public static final int gESessionKey_QuickUse0 = 19;
		public static final int gESessionKey_QuickUse1 = 20;
		public static final int gESessionKey_QuickUse2 = 21;
		public static final int gESessionKey_QuickUse3 = 22;
		public static final int gESessionKey_QuickUse4 = 23;
		public static final int gESessionKey_QuickUse5 = 24;
		public static final int gESessionKey_QuickUse6 = 25;
		public static final int gESessionKey_QuickUse7 = 26;
		public static final int gESessionKey_QuickUse8 = 27;
		public static final int gESessionKey_QuickUse9 = 28;
		public static final int gESessionKey_Confirm = 29;
		public static final int gESessionKey_Cancel = 30;
		public static final int gESessionKey_InventoryMode = 31;
		public static final int gESessionKey_JournalModeLog = 32;
		public static final int gESessionKey_JournalModeCharScreen = 33;
		public static final int gESessionKey_JournalModeMagBook = 34;
		public static final int gESessionKey_JournalModeMap = 35;
		public static final int gESessionKey_QuickLoad = 36;
		public static final int gESessionKey_QuickSave = 37;
		public static final int gESessionKey_WeaponMode = 38;
		public static final int gESessionKey_Lock = 39;
		public static final int gESessionKey_Look = 40;
		public static final int gESessionKey_FirstPerson = 41;
		public static final int gESessionKey_ResetCamera = 42;

		public static final int gESessionKey_MAX = 45;
	}

	// gCInfoManager_PS.lastSessionState
	// gCSession.State

	public static class gESession_State extends G3Enum {
		public static final int gESession_State_None = 0;
		public static final int gESession_State_Movement = 1;
		public static final int gESession_State_Fight = 2;
		public static final int gESession_State_Ride_Movement = 3;
		public static final int gESession_State_Ride_Fight = 4;
		public static final int gESession_State_ItemUse = 5;
		public static final int gESession_State_Inventory = 6;
		public static final int gESession_State_Dialog = 7;
		public static final int gESession_State_Trade = 8;
		public static final int gESession_State_InteractObj = 9;
		public static final int gESession_State_Journal = 10;
		public static final int gESession_State_Editor = 11;
	}

	// gCRecipe_PS.Craft
	// gCSkill_PS.Category

	public static class gESkillCategory extends G3Enum {
		public static final int gESkillCategory_Combat = 0;
		public static final int gESkillCategory_Hunting = 1;
		public static final int gESkillCategory_Magic = 2;
		public static final int gESkillCategory_Smithing = 3;
		public static final int gESkillCategory_Theft = 4;
		public static final int gESkillCategory_Alchemy = 5;
		public static final int gESkillCategory_Misc = 6;
		public static final int gESkillCategory_Cooking = 7;
	}

	// gCInventorySlot.Slot

	public static class gESlot extends G3Enum {
		public static final int gESlot_None = 0;
		public static final int gESlot_RightHand = 1;
		public static final int gESlot_LeftHand = 2;
		public static final int gESlot_EquipLeftHand = 3;
		public static final int gESlot_Beard = 4;
		public static final int gESlot_BackLeft = 5;
		public static final int gESlot_BackRight = 6;
		public static final int gESlot_Bow = 7;
		public static final int gESlot_Crossbow = 8;
		public static final int gESlot_Armor = 9;
		public static final int gESlot_Belt = 10;
		public static final int gESlot_Amulet = 11;
		public static final int gESlot_Ring1 = 12;
		public static final int gESlot_Ring2 = 13;
		public static final int gESlot_Ammo = 14;
		public static final int gESlot_Hair = 15;
		public static final int gESlot_Head = 16;
		public static final int gESlot_Body = 17;
		public static final int gESlot_Helmet = 18;

		public static int fromUseType(int useType) {
			return switch (useType) {
				case gEUseType.gEUseType_Beard -> gESlot_Beard;
				case gEUseType.gEUseType_Bow -> gESlot_Bow;
				case gEUseType.gEUseType_CrossBow -> gESlot_Crossbow;
				case gEUseType.gEUseType_Armor -> gESlot_Armor;
				case gEUseType.gEUseType_Amulet -> gESlot_Amulet;
				case gEUseType.gEUseType_Hair -> gESlot_Hair;
				case gEUseType.gEUseType_Head -> gESlot_Head;
				case gEUseType.gEUseType_Body -> gESlot_Body;
				case gEUseType.gEUseType_Helmet -> gESlot_Helmet;
				default -> gESlot_None;
			};
		}

		public static String getSlotName(int slotType) {
			return switch (slotType) {
				case gESlot_Head -> "Slot_Head";
				case gESlot_Body -> "Slot_Body";
				case gESlot_Beard -> "Slot_Beard";
				case gESlot_Hair -> "Slot_Hair";
				case gESlot_Helmet -> "Slot_Helmet";
				default -> null;
			};
		}
	}

	// gCNPC_PS.Species

	public static class gESpecies extends G3Enum {
		public static final int gESpecies_Human = 0;
		public static final int gESpecies_Skeleton = 1;
		public static final int gESpecies_Zombie = 2;
		public static final int gESpecies_Golem = 3;
		public static final int gESpecies_Demon = 4;
		public static final int gESpecies_Orc = 5;
		public static final int gESpecies_Goblin = 6;
		public static final int gESpecies_Troll = 7;
		public static final int gESpecies_Minecrawler = 8;
		public static final int gESpecies_Scavenger = 9;
		public static final int gESpecies_Wolf = 10;
		public static final int gESpecies_Boar = 11;
		public static final int gESpecies_Sabertooth = 12;
		public static final int gESpecies_Shadowbeast = 13;
		public static final int gESpecies_Bison = 14;
		public static final int gESpecies_Rhino = 15;
		public static final int gESpecies_Ripper = 16;
		public static final int gESpecies_Lurker = 17;
		public static final int gESpecies_Varan = 18;
		public static final int gESpecies_Snapper = 19;
		public static final int gESpecies_Alligator = 20;
		public static final int gESpecies_Trex = 21;
		public static final int gESpecies_FireVaran = 22;
		public static final int gESpecies_Bloodfly = 23;
		public static final int gESpecies_Lizard = 24;
		public static final int gESpecies_Fish = 25;
		public static final int gESpecies_Meatbug = 26;
		public static final int gESpecies_Vulture = 27;
		public static final int gESpecies_Rabbit = 28;
		public static final int gESpecies_Deer = 29;
		public static final int gESpecies_Chicken = 30;
		public static final int gESpecies_Molerat = 31;
		public static final int gESpecies_Snake = 32;
		public static final int gESpecies_Pig = 33;
		public static final int gESpecies_Cow = 34;
		public static final int gESpecies_Spider = 35;
		public static final int gESpecies_Turtle = 36;
		public static final int gESpecies_Bird = 37;
		public static final int gESpecies_Gargoyle = 38;
		public static final int gESpecies_SwampLurker = 39;
		public static final int gESpecies_EMPTY_B = 40;
		public static final int gESpecies_Ogre = 41;
		public static final int gESpecies_Ray = 42;
		public static final int gESpecies_Scorpion = 43;
		public static final int gESpecies_Seagull = 44;
		public static final int gESpecies_Walrus = 45;
		public static final int gESpecies_Whale = 46;
		public static final int gESpecies_Eagle = 47;
		public static final int gESpecies_FireGolem = 48;
		public static final int gESpecies_IceGolem = 49;
		public static final int gESpecies_ScorpionKing = 50;
		public static final int gESpecies_Stalker = 51;
		public static final int gESpecies_Dragon = 52;
	}

	// gCMagic_PS.SpellDeity

	public static class gESpellDeity extends G3Enum {
		public static final int gESpellDeity_Adanos = 0;
		public static final int gESpellDeity_Beliar = 1;
		public static final int gESpellDeity_Innos = 2;
		public static final int gESpellDeity_None = 3;
	}

	// gCMagic_PS.SpellTarget

	public static class gESpellTarget extends G3Enum {
		public static final int gESpellTarget_Caster = 0;
		public static final int gESpellTarget_Focus = 1;
	}

	// gCMagic_PS.SpellType

	public static class gESpellType extends G3Enum {
		public static final int gESpellType_Projectile = 0;
		public static final int gESpellType_Target = 1;
		public static final int gESpellType_Summon = 2;
	}

	// gCInventoryStack.Type

	public static class gEStackType extends G3Enum {
		public static final int gEStackType_Normal = 0;
		public static final int gEStackType_Treasure = 1;
		public static final int gEStackType_Merchandise = 2;
	}

	// gCDialog_PS.TradeCategory

	public static class gETradeCategory extends G3Enum {
		public static final int gETradeCategory_None = 0;
		public static final int gETradeCategory_Valuable = 1;
		public static final int gETradeCategory_Trophy = 2;
		public static final int gETradeCategory_Empty_A = 3;
		public static final int gETradeCategory_Empty_B = 4;
	}

	// gCTreasureSet_PS.TreasureDistribution

	public static class gETreasureDistribution extends G3Enum {
		public static final int gETreasureDistribution_Plunder = 0;
		public static final int gETreasureDistribution_Unique = 1;
		public static final int gETreasureDistribution_Trophy = 2;
		public static final int gETreasureDistribution_Weaponry = 3;
		public static final int gETreasureDistribution_Trade_Generate = 4;
		public static final int gETreasureDistribution_Trade_Refresh = 5;
		public static final int gETreasureDistribution_Mining = 6;
		public static final int gETreasureDistribution_Pickpocket = 7;
		public static final int gETreasureDistribution_NotRandom = 8;
		public static final int gETreasureDistribution_Guard = 9;
		public static final int gETreasureDistribution_Ammunition = 10;
	}

	// gCInteraction_PS.UseType

	public static class gEUseType extends G3Enum {
		public static final int gEUseType_None = 0;
		public static final int gEUseType_Action = 1;
		public static final int gEUseType_1H = 2;
		public static final int gEUseType_2H = 3;
		public static final int gEUseType_Arrow = 4;
		public static final int gEUseType_Bow = 5;
		public static final int gEUseType_CrossBow = 6;
		public static final int gEUseType_Bolt = 7;
		public static final int gEUseType_Fist = 8;
		public static final int gEUseType_Shield = 9;
		public static final int gEUseType_Armor = 10;
		public static final int gEUseType_Helmet = 11;
		public static final int gEUseType_Staff = 12;
		public static final int gEUseType_Amulet = 13;
		public static final int gEUseType_Ring = 14;
		public static final int gEUseType_Cast = 15;
		public static final int gEUseType_Potion = 16;
		public static final int gEUseType_Plant = 17;
		public static final int gEUseType_Meat = 18;
		public static final int gEUseType_Fruit = 19;
		public static final int gEUseType_Bread = 20;
		public static final int gEUseType_Bottle = 21;
		public static final int gEUseType_Cup = 22;
		public static final int gEUseType_Bowl = 23;
		public static final int gEUseType_Torch = 24;
		public static final int gEUseType_Alarmhorn = 25;
		public static final int gEUseType_Broom = 26;
		public static final int gEUseType_Brush = 27;
		public static final int gEUseType_Lute = 28;
		public static final int gEUseType_Rake = 29;
		public static final int gEUseType_TrophyTeeth = 30;
		public static final int gEUseType_Valuable = 31;
		public static final int gEUseType_Smoke = 32;
		public static final int gEUseType_OrcPipe = 33;
		public static final int gEUseType_Scoop = 34;
		public static final int gEUseType_Stick = 35;
		public static final int gEUseType_Shovel = 36;
		public static final int gEUseType_Hammer = 37;
		public static final int gEUseType_Fan = 38;
		public static final int gEUseType_Pan = 39;
		public static final int gEUseType_Saw = 40;
		public static final int gEUseType_TrophySkin = 41;
		public static final int gEUseType_Map = 42;
		public static final int gEUseType_Book = 43;
		public static final int gEUseType_Letter = 44;
		public static final int gEUseType_Key = 45;
		public static final int gEUseType_Lockpick = 46;
		public static final int gEUseType_CarryFront = 47;
		public static final int gEUseType_CarryShoulder = 48;
		public static final int gEUseType_Pickaxe = 49;
		public static final int gEUseType_TrophyFur = 50;
		public static final int gEUseType_Halberd = 51;
		public static final int gEUseType_Axe = 52;
		public static final int gEUseType_ITEM_E = 53;
		public static final int gEUseType_Modify = 54;
		public static final int gEUseType_PhysicalFist = 55;
		public static final int gEUseType_ITEM_H = 56;
		public static final int gEUseType_Anvil = 57;
		public static final int gEUseType_Forge = 58;
		public static final int gEUseType_GrindStone = 59;
		public static final int gEUseType_Cauldron = 60;
		public static final int gEUseType_Barbecue = 61;
		public static final int gEUseType_Alchemy = 62;
		public static final int gEUseType_Bookshelf = 63;
		public static final int gEUseType_Bookstand = 64;
		public static final int gEUseType_TakeStone = 65;
		public static final int gEUseType_DropStone = 66;
		public static final int gEUseType_PickOre = 67;
		public static final int gEUseType_PickGround = 68;
		public static final int gEUseType_DigGround = 69;
		public static final int gEUseType_Field = 70;
		public static final int gEUseType_Repair = 71;
		public static final int gEUseType_SawLog = 72;
		public static final int gEUseType_Lumberjack = 73;
		public static final int gEUseType_Bed = 74;
		public static final int gEUseType_SleepGround = 75;
		public static final int gEUseType_CleanFloor = 76;
		public static final int gEUseType_Dance = 77;
		public static final int gEUseType_FanBoss = 78;
		public static final int gEUseType_Boss = 79;
		public static final int gEUseType_Throne = 80;
		public static final int gEUseType_Pace = 81;
		public static final int gEUseType_Bard = 82;
		public static final int gEUseType_Stool = 83;
		public static final int gEUseType_Bench = 84;
		public static final int gEUseType_Waterpipe = 85;
		public static final int gEUseType_WaterBarrel = 86;
		public static final int gEUseType_PirateTreasure = 87;
		public static final int gEUseType_Campfire = 88;
		public static final int gEUseType_SitCampfire = 89;
		public static final int gEUseType_SitGround = 90;
		public static final int gEUseType_Smalltalk = 91;
		public static final int gEUseType_Preach = 92;
		public static final int gEUseType_Spectator = 93;
		public static final int gEUseType_Stand = 94;
		public static final int gEUseType_Guard = 95;
		public static final int gEUseType_Trader = 96;
		public static final int gEUseType_Listener = 97;
		public static final int gEUseType_OrcDance = 98;
		public static final int gEUseType_Stoneplate = 99;
		public static final int gEUseType_OrcDrum = 100;
		public static final int gEUseType_Door = 101;
		public static final int gEUseType_OrcBoulder = 102;
		public static final int gEUseType_EatGround = 103;
		public static final int gEUseType_DrinkWater = 104;
		public static final int gEUseType_Pee = 105;
		public static final int gEUseType_Chest = 106;
		public static final int gEUseType_Shrine = 107;
		public static final int gEUseType_AttackPoint = 108;
		public static final int gEUseType_Roam = 109;
		public static final int gEUseType_BODY_A = 110;
		public static final int gEUseType_Beard = 111;
		public static final int gEUseType_Hair = 112;
		public static final int gEUseType_Head = 113;
		public static final int gEUseType_Body = 114;
		public static final int gEUseType_Flee = 115;
		public static final int gEUseType_Talk = 116;
	}

	// eCParticleSystemBase_PS.VisualOrientation

	public static class gEVisualOrientation extends G3Enum {
		// public static final int gEVisualOrientation_None = ???;
		// public static final int gEVisualOrientation_Velo = ???;
		// public static final int gEVisualOrientation_Velo3D = ???;
	}

	// gCPlayerMemory_PS.LastWeaponConfig

	public static class gEWeaponConfig extends G3Enum {
		public static final int gEWeaponConfig_Melee = 0;
		public static final int gEWeaponConfig_Ranged = 1;
		public static final int gEWeaponConfig_Magic = 2;
	}

	// gCAIZone_PS.ZoneType

	public static class gEZoneType extends G3Enum {
		public static final int gEZoneType_Outdoor = 0;
		public static final int gEZoneType_Indoor = 1;
	}

	public static class gEItemQuality extends G3Enum {
		public static final int gEItemQuality_Diseased = 2;
		public static final int gEItemQuality_Poisoned = 4;
		public static final int gEItemQuality_Burning = 8;
		public static final int gEItemQuality_Frozen = 16;
		public static final int gEItemQuality_Sharp = 32;
		public static final int gEItemQuality_Blessed = 64;
		public static final int gEItemQuality_Forged = 128;
		public static final int gEItemQuality_Worn = 256;
	}

	public static class eEInsertType extends G3Enum {
		public static final int eEInsertType_None = 0;
		public static final int eEInsertType_Floor = 1;
		public static final int eEInsertType_Ground = 2;
		public static final int eEInsertType_Tree = 3;
		public static final int eEInsertType_Wall = 4;
		public static final int eEInsertType_Arrow = 5;
		public static final int eEInsertType_Manual = 6;
		public static final int eEInsertType_Reserved1 = 7;
		public static final int eEInsertType_Reserved2 = 8;
		public static final int eEInsertType_Reserved3 = 9;
		public static final int eEInsertType_Reserved4 = 10;
		public static final int eEInsertType_MAX = 11;
	}

	public static class eEJogIntFlag extends G3Enum {
		// TODO: ???
	}

	public static class eEJogIntProjectionMode extends G3Enum {
		// TODO: ???
	}

	public static class eEVertexTypeStruct extends G3Enum {
		public static final int eEVertexTypeStruct_bCVector2 = 0;
		public static final int eEVertexTypeStruct_bCVector3 = 1;
		public static final int eEVertexTypeStruct_bCVector4 = 2;
		public static final int eEVertexTypeStruct_GEU16 = 3;
		public static final int eEVertexTypeStruct_GEU32 = 4;
		public static final int eEVertexTypeStruct_GEFloat = 5;
	}

	public static class eEVertexStreamArrayType extends G3Enum {
		/**
		 * Faces / Triangles
		 */
		public static final int eEVertexStreamArrayType_Face = 0;
		/**
		 * Vertex format includes the position of an untransformed vertex. (D3DFVF_XYZ) (bCVector3)
		 */
		public static final int eEVertexStreamArrayType_VertexPosition = 1;
		/**
		 * Vertex format includes the position of a transformed vertex. (D3DFVF_XYZRHW) (bCVector4)
		 */
		public static final int eEVertexStreamArrayType_VertexPositionTransformed = 2;
		/**
		 * Vertex format includes a vertex normal vector. (D3DFVF_NORMAL) (bCVector3)
		 */
		public static final int eEVertexStreamArrayType_Normal = 3;
		/**
		 * Vertex format includes a diffuse color component. (D3DFVF_DIFFUSE) (DWORD in ARGB order)
		 * <p>
		 * Bi-Tangent Heading (G3MC Manual) - 00FF0000
		 * <p>
		 * Texture Fading (G3MC)
		 */
		public static final int eEVertexStreamArrayType_Diffuse = 4;
		/**
		 * Vertex format includes a specular color component. (D3DFVF_SPECULAR) (DWORD in ARGB
		 * order)
		 * <p>
		 * ? (G3MC Manual) - FF000000
		 * <p>
		 * "eCTexCoordSrcBumpOffset::GetImplementation - No color source for height set!"
		 * <p>
		 * "eCTexCoordSrcBumpOffset::GetImplementation - Invalid color source component. Color
		 * component forced to alpha!"
		 */
		public static final int eEVertexStreamArrayType_Specular = 5;
		/**
		 * Vertex format specified in point size. This size is expressed in camera space units for
		 * vertices that are not transformed and lit, and in device-space units for transformed and
		 * lit vertices. (D3DFVF_PSIZE) (GEFloat)
		 */
		public static final int eEVertexStreamArrayType_PointSize = 6;
		/**
		 * Vertex format contains position data, and a corresponding number of weighting (beta)
		 * values to use for multimatrix vertex blending operations. (D3DFVF_XYZB1 through
		 * D3DFVF_XYZB5) (GEFloat)
		 */
		public static final int eEVertexStreamArrayType_XYZB1 = 7;
		public static final int eEVertexStreamArrayType_XYZB2 = 8;
		public static final int eEVertexStreamArrayType_XYZB3 = 9;
		public static final int eEVertexStreamArrayType_XYZB4 = 10;
		public static final int eEVertexStreamArrayType_XYZB5 = 11;
		public static final int eEVertexStreamArrayType_TextureCoordinate = 12;
		public static final int eEVertexStreamArrayType_13 = 13;
		public static final int eEVertexStreamArrayType_14 = 14;
		/**
		 * Water Reflections (G3MC)
		 */
		public static final int eEVertexStreamArrayType_15 = 15;
		public static final int eEVertexStreamArrayType_16 = 16;
		public static final int eEVertexStreamArrayType_17 = 17;
		public static final int eEVertexStreamArrayType_18 = 18;
		public static final int eEVertexStreamArrayType_19 = 19;
		public static final int eEVertexStreamArrayType_20 = 20;
		public static final int eEVertexStreamArrayType_21 = 21;
		public static final int eEVertexStreamArrayType_22 = 22;
		public static final int eEVertexStreamArrayType_23 = 23;
		public static final int eEVertexStreamArrayType_24 = 24;
		public static final int eEVertexStreamArrayType_25 = 25;
		public static final int eEVertexStreamArrayType_26 = 26;
		public static final int eEVertexStreamArrayType_27 = 27;
		public static final int eEVertexStreamArrayType_28 = 28;
		public static final int eEVertexStreamArrayType_29 = 29;
		public static final int eEVertexStreamArrayType_30 = 30;
		public static final int eEVertexStreamArrayType_31 = 31;
		public static final int eEVertexStreamArrayType_32 = 32;
		public static final int eEVertexStreamArrayType_33 = 33;
		public static final int eEVertexStreamArrayType_34 = 34;
		public static final int eEVertexStreamArrayType_35 = 35;
		public static final int eEVertexStreamArrayType_36 = 36;
		public static final int eEVertexStreamArrayType_37 = 37;
		public static final int eEVertexStreamArrayType_38 = 38;
		public static final int eEVertexStreamArrayType_39 = 39;
		public static final int eEVertexStreamArrayType_40 = 40;
		public static final int eEVertexStreamArrayType_41 = 41;
		public static final int eEVertexStreamArrayType_42 = 42;
		public static final int eEVertexStreamArrayType_43 = 43;
		public static final int eEVertexStreamArrayType_44 = 44;
		public static final int eEVertexStreamArrayType_45 = 45;
		public static final int eEVertexStreamArrayType_46 = 46;
		public static final int eEVertexStreamArrayType_47 = 47;
		public static final int eEVertexStreamArrayType_48 = 48;
		public static final int eEVertexStreamArrayType_49 = 49;
		public static final int eEVertexStreamArrayType_50 = 50;
		public static final int eEVertexStreamArrayType_51 = 51;
		public static final int eEVertexStreamArrayType_52 = 52;
		public static final int eEVertexStreamArrayType_53 = 53;
		public static final int eEVertexStreamArrayType_54 = 54;
		public static final int eEVertexStreamArrayType_55 = 55;
		public static final int eEVertexStreamArrayType_56 = 56;
		public static final int eEVertexStreamArrayType_57 = 57;
		public static final int eEVertexStreamArrayType_58 = 58;
		public static final int eEVertexStreamArrayType_59 = 59;
		public static final int eEVertexStreamArrayType_60 = 60;
		public static final int eEVertexStreamArrayType_61 = 61;
		public static final int eEVertexStreamArrayType_62 = 62;
		public static final int eEVertexStreamArrayType_63 = 63;
		/**
		 * Tangent Vector (G3MC Manual)
		 */
		public static final int eEVertexStreamArrayType_64 = 64;
		public static final int eEVertexStreamArrayType_65 = 65;
		public static final int eEVertexStreamArrayType_66 = 66;
		public static final int eEVertexStreamArrayType_67 = 67;
		public static final int eEVertexStreamArrayType_68 = 68;
		public static final int eEVertexStreamArrayType_69 = 69;
		public static final int eEVertexStreamArrayType_70 = 70;
		public static final int eEVertexStreamArrayType_71 = 71;
		public static final int eEVertexStreamArrayType_72 = 72;
		public static final int eEVertexStreamArrayType_UVLightmapGroups = 73;
	}

	public static class D3DFVF extends G3Enum {
		public static final int D3DFVF_RESERVED0 = 0x1;
		public static final int D3DFVF_POSITION_MASK = 0x400E;
		public static final int D3DFVF_XYZ = 0x2;
		public static final int D3DFVF_XYZRHW = 0x4;
		public static final int D3DFVF_XYZB1 = 0x6;
		public static final int D3DFVF_XYZB2 = 0x8;
		public static final int D3DFVF_XYZB3 = 0xA;
		public static final int D3DFVF_XYZB4 = 0xC;
		public static final int D3DFVF_XYZB5 = 0xE;
		public static final int D3DFVF_XYZW = 0x4002;
		public static final int D3DFVF_NORMAL = 0x10;
		public static final int D3DFVF_PSIZE = 0x20;
		public static final int D3DFVF_DIFFUSE = 0x40;
		public static final int D3DFVF_SPECULAR = 0x80;
		public static final int D3DFVF_TEXCOUNT_MASK = 0xF00;
		public static final int D3DFVF_TEXCOUNT_SHIFT = 0x8;
		public static final int D3DFVF_TEX0 = 0x0;
		public static final int D3DFVF_TEX1 = 0x100;
		public static final int D3DFVF_TEX2 = 0x200;
		public static final int D3DFVF_TEX3 = 0x300;
		public static final int D3DFVF_TEX4 = 0x400;
		public static final int D3DFVF_TEX5 = 0x500;
		public static final int D3DFVF_TEX6 = 0x600;
		public static final int D3DFVF_TEX7 = 0x700;
		public static final int D3DFVF_TEX8 = 0x800;
		public static final int D3DFVF_LASTBETA_UBYTE4 = 0x1000;
		public static final int D3DFVF_LASTBETA_D3DCOLOR = 0x8000;
		public static final int D3DFVF_RESERVED2 = 0x6000; // 2 reserved bits

		// #define D3DFVF_TEXTUREFORMAT2 0 // Two floating point values
		// #define D3DFVF_TEXTUREFORMAT1 3 // One floating point value
		// #define D3DFVF_TEXTUREFORMAT3 1 // Three floating point values
		// #define D3DFVF_TEXTUREFORMAT4 2 // Four floating point values

		// #define D3DFVF_TEXCOORDSIZE3(CoordIndex) (D3DFVF_TEXTUREFORMAT3 << (CoordIndex*2 + 16))
		// #define D3DFVF_TEXCOORDSIZE2(CoordIndex) (D3DFVF_TEXTUREFORMAT2)
		// #define D3DFVF_TEXCOORDSIZE4(CoordIndex) (D3DFVF_TEXTUREFORMAT4 << (CoordIndex*2 + 16))
		// #define D3DFVF_TEXCOORDSIZE1(CoordIndex) (D3DFVF_TEXTUREFORMAT1 << (CoordIndex*2 + 16))

	}

	public static class gEEffectCommand extends G3Enum {
		public static final int gEEffectCommand_SpawnEntity = 0;
		public static final int gEEffectCommand_ModifyEntity = 1;
		public static final int gEEffectCommand_TriggerEntity = 2;
		public static final int gEEffectCommand_KillEntity = 3;
		public static final int gEEffectCommand_PlaySound = 4;
		public static final int gEEffectCommand_PlayVoice = 5;
		public static final int gEEffectCommand_CreateDecal = 6;
		public static final int gEEffectCommand_Earthquake = 7;
		public static final int gEEffectCommand_MusicTrigger = 8;
	}

	public static class gETurnDirection extends G3Enum {
		public static final int gETurnDirection_Left = 0;
		public static final int gETurnDirection_Right = 1;
	}
}

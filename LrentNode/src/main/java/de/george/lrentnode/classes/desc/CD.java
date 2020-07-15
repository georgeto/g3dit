package de.george.lrentnode.classes.desc;

import java.util.Optional;

import de.george.g3utils.structure.bCBox;
import de.george.g3utils.structure.bCEulerAngles;
import de.george.g3utils.structure.bCMatrix;
import de.george.g3utils.structure.bCRect;
import de.george.g3utils.structure.bCVector;
import de.george.g3utils.structure.bCVector2;
import de.george.lrentnode.enums.G3Enums.*;
import de.george.lrentnode.properties.Unknown;
import de.george.lrentnode.properties.bCPropertyID;
import de.george.lrentnode.properties.bCRange1;
import de.george.lrentnode.properties.bCRange3;
import de.george.lrentnode.properties.bCString;
import de.george.lrentnode.properties.bTObjArray_bCString;
import de.george.lrentnode.properties.bTObjArray_bTAutoPOSmartPtr_gCQuest_PS;
import de.george.lrentnode.properties.bTObjArray_bTPropertyContainer;
import de.george.lrentnode.properties.bTObjArray_eCEntityProxy;
import de.george.lrentnode.properties.bTPropertyContainer;
import de.george.lrentnode.properties.bTPropertyObject;
import de.george.lrentnode.properties.bTValArray_bCMotion;
import de.george.lrentnode.properties.bTValArray_bCPropertyID;
import de.george.lrentnode.properties.bTValArray_bCVector;
import de.george.lrentnode.properties.bTValArray_bool;
import de.george.lrentnode.properties.bTValArray_float;
import de.george.lrentnode.properties.bTValArray_gEDirection;
import de.george.lrentnode.properties.bTValArray_long;
import de.george.lrentnode.properties.bTValArray_unsigned_short;
import de.george.lrentnode.properties.eCEntityProxy;
import de.george.lrentnode.properties.gBool;
import de.george.lrentnode.properties.gChar;
import de.george.lrentnode.properties.gFloat;
import de.george.lrentnode.properties.gInt;
import de.george.lrentnode.properties.gLong;
import de.george.lrentnode.properties.gShort;
import de.george.lrentnode.properties.gUnsignedShort;
import de.george.lrentnode.structures.bCFloatAlphaColor;
import de.george.lrentnode.structures.bCFloatColor;
import de.george.lrentnode.structures.bCGuid;
import one.util.streamex.StreamEx;

public interface CD {
	@SuppressWarnings({"unchecked", "rawtypes"})
	public static StreamEx<Class<ClassDescriptor>> getClassDescriptors() {
		return (StreamEx) StreamEx.of(CD.class.getDeclaredClasses());
	}

	public static Optional<Class<ClassDescriptor>> getClassDescriptor(String className) {
		return CD.getClassDescriptors().filter(c -> ClassDescriptor.getName(c).equals(className)).findFirst();
	}

	//@foff
	public static interface eCEntityPropertySet extends ClassDescriptor {
	}

	public static interface eCAudioEmitter_PS extends eCEntityPropertySet {
		public static final PropertyDescriptor<gBool> AutoKill = new PropertyDescriptor<>("AutoKill", gBool.class, "bool", "General", eCAudioEmitter_PS.class);
		public static final PropertyDescriptor<gBool> AutoReset = new PropertyDescriptor<>("AutoReset", gBool.class, "bool", "General", eCAudioEmitter_PS.class);
		public static final PropertyDescriptor<gBool> Enabled = new PropertyDescriptor<>("Enabled", gBool.class, "bool", "General", eCAudioEmitter_PS.class);
		public static final PropertyDescriptor<gFloat> MaxDistance = new PropertyDescriptor<>("MaxDistance", gFloat.class, "float", "Audibility", eCAudioEmitter_PS.class);
		public static final PropertyDescriptor<bCRange1> MaxNumRepeats = new PropertyDescriptor<>("MaxNumRepeats", bCRange1.class, "bCRange1", "Spawning", eCAudioEmitter_PS.class);
		public static final PropertyDescriptor<gFloat> MinDistance = new PropertyDescriptor<>("MinDistance", gFloat.class, "float", "Audibility", eCAudioEmitter_PS.class);
		public static final PropertyDescriptor<bCRange1> RepeatProbability = new PropertyDescriptor<>("RepeatProbability", bCRange1.class, "bCRange1", "Spawning", eCAudioEmitter_PS.class);
		public static final PropertyDescriptor<bCRange1> SecondsBetweenRepeats = new PropertyDescriptor<>("SecondsBetweenRepeats", bCRange1.class, "bCRange1", "Spawning", eCAudioEmitter_PS.class);
		public static final PropertyDescriptor<bCString> Sound = new PropertyDescriptor<>("Sound", bCString.class, "bCString", "General", eCAudioEmitter_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<eEAudioEmitterMode>> SpawningMode = new PropertyDescriptor<>("SpawningMode", bTPropertyContainer.class, "bTPropertyContainer<enum eEAudioEmitterMode>", "Spawning", eCAudioEmitter_PS.class);
		public static final PropertyDescriptor<gBool> UseMaxRepeats = new PropertyDescriptor<>("UseMaxRepeats", gBool.class, "bool", "Spawning", eCAudioEmitter_PS.class);
		public static final PropertyDescriptor<gBool> UseStream = new PropertyDescriptor<>("UseStream", gBool.class, "bool", "General", eCAudioEmitter_PS.class);
		public static final PropertyDescriptor<gFloat> Volume = new PropertyDescriptor<>("Volume", gFloat.class, "float", "Audibility", eCAudioEmitter_PS.class);
	}

	public static interface eCCameraBase extends ClassDescriptor {
		public static final PropertyDescriptor<gFloat> AspectRatio = new PropertyDescriptor<>("AspectRatio", gFloat.class, "float", "", eCCameraBase.class);
		public static final PropertyDescriptor<bCVector2> ClipDepth = new PropertyDescriptor<>("ClipDepth", bCVector2.class, "bCVector2", "", eCCameraBase.class);
		public static final PropertyDescriptor<bCVector2> ClipProjection = new PropertyDescriptor<>("ClipProjection", bCVector2.class, "bCVector2", "", eCCameraBase.class);
		public static final PropertyDescriptor<gFloat> DepthBias = new PropertyDescriptor<>("DepthBias", gFloat.class, "float", "", eCCameraBase.class);
		public static final PropertyDescriptor<gFloat> FieldOfView = new PropertyDescriptor<>("FieldOfView", gFloat.class, "float", "", eCCameraBase.class);
		public static final PropertyDescriptor<gBool> Perspective = new PropertyDescriptor<>("Perspective", gBool.class, "bool", "", eCCameraBase.class);
		public static final PropertyDescriptor<bCVector> Position = new PropertyDescriptor<>("Position", bCVector.class, "bCVector", "", eCCameraBase.class);
		public static final PropertyDescriptor<bCMatrix> ProjectionMatrix = new PropertyDescriptor<>("ProjectionMatrix", bCMatrix.class, "bCMatrix", "", eCCameraBase.class);
		public static final PropertyDescriptor<bCVector> Rotation = new PropertyDescriptor<>("Rotation", bCVector.class, "bCVector", "", eCCameraBase.class);
		public static final PropertyDescriptor<bCVector> Scale = new PropertyDescriptor<>("Scale", bCVector.class, "bCVector", "", eCCameraBase.class);
		public static final PropertyDescriptor<bCRect> Screen = new PropertyDescriptor<>("Screen", bCRect.class, "bCRect", "", eCCameraBase.class);
		public static final PropertyDescriptor<bCMatrix> ViewMatrix = new PropertyDescriptor<>("ViewMatrix", bCMatrix.class, "bCMatrix", "", eCCameraBase.class);
	}

	public static interface eCCollisionShape extends ClassDescriptor {
		public static final PropertyDescriptor<gBool> DisableCollision = new PropertyDescriptor<>("DisableCollision", gBool.class, "bool", "", eCCollisionShape.class);
		public static final PropertyDescriptor<gBool> DisableResponse = new PropertyDescriptor<>("DisableResponse", gBool.class, "bool", "", eCCollisionShape.class);
		public static final PropertyDescriptor<gBool> EnableCCD = new PropertyDescriptor<>("EnableCCD", gBool.class, "bool", "", eCCollisionShape.class);
		// public static final PropertyDescriptor<gUnsignedShort> FileVersion = new PropertyDescriptor<>("FileVersion", gUnsignedShort.class, "unsigned short", eCCollisionShape.class);
		public static final PropertyDescriptor<gShort> FileVersion = new PropertyDescriptor<>("FileVersion", gShort.class, "short", "Readonly", eCCollisionShape.class);
		public static final PropertyDescriptor<bTPropertyContainer<eEShapeGroup>> Group = new PropertyDescriptor<>("Group", bTPropertyContainer.class, "bTPropertyContainer<enum eEShapeGroup>", "", eCCollisionShape.class);
		public static final PropertyDescriptor<gBool> IgnoredByTraceRay = new PropertyDescriptor<>("IgnoredByTraceRay", gBool.class, "bool", "", eCCollisionShape.class);
		public static final PropertyDescriptor<gBool> IsLazyGenerated = new PropertyDescriptor<>("IsLazyGenerated", gBool.class, "bool", "Readonly", eCCollisionShape.class);
		public static final PropertyDescriptor<bTPropertyContainer<eEShapeMaterial>> Material = new PropertyDescriptor<>("Material", bTPropertyContainer.class, "bTPropertyContainer<enum eEShapeMaterial>", "", eCCollisionShape.class);
		public static final PropertyDescriptor<gBool> OverrideEntityAABB = new PropertyDescriptor<>("OverrideEntityAABB", gBool.class, "bool", "", eCCollisionShape.class);
		public static final PropertyDescriptor<bTPropertyContainer<eEShapeAABBAdapt>> ShapeAABBAdaptMode = new PropertyDescriptor<>("ShapeAABBAdaptMode", bTPropertyContainer.class, "bTPropertyContainer<enum eEShapeAABBAdapt>", "", eCCollisionShape.class);
		public static final PropertyDescriptor<gFloat> SkinWidth = new PropertyDescriptor<>("SkinWidth", gFloat.class, "float", "", eCCollisionShape.class);
		public static final PropertyDescriptor<gBool> TriggersOnIntersect = new PropertyDescriptor<>("TriggersOnIntersect", gBool.class, "bool", "", eCCollisionShape.class);
		public static final PropertyDescriptor<gBool> TriggersOnTouch = new PropertyDescriptor<>("TriggersOnTouch", gBool.class, "bool", "", eCCollisionShape.class);
		public static final PropertyDescriptor<gBool> TriggersOnUntouch = new PropertyDescriptor<>("TriggersOnUntouch", gBool.class, "bool", "", eCCollisionShape.class);
		public static final PropertyDescriptor<bTPropertyContainer<eECollisionShapeType>> ShapeType = new PropertyDescriptor<>("ShapeType", bTPropertyContainer.class, "bTPropertyContainer<enum eECollisionShapeType>", "Readonly", eCCollisionShape.class);
	}

	public static interface eCCollisionShapeBase_PS extends eCEntityPropertySet {
	}

	public static interface eCCollisionShape_PS extends eCCollisionShapeBase_PS {
		public static final PropertyDescriptor<gBool> DisableCollision = new PropertyDescriptor<>("DisableCollision", gBool.class, "bool", "", eCCollisionShape_PS.class);
		public static final PropertyDescriptor<gBool> DisableResponse = new PropertyDescriptor<>("DisableResponse", gBool.class, "bool", "", eCCollisionShape_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<eECollisionGroup>> Group = new PropertyDescriptor<>("Group", bTPropertyContainer.class, "bTPropertyContainer<enum eECollisionGroup>", "", eCCollisionShape_PS.class);
		public static final PropertyDescriptor<gBool> IgnoredByTraceRay = new PropertyDescriptor<>("IgnoredByTraceRay", gBool.class, "bool", "", eCCollisionShape_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<eEPhysicRangeType>> Range = new PropertyDescriptor<>("Range", bTPropertyContainer.class, "bTPropertyContainer<enum eEPhysicRangeType>", "", eCCollisionShape_PS.class);
	}

	public static interface eCShaderEllementBase extends ClassDescriptor {
	}

	public static interface eCColorSrcBase extends eCShaderEllementBase {
	}

	public static interface eCColorSrcCombiner extends eCColorSrcBase {
		public static final PropertyDescriptor<bTPropertyContainer<eEColorSrcCombinerType>> CombinerType = new PropertyDescriptor<>("CombinerType", bTPropertyContainer.class, "bTPropertyContainer<enum eEColorSrcCombinerType>", "", eCColorSrcCombiner.class);
	}

	public static interface eCColorSrcConstant extends eCColorSrcBase {
		public static final PropertyDescriptor<gFloat> Alpha = new PropertyDescriptor<>("Alpha", gFloat.class, "float", "", eCColorSrcConstant.class);
		public static final PropertyDescriptor<bCFloatColor> Color = new PropertyDescriptor<>("Color", bCFloatColor.class, "bCFloatColor", "", eCColorSrcConstant.class);
	}

	public static interface eCColorSrcCubeSampler extends eCColorSrcBase {
		public static final PropertyDescriptor<bCString> ImageFilePath = new PropertyDescriptor<>("ImageFilePath", bCString.class, "bCImageResourceString", "", eCColorSrcCubeSampler.class);
	}

	public static interface eCColorSrcMask extends eCColorSrcBase {
		public static final PropertyDescriptor<gFloat> MaskThreshold = new PropertyDescriptor<>("MaskThreshold", gFloat.class, "float", "", eCColorSrcMask.class);
	}

	public static interface eCColorSrcSampler extends eCColorSrcBase {
		public static final PropertyDescriptor<gFloat> AnimationSpeed = new PropertyDescriptor<>("AnimationSpeed", gFloat.class, "float", "", eCColorSrcSampler.class);
		public static final PropertyDescriptor<bCString> ImageFilePath = new PropertyDescriptor<>("ImageFilePath", bCString.class, "bCImageResourceString", "", eCColorSrcSampler.class);
		public static final PropertyDescriptor<bTPropertyContainer<eEColorSrcSwitchRepeat>> SwitchRepeat = new PropertyDescriptor<>("SwitchRepeat", bTPropertyContainer.class, "bTPropertyContainer<enum eEColorSrcSwitchRepeat>", "", eCColorSrcSampler.class);
		public static final PropertyDescriptor<bTPropertyContainer<eEColorSrcSampleTexRepeat>> TexRepeatU = new PropertyDescriptor<>("TexRepeatU", bTPropertyContainer.class, "bTPropertyContainer<enum eEColorSrcSampleTexRepeat>", "", eCColorSrcSampler.class);
		public static final PropertyDescriptor<bTPropertyContainer<eEColorSrcSampleTexRepeat>> TexRepeatV = new PropertyDescriptor<>("TexRepeatV", bTPropertyContainer.class, "bTPropertyContainer<enum eEColorSrcSampleTexRepeat>", "", eCColorSrcSampler.class);
	}

	public static interface eCColorSrcSkydomeSampler extends eCColorSrcBase {
		public static final PropertyDescriptor<gInt> MinSize = new PropertyDescriptor<>("MinSize", gInt.class, "int", "", eCColorSrcSkydomeSampler.class);
		public static final PropertyDescriptor<gFloat> UpdateRateInSec = new PropertyDescriptor<>("UpdateRateInSec", gFloat.class, "float", "", eCColorSrcSkydomeSampler.class);
	}

	public static interface eCContextBase extends ClassDescriptor {
		public static final PropertyDescriptor<bCBox> ContextBox = new PropertyDescriptor<>("ContextBox", bCBox.class, "bCBox", "", eCContextBase.class);
		public static final PropertyDescriptor<bCGuid> ID = new PropertyDescriptor<>("ID", bCGuid.class, "bCGuid", "", eCContextBase.class);
	}

	public static interface eCDecal_PS extends eCEntityPropertySet {
		public static final PropertyDescriptor<gBool> AutoKill = new PropertyDescriptor<>("AutoKill", gBool.class, "bool", "Decal", eCDecal_PS.class);
		public static final PropertyDescriptor<gBool> CreateOnDynamicEntities = new PropertyDescriptor<>("CreateOnDynamicEntities", gBool.class, "bool", "Decal", eCDecal_PS.class);
		public static final PropertyDescriptor<bCEulerAngles> DirectionOffset = new PropertyDescriptor<>("DirectionOffset", bCEulerAngles.class, "bCEulerAngles", "Direction", eCDecal_PS.class);
		public static final PropertyDescriptor<gFloat> FadeInTime = new PropertyDescriptor<>("FadeInTime", gFloat.class, "float", "Fading", eCDecal_PS.class);
		public static final PropertyDescriptor<gFloat> FadeOutTime = new PropertyDescriptor<>("FadeOutTime", gFloat.class, "float", "Fading", eCDecal_PS.class);
		public static final PropertyDescriptor<bCString> ImageOrMaterial = new PropertyDescriptor<>("ImageOrMaterial", bCString.class, "bCImageOrMaterialResourceString", "Decal", eCDecal_PS.class);
		public static final PropertyDescriptor<gFloat> LifeTime = new PropertyDescriptor<>("LifeTime", gFloat.class, "float", "Decal", eCDecal_PS.class);
		public static final PropertyDescriptor<bCVector> Offset = new PropertyDescriptor<>("Offset", bCVector.class, "bCVector", "Position", eCDecal_PS.class);
		public static final PropertyDescriptor<bCVector> Size = new PropertyDescriptor<>("Size", bCVector.class, "bCVector", "Extent", eCDecal_PS.class);
		public static final PropertyDescriptor<gBool> UseEntityDirection = new PropertyDescriptor<>("UseEntityDirection", gBool.class, "bool", "Direction", eCDecal_PS.class);
	}

	public static interface eCDirectionalLight_PS extends eCDynamicLight_PS {
		public static final PropertyDescriptor<gBool> CastShadows = new PropertyDescriptor<>("CastShadows", gBool.class, "bool", "", eCDirectionalLight_PS.class);
		public static final PropertyDescriptor<bCFloatColor> Color = new PropertyDescriptor<>("Color", bCFloatColor.class, "bCFloatColor", "", eCDirectionalLight_PS.class);
		public static final PropertyDescriptor<bCEulerAngles> DirectionOffset = new PropertyDescriptor<>("DirectionOffset", bCEulerAngles.class, "bCEulerAngles", "", eCDirectionalLight_PS.class);
		public static final PropertyDescriptor<gFloat> Intensity = new PropertyDescriptor<>("Intensity", gFloat.class, "float", "", eCDirectionalLight_PS.class);
		public static final PropertyDescriptor<gFloat> ShadowDensity = new PropertyDescriptor<>("ShadowDensity", gFloat.class, "float", "", eCDirectionalLight_PS.class);
		public static final PropertyDescriptor<bCFloatColor> SpecularColor = new PropertyDescriptor<>("SpecularColor", bCFloatColor.class, "bCFloatColor", "", eCDirectionalLight_PS.class);
	}

	public static interface eCDynamicLight_PS extends eCEntityPropertySet {
		public static final PropertyDescriptor<gBool> Enabled = new PropertyDescriptor<>("Enabled", gBool.class, "bool", "", eCDynamicLight_PS.class);
	}

	public static interface eCEditorEntityPropertySet extends eCEntityPropertySet {
		public static final PropertyDescriptor<bCString> MeshFileName = new PropertyDescriptor<>("MeshFileName", bCString.class, "bCString", "", eCEditorEntityPropertySet.class);
	}

	public static interface eCEffector extends ClassDescriptor {
		public static final PropertyDescriptor<eCEntityProxy> Entity1 = new PropertyDescriptor<>("Entity1", eCEntityProxy.class, "eCEntityProxy", "", eCEffector.class);
		public static final PropertyDescriptor<eCEntityProxy> Entity2 = new PropertyDescriptor<>("Entity2", eCEntityProxy.class, "eCEntityProxy", "", eCEffector.class);
		public static final PropertyDescriptor<bCVector> Pos1 = new PropertyDescriptor<>("Pos1", bCVector.class, "bCVector", "", eCEffector.class);
		public static final PropertyDescriptor<bCVector> Pos2 = new PropertyDescriptor<>("Pos2", bCVector.class, "bCVector", "", eCEffector.class);
	}

	public static interface eCEngineCaps_PS extends eCEntityPropertySet {
		public static final PropertyDescriptor<gBool> AlphaPass = new PropertyDescriptor<>("AlphaPass", gBool.class, "bool", "", eCEngineCaps_PS.class);
		public static final PropertyDescriptor<gBool> ChildDependenciesRendering = new PropertyDescriptor<>("ChildDependenciesRendering", gBool.class, "bool", "", eCEngineCaps_PS.class);
		public static final PropertyDescriptor<gBool> DrawBoundingVolumes = new PropertyDescriptor<>("DrawBoundingVolumes", gBool.class, "bool", "", eCEngineCaps_PS.class);
		public static final PropertyDescriptor<gBool> DrawEntityAxes = new PropertyDescriptor<>("DrawEntityAxes", gBool.class, "bool", "", eCEngineCaps_PS.class);
		public static final PropertyDescriptor<gBool> DrawEntityNames = new PropertyDescriptor<>("DrawEntityNames", gBool.class, "bool", "", eCEngineCaps_PS.class);
		public static final PropertyDescriptor<gBool> DynamicVisualsRendering = new PropertyDescriptor<>("DynamicVisualsRendering", gBool.class, "bool", "", eCEngineCaps_PS.class);
		public static final PropertyDescriptor<gBool> MaterialPass = new PropertyDescriptor<>("MaterialPass", gBool.class, "bool", "", eCEngineCaps_PS.class);
		public static final PropertyDescriptor<gBool> EnableObjectVPTHCCulling = new PropertyDescriptor<>("EnableObjectVPTHCCulling", gBool.class, "bool", "", eCEngineCaps_PS.class);
		public static final PropertyDescriptor<gBool> PVSPrefetcherWorkerThread = new PropertyDescriptor<>("PVSPrefetcherWorkerThread", gBool.class, "bool", "", eCEngineCaps_PS.class);
		public static final PropertyDescriptor<gBool> StaticVisualsRendering = new PropertyDescriptor<>("StaticVisualsRendering", gBool.class, "bool", "", eCEngineCaps_PS.class);
		public static final PropertyDescriptor<gBool> ZPass = new PropertyDescriptor<>("ZPass", gBool.class, "bool", "", eCEngineCaps_PS.class);
		public static final PropertyDescriptor<gFloat> FarClippingPlane = new PropertyDescriptor<>("FarClippingPlane", gFloat.class, "float", "", eCEngineCaps_PS.class);
		public static final PropertyDescriptor<gFloat> GlobalVisualLoDFactor = new PropertyDescriptor<>("GlobalVisualLoDFactor", gFloat.class, "float", "", eCEngineCaps_PS.class);
		public static final PropertyDescriptor<gFloat> ScreenObjectDistanceCulling = new PropertyDescriptor<>("ScreenObjectDistanceCulling", gFloat.class, "float", "", eCEngineCaps_PS.class);
	}

	public static interface eCEventDebugger_PS extends eCTriggerBase_PS {
		public static final PropertyDescriptor<gBool> WatchDamageEvents = new PropertyDescriptor<>("WatchDamageEvents", gBool.class, "bool", "", eCEventDebugger_PS.class);
		public static final PropertyDescriptor<gBool> WatchIntersectEvents = new PropertyDescriptor<>("WatchIntersectEvents", gBool.class, "bool", "", eCEventDebugger_PS.class);
		public static final PropertyDescriptor<gBool> WatchTouchEvents = new PropertyDescriptor<>("WatchTouchEvents", gBool.class, "bool", "", eCEventDebugger_PS.class);
		public static final PropertyDescriptor<gBool> WatchTriggerEvents = new PropertyDescriptor<>("WatchTriggerEvents", gBool.class, "bool", "", eCEventDebugger_PS.class);
		public static final PropertyDescriptor<gBool> WatchUntouchEvents = new PropertyDescriptor<>("WatchUntouchEvents", gBool.class, "bool", "", eCEventDebugger_PS.class);
		public static final PropertyDescriptor<gBool> WatchUntriggerEvents = new PropertyDescriptor<>("WatchUntriggerEvents", gBool.class, "bool", "", eCEventDebugger_PS.class);
	}

	public static interface eCEventFilter_PS extends eCTriggerBase_PS {
		public static final PropertyDescriptor<gUnsignedShort> DefaultDamage = new PropertyDescriptor<>("DefaultDamage", gUnsignedShort.class, "unsigned short", "", eCEventFilter_PS.class);
		public static final PropertyDescriptor<gUnsignedShort> DefaultDamageType = new PropertyDescriptor<>("DefaultDamageType", gUnsignedShort.class, "unsigned short", "", eCEventFilter_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<eEEventType>> FireEventWhenDamaged = new PropertyDescriptor<>("FireEventWhenDamaged", bTPropertyContainer.class, "bTPropertyContainer<enum eEEventType>", "", eCEventFilter_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<eEEventType>> FireEventWhenIntersects = new PropertyDescriptor<>("FireEventWhenIntersects", bTPropertyContainer.class, "bTPropertyContainer<enum eEEventType>", "", eCEventFilter_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<eEEventType>> FireEventWhenTouched = new PropertyDescriptor<>("FireEventWhenTouched", bTPropertyContainer.class, "bTPropertyContainer<enum eEEventType>", "", eCEventFilter_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<eEEventType>> FireEventWhenTriggered = new PropertyDescriptor<>("FireEventWhenTriggered", bTPropertyContainer.class, "bTPropertyContainer<enum eEEventType>", "", eCEventFilter_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<eEEventType>> FireEventWhenUntouched = new PropertyDescriptor<>("FireEventWhenUntouched", bTPropertyContainer.class, "bTPropertyContainer<enum eEEventType>", "", eCEventFilter_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<eEEventType>> FireEventWhenUntriggered = new PropertyDescriptor<>("FireEventWhenUntriggered", bTPropertyContainer.class, "bTPropertyContainer<enum eEEventType>", "", eCEventFilter_PS.class);
	}

	public static interface eCEventLogic_PS extends eCTrigger_PS {
		public static final PropertyDescriptor<Unknown> PropertyLogic = new PropertyDescriptor<>("PropertyLogic", Unknown.class, "bTObjArray<class bTAutoPOSmartPtr<class eCPropertyLogic> >", "", eCEventLogic_PS.class);
		public static final PropertyDescriptor<gBool> ReverseLogicOnUntrigger = new PropertyDescriptor<>("ReverseLogicOnUntrigger", gBool.class, "bool", "", eCEventLogic_PS.class);
	}

	public static interface eCFixedJoint extends eCJoint {
		public static final PropertyDescriptor<bTPropertyObject> FixedJointDesc = new PropertyDescriptor<>("FixedJointDesc", bTPropertyObject.class, "bTPropertyObject<class eCFixedJointDesc,class eCJointDesc>", "", eCFixedJoint.class);
	}

	public static interface eCForceField_PS extends eCTrigger_PS {
		public static final PropertyDescriptor<gBool> ApplyToNPCs = new PropertyDescriptor<>("ApplyToNPCs", gBool.class, "bool", "", eCForceField_PS.class);
		public static final PropertyDescriptor<gBool> ApplyToObjects = new PropertyDescriptor<>("ApplyToObjects", gBool.class, "bool", "", eCForceField_PS.class);
		public static final PropertyDescriptor<gBool> ApplyToPlayers = new PropertyDescriptor<>("ApplyToPlayers", gBool.class, "bool", "", eCForceField_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<eEForceFieldDirMode>> DirMode = new PropertyDescriptor<>("DirMode", bTPropertyContainer.class, "bTPropertyContainer<enum eEForceFieldDirMode>", "", eCForceField_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<eEForceFieldDistanceScale>> DistanceScaleType = new PropertyDescriptor<>("DistanceScaleType", bTPropertyContainer.class, "bTPropertyContainer<enum eEForceFieldDistanceScale>", "", eCForceField_PS.class);
		public static final PropertyDescriptor<gFloat> Force = new PropertyDescriptor<>("Force", gFloat.class, "float", "", eCForceField_PS.class);
		public static final PropertyDescriptor<gFloat> InnerRadius = new PropertyDescriptor<>("InnerRadius", gFloat.class, "float", "", eCForceField_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<eEForceFieldType>> Type = new PropertyDescriptor<>("Type", bTPropertyContainer.class, "bTPropertyContainer<enum eEForceFieldType>", "", eCForceField_PS.class);
	}

	public static interface eCHemisphere_PS extends eCDynamicLight_PS {
		public static final PropertyDescriptor<bCFloatColor> BackLight = new PropertyDescriptor<>("BackLight", bCFloatColor.class, "bCFloatColor", "", eCHemisphere_PS.class);
		public static final PropertyDescriptor<bCEulerAngles> BackLightAxisDirectionOffset = new PropertyDescriptor<>("BackLightAxisDirectionOffset", bCEulerAngles.class, "bCEulerAngles", "", eCHemisphere_PS.class);
		public static final PropertyDescriptor<bCFloatColor> GeneralAmbient = new PropertyDescriptor<>("GeneralAmbient", bCFloatColor.class, "bCFloatColor", "", eCHemisphere_PS.class);
		public static final PropertyDescriptor<bCFloatColor> GroundLight = new PropertyDescriptor<>("GroundLight", bCFloatColor.class, "bCFloatColor", "", eCHemisphere_PS.class);
		public static final PropertyDescriptor<gFloat> Intensity = new PropertyDescriptor<>("Intensity", gFloat.class, "float", "", eCHemisphere_PS.class);
		public static final PropertyDescriptor<bCFloatColor> SunLight = new PropertyDescriptor<>("SunLight", bCFloatColor.class, "bCFloatColor", "", eCHemisphere_PS.class);
		public static final PropertyDescriptor<bCEulerAngles> SunLightAxisDirectionOffset = new PropertyDescriptor<>("SunLightAxisDirectionOffset", bCEulerAngles.class, "bCEulerAngles", "", eCHemisphere_PS.class);
	}

	public static interface eCIlluminated_PS extends eCEntityPropertySet {
		public static final PropertyDescriptor<gBool> CastShadows = new PropertyDescriptor<>("CastShadows", gBool.class, "bool", "", eCIlluminated_PS.class);
		public static final PropertyDescriptor<gBool> CastStaticShadows = new PropertyDescriptor<>("CastStaticShadows", gBool.class, "bool", "", eCIlluminated_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<eEDirectionalShadowType>> DirectionalShadowType = new PropertyDescriptor<>("DirectionalShadowType", bTPropertyContainer.class, "bTPropertyContainer<enum eEDirectionalShadowType>", "", eCIlluminated_PS.class);
		public static final PropertyDescriptor<gBool> EnableAmbient = new PropertyDescriptor<>("EnableAmbient", gBool.class, "bool", "", eCIlluminated_PS.class);
		public static final PropertyDescriptor<gBool> EnableDynamicLighting = new PropertyDescriptor<>("EnableDynamicLighting", gBool.class, "bool", "", eCIlluminated_PS.class);
		public static final PropertyDescriptor<gBool> ReciveShadows = new PropertyDescriptor<>("ReciveShadows", gBool.class, "bool", "", eCIlluminated_PS.class);
		public static final PropertyDescriptor<gBool> ReciveStaticShadows = new PropertyDescriptor<>("ReciveStaticShadows", gBool.class, "bool", "", eCIlluminated_PS.class);
		public static final PropertyDescriptor<gBool> ReciveTreeShadows = new PropertyDescriptor<>("ReciveTreeShadows", gBool.class, "bool", "", eCIlluminated_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<eEStaticIlluminated>> StaticIlluminated = new PropertyDescriptor<>("StaticIlluminated", bTPropertyContainer.class, "bTPropertyContainer<enum eEStaticIlluminated>", "", eCIlluminated_PS.class);
	}

	public static interface eCJoint extends ClassDescriptor {
		public static final PropertyDescriptor<eCEntityProxy> Entity1 = new PropertyDescriptor<>("Entity1", eCEntityProxy.class, "eCEntityProxy", "", eCJoint.class);
		public static final PropertyDescriptor<eCEntityProxy> Entity2 = new PropertyDescriptor<>("Entity2", eCEntityProxy.class, "eCEntityProxy", "", eCJoint.class);
	}

	public static interface eCJointDesc extends ClassDescriptor {
		public static final PropertyDescriptor<bTPropertyContainer<eEJogIntFlag>> Flag = new PropertyDescriptor<>("Flag", bTPropertyContainer.class, "bTPropertyContainer<enum eEJointFlag>", "", eCJointDesc.class);
		public static final PropertyDescriptor<gFloat> MaxForce = new PropertyDescriptor<>("MaxForce", gFloat.class, "float", "", eCJointDesc.class);
		public static final PropertyDescriptor<gFloat> MaxTorque = new PropertyDescriptor<>("MaxTorque", gFloat.class, "float", "", eCJointDesc.class);
	}

	public static interface eCJointLimitDesc extends ClassDescriptor {
		public static final PropertyDescriptor<gFloat> Hardness = new PropertyDescriptor<>("Hardness", gFloat.class, "float", "", eCJointLimitDesc.class);
		public static final PropertyDescriptor<gFloat> Restitution = new PropertyDescriptor<>("Restitution", gFloat.class, "float", "", eCJointLimitDesc.class);
		public static final PropertyDescriptor<gFloat> Value = new PropertyDescriptor<>("Value", gFloat.class, "float", "", eCJointLimitDesc.class);
	}

	public static interface eCJointLimitPairDesc extends ClassDescriptor {
		public static final PropertyDescriptor<gFloat> High = new PropertyDescriptor<>("High", gFloat.class, "float", "", eCJointLimitPairDesc.class);
		public static final PropertyDescriptor<gFloat> Low = new PropertyDescriptor<>("Low", gFloat.class, "float", "", eCJointLimitPairDesc.class);
	}

	public static interface eCMainCache extends ClassDescriptor {
		public static final PropertyDescriptor<gBool> DisableThreading = new PropertyDescriptor<>("DisableThreading", gBool.class, "bool", "", eCMainCache.class);
	}

	public static interface eCParticle_PS extends eCEntityPropertySet {
		public static final PropertyDescriptor<bCVector> Acceleration = new PropertyDescriptor<>("Acceleration", bCVector.class, "bCVector", "Movement", eCParticle_PS.class);
		public static final PropertyDescriptor<gBool> AddLocationFromTarget = new PropertyDescriptor<>("AddLocationFromTarget", gBool.class, "bool", "Movement", eCParticle_PS.class);
		public static final PropertyDescriptor<gChar> AlphaReference = new PropertyDescriptor<>("AlphaReference", gChar.class, "unsigned char", "Render", eCParticle_PS.class);
		public static final PropertyDescriptor<gBool> AlphaTest = new PropertyDescriptor<>("AlphaTest", gBool.class, "bool", "Render", eCParticle_PS.class);
		public static final PropertyDescriptor<gBool> AutoKill = new PropertyDescriptor<>("AutoKill", gBool.class, "bool", "General", eCParticle_PS.class);
		public static final PropertyDescriptor<gBool> AutoReset = new PropertyDescriptor<>("AutoReset", gBool.class, "bool", "General", eCParticle_PS.class);
		public static final PropertyDescriptor<gBool> AutomaticSpawning = new PropertyDescriptor<>("AutomaticSpawning", gBool.class, "bool", "General", eCParticle_PS.class);
		public static final PropertyDescriptor<gBool> BlendBetweenSubdivisions = new PropertyDescriptor<>("BlendBetweenSubdivisions", gBool.class, "bool", "Texture", eCParticle_PS.class);
		public static final PropertyDescriptor<bCRange3> StartBoxLocation = new PropertyDescriptor<>("StartBoxLocation", bCRange3.class, "bCRange3", "Location", eCParticle_PS.class);
		public static final PropertyDescriptor<gBool> CollideWithCharacters = new PropertyDescriptor<>("CollideWithCharacters", gBool.class, "bool", "Collision", eCParticle_PS.class);
		public static final PropertyDescriptor<gBool> CollideWithDynamic = new PropertyDescriptor<>("CollideWithDynamic", gBool.class, "bool", "Collision", eCParticle_PS.class);
		public static final PropertyDescriptor<gBool> CollideWithStatic = new PropertyDescriptor<>("CollideWithStatic", gBool.class, "bool", "Collision", eCParticle_PS.class);
		public static final PropertyDescriptor<gBool> CollideWithTransparent = new PropertyDescriptor<>("CollideWithTransparent", gBool.class, "bool", "Collision", eCParticle_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<eECollisionSound>> CollisionSound = new PropertyDescriptor<>("CollisionSound", bTPropertyContainer.class, "bTPropertyContainer<enum eECollisionSound>", "Sound", eCParticle_PS.class);
		public static final PropertyDescriptor<bCRange1> CollisionSoundIndex = new PropertyDescriptor<>("CollisionSoundIndex", bCRange1.class, "bCRange1", "Sound", eCParticle_PS.class);
		public static final PropertyDescriptor<bCRange1> CollisionSoundProbability = new PropertyDescriptor<>("CollisionSoundProbability", bCRange1.class, "bCRange1", "Sound", eCParticle_PS.class);
		public static final PropertyDescriptor<gFloat> ColorScaleRepeats = new PropertyDescriptor<>("ColorScaleRepeats", gFloat.class, "float", "Color", eCParticle_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<eECoordinateSystem>> CoordinateSystem = new PropertyDescriptor<>("CoordinateSystem", bTPropertyContainer.class, "bTPropertyContainer<enum eECoordinateSystem>", "Movement", eCParticle_PS.class);
		public static final PropertyDescriptor<bCRange3> DampingFactor = new PropertyDescriptor<>("DampingFactor", bCRange3.class, "bCRange3", "Collision", eCParticle_PS.class);
		public static final PropertyDescriptor<gBool> DepthTest = new PropertyDescriptor<>("DepthTest", gBool.class, "bool", "Render", eCParticle_PS.class);
		public static final PropertyDescriptor<gBool> DepthWrite = new PropertyDescriptor<>("DepthWrite", gBool.class, "bool", "Render", eCParticle_PS.class);
		public static final PropertyDescriptor<gBool> DisableFogging = new PropertyDescriptor<>("DisableFogging", gBool.class, "bool", "Render", eCParticle_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<eETextureDrawStyle>> DrawStyle = new PropertyDescriptor<>("DrawStyle", bTPropertyContainer.class, "bTPropertyContainer<enum eETextureDrawStyle>", "Texture", eCParticle_PS.class);
		public static final PropertyDescriptor<gBool> Enabled = new PropertyDescriptor<>("Enabled", gBool.class, "bool", "General", eCParticle_PS.class);
		public static final PropertyDescriptor<bCVector> ExtentMulitplier = new PropertyDescriptor<>("ExtentMulitplier", bCVector.class, "bCVector", "Collision", eCParticle_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<eEFacingDirection>> FacingDirection = new PropertyDescriptor<>("FacingDirection", bTPropertyContainer.class, "bTPropertyContainer<enum eEFacingDirection>", "Spin", eCParticle_PS.class);
		public static final PropertyDescriptor<gBool> FadeIn = new PropertyDescriptor<>("FadeIn", gBool.class, "bool", "Color", eCParticle_PS.class);
		public static final PropertyDescriptor<gFloat> FadeInEndTime = new PropertyDescriptor<>("FadeInEndTime", gFloat.class, "float", "Color", eCParticle_PS.class);
		public static final PropertyDescriptor<bCFloatAlphaColor> FaceInFactor = new PropertyDescriptor<>("FaceInFactor", bCFloatAlphaColor.class, "bCFloatAlphaColor", "Color", eCParticle_PS.class);
		public static final PropertyDescriptor<gBool> FadeOut = new PropertyDescriptor<>("FadeOut", gBool.class, "bool", "Color", eCParticle_PS.class);
		public static final PropertyDescriptor<bCFloatAlphaColor> FaceOutFactor = new PropertyDescriptor<>("FaceOutFactor", bCFloatAlphaColor.class, "bCFloatAlphaColor", "Color", eCParticle_PS.class);
		public static final PropertyDescriptor<gFloat> FadeOutStartTime = new PropertyDescriptor<>("FadeOutStartTime", gFloat.class, "float", "Color", eCParticle_PS.class);
		public static final PropertyDescriptor<bCRange1> InitialAge = new PropertyDescriptor<>("InitialAge", bCRange1.class, "bCRange1", "Time", eCParticle_PS.class);
		public static final PropertyDescriptor<bCRange1> InitialDelay = new PropertyDescriptor<>("InitialDelay", bCRange1.class, "bCRange1", "Time", eCParticle_PS.class);
		public static final PropertyDescriptor<gBool> Instanced = new PropertyDescriptor<>("Instanced", gBool.class, "bool", "Render", eCParticle_PS.class);
		public static final PropertyDescriptor<bCRange1> Lifetime = new PropertyDescriptor<>("Lifetime", bCRange1.class, "bCRange1", "Time", eCParticle_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<eELightingStyle>> LightingStyle = new PropertyDescriptor<>("LightingStyle", bTPropertyContainer.class, "bTPropertyContainer<enum eELightingStyle>", "Light", eCParticle_PS.class);
		public static final PropertyDescriptor<bCString> Material = new PropertyDescriptor<>("Material", bCString.class, "bCImageOrMaterialResourceString", "Material", eCParticle_PS.class);
		public static final PropertyDescriptor<bCVector> MaxAbsoluteVelocity = new PropertyDescriptor<>("MaxAbsoluteVelocity", bCVector.class, "bCVector", "Movement", eCParticle_PS.class);
		public static final PropertyDescriptor<bCRange1> MaxCollisions = new PropertyDescriptor<>("MaxCollisions", bCRange1.class, "bCRange1", "Collision", eCParticle_PS.class);
		public static final PropertyDescriptor<gInt> MaxNumParticles = new PropertyDescriptor<>("MaxNumParticles", gInt.class, "int", "General", eCParticle_PS.class);
		public static final PropertyDescriptor<gLong> MaxSimultaneousSounds = new PropertyDescriptor<>("MaxSimultaneousSounds", gLong.class, "unsigned long", "Sound", eCParticle_PS.class);
		public static final PropertyDescriptor<gFloat> MinSquaredVelocity = new PropertyDescriptor<>("MinSquaredVelocity", gFloat.class, "float", "Collision", eCParticle_PS.class);
		public static final PropertyDescriptor<gFloat> ParticlesPerSecond = new PropertyDescriptor<>("ParticlesPerSecond", gFloat.class, "float", "General", eCParticle_PS.class);
		public static final PropertyDescriptor<bCVector> ProjectionNormal = new PropertyDescriptor<>("ProjectionNormal", bCVector.class, "bCVector", "Spin", eCParticle_PS.class);
		public static final PropertyDescriptor<gBool> RandomSubdivision = new PropertyDescriptor<>("RandomSubdivision", gBool.class, "bool", "Texture", eCParticle_PS.class);
		public static final PropertyDescriptor<bCVector> RelativeSpinPivot = new PropertyDescriptor<>("RelativeSpinPivot", bCVector.class, "bCVector", "Spin", eCParticle_PS.class);
		public static final PropertyDescriptor<gFloat> RelativeWarmupTime = new PropertyDescriptor<>("RelativeWarmupTime", gFloat.class, "float", "Time", eCParticle_PS.class);
		public static final PropertyDescriptor<gBool> ResetOnTrigger = new PropertyDescriptor<>("ResetOnTrigger", gBool.class, "bool", "Trigger", eCParticle_PS.class);
		public static final PropertyDescriptor<gBool> RespawnDeadParticles = new PropertyDescriptor<>("RespawnDeadParticles", gBool.class, "bool", "General", eCParticle_PS.class);
		public static final PropertyDescriptor<bCRange3> RevolutionCenterOffset = new PropertyDescriptor<>("RevolutionCenterOffset", bCRange3.class, "bCRange3", "Revolution", eCParticle_PS.class);
		public static final PropertyDescriptor<bCRange3> RevolutionsPerSecond = new PropertyDescriptor<>("RevolutionsPerSecond", bCRange3.class, "bCRange3", "Revolution", eCParticle_PS.class);
		public static final PropertyDescriptor<gFloat> RevolutionScaleRepeats = new PropertyDescriptor<>("RevolutionScaleRepeats", gFloat.class, "float", "Revolution", eCParticle_PS.class);
		public static final PropertyDescriptor<bCRange3> RotationDampingFactor = new PropertyDescriptor<>("RotationDampingFactor", bCRange3.class, "bCRange3", "Collision", eCParticle_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<eERotationFrom>> UseRotationFrom = new PropertyDescriptor<>("UseRotationFrom", bTPropertyContainer.class, "bTPropertyContainer<enum eERotationFrom>", "Rotation", eCParticle_PS.class);
		public static final PropertyDescriptor<gFloat> SecondsBeforeInactive = new PropertyDescriptor<>("SecondsBeforeInactive", gFloat.class, "float", "Time", eCParticle_PS.class);
		public static final PropertyDescriptor<gFloat> SizeScaleRepeats = new PropertyDescriptor<>("SizeScaleRepeats", gFloat.class, "float", "Size", eCParticle_PS.class);
		public static final PropertyDescriptor<gChar> SoundPriority = new PropertyDescriptor<>("SoundPriority", gChar.class, "unsigned char", "Sound", eCParticle_PS.class);
		public static final PropertyDescriptor<eCEntityProxy> SpawnFromOtherEmitter = new PropertyDescriptor<>("SpawnFromOtherEmitter", eCEntityProxy.class, "eCEntityProxy", "Collision", eCParticle_PS.class);
		public static final PropertyDescriptor<bCRange1> SpawnNumParticles = new PropertyDescriptor<>("SpawnNumParticles", bCRange1.class, "bCRange1", "Collision", eCParticle_PS.class);
		public static final PropertyDescriptor<bCRange3> SpawnedVelocityScale = new PropertyDescriptor<>("SpawnedVelocityScale", bCRange3.class, "bCRange3", "Collision", eCParticle_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<eESpawningSound>> SpawningSound = new PropertyDescriptor<>("SpawningSound", bTPropertyContainer.class, "bTPropertyContainer<enum eESpawningSound>", "Sound", eCParticle_PS.class);
		public static final PropertyDescriptor<bCRange1> SpawningSoundIndex = new PropertyDescriptor<>("SpawningSoundIndex", bCRange1.class, "bCRange1", "Sound", eCParticle_PS.class);
		public static final PropertyDescriptor<bCRange1> SpawningSoundProbability = new PropertyDescriptor<>("SpawningSoundProbability", bCRange1.class, "bCRange1", "Sound", eCParticle_PS.class);
		public static final PropertyDescriptor<bCRange1> StartSphereRadius = new PropertyDescriptor<>("StartSphereRadius", bCRange1.class, "bCRange1", "Location", eCParticle_PS.class);
		public static final PropertyDescriptor<gFloat> SpinDirection = new PropertyDescriptor<>("SpinDirection", gFloat.class, "float", "Spin", eCParticle_PS.class);
		public static final PropertyDescriptor<gBool> SpinParticles = new PropertyDescriptor<>("SpinParticles", gBool.class, "bool", "Spin", eCParticle_PS.class);
		public static final PropertyDescriptor<bCRange3> SpinsPerSecond = new PropertyDescriptor<>("SpinsPerSecond", bCRange3.class, "bCRange3", "Spin", eCParticle_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<eELocationShape>> StartLocationShape = new PropertyDescriptor<>("StartLocationShape", bTPropertyContainer.class, "bTPropertyContainer<enum eELocationShape>", "Location", eCParticle_PS.class);
		public static final PropertyDescriptor<bCVector> StartLocationOffset = new PropertyDescriptor<>("StartLocationOffset", bCVector.class, "bCVector", "Location", eCParticle_PS.class);
		public static final PropertyDescriptor<bCRange3> StartSize = new PropertyDescriptor<>("StartSize", bCRange3.class, "bCRange3", "Size", eCParticle_PS.class);
		public static final PropertyDescriptor<bCRange3> StartSpin = new PropertyDescriptor<>("StartSpin", bCRange3.class, "bCRange3", "Spin", eCParticle_PS.class);
		public static final PropertyDescriptor<bCRange3> StartVelocity = new PropertyDescriptor<>("StartVelocity", bCRange3.class, "bCRange3", "Movement", eCParticle_PS.class);
		public static final PropertyDescriptor<gBool> StretchToEmitter = new PropertyDescriptor<>("StretchToEmitter", gBool.class, "bool", "Size", eCParticle_PS.class);
		public static final PropertyDescriptor<gInt> SubdivisionEnd = new PropertyDescriptor<>("SubdivisionEnd", gInt.class, "int", "Texture", eCParticle_PS.class);
		public static final PropertyDescriptor<gBool> SubdivisionRowMajor = new PropertyDescriptor<>("SubdivisionRowMajor", gBool.class, "bool", "Texture", eCParticle_PS.class);
		public static final PropertyDescriptor<gFloat> SubdivisionScaleRepeats = new PropertyDescriptor<>("SubdivisionScaleRepeats", gFloat.class, "float", "Texture", eCParticle_PS.class);
		public static final PropertyDescriptor<gInt> SubdivisionStart = new PropertyDescriptor<>("SubdivisionStart", gInt.class, "int", "Texture", eCParticle_PS.class);
		public static final PropertyDescriptor<eCEntityProxy> Target = new PropertyDescriptor<>("Target", eCEntityProxy.class, "eCEntityProxy", "Movement", eCParticle_PS.class);
		public static final PropertyDescriptor<bCString> Texture = new PropertyDescriptor<>("Texture", bCString.class, "bCString", "Texture", eCParticle_PS.class);
		public static final PropertyDescriptor<gFloat> TicksPerSecond = new PropertyDescriptor<>("TicksPerSecond", gFloat.class, "float", "Time", eCParticle_PS.class);
		public static final PropertyDescriptor<bCRange1> TriggerNumParticles = new PropertyDescriptor<>("TriggerNumParticles", bCRange1.class, "bCRange1", "Trigger", eCParticle_PS.class);
		public static final PropertyDescriptor<gFloat> TriggerParticlesPerSecond = new PropertyDescriptor<>("TriggerParticlesPerSecond", gFloat.class, "float", "Trigger", eCParticle_PS.class);
		public static final PropertyDescriptor<bCString> TriggerTag = new PropertyDescriptor<>("TriggerTag", bCString.class, "bCString", "Trigger", eCParticle_PS.class);
		public static final PropertyDescriptor<gInt> NumUSubdivisions = new PropertyDescriptor<>("NumUSubdivisions", gInt.class, "int", "Texture", eCParticle_PS.class);
		public static final PropertyDescriptor<gBool> UniformSize = new PropertyDescriptor<>("UniformSize", gBool.class, "bool", "Size", eCParticle_PS.class);
		public static final PropertyDescriptor<gBool> UseCollision = new PropertyDescriptor<>("UseCollision", gBool.class, "bool", "Collision", eCParticle_PS.class);
		public static final PropertyDescriptor<gBool> UseColorScale = new PropertyDescriptor<>("UseColorScale", gBool.class, "bool", "Color", eCParticle_PS.class);
		public static final PropertyDescriptor<gBool> UseMaxCollisions = new PropertyDescriptor<>("UseMaxCollisions", gBool.class, "bool", "Collision", eCParticle_PS.class);
		public static final PropertyDescriptor<gBool> UseRevolution = new PropertyDescriptor<>("UseRevolution", gBool.class, "bool", "Revolution", eCParticle_PS.class);
		public static final PropertyDescriptor<gBool> UseRevolutionScale = new PropertyDescriptor<>("UseRevolutionScale", gBool.class, "bool", "Revolution", eCParticle_PS.class);
		public static final PropertyDescriptor<gBool> UseRotationDamping = new PropertyDescriptor<>("UseRotationDamping", gBool.class, "bool", "Collision", eCParticle_PS.class);
		public static final PropertyDescriptor<gBool> UseSizeScale = new PropertyDescriptor<>("UseSizeScale", gBool.class, "bool", "Size", eCParticle_PS.class);
		public static final PropertyDescriptor<gBool> UseSpawnedVelocityScale = new PropertyDescriptor<>("UseSpawnedVelocityScale", gBool.class, "bool", "Collision", eCParticle_PS.class);
		public static final PropertyDescriptor<gBool> UseSubdivisionScale = new PropertyDescriptor<>("UseSubdivisionScale", gBool.class, "bool", "Texture", eCParticle_PS.class);
		public static final PropertyDescriptor<gBool> UseVelocityScale = new PropertyDescriptor<>("UseVelocityScale", gBool.class, "bool", "Movement", eCParticle_PS.class);
		public static final PropertyDescriptor<gInt> NumVSubdivisions = new PropertyDescriptor<>("NumVSubdivisions", gInt.class, "int", "Texture", eCParticle_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<eEVelocityDirectionFrom>> VelocityDirectionFrom = new PropertyDescriptor<>("VelocityDirectionFrom", bTPropertyContainer.class, "bTPropertyContainer<enum eEVelocityDirectionFrom>", "Movement", eCParticle_PS.class);
		public static final PropertyDescriptor<bCRange3> VelocityLoss = new PropertyDescriptor<>("VelocityLoss", bCRange3.class, "bCRange3", "Movement", eCParticle_PS.class);
		public static final PropertyDescriptor<gFloat> VelocityScaleRepeats = new PropertyDescriptor<>("VelocityScaleRepeats", gFloat.class, "float", "Movement", eCParticle_PS.class);
		public static final PropertyDescriptor<gFloat> WarmupTicksPerSecond = new PropertyDescriptor<>("WarmupTicksPerSecond", gFloat.class, "float", "Time", eCParticle_PS.class);
	}

	public static interface eCInputReceiver extends ClassDescriptor {
	}

	public static interface eCEngineComponentBase extends eCInputReceiver {
	}

	public static interface eCPhysicsScene extends eCEngineComponentBase {
		public static final PropertyDescriptor<gBool> IsPhysicsEnabled = new PropertyDescriptor<>("IsPhysicsEnabled", gBool.class, "bool", "", eCPhysicsScene.class);
	}

	public static interface eCPhysicsScene_PS extends eCEntityPropertySet {
		public static final PropertyDescriptor<gFloat> BounceTreshold = new PropertyDescriptor<>("BounceTreshold", gFloat.class, "float", "", eCPhysicsScene_PS.class);
		public static final PropertyDescriptor<gFloat> DefaultSkinWidth = new PropertyDescriptor<>("DefaultSkinWidth", gFloat.class, "float", "", eCPhysicsScene_PS.class);
		public static final PropertyDescriptor<gFloat> DefaultSleepAngVeloSquared = new PropertyDescriptor<>("DefaultSleepAngVeloSquared", gFloat.class, "float", "", eCPhysicsScene_PS.class);
		public static final PropertyDescriptor<gFloat> DefaultSleepLinVeloSquared = new PropertyDescriptor<>("DefaultSleepLinVeloSquared", gFloat.class, "float", "", eCPhysicsScene_PS.class);
		public static final PropertyDescriptor<gFloat> DynFrictionScaling = new PropertyDescriptor<>("DynFrictionScaling", gFloat.class, "float", "", eCPhysicsScene_PS.class);
		public static final PropertyDescriptor<bCVector> GravityVector = new PropertyDescriptor<>("GravityVector", bCVector.class, "bCVector", "", eCPhysicsScene_PS.class);
		public static final PropertyDescriptor<gFloat> MaximumAngularVelocity = new PropertyDescriptor<>("MaximumAngularVelocity", gFloat.class, "float", "", eCPhysicsScene_PS.class);
		public static final PropertyDescriptor<gBool> IsPhysicsEnabled = new PropertyDescriptor<>("IsPhysicsEnabled", gBool.class, "bool", "", eCPhysicsScene_PS.class);
		public static final PropertyDescriptor<gFloat> SimulationFPS = new PropertyDescriptor<>("SimulationFPS", gFloat.class, "float", "", eCPhysicsScene_PS.class);
		public static final PropertyDescriptor<gFloat> StatFrictionScaling = new PropertyDescriptor<>("StatFrictionScaling", gFloat.class, "float", "", eCPhysicsScene_PS.class);
	}

	public static interface eCPointLight_PS extends eCDynamicLight_PS {
		public static final PropertyDescriptor<gBool> CastShadows = new PropertyDescriptor<>("CastShadows", gBool.class, "bool", "", eCPointLight_PS.class);
		public static final PropertyDescriptor<bCFloatColor> Color = new PropertyDescriptor<>("Color", bCFloatColor.class, "bCFloatColor", "", eCPointLight_PS.class);
		public static final PropertyDescriptor<gFloat> DecayDuration = new PropertyDescriptor<>("DecayDuration", gFloat.class, "float", "", eCPointLight_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<eEDynamicLightEffect>> Effect = new PropertyDescriptor<>("Effect", bTPropertyContainer.class, "bTPropertyContainer<enum eEDynamicLightEffect>", "", eCPointLight_PS.class);
		public static final PropertyDescriptor<bCFloatColor> EffectColor = new PropertyDescriptor<>("EffectColor", bCFloatColor.class, "bCFloatColor", "", eCPointLight_PS.class);
		public static final PropertyDescriptor<gFloat> EffectPeriod = new PropertyDescriptor<>("EffectPeriod", gFloat.class, "float", "", eCPointLight_PS.class);
		public static final PropertyDescriptor<gFloat> EffectPhase = new PropertyDescriptor<>("EffectPhase", gFloat.class, "float", "", eCPointLight_PS.class);
		public static final PropertyDescriptor<gFloat> Intensity = new PropertyDescriptor<>("Intensity", gFloat.class, "float", "", eCPointLight_PS.class);
		public static final PropertyDescriptor<bCVector> PositionOffset = new PropertyDescriptor<>("PositionOffset", bCVector.class, "bCVector", "", eCPointLight_PS.class);
		public static final PropertyDescriptor<gFloat> Range = new PropertyDescriptor<>("Range", gFloat.class, "float", "", eCPointLight_PS.class);
		public static final PropertyDescriptor<bCFloatColor> SpecularColor = new PropertyDescriptor<>("SpecularColor", bCFloatColor.class, "bCFloatColor", "", eCPointLight_PS.class);
	}

	public static interface eCPrecipitation_PS extends eCEntityPropertySet {
		public static final PropertyDescriptor<gInt> CurrentTextureTile = new PropertyDescriptor<>("CurrentTextureTile", gInt.class, "int", "", eCPrecipitation_PS.class);
		public static final PropertyDescriptor<bCVector> Direction = new PropertyDescriptor<>("Direction", bCVector.class, "bCVector", "", eCPrecipitation_PS.class);
		public static final PropertyDescriptor<gFloat> DirectionScale = new PropertyDescriptor<>("DirectionScale", gFloat.class, "float", "", eCPrecipitation_PS.class);
		public static final PropertyDescriptor<gFloat> JitterPower = new PropertyDescriptor<>("JitterPower", gFloat.class, "float", "", eCPrecipitation_PS.class);
		public static final PropertyDescriptor<gFloat> JitterSpeed = new PropertyDescriptor<>("JitterSpeed", gFloat.class, "float", "", eCPrecipitation_PS.class);
		public static final PropertyDescriptor<gLong> MaxParticles = new PropertyDescriptor<>("MaxParticles", gLong.class, "unsigned long", "", eCPrecipitation_PS.class);
		public static final PropertyDescriptor<gLong> MaxSpawnPoints = new PropertyDescriptor<>("MaxSpawnPoints", gLong.class, "unsigned long", "", eCPrecipitation_PS.class);
		public static final PropertyDescriptor<gInt> ParticlesPerSecond = new PropertyDescriptor<>("ParticlesPerSecond", gInt.class, "int", "", eCPrecipitation_PS.class);
		public static final PropertyDescriptor<gFloat> PrecipitationCubeSize = new PropertyDescriptor<>("PrecipitationCubeSize", gFloat.class, "float", "", eCPrecipitation_PS.class);
		public static final PropertyDescriptor<bCVector2> Size = new PropertyDescriptor<>("Size", bCVector2.class, "bCVector2", "", eCPrecipitation_PS.class);
		public static final PropertyDescriptor<gFloat> SpawnPointDizzer = new PropertyDescriptor<>("SpawnPointDizzer", gFloat.class, "float", "", eCPrecipitation_PS.class);
		public static final PropertyDescriptor<bCVector2> SpeedMinMax = new PropertyDescriptor<>("SpeedMinMax", bCVector2.class, "bCVector2", "", eCPrecipitation_PS.class);
		public static final PropertyDescriptor<gFloat> SplashDuration = new PropertyDescriptor<>("SplashDuration", gFloat.class, "float", "", eCPrecipitation_PS.class);
		public static final PropertyDescriptor<bCVector2> SplashSizeScale = new PropertyDescriptor<>("SplashSizeScale", bCVector2.class, "bCVector2", "", eCPrecipitation_PS.class);
		public static final PropertyDescriptor<gInt> SplashTextureTile = new PropertyDescriptor<>("SplashTextureTile", gInt.class, "int", "", eCPrecipitation_PS.class);
		public static final PropertyDescriptor<bCString> Texture = new PropertyDescriptor<>("Texture", bCString.class, "bCImageResourceString", "", eCPrecipitation_PS.class);
		public static final PropertyDescriptor<gInt> TextureTileCountU = new PropertyDescriptor<>("TextureTileCountU", gInt.class, "int", "", eCPrecipitation_PS.class);
		public static final PropertyDescriptor<gInt> TextureTileCountV = new PropertyDescriptor<>("TextureTileCountV", gInt.class, "int", "", eCPrecipitation_PS.class);
	}

	public static interface eCProcessibleElement extends ClassDescriptor {
		public static final PropertyDescriptor<gBool> IsPersistable = new PropertyDescriptor<>("IsPersistable", gBool.class, "bool", "", eCProcessibleElement.class);
	}

	public static interface eCPropertyLogic extends ClassDescriptor {
		public static final PropertyDescriptor<bCString> ConditionOperator = new PropertyDescriptor<>("ConditionOperator", bCString.class, "bCString", "", eCPropertyLogic.class);
		public static final PropertyDescriptor<gFloat> ConditionValue = new PropertyDescriptor<>("ConditionValue", gFloat.class, "float", "", eCPropertyLogic.class);
		public static final PropertyDescriptor<Unknown> ConditionValueProperty = new PropertyDescriptor<>("ConditionValueProperty", Unknown.class, "eCPropertyProxy", "", eCPropertyLogic.class);
		public static final PropertyDescriptor<Unknown> PropertyValue = new PropertyDescriptor<>("PropertyValue", Unknown.class, "eCPropertyProxy", "", eCPropertyLogic.class);
		public static final PropertyDescriptor<Unknown> PropertyValueLowerBound = new PropertyDescriptor<>("PropertyValueLowerBound", Unknown.class, "eCPropertyProxy", "", eCPropertyLogic.class);
		public static final PropertyDescriptor<Unknown> PropertyValueUpperBound = new PropertyDescriptor<>("PropertyValueUpperBound", Unknown.class, "eCPropertyProxy", "", eCPropertyLogic.class);
		public static final PropertyDescriptor<Unknown> TargetEntityProperty = new PropertyDescriptor<>("TargetEntityProperty", Unknown.class, "eCPropertyProxy", "", eCPropertyLogic.class);
		public static final PropertyDescriptor<Unknown> TargetProperty = new PropertyDescriptor<>("TargetProperty", Unknown.class, "eCPropertyProxy", "", eCPropertyLogic.class);
		public static final PropertyDescriptor<gFloat> ValueLowerBound = new PropertyDescriptor<>("ValueLowerBound", gFloat.class, "float", "", eCPropertyLogic.class);
		public static final PropertyDescriptor<gFloat> ValueUpperBound = new PropertyDescriptor<>("ValueUpperBound", gFloat.class, "float", "", eCPropertyLogic.class);
	}

	public static interface eCResourceAdmin extends eCEngineComponentBase {
		public static final PropertyDescriptor<bTPropertyObject> MainCache = new PropertyDescriptor<>("MainCache", bTPropertyObject.class, "bTPropertyObject<class eCMainCache,class bCObjectRefBase>", "", eCResourceAdmin.class);
	}

	public static interface eCResourceBase_PS extends ClassDescriptor {
		public static final PropertyDescriptor<gFloat> ResourcePriority = new PropertyDescriptor<>("ResourcePriority", gFloat.class, "float", "", eCResourceBase_PS.class);
	}

	public static interface eCResourceImageList_PS extends eCResourceBase_PS {
		public static final PropertyDescriptor<Unknown> Images = new PropertyDescriptor<>("Images", Unknown.class, "bTRefPtrArray<class eCResourceDataEntity *>", "", eCResourceImageList_PS.class);
	}

	public static interface eCResourceMaterialObjectExtended_PS extends eCResourceBase_PS {
		public static final PropertyDescriptor<gBool> IsBlended = new PropertyDescriptor<>("IsBlended", gBool.class, "bool", "", eCResourceMaterialObjectExtended_PS.class);
	}

	public static interface eCResourceMaterialObject_PS extends eCResourceBase_PS {
		public static final PropertyDescriptor<bCVector> AmbientReflection = new PropertyDescriptor<>("AmbientReflection", bCVector.class, "bCVector", "", eCResourceMaterialObject_PS.class);
		public static final PropertyDescriptor<gLong> DetailMapChannel = new PropertyDescriptor<>("DetailMapChannel", gLong.class, "unsigned long", "", eCResourceMaterialObject_PS.class);
		public static final PropertyDescriptor<bCString> DetailMapFilePath = new PropertyDescriptor<>("DetailMapFilePath", bCString.class, "bCString", "", eCResourceMaterialObject_PS.class);
		public static final PropertyDescriptor<bCMatrix> DetailMapTexMatrix = new PropertyDescriptor<>("DetailMapTexMatrix", bCMatrix.class, "bCMatrix", "", eCResourceMaterialObject_PS.class);
		public static final PropertyDescriptor<gLong> DiffuseMapChannel = new PropertyDescriptor<>("DiffuseMapChannel", gLong.class, "unsigned long", "", eCResourceMaterialObject_PS.class);
		public static final PropertyDescriptor<bCString> DiffuseMapFilePath = new PropertyDescriptor<>("DiffuseMapFilePath", bCString.class, "bCString", "", eCResourceMaterialObject_PS.class);
		public static final PropertyDescriptor<bCMatrix> DiffuseMapTexMatrix = new PropertyDescriptor<>("DiffuseMapTexMatrix", bCMatrix.class, "bCMatrix", "", eCResourceMaterialObject_PS.class);
		public static final PropertyDescriptor<bCVector> DiffuseReflection = new PropertyDescriptor<>("DiffuseReflection", bCVector.class, "bCVector", "", eCResourceMaterialObject_PS.class);
		public static final PropertyDescriptor<gBool> HasDetailNormalMap = new PropertyDescriptor<>("HasDetailNormalMap", gBool.class, "bool", "", eCResourceMaterialObject_PS.class);
		public static final PropertyDescriptor<gBool> HasDetailSpecularMap = new PropertyDescriptor<>("HasDetailSpecularMap", gBool.class, "bool", "", eCResourceMaterialObject_PS.class);
		public static final PropertyDescriptor<gBool> HasHeightMap = new PropertyDescriptor<>("HasHeightMap", gBool.class, "bool", "", eCResourceMaterialObject_PS.class);
		public static final PropertyDescriptor<gBool> IsBlended = new PropertyDescriptor<>("IsBlended", gBool.class, "bool", "", eCResourceMaterialObject_PS.class);
		public static final PropertyDescriptor<gBool> IsDepthRobust = new PropertyDescriptor<>("IsDepthRobust", gBool.class, "bool", "", eCResourceMaterialObject_PS.class);
		public static final PropertyDescriptor<gBool> IsUniDetailed = new PropertyDescriptor<>("IsUniDetailed", gBool.class, "bool", "", eCResourceMaterialObject_PS.class);
		public static final PropertyDescriptor<gLong> NormalMapChannel = new PropertyDescriptor<>("NormalMapChannel", gLong.class, "unsigned long", "", eCResourceMaterialObject_PS.class);
		public static final PropertyDescriptor<bCString> NormalMapFilePath = new PropertyDescriptor<>("NormalMapFilePath", bCString.class, "bCString", "", eCResourceMaterialObject_PS.class);
		public static final PropertyDescriptor<bCMatrix> NormalMapTexMatrix = new PropertyDescriptor<>("NormalMapTexMatrix", bCMatrix.class, "bCMatrix", "", eCResourceMaterialObject_PS.class);
		public static final PropertyDescriptor<gLong> SecondDetailMapChannel = new PropertyDescriptor<>("SecondDetailMapChannel", gLong.class, "unsigned long", "", eCResourceMaterialObject_PS.class);
		public static final PropertyDescriptor<bCString> SecondDetailMapFilePath = new PropertyDescriptor<>("SecondDetailMapFilePath", bCString.class, "bCString", "", eCResourceMaterialObject_PS.class);
		public static final PropertyDescriptor<bCMatrix> SecondDetailMapTexMatrix = new PropertyDescriptor<>("SecondDetailMapTexMatrix", bCMatrix.class, "bCMatrix", "", eCResourceMaterialObject_PS.class);
		public static final PropertyDescriptor<gLong> SecondDiffuseMapChannel = new PropertyDescriptor<>("SecondDiffuseMapChannel", gLong.class, "unsigned long", "", eCResourceMaterialObject_PS.class);
		public static final PropertyDescriptor<bCString> SecondDiffuseMapFilePath = new PropertyDescriptor<>("SecondDiffuseMapFilePath", bCString.class, "bCString", "", eCResourceMaterialObject_PS.class);
		public static final PropertyDescriptor<bCMatrix> SecondDiffuseMapTexMatrix = new PropertyDescriptor<>("SecondDiffuseMapTexMatrix", bCMatrix.class, "bCMatrix", "", eCResourceMaterialObject_PS.class);
		public static final PropertyDescriptor<gLong> SecondNormalMapChannel = new PropertyDescriptor<>("SecondNormalMapChannel", gLong.class, "unsigned long", "", eCResourceMaterialObject_PS.class);
		public static final PropertyDescriptor<bCString> SecondNormalMapFilePath = new PropertyDescriptor<>("SecondNormalMapFilePath", bCString.class, "bCString", "", eCResourceMaterialObject_PS.class);
		public static final PropertyDescriptor<bCMatrix> SecondNormalMapTexMatrix = new PropertyDescriptor<>("SecondNormalMapTexMatrix", bCMatrix.class, "bCMatrix", "", eCResourceMaterialObject_PS.class);
		public static final PropertyDescriptor<gLong> SecondSpecularMapChannel = new PropertyDescriptor<>("SecondSpecularMapChannel", gLong.class, "unsigned long", "", eCResourceMaterialObject_PS.class);
		public static final PropertyDescriptor<bCString> SecondSpecularMapFilePath = new PropertyDescriptor<>("SecondSpecularMapFilePath", bCString.class, "bCString", "", eCResourceMaterialObject_PS.class);
		public static final PropertyDescriptor<bCMatrix> SecondSpecularMapTexMatrix = new PropertyDescriptor<>("SecondSpecularMapTexMatrix", bCMatrix.class, "bCMatrix", "", eCResourceMaterialObject_PS.class);
		public static final PropertyDescriptor<gBool> SpecularEnabled = new PropertyDescriptor<>("SpecularEnabled", gBool.class, "bool", "", eCResourceMaterialObject_PS.class);
		public static final PropertyDescriptor<gLong> SpecularMapChannel = new PropertyDescriptor<>("SpecularMapChannel", gLong.class, "unsigned long", "", eCResourceMaterialObject_PS.class);
		public static final PropertyDescriptor<bCString> SpecularMapFilePath = new PropertyDescriptor<>("SpecularMapFilePath", bCString.class, "bCString", "", eCResourceMaterialObject_PS.class);
		public static final PropertyDescriptor<gFloat> SpecularMapPower = new PropertyDescriptor<>("SpecularMapPower", gFloat.class, "float", "", eCResourceMaterialObject_PS.class);
		public static final PropertyDescriptor<bCMatrix> SpecularMapTexMatrix = new PropertyDescriptor<>("SpecularMapTexMatrix", bCMatrix.class, "bCMatrix", "", eCResourceMaterialObject_PS.class);
		public static final PropertyDescriptor<gFloat> SpecularPower = new PropertyDescriptor<>("SpecularPower", gFloat.class, "float", "", eCResourceMaterialObject_PS.class);
		public static final PropertyDescriptor<bCVector> SpecularReflection = new PropertyDescriptor<>("SpecularReflection", bCVector.class, "bCVector", "", eCResourceMaterialObject_PS.class);
		public static final PropertyDescriptor<gBool> UseLightmaps = new PropertyDescriptor<>("UseLightmaps", gBool.class, "bool", "", eCResourceMaterialObject_PS.class);
		public static final PropertyDescriptor<gBool> UseSpecShaderColor = new PropertyDescriptor<>("UseSpecShaderColor", gBool.class, "bool", "", eCResourceMaterialObject_PS.class);
	}

	public static interface eCResourceMeshComplex_PS extends eCResourceBase_PS {
		public static final PropertyDescriptor<bCBox> BoundingBox = new PropertyDescriptor<>("BoundingBox", bCBox.class, "bCBox", "", eCResourceMeshComplex_PS.class);
	}

	public static interface eCResourceMeshLoD_PS extends eCResourceBase_PS {
		public static final PropertyDescriptor<bCBox> BoundingBox = new PropertyDescriptor<>("BoundingBox", bCBox.class, "bCBox", "", eCResourceMeshLoD_PS.class);
		public static final PropertyDescriptor<gFloat> LoDScale = new PropertyDescriptor<>("LoDScale", gFloat.class, "float", "", eCResourceMeshLoD_PS.class);
	}

	public static interface eCResourceMesh_PS extends eCResourceBase_PS {
		public static final PropertyDescriptor<bCBox> BoundingBox = new PropertyDescriptor<>("BoundingBox", bCBox.class, "bCBox", "", eCResourceMesh_PS.class);
		public static final PropertyDescriptor<gLong> FVF = new PropertyDescriptor<>("FVF", gLong.class, "unsigned long", "", eCResourceMesh_PS.class);
		public static final PropertyDescriptor<bCString> MaterialFileName = new PropertyDescriptor<>("MaterialFileName", bCString.class, "bCString", "", eCResourceMesh_PS.class);
	}

	public static interface eCResourceShaderMaterial_PS extends eCResourceBase_PS {
		public static final PropertyDescriptor<gBool> DisableCollision = new PropertyDescriptor<>("DisableCollision", gBool.class, "bool", "", eCResourceShaderMaterial_PS.class);
		public static final PropertyDescriptor<gBool> DisableResponse = new PropertyDescriptor<>("DisableResponse", gBool.class, "bool", "", eCResourceShaderMaterial_PS.class);
		public static final PropertyDescriptor<gBool> IgnoredByTraceRay = new PropertyDescriptor<>("IgnoredByTraceRay", gBool.class, "bool", "", eCResourceShaderMaterial_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<eEShapeMaterial>> PhysicMaterial = new PropertyDescriptor<>("PhysicMaterial", bTPropertyContainer.class, "bTPropertyContainer<enum eEShapeMaterial>", "", eCResourceShaderMaterial_PS.class);
	}

	public static interface eCResourceSimpleSound_PS extends eCResourceSound_PS {
		public static final PropertyDescriptor<bCString> Sample = new PropertyDescriptor<>("Sample", bCString.class, "bCString", "Sample", eCResourceSimpleSound_PS.class);
	}

	public static interface eCResourceSoundList_PS extends eCResourceSound_PS {
		public static final PropertyDescriptor<gLong> Reserved = new PropertyDescriptor<>("Reserved", gLong.class, "unsigned long", "", eCResourceSoundList_PS.class);
	}

	public static interface eCResourceSound_PS extends eCResourceBase_PS {
		public static final PropertyDescriptor<gFloat> Frequency = new PropertyDescriptor<>("Frequency", gFloat.class, "float", "General", eCResourceSound_PS.class);
		public static final PropertyDescriptor<gFloat> FrequencyVariance = new PropertyDescriptor<>("FrequencyVariance", gFloat.class, "float", "Variation", eCResourceSound_PS.class);
		public static final PropertyDescriptor<gFloat> InsideConeAngle = new PropertyDescriptor<>("InsideConeAngle", gFloat.class, "float", "Direction", eCResourceSound_PS.class);
		public static final PropertyDescriptor<gBool> LoopForever = new PropertyDescriptor<>("LoopForever", gBool.class, "bool", "Looping", eCResourceSound_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<eESoundLoopMode>> LoopMode = new PropertyDescriptor<>("LoopMode", bTPropertyContainer.class, "bTPropertyContainer<enum eESoundLoopMode>", "Looping", eCResourceSound_PS.class);
		public static final PropertyDescriptor<gLong> LoopRepeats = new PropertyDescriptor<>("LoopRepeats", gLong.class, "long", "Looping", eCResourceSound_PS.class);
		public static final PropertyDescriptor<gFloat> MaxDistance = new PropertyDescriptor<>("MaxDistance", gFloat.class, "float", "Extent", eCResourceSound_PS.class);
		public static final PropertyDescriptor<gFloat> MinDistance = new PropertyDescriptor<>("MinDistance", gFloat.class, "float", "Extent", eCResourceSound_PS.class);
		public static final PropertyDescriptor<gFloat> OutsideConeAngle = new PropertyDescriptor<>("OutsideConeAngle", gFloat.class, "float", "Direction", eCResourceSound_PS.class);
		public static final PropertyDescriptor<gFloat> OutsideConeVolume = new PropertyDescriptor<>("OutsideConeVolume", gFloat.class, "float", "Direction", eCResourceSound_PS.class);
		public static final PropertyDescriptor<gFloat> Pan = new PropertyDescriptor<>("Pan", gFloat.class, "float", "General", eCResourceSound_PS.class);
		public static final PropertyDescriptor<gFloat> PanVariance = new PropertyDescriptor<>("PanVariance", gFloat.class, "float", "Variation", eCResourceSound_PS.class);
		public static final PropertyDescriptor<gChar> Priority = new PropertyDescriptor<>("Priority", gChar.class, "unsigned char", "General", eCResourceSound_PS.class);
		public static final PropertyDescriptor<gBool> Use3D = new PropertyDescriptor<>("Use3D", gBool.class, "bool", "Extent", eCResourceSound_PS.class);
		public static final PropertyDescriptor<gBool> UseHardware = new PropertyDescriptor<>("UseHardware", gBool.class, "bool", "Sample", eCResourceSound_PS.class);
		public static final PropertyDescriptor<gBool> UseLinearRollOff = new PropertyDescriptor<>("UseLinearRollOff", gBool.class, "bool", "Extent", eCResourceSound_PS.class);
		public static final PropertyDescriptor<gBool> UseStream = new PropertyDescriptor<>("UseStream", gBool.class, "bool", "Sample", eCResourceSound_PS.class);
		public static final PropertyDescriptor<gFloat> Volume = new PropertyDescriptor<>("Volume", gFloat.class, "float", "General", eCResourceSound_PS.class);
		public static final PropertyDescriptor<gFloat> VolumeVariance = new PropertyDescriptor<>("VolumeVariance", gFloat.class, "float", "Variation", eCResourceSound_PS.class);
	}

	public static interface eCRigidBodyBase_PS extends eCEntityPropertySet {
		public static final PropertyDescriptor<gBool> PhysicsEnabled = new PropertyDescriptor<>("PhysicsEnabled", gBool.class, "bool", "", eCRigidBodyBase_PS.class);
	}

	public static interface eCRigidBody_PS extends eCRigidBodyBase_PS {
		public static final PropertyDescriptor<gFloat> AngularDamping = new PropertyDescriptor<>("AngularDamping", gFloat.class, "float", "", eCRigidBody_PS.class);
		public static final PropertyDescriptor<bCVector> StartAngularVelocity = new PropertyDescriptor<>("StartAngularVelocity", bCVector.class, "bCVector", "", eCRigidBody_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<eERigidbody_Flag>> BodyFlag = new PropertyDescriptor<>("BodyFlag", bTPropertyContainer.class, "bTPropertyContainer<enum eERigidbody_Flag>", "", eCRigidBody_PS.class);
		public static final PropertyDescriptor<gFloat> CCDMotionTreshold = new PropertyDescriptor<>("CCDMotionTreshold", gFloat.class, "float", "", eCRigidBody_PS.class);
		public static final PropertyDescriptor<bCVector> CenterOfMass = new PropertyDescriptor<>("CenterOfMass", bCVector.class, "bCVector", "", eCRigidBody_PS.class);
		public static final PropertyDescriptor<bCVector> StartForce = new PropertyDescriptor<>("StartForce", bCVector.class, "bCVector", "", eCRigidBody_PS.class);
		public static final PropertyDescriptor<bCVector> StartTorque = new PropertyDescriptor<>("StartTorque", bCVector.class, "bCVector", "", eCRigidBody_PS.class);
		public static final PropertyDescriptor<gFloat> LinearDamping = new PropertyDescriptor<>("LinearDamping", gFloat.class, "float", "", eCRigidBody_PS.class);
		public static final PropertyDescriptor<bCVector> StartVelocity = new PropertyDescriptor<>("StartVelocity", bCVector.class, "bCVector", "", eCRigidBody_PS.class);
		public static final PropertyDescriptor<gFloat> TotalMass = new PropertyDescriptor<>("TotalMass", gFloat.class, "float", "", eCRigidBody_PS.class);
		public static final PropertyDescriptor<bCVector> MassSpaceInertia = new PropertyDescriptor<>("MassSpaceInertia", bCVector.class, "bCVector", "", eCRigidBody_PS.class);
		public static final PropertyDescriptor<gFloat> MaxAngularVelocity = new PropertyDescriptor<>("MaxAngularVelocity", gFloat.class, "float", "", eCRigidBody_PS.class);
		public static final PropertyDescriptor<gFloat> WakeUpCounter = new PropertyDescriptor<>("WakeUpCounter", gFloat.class, "float", "", eCRigidBody_PS.class);
	}

	public static interface eCShaderBase extends eCShaderEllementBase {
		public static final PropertyDescriptor<bTPropertyContainer<eEShaderMaterialBlendMode>> BlendMode = new PropertyDescriptor<>("BlendMode", bTPropertyContainer.class, "bTPropertyContainer<enum eEShaderMaterialBlendMode>", "", eCShaderBase.class);
		public static final PropertyDescriptor<bCString> FallbackMaterial = new PropertyDescriptor<>("FallbackMaterial", bCString.class, "bCImageOrMaterialResourceString", "", eCShaderBase.class);
		public static final PropertyDescriptor<gChar> MaskReference = new PropertyDescriptor<>("MaskReference", gChar.class, "unsigned char", "", eCShaderBase.class);
		public static final PropertyDescriptor<bTPropertyContainer<eEShaderMaterialVersion>> MaxShaderVersion = new PropertyDescriptor<>("MaxShaderVersion", bTPropertyContainer.class, "bTPropertyContainer<enum eEShaderMaterialVersion>", "", eCShaderBase.class);
		public static final PropertyDescriptor<gBool> UseDepthBias = new PropertyDescriptor<>("UseDepthBias", gBool.class, "bool", "", eCShaderBase.class);
	}

	public static interface eCShaderDefault extends eCShaderBase {
		public static final PropertyDescriptor<gBool> DisableLighting = new PropertyDescriptor<>("DisableLighting", gBool.class, "bool", "", eCShaderDefault.class);
		public static final PropertyDescriptor<gBool> EnableSpecular = new PropertyDescriptor<>("EnableSpecular", gBool.class, "bool", "", eCShaderDefault.class);
		public static final PropertyDescriptor<bTPropertyContainer<eEShaderMaterialTransformation>> TransformationType = new PropertyDescriptor<>("TransformationType", bTPropertyContainer.class, "bTPropertyContainer<enum eEShaderMaterialTransformation>", "", eCShaderDefault.class);
	}

	public static interface eCShaderLeaf extends eCShaderBase {
		public static final PropertyDescriptor<gBool> DisableLighting = new PropertyDescriptor<>("DisableLighting", gBool.class, "bool", "", eCShaderLeaf.class);
		public static final PropertyDescriptor<gBool> EnableSpecular = new PropertyDescriptor<>("EnableSpecular", gBool.class, "bool", "", eCShaderLeaf.class);
		public static final PropertyDescriptor<gBool> EnableSubSurface = new PropertyDescriptor<>("EnableSubSurface", gBool.class, "bool", "", eCShaderLeaf.class);
		public static final PropertyDescriptor<bTPropertyContainer<eEShaderMaterialTransformation>> TransformationType = new PropertyDescriptor<>("TransformationType", bTPropertyContainer.class, "bTPropertyContainer<enum eEShaderMaterialTransformation>", "", eCShaderLeaf.class);
	}

	public static interface eCShaderParticle extends eCShaderBase {
		public static final PropertyDescriptor<gFloat> DistortionScale = new PropertyDescriptor<>("DistortionScale", gFloat.class, "float", "", eCShaderParticle.class);
		public static final PropertyDescriptor<gFloat> SoftParticleScale = new PropertyDescriptor<>("SoftParticleScale", gFloat.class, "float", "", eCShaderParticle.class);
	}

	public static interface eCShaderSkin extends eCShaderBase {
		public static final PropertyDescriptor<bCFloatColor> RimColor = new PropertyDescriptor<>("RimColor", bCFloatColor.class, "bCFloatColor", "", eCShaderSkin.class);
		public static final PropertyDescriptor<gBool> DisableLighting = new PropertyDescriptor<>("DisableLighting", gBool.class, "bool", "", eCShaderSkin.class);
		public static final PropertyDescriptor<gBool> EnableRimLighting = new PropertyDescriptor<>("EnableRimLighting", gBool.class, "bool", "", eCShaderSkin.class);
		public static final PropertyDescriptor<gBool> EnableSpecular = new PropertyDescriptor<>("EnableSpecular", gBool.class, "bool", "", eCShaderSkin.class);
		public static final PropertyDescriptor<gFloat> RimPower = new PropertyDescriptor<>("RimPower", gFloat.class, "float", "", eCShaderSkin.class);
		public static final PropertyDescriptor<gFloat> SubSurfaceRollOff = new PropertyDescriptor<>("SubSurfaceRollOff", gFloat.class, "float", "", eCShaderSkin.class);
		public static final PropertyDescriptor<bTPropertyContainer<eEShaderMaterialTransformation>> TransformationType = new PropertyDescriptor<>("TransformationType", bTPropertyContainer.class, "bTPropertyContainer<enum eEShaderMaterialTransformation>", "", eCShaderSkin.class);
	}

	public static interface eCShaderSkyDome extends eCShaderBase {
		public static final PropertyDescriptor<gBool> BillboardOnly = new PropertyDescriptor<>("BillboardOnly", gBool.class, "bool", "", eCShaderSkyDome.class);
		public static final PropertyDescriptor<bCFloatColor> HazeColor = new PropertyDescriptor<>("HazeColor", bCFloatColor.class, "bCFloatColor", "", eCShaderSkyDome.class);
		public static final PropertyDescriptor<bCFloatColor> SkyColor = new PropertyDescriptor<>("SkyColor", bCFloatColor.class, "bCFloatColor", "", eCShaderSkyDome.class);
		public static final PropertyDescriptor<bCFloatColor> SunColor = new PropertyDescriptor<>("SunColor", bCFloatColor.class, "bCFloatColor", "", eCShaderSkyDome.class);
		public static final PropertyDescriptor<bCVector> SunNormal = new PropertyDescriptor<>("SunNormal", bCVector.class, "bCVector", "", eCShaderSkyDome.class);
		public static final PropertyDescriptor<gBool> SunOcclusionOnly = new PropertyDescriptor<>("SunOcclusionOnly", gBool.class, "bool", "", eCShaderSkyDome.class);
	}

	public static interface eCShaderWater extends eCShaderBase {
		public static final PropertyDescriptor<gFloat> DepthBlueHalfLife = new PropertyDescriptor<>("DepthBlueHalfLife", gFloat.class, "float", "", eCShaderWater.class);
		public static final PropertyDescriptor<gFloat> DepthGreenHalfLife = new PropertyDescriptor<>("DepthGreenHalfLife", gFloat.class, "float", "", eCShaderWater.class);
		public static final PropertyDescriptor<gFloat> DepthRedHalfLife = new PropertyDescriptor<>("DepthRedHalfLife", gFloat.class, "float", "", eCShaderWater.class);
		public static final PropertyDescriptor<gFloat> DepthScale = new PropertyDescriptor<>("DepthScale", gFloat.class, "float", "", eCShaderWater.class);
		public static final PropertyDescriptor<gBool> EnableSpecular = new PropertyDescriptor<>("EnableSpecular", gBool.class, "bool", "", eCShaderWater.class);
		public static final PropertyDescriptor<gFloat> FresnelConstant = new PropertyDescriptor<>("FresnelConstant", gFloat.class, "float", "", eCShaderWater.class);
		public static final PropertyDescriptor<bCFloatColor> ReflectionColor = new PropertyDescriptor<>("ReflectionColor", bCFloatColor.class, "bCFloatColor", "", eCShaderWater.class);
		public static final PropertyDescriptor<gFloat> ShoreFadingScale = new PropertyDescriptor<>("ShoreFadingScale", gFloat.class, "float", "", eCShaderWater.class);
	}

	public static interface eCSkydome_PS extends eCEntityPropertySet {
		public static final PropertyDescriptor<gFloat> CloudSize1 = new PropertyDescriptor<>("CloudSize1", gFloat.class, "float", "Clouds", eCSkydome_PS.class);
		public static final PropertyDescriptor<gFloat> CloudSize2 = new PropertyDescriptor<>("CloudSize2", gFloat.class, "float", "Clouds", eCSkydome_PS.class);
		public static final PropertyDescriptor<bCFloatColor> CloudColor = new PropertyDescriptor<>("CloudColor", bCFloatColor.class, "bCFloatColor", "Clouds", eCSkydome_PS.class);
		public static final PropertyDescriptor<bCVector2> CloudDirection = new PropertyDescriptor<>("CloudDirection", bCVector2.class, "bCVector2", "Clouds", eCSkydome_PS.class);
		public static final PropertyDescriptor<bCString> CloudTexture1 = new PropertyDescriptor<>("CloudTexture1", bCString.class, "bCImageResourceString", "Clouds", eCSkydome_PS.class);
		public static final PropertyDescriptor<bCString> CloudTexture2 = new PropertyDescriptor<>("CloudTexture2", bCString.class, "bCImageResourceString", "Clouds", eCSkydome_PS.class);
		public static final PropertyDescriptor<gFloat> CloudThickness = new PropertyDescriptor<>("CloudThickness", gFloat.class, "float", "Clouds", eCSkydome_PS.class);
		public static final PropertyDescriptor<gFloat> LensFlareSize = new PropertyDescriptor<>("LensFlareSize", gFloat.class, "float", "LensFlare", eCSkydome_PS.class);
		public static final PropertyDescriptor<bCString> LensFlareTextures = new PropertyDescriptor<>("LensFlareTextures", bCString.class, "bCImageResourceString", "LensFlare", eCSkydome_PS.class);
		public static final PropertyDescriptor<bCFloatColor> MoonColor = new PropertyDescriptor<>("MoonColor", bCFloatColor.class, "bCFloatColor", "Moon", eCSkydome_PS.class);
		public static final PropertyDescriptor<gFloat> MoonSize = new PropertyDescriptor<>("MoonSize", gFloat.class, "float", "Moon", eCSkydome_PS.class);
		public static final PropertyDescriptor<bCString> MoonTexture = new PropertyDescriptor<>("MoonTexture", bCString.class, "bCImageResourceString", "Moon", eCSkydome_PS.class);
		public static final PropertyDescriptor<gInt> StarCount = new PropertyDescriptor<>("StarCount", gInt.class, "int", "Stars", eCSkydome_PS.class);
		public static final PropertyDescriptor<bCString> StarTexture = new PropertyDescriptor<>("StarTexture", bCString.class, "bCImageResourceString", "Stars", eCSkydome_PS.class);
		public static final PropertyDescriptor<bCFloatColor> SunColor = new PropertyDescriptor<>("SunColor", bCFloatColor.class, "bCFloatColor", "Sun", eCSkydome_PS.class);
		public static final PropertyDescriptor<gFloat> SunSize = new PropertyDescriptor<>("SunSize", gFloat.class, "float", "Sun", eCSkydome_PS.class);
		public static final PropertyDescriptor<bCString> SunTexture = new PropertyDescriptor<>("SunTexture", bCString.class, "bCImageResourceString", "Sun", eCSkydome_PS.class);
	}

	public static interface eCSpeedTreeWind_PS extends eCEntityPropertySet {
		public static final PropertyDescriptor<bCVector> BendHigh = new PropertyDescriptor<>("BendHigh", bCVector.class, "bCVector", "", eCSpeedTreeWind_PS.class);
		public static final PropertyDescriptor<bCVector> BendLow = new PropertyDescriptor<>("BendLow", bCVector.class, "bCVector", "", eCSpeedTreeWind_PS.class);
		public static final PropertyDescriptor<bCVector> GustControl = new PropertyDescriptor<>("GustControl", bCVector.class, "bCVector", "", eCSpeedTreeWind_PS.class);
		public static final PropertyDescriptor<bCVector2> GustDuration = new PropertyDescriptor<>("GustDuration", bCVector2.class, "bCVector2", "", eCSpeedTreeWind_PS.class);
		public static final PropertyDescriptor<gFloat> GustFrequency = new PropertyDescriptor<>("GustFrequency", gFloat.class, "float", "", eCSpeedTreeWind_PS.class);
		public static final PropertyDescriptor<bCVector2> GustStrength = new PropertyDescriptor<>("GustStrength", bCVector2.class, "bCVector2", "", eCSpeedTreeWind_PS.class);
		public static final PropertyDescriptor<bCVector2> LeafRockingAngles = new PropertyDescriptor<>("LeafRockingAngles", bCVector2.class, "bCVector2", "", eCSpeedTreeWind_PS.class);
		public static final PropertyDescriptor<bCVector2> LeafRockingFrequency = new PropertyDescriptor<>("LeafRockingFrequency", bCVector2.class, "bCVector2", "", eCSpeedTreeWind_PS.class);
		public static final PropertyDescriptor<bCVector> LeafRockingHigh = new PropertyDescriptor<>("LeafRockingHigh", bCVector.class, "bCVector", "", eCSpeedTreeWind_PS.class);
		public static final PropertyDescriptor<bCVector> LeafRocking = new PropertyDescriptor<>("LeafRocking", bCVector.class, "bCVector", "", eCSpeedTreeWind_PS.class);
		public static final PropertyDescriptor<bCVector2> LeafRustlingAngles = new PropertyDescriptor<>("LeafRustlingAngles", bCVector2.class, "bCVector2", "", eCSpeedTreeWind_PS.class);
		public static final PropertyDescriptor<bCVector2> LeafRustlingFrequency = new PropertyDescriptor<>("LeafRustlingFrequency", bCVector2.class, "bCVector2", "", eCSpeedTreeWind_PS.class);
		public static final PropertyDescriptor<bCVector> LeafRustlingHigh = new PropertyDescriptor<>("LeafRustlingHigh", bCVector.class, "bCVector", "", eCSpeedTreeWind_PS.class);
		public static final PropertyDescriptor<bCVector> LeafRustling = new PropertyDescriptor<>("LeafRustling", bCVector.class, "bCVector", "", eCSpeedTreeWind_PS.class);
		public static final PropertyDescriptor<gFloat> LeafStrengthExponent = new PropertyDescriptor<>("LeafStrengthExponent", gFloat.class, "float", "", eCSpeedTreeWind_PS.class);
		public static final PropertyDescriptor<gFloat> MaxBendAngle = new PropertyDescriptor<>("MaxBendAngle", gFloat.class, "float", "", eCSpeedTreeWind_PS.class);
		public static final PropertyDescriptor<gFloat> StrengthAdjustmentExponent = new PropertyDescriptor<>("StrengthAdjustmentExponent", gFloat.class, "float", "", eCSpeedTreeWind_PS.class);
		public static final PropertyDescriptor<bCVector2> VibrationAngles = new PropertyDescriptor<>("VibrationAngles", bCVector2.class, "bCVector2", "", eCSpeedTreeWind_PS.class);
		public static final PropertyDescriptor<bCVector2> VibrationFrequency = new PropertyDescriptor<>("VibrationFrequency", bCVector2.class, "bCVector2", "", eCSpeedTreeWind_PS.class);
		public static final PropertyDescriptor<bCVector> VibrationHigh = new PropertyDescriptor<>("VibrationHigh", bCVector.class, "bCVector", "", eCSpeedTreeWind_PS.class);
		public static final PropertyDescriptor<bCVector> VibrationLow = new PropertyDescriptor<>("VibrationLow", bCVector.class, "bCVector", "", eCSpeedTreeWind_PS.class);
		public static final PropertyDescriptor<gFloat> WindStrange = new PropertyDescriptor<>("WindStrange", gFloat.class, "float", "", eCSpeedTreeWind_PS.class);
	}

	public static interface eCSpeedTree_PS extends eCEntityPropertySet {
		public static final PropertyDescriptor<bTPropertyContainer<eEAmbientEnvironment>> AmbientEnvironment = new PropertyDescriptor<>("AmbientEnvironment", bTPropertyContainer.class, "bTPropertyContainer<enum eEAmbientEnvironment>", "", eCSpeedTree_PS.class);
		public static final PropertyDescriptor<gBool> EnableWind = new PropertyDescriptor<>("EnableWind", gBool.class, "bool", "", eCSpeedTree_PS.class);
		public static final PropertyDescriptor<gBool> InfluenceGlobalAmbientOcclusion = new PropertyDescriptor<>("InfluenceGlobalAmbientOcclusion", gBool.class, "bool", "", eCSpeedTree_PS.class);
		public static final PropertyDescriptor<bCString> ResourceFilePath = new PropertyDescriptor<>("ResourceFilePath", bCString.class, "bCSpeedTreeResourceString", "", eCSpeedTree_PS.class);
	}

	public static interface eCSphericalJoint extends eCJoint {
		public static final PropertyDescriptor<bTPropertyObject> SphericalJointDesc = new PropertyDescriptor<>("SphericalJointDesc", bTPropertyObject.class, "bTPropertyObject<class eCSphericalJointDesc,class eCJointDesc>", "", eCSphericalJoint.class);
	}

	public static interface eCSphericalJointDesc extends eCJointDesc {
		public static final PropertyDescriptor<gBool> EnableJointSpring = new PropertyDescriptor<>("EnableJointSpring", gBool.class, "bool", "", eCSphericalJointDesc.class);
		public static final PropertyDescriptor<gBool> EnableSwingLimit = new PropertyDescriptor<>("EnableSwingLimit", gBool.class, "bool", "", eCSphericalJointDesc.class);
		public static final PropertyDescriptor<gBool> EnableSwingSpring = new PropertyDescriptor<>("EnableSwingSpring", gBool.class, "bool", "", eCSphericalJointDesc.class);
		public static final PropertyDescriptor<gBool> EnableTwistLimit = new PropertyDescriptor<>("EnableTwistLimit", gBool.class, "bool", "", eCSphericalJointDesc.class);
		public static final PropertyDescriptor<gBool> EnableTwistSpring = new PropertyDescriptor<>("EnableTwistSpring", gBool.class, "bool", "", eCSphericalJointDesc.class);
		public static final PropertyDescriptor<bTPropertyContainer<eEJogIntProjectionMode>> JointProjectionMode = new PropertyDescriptor<>("JointProjectionMode", bTPropertyContainer.class, "bTPropertyContainer<enum eEJointProjectionMode>", "", eCSphericalJointDesc.class);
		public static final PropertyDescriptor<bTPropertyObject> JointSpring = new PropertyDescriptor<>("JointSpring", bTPropertyObject.class, "bTPropertyObject<class eCSpringDesc,class bCObjectRefBase>", "", eCSphericalJointDesc.class);
		public static final PropertyDescriptor<gFloat> ProjectionDistance = new PropertyDescriptor<>("ProjectionDistance", gFloat.class, "float", "", eCSphericalJointDesc.class);
		public static final PropertyDescriptor<bCVector> SwingAxis = new PropertyDescriptor<>("SwingAxis", bCVector.class, "bCVector", "", eCSphericalJointDesc.class);
		public static final PropertyDescriptor<bTPropertyObject> SwingLimit = new PropertyDescriptor<>("SwingLimit", bTPropertyObject.class, "bTPropertyObject<class eCJointLimitDesc,class bCObjectRefBase>", "", eCSphericalJointDesc.class);
		public static final PropertyDescriptor<bTPropertyObject> SwingSpring = new PropertyDescriptor<>("SwingSpring", bTPropertyObject.class, "bTPropertyObject<class eCSpringDesc,class bCObjectRefBase>", "", eCSphericalJointDesc.class);
		public static final PropertyDescriptor<bTPropertyObject> TwistLimit = new PropertyDescriptor<>("TwistLimit", bTPropertyObject.class, "bTPropertyObject<class eCJointLimitPairDesc,class bCObjectRefBase>", "", eCSphericalJointDesc.class);
		public static final PropertyDescriptor<bTPropertyObject> TwistSpring = new PropertyDescriptor<>("TwistSpring", bTPropertyObject.class, "bTPropertyObject<class eCSpringDesc,class bCObjectRefBase>", "", eCSphericalJointDesc.class);
	}

	public static interface eCSpringAndDamperEffector extends eCEffector {
		public static final PropertyDescriptor<gFloat> DamperMaxCompressForce = new PropertyDescriptor<>("DamperMaxCompressForce", gFloat.class, "float", "", eCSpringAndDamperEffector.class);
		public static final PropertyDescriptor<gFloat> DamperMaxStretchForce = new PropertyDescriptor<>("DamperMaxStretchForce", gFloat.class, "float", "", eCSpringAndDamperEffector.class);
		public static final PropertyDescriptor<gFloat> DamperVelCompressSaturate = new PropertyDescriptor<>("DamperVelCompressSaturate", gFloat.class, "float", "", eCSpringAndDamperEffector.class);
		public static final PropertyDescriptor<gFloat> DamperVelStretchSaturate = new PropertyDescriptor<>("DamperVelStretchSaturate", gFloat.class, "float", "", eCSpringAndDamperEffector.class);
		public static final PropertyDescriptor<gFloat> SpringDistCompressSaturate = new PropertyDescriptor<>("SpringDistCompressSaturate", gFloat.class, "float", "", eCSpringAndDamperEffector.class);
		public static final PropertyDescriptor<gFloat> SpringDistRelaxed = new PropertyDescriptor<>("SpringDistRelaxed", gFloat.class, "float", "", eCSpringAndDamperEffector.class);
		public static final PropertyDescriptor<gFloat> SpringDistStretchSaturate = new PropertyDescriptor<>("SpringDistStretchSaturate", gFloat.class, "float", "", eCSpringAndDamperEffector.class);
		public static final PropertyDescriptor<gFloat> SpringMaxCompressForce = new PropertyDescriptor<>("SpringMaxCompressForce", gFloat.class, "float", "", eCSpringAndDamperEffector.class);
		public static final PropertyDescriptor<gFloat> SpringMaxStretchForce = new PropertyDescriptor<>("SpringMaxStretchForce", gFloat.class, "float", "", eCSpringAndDamperEffector.class);
	}

	public static interface eCSpringDesc extends ClassDescriptor {
		public static final PropertyDescriptor<gFloat> Damper = new PropertyDescriptor<>("Damper", gFloat.class, "float", "", eCSpringDesc.class);
		public static final PropertyDescriptor<gFloat> Spring = new PropertyDescriptor<>("Spring", gFloat.class, "float", "", eCSpringDesc.class);
		public static final PropertyDescriptor<gFloat> TargetValue = new PropertyDescriptor<>("TargetValue", gFloat.class, "float", "", eCSpringDesc.class);
	}

	public static interface eCStaticLight_PS extends eCEntityPropertySet {
	}

	public static interface eCStaticPointLight_PS extends eCStaticLight_PS {
		public static final PropertyDescriptor<gBool> CastShadows = new PropertyDescriptor<>("CastShadows", gBool.class, "bool", "", eCStaticPointLight_PS.class);
		public static final PropertyDescriptor<bCFloatColor> Color = new PropertyDescriptor<>("Color", bCFloatColor.class, "bCFloatColor", "", eCStaticPointLight_PS.class);
		public static final PropertyDescriptor<bCVector> Offset = new PropertyDescriptor<>("Offset", bCVector.class, "bCVector", "", eCStaticPointLight_PS.class);
		public static final PropertyDescriptor<gFloat> Range = new PropertyDescriptor<>("Range", gFloat.class, "float", "", eCStaticPointLight_PS.class);
	}

	public static interface eCStaticSpotLight_PS extends eCStaticLight_PS {
		public static final PropertyDescriptor<gBool> CastShadows = new PropertyDescriptor<>("CastShadows", gBool.class, "bool", "", eCStaticSpotLight_PS.class);
		public static final PropertyDescriptor<bCFloatColor> Color = new PropertyDescriptor<>("Color", bCFloatColor.class, "bCFloatColor", "", eCStaticSpotLight_PS.class);
		public static final PropertyDescriptor<gFloat> ConeFallOff = new PropertyDescriptor<>("ConeFallOff", gFloat.class, "float", "", eCStaticSpotLight_PS.class);
		public static final PropertyDescriptor<gFloat> InnerAngle = new PropertyDescriptor<>("InnerAngle", gFloat.class, "float", "", eCStaticSpotLight_PS.class);
		public static final PropertyDescriptor<gFloat> OuterAngle = new PropertyDescriptor<>("OuterAngle", gFloat.class, "float", "", eCStaticSpotLight_PS.class);
		public static final PropertyDescriptor<gFloat> Range = new PropertyDescriptor<>("Range", gFloat.class, "float", "", eCStaticSpotLight_PS.class);
	}

	public static interface eCStrip_PS extends eCEntityPropertySet {
		public static final PropertyDescriptor<gFloat> EndAlpha = new PropertyDescriptor<>("EndAlpha", gFloat.class, "float", "Color", eCStrip_PS.class);
		public static final PropertyDescriptor<gFloat> StartAlpha = new PropertyDescriptor<>("StartAlpha", gFloat.class, "float", "Color", eCStrip_PS.class);
		public static final PropertyDescriptor<gBool> Enabled = new PropertyDescriptor<>("Enabled", gBool.class, "bool", "Rendering", eCStrip_PS.class);
		public static final PropertyDescriptor<bCFloatColor> EndColor = new PropertyDescriptor<>("EndColor", bCFloatColor.class, "bCFloatColor", "Color", eCStrip_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<eEFinalBlend>> FinalBlend = new PropertyDescriptor<>("FinalBlend", bTPropertyContainer.class, "bTPropertyContainer<enum eEFinalBlend>", "Rendering", eCStrip_PS.class);
		public static final PropertyDescriptor<gInt> MaxSegmentCount = new PropertyDescriptor<>("MaxSegmentCount", gInt.class, "int", "Spawning", eCStrip_PS.class);
		public static final PropertyDescriptor<bCVector> OffsetLeft = new PropertyDescriptor<>("OffsetLeft", bCVector.class, "bCVector", "Location", eCStrip_PS.class);
		public static final PropertyDescriptor<bCVector> OffsetRight = new PropertyDescriptor<>("OffsetRight", bCVector.class, "bCVector", "Location", eCStrip_PS.class);
		public static final PropertyDescriptor<gFloat> SegmentLifeTime = new PropertyDescriptor<>("SegmentLifeTime", gFloat.class, "float", "Spawning", eCStrip_PS.class);
		public static final PropertyDescriptor<gFloat> SegmentsPerSecond = new PropertyDescriptor<>("SegmentsPerSecond", gFloat.class, "float", "Spawning", eCStrip_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<eEStripSpawning>> SpawnMode = new PropertyDescriptor<>("SpawnMode", bTPropertyContainer.class, "bTPropertyContainer<enum eEStripSpawning>", "Spawning", eCStrip_PS.class);
		public static final PropertyDescriptor<bCFloatColor> StartColor = new PropertyDescriptor<>("StartColor", bCFloatColor.class, "bCFloatColor", "Color", eCStrip_PS.class);
		public static final PropertyDescriptor<bCString> Texture = new PropertyDescriptor<>("Texture", bCString.class, "bCString", "Texture", eCStrip_PS.class);
	}

	public static interface eCTexCoordSrcBase extends eCShaderEllementBase {
	}

	public static interface eCTexCoordSrcBumpOffset extends eCTexCoordSrcBase {
		public static final PropertyDescriptor<gFloat> OffsetAmount = new PropertyDescriptor<>("OffsetAmount", gFloat.class, "float", "", eCTexCoordSrcBumpOffset.class);
	}

	public static interface eCTexCoordSrcOscillator extends eCTexCoordSrcBase {
		public static final PropertyDescriptor<bCVector2> Amplitude = new PropertyDescriptor<>("Amplitude", bCVector2.class, "bCVector2", "", eCTexCoordSrcOscillator.class);
		public static final PropertyDescriptor<bCVector2> Offset = new PropertyDescriptor<>("Offset", bCVector2.class, "bCVector2", "", eCTexCoordSrcOscillator.class);
		public static final PropertyDescriptor<bTPropertyContainer<eETexCoordSrcOscillatorType>> OscillatorTypeU = new PropertyDescriptor<>("OscillatorTypeU", bTPropertyContainer.class, "bTPropertyContainer<enum eETexCoordSrcOscillatorType>", "", eCTexCoordSrcOscillator.class);
		public static final PropertyDescriptor<bTPropertyContainer<eETexCoordSrcOscillatorType>> OscillatorTypeV = new PropertyDescriptor<>("OscillatorTypeV", bTPropertyContainer.class, "bTPropertyContainer<enum eETexCoordSrcOscillatorType>", "", eCTexCoordSrcOscillator.class);
		public static final PropertyDescriptor<bCVector2> Phase = new PropertyDescriptor<>("Phase", bCVector2.class, "bCVector2", "", eCTexCoordSrcOscillator.class);
		public static final PropertyDescriptor<bCVector2> Rate = new PropertyDescriptor<>("Rate", bCVector2.class, "bCVector2", "", eCTexCoordSrcOscillator.class);
	}

	public static interface eCTexCoordSrcRotator extends eCTexCoordSrcBase {
		public static final PropertyDescriptor<bCEulerAngles> Amplitude = new PropertyDescriptor<>("Amplitude", bCEulerAngles.class, "bCEulerAngles", "Oscillate", eCTexCoordSrcRotator.class);
		public static final PropertyDescriptor<bCEulerAngles> Angle = new PropertyDescriptor<>("Angle", bCEulerAngles.class, "bCEulerAngles", "", eCTexCoordSrcRotator.class);
		public static final PropertyDescriptor<bCVector2> Offset = new PropertyDescriptor<>("Offset", bCVector2.class, "bCVector2", "", eCTexCoordSrcRotator.class);
		public static final PropertyDescriptor<bCEulerAngles> Phase = new PropertyDescriptor<>("Phase", bCEulerAngles.class, "bCEulerAngles", "Oscillate", eCTexCoordSrcRotator.class);
		public static final PropertyDescriptor<bCEulerAngles> Rate = new PropertyDescriptor<>("Rate", bCEulerAngles.class, "bCEulerAngles", "Oscillate", eCTexCoordSrcRotator.class);
		public static final PropertyDescriptor<bTPropertyContainer<eETexCoordSrcRotatorType>> RotationType = new PropertyDescriptor<>("RotationType", bTPropertyContainer.class, "bTPropertyContainer<enum eETexCoordSrcRotatorType>", "", eCTexCoordSrcRotator.class);
	}

	public static interface eCTexCoordSrcScale extends eCTexCoordSrcBase {
		public static final PropertyDescriptor<bCVector2> Scale = new PropertyDescriptor<>("Scale", bCVector2.class, "bCVector2", "", eCTexCoordSrcScale.class);
	}

	public static interface eCTexCoordSrcScroller extends eCTexCoordSrcBase {
		public static final PropertyDescriptor<bCVector2> Direction = new PropertyDescriptor<>("Direction", bCVector2.class, "bCVector2", "", eCTexCoordSrcScroller.class);
		public static final PropertyDescriptor<bCVector2> Offset = new PropertyDescriptor<>("Offset", bCVector2.class, "bCVector2", "", eCTexCoordSrcScroller.class);
		public static final PropertyDescriptor<gFloat> Rate = new PropertyDescriptor<>("Rate", gFloat.class, "float", "", eCTexCoordSrcScroller.class);
	}

	public static interface eCTriggerBase_PS extends eCEntityPropertySet {
		public static final PropertyDescriptor<bCString> TargetEntity = new PropertyDescriptor<>("TargetEntity", bCString.class, "bCString", "", eCTriggerBase_PS.class);
	}

	public static interface eCTriggerCombiner_PS extends eCTriggerBase_PS {
		public static final PropertyDescriptor<bCString> FailureTriggerTarget = new PropertyDescriptor<>("FailureTriggerTarget", bCString.class, "bCString", "", eCTriggerCombiner_PS.class);
		public static final PropertyDescriptor<gBool> FirstFalseIsFailure = new PropertyDescriptor<>("FirstFalseIsFailure", gBool.class, "bool", "", eCTriggerCombiner_PS.class);
		public static final PropertyDescriptor<gBool> OrderIsRelevant = new PropertyDescriptor<>("OrderIsRelevant", gBool.class, "bool", "", eCTriggerCombiner_PS.class);
		public static final PropertyDescriptor<bCString> SlaveEntityName1 = new PropertyDescriptor<>("SlaveEntityName1", bCString.class, "bCString", "", eCTriggerCombiner_PS.class);
		public static final PropertyDescriptor<bCString> SlaveEntityName10 = new PropertyDescriptor<>("SlaveEntityName10", bCString.class, "bCString", "", eCTriggerCombiner_PS.class);
		public static final PropertyDescriptor<bCString> SlaveEntityName2 = new PropertyDescriptor<>("SlaveEntityName2", bCString.class, "bCString", "", eCTriggerCombiner_PS.class);
		public static final PropertyDescriptor<bCString> SlaveEntityName3 = new PropertyDescriptor<>("SlaveEntityName3", bCString.class, "bCString", "", eCTriggerCombiner_PS.class);
		public static final PropertyDescriptor<bCString> SlaveEntityName4 = new PropertyDescriptor<>("SlaveEntityName4", bCString.class, "bCString", "", eCTriggerCombiner_PS.class);
		public static final PropertyDescriptor<bCString> SlaveEntityName5 = new PropertyDescriptor<>("SlaveEntityName5", bCString.class, "bCString", "", eCTriggerCombiner_PS.class);
		public static final PropertyDescriptor<bCString> SlaveEntityName6 = new PropertyDescriptor<>("SlaveEntityName6", bCString.class, "bCString", "", eCTriggerCombiner_PS.class);
		public static final PropertyDescriptor<bCString> SlaveEntityName7 = new PropertyDescriptor<>("SlaveEntityName7", bCString.class, "bCString", "", eCTriggerCombiner_PS.class);
		public static final PropertyDescriptor<bCString> SlaveEntityName8 = new PropertyDescriptor<>("SlaveEntityName8", bCString.class, "bCString", "", eCTriggerCombiner_PS.class);
		public static final PropertyDescriptor<bCString> SlaveEntityName9 = new PropertyDescriptor<>("SlaveEntityName9", bCString.class, "bCString", "", eCTriggerCombiner_PS.class);
		public static final PropertyDescriptor<gBool> UntriggerCancels = new PropertyDescriptor<>("UntriggerCancels", gBool.class, "bool", "", eCTriggerCombiner_PS.class);
	}

	public static interface eCTriggerList_PS extends eCTrigger_PS {
		public static final PropertyDescriptor<gFloat> FireDelayTarget1 = new PropertyDescriptor<>("FireDelayTarget1", gFloat.class, "float", "", eCTriggerList_PS.class);
		public static final PropertyDescriptor<gFloat> FireDelayTarget10 = new PropertyDescriptor<>("FireDelayTarget10", gFloat.class, "float", "", eCTriggerList_PS.class);
		public static final PropertyDescriptor<gFloat> FireDelayTarget2 = new PropertyDescriptor<>("FireDelayTarget2", gFloat.class, "float", "", eCTriggerList_PS.class);
		public static final PropertyDescriptor<gFloat> FireDelayTarget3 = new PropertyDescriptor<>("FireDelayTarget3", gFloat.class, "float", "", eCTriggerList_PS.class);
		public static final PropertyDescriptor<gFloat> FireDelayTarget4 = new PropertyDescriptor<>("FireDelayTarget4", gFloat.class, "float", "", eCTriggerList_PS.class);
		public static final PropertyDescriptor<gFloat> FireDelayTarget5 = new PropertyDescriptor<>("FireDelayTarget5", gFloat.class, "float", "", eCTriggerList_PS.class);
		public static final PropertyDescriptor<gFloat> FireDelayTarget6 = new PropertyDescriptor<>("FireDelayTarget6", gFloat.class, "float", "", eCTriggerList_PS.class);
		public static final PropertyDescriptor<gFloat> FireDelayTarget7 = new PropertyDescriptor<>("FireDelayTarget7", gFloat.class, "float", "", eCTriggerList_PS.class);
		public static final PropertyDescriptor<gFloat> FireDelayTarget8 = new PropertyDescriptor<>("FireDelayTarget8", gFloat.class, "float", "", eCTriggerList_PS.class);
		public static final PropertyDescriptor<gFloat> FireDelayTarget9 = new PropertyDescriptor<>("FireDelayTarget9", gFloat.class, "float", "", eCTriggerList_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<eETriggerListProcessType>> ProcessType = new PropertyDescriptor<>("ProcessType", bTPropertyContainer.class, "bTPropertyContainer<enum eETriggerListProcessType>", "", eCTriggerList_PS.class);
		public static final PropertyDescriptor<bCString> TriggerTarget1 = new PropertyDescriptor<>("TriggerTarget1", bCString.class, "bCString", "", eCTriggerList_PS.class);
		public static final PropertyDescriptor<bCString> TriggerTarget10 = new PropertyDescriptor<>("TriggerTarget10", bCString.class, "bCString", "", eCTriggerList_PS.class);
		public static final PropertyDescriptor<bCString> TriggerTarget2 = new PropertyDescriptor<>("TriggerTarget2", bCString.class, "bCString", "", eCTriggerList_PS.class);
		public static final PropertyDescriptor<bCString> TriggerTarget3 = new PropertyDescriptor<>("TriggerTarget3", bCString.class, "bCString", "", eCTriggerList_PS.class);
		public static final PropertyDescriptor<bCString> TriggerTarget4 = new PropertyDescriptor<>("TriggerTarget4", bCString.class, "bCString", "", eCTriggerList_PS.class);
		public static final PropertyDescriptor<bCString> TriggerTarget5 = new PropertyDescriptor<>("TriggerTarget5", bCString.class, "bCString", "", eCTriggerList_PS.class);
		public static final PropertyDescriptor<bCString> TriggerTarget6 = new PropertyDescriptor<>("TriggerTarget6", bCString.class, "bCString", "", eCTriggerList_PS.class);
		public static final PropertyDescriptor<bCString> TriggerTarget7 = new PropertyDescriptor<>("TriggerTarget7", bCString.class, "bCString", "", eCTriggerList_PS.class);
		public static final PropertyDescriptor<bCString> TriggerTarget8 = new PropertyDescriptor<>("TriggerTarget8", bCString.class, "bCString", "", eCTriggerList_PS.class);
		public static final PropertyDescriptor<bCString> TriggerTarget9 = new PropertyDescriptor<>("TriggerTarget9", bCString.class, "bCString", "", eCTriggerList_PS.class);
	}

	public static interface eCTrigger_PS extends eCTriggerBase_PS {
		public static final PropertyDescriptor<gFloat> AutoUntriggerAfterSec = new PropertyDescriptor<>("AutoUntriggerAfterSec", gFloat.class, "float", "Timer Features", eCTrigger_PS.class);
		public static final PropertyDescriptor<gInt> DamageTreshold = new PropertyDescriptor<>("DamageTreshold", gInt.class, "int", "Misc", eCTrigger_PS.class);
		public static final PropertyDescriptor<bTObjArray_eCEntityProxy> EntitiesVisited = new PropertyDescriptor<>("EntitiesVisited", bTObjArray_eCEntityProxy.class, "bTObjArray<class eCEntityProxy>", "Internal", eCTrigger_PS.class);
		public static final PropertyDescriptor<bTValArray_unsigned_short> EntitiesVisitedCount = new PropertyDescriptor<>("EntitiesVisitedCount", bTValArray_unsigned_short.class, "bTValArray<unsigned short>", "Internal", eCTrigger_PS.class);
		public static final PropertyDescriptor<gFloat> FireDelaySec = new PropertyDescriptor<>("FireDelaySec", gFloat.class, "float", "Timer Features", eCTrigger_PS.class);
		public static final PropertyDescriptor<eCEntityProxy> InflictorEntity = new PropertyDescriptor<>("InflictorEntity", eCEntityProxy.class, "eCEntityProxy", "Script", eCTrigger_PS.class);
		public static final PropertyDescriptor<gBool> IsEnabled = new PropertyDescriptor<>("IsEnabled", gBool.class, "bool", "Basic", eCTrigger_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<eEEventType>> LastEventType = new PropertyDescriptor<>("LastEventType", bTPropertyContainer.class, "bTPropertyContainer<enum eEEventType>", "Internal", eCTrigger_PS.class);
		public static final PropertyDescriptor<gUnsignedShort> MaxActivationCount = new PropertyDescriptor<>("MaxActivationCount", gUnsignedShort.class, "unsigned short", "Basic", eCTrigger_PS.class);
		public static final PropertyDescriptor<gUnsignedShort> MaxCountPerEntity = new PropertyDescriptor<>("MaxCountPerEntity", gUnsignedShort.class, "unsigned short", "Basic", eCTrigger_PS.class);
		public static final PropertyDescriptor<eCEntityProxy> OtherEntity = new PropertyDescriptor<>("OtherEntity", eCEntityProxy.class, "eCEntityProxy", "Script", eCTrigger_PS.class);
		public static final PropertyDescriptor<gBool> ReactToCacheInRange = new PropertyDescriptor<>("ReactToCacheInRange", gBool.class, "bool", "Reaction Filter", eCTrigger_PS.class);
		public static final PropertyDescriptor<gBool> ReactToDamage = new PropertyDescriptor<>("ReactToDamage", gBool.class, "bool", "Reaction Filter", eCTrigger_PS.class);
		public static final PropertyDescriptor<gBool> ReactToIntersect = new PropertyDescriptor<>("ReactToIntersect", gBool.class, "bool", "Reaction Filter", eCTrigger_PS.class);
		public static final PropertyDescriptor<gBool> ReactToLoad = new PropertyDescriptor<>("ReactToLoad", gBool.class, "bool", "Reaction Filter", eCTrigger_PS.class);
		public static final PropertyDescriptor<gBool> ReactToProcessingRange = new PropertyDescriptor<>("ReactToProcessingRange", gBool.class, "bool", "Reaction Filter", eCTrigger_PS.class);
		public static final PropertyDescriptor<gBool> ReactToStart = new PropertyDescriptor<>("ReactToStart", gBool.class, "bool", "Reaction Filter", eCTrigger_PS.class);
		public static final PropertyDescriptor<gBool> ReactToTouch = new PropertyDescriptor<>("ReactToTouch", gBool.class, "bool", "Reaction Filter", eCTrigger_PS.class);
		public static final PropertyDescriptor<gBool> ReactToTrigger = new PropertyDescriptor<>("ReactToTrigger", gBool.class, "bool", "Reaction Filter", eCTrigger_PS.class);
		public static final PropertyDescriptor<gUnsignedShort> RecognizesCollisionGroup = new PropertyDescriptor<>("RecognizesCollisionGroup", gUnsignedShort.class, "unsigned short", "Recognition Filter", eCTrigger_PS.class);
		public static final PropertyDescriptor<bCString> RecognizesEntityName = new PropertyDescriptor<>("RecognizesEntityName", bCString.class, "bCString", "Recognition Filter", eCTrigger_PS.class);
		public static final PropertyDescriptor<bCString> RecognizesEvent = new PropertyDescriptor<>("RecognizesEvent", bCString.class, "bCString", "Recognition Filter", eCTrigger_PS.class);
		public static final PropertyDescriptor<gBool> RecognizesNPCs = new PropertyDescriptor<>("RecognizesNPCs", gBool.class, "bool", "Recognition Filter", eCTrigger_PS.class);
		public static final PropertyDescriptor<gBool> RecognizesObjects = new PropertyDescriptor<>("RecognizesObjects", gBool.class, "bool", "Recognition Filter", eCTrigger_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<eEPropertySetType>> RecognizesPSType = new PropertyDescriptor<>("RecognizesPSType", bTPropertyContainer.class, "bTPropertyContainer<enum eEPropertySetType>", "Recognition Filter", eCTrigger_PS.class);
		public static final PropertyDescriptor<gBool> RecognizesPlayers = new PropertyDescriptor<>("RecognizesPlayers", gBool.class, "bool", "Recognition Filter", eCTrigger_PS.class);
		public static final PropertyDescriptor<gUnsignedShort> RecognizesShapeGroup = new PropertyDescriptor<>("RecognizesShapeGroup", gUnsignedShort.class, "unsigned short", "Recognition Filter", eCTrigger_PS.class);
		public static final PropertyDescriptor<gFloat> RetriggerWaitSec = new PropertyDescriptor<>("RetriggerWaitSec", gFloat.class, "float", "Timer Features", eCTrigger_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<eEEventType>> RunningEventType = new PropertyDescriptor<>("RunningEventType", bTPropertyContainer.class, "bTPropertyContainer<enum eEEventType>", "Script", eCTrigger_PS.class);
		public static final PropertyDescriptor<gBool> SendUntrigger = new PropertyDescriptor<>("SendUntrigger", gBool.class, "bool", "Basic", eCTrigger_PS.class);
		public static final PropertyDescriptor<bCString> TouchType = new PropertyDescriptor<>("TouchType", bCString.class, "bCString", "Script", eCTrigger_PS.class);
		public static final PropertyDescriptor<gFloat> VelocityTresholdMSec = new PropertyDescriptor<>("VelocityTresholdMSec", gFloat.class, "float", "Misc", eCTrigger_PS.class);
	}

	public static interface eCVegetationBrush_PS extends eCEntityPropertySet {
		public static final PropertyDescriptor<gBool> AlignRotation = new PropertyDescriptor<>("AlignRotation", gBool.class, "bool", "", eCVegetationBrush_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<eEVegetationBrushMode>> Mode = new PropertyDescriptor<>("Mode", bTPropertyContainer.class, "bTPropertyContainer<enum eEVegetationBrushMode>", "", eCVegetationBrush_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<eEVegetationBrushPlace>> Placement = new PropertyDescriptor<>("Placement", bTPropertyContainer.class, "bTPropertyContainer<enum eEVegetationBrushPlace>", "", eCVegetationBrush_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<eEVegetationBrushShape>> Shape = new PropertyDescriptor<>("Shape", bTPropertyContainer.class, "bTPropertyContainer<enum eEVegetationBrushShape>", "", eCVegetationBrush_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<eEVegetationBrushColorFunction>> ColorFunction = new PropertyDescriptor<>("ColorFunction", bTPropertyContainer.class, "bTPropertyContainer<enum eEVegetationBrushColorFunction>", "", eCVegetationBrush_PS.class);
		public static final PropertyDescriptor<bCVector2> ColorNoiseModify = new PropertyDescriptor<>("ColorNoiseModify", bCVector2.class, "bCVector2", "", eCVegetationBrush_PS.class);
		public static final PropertyDescriptor<gInt> ColorNoiseOctaves = new PropertyDescriptor<>("ColorNoiseOctaves", gInt.class, "int", "", eCVegetationBrush_PS.class);
		public static final PropertyDescriptor<gFloat> ColorNoiseScale = new PropertyDescriptor<>("ColorNoiseScale", gFloat.class, "float", "", eCVegetationBrush_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<bENoiseTurbulence>> ColorNoiseTurbulence = new PropertyDescriptor<>("ColorNoiseTurbulence", bTPropertyContainer.class, "bTPropertyContainer<enum bENoiseTurbulence>", "", eCVegetationBrush_PS.class);
		public static final PropertyDescriptor<gFloat> Density = new PropertyDescriptor<>("Density", gFloat.class, "float", "", eCVegetationBrush_PS.class);
		public static final PropertyDescriptor<bCVector2> MaxScaling = new PropertyDescriptor<>("MaxScaling", bCVector2.class, "bCVector2", "", eCVegetationBrush_PS.class);
		public static final PropertyDescriptor<bCVector2> MinScaling = new PropertyDescriptor<>("MinScaling", bCVector2.class, "bCVector2", "", eCVegetationBrush_PS.class);
		public static final PropertyDescriptor<bCFloatColor> ColorPepper = new PropertyDescriptor<>("ColorPepper", bCFloatColor.class, "bCFloatColor", "", eCVegetationBrush_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<eEVegetationBrushProbabilityFunction>> ProbabilityFunction = new PropertyDescriptor<>("ProbabilityFunction", bTPropertyContainer.class, "bTPropertyContainer<enum eEVegetationBrushProbabilityFunction>", "", eCVegetationBrush_PS.class);
		public static final PropertyDescriptor<bCVector2> ProbabilityNoiseModify = new PropertyDescriptor<>("ProbabilityNoiseModify", bCVector2.class, "bCVector2", "", eCVegetationBrush_PS.class);
		public static final PropertyDescriptor<gInt> ProbabilityNoiseOctaves = new PropertyDescriptor<>("ProbabilityNoiseOctaves", gInt.class, "int", "", eCVegetationBrush_PS.class);
		public static final PropertyDescriptor<gFloat> ProbabilityNoiseScale = new PropertyDescriptor<>("ProbabilityNoiseScale", gFloat.class, "float", "", eCVegetationBrush_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<bENoiseTurbulence>> ProbabilityNoiseTurbulence = new PropertyDescriptor<>("ProbabilityNoiseTurbulence", bTPropertyContainer.class, "bTPropertyContainer<enum bENoiseTurbulence>", "", eCVegetationBrush_PS.class);
		public static final PropertyDescriptor<gFloat> RandomRotation = new PropertyDescriptor<>("RandomRotation", gFloat.class, "float", "", eCVegetationBrush_PS.class);
		public static final PropertyDescriptor<bCFloatColor> ColorSalt = new PropertyDescriptor<>("ColorSalt", bCFloatColor.class, "bCFloatColor", "", eCVegetationBrush_PS.class);
		public static final PropertyDescriptor<gFloat> Size = new PropertyDescriptor<>("Size", gFloat.class, "float", "", eCVegetationBrush_PS.class);
		public static final PropertyDescriptor<gBool> UniformDistribution = new PropertyDescriptor<>("UniformDistribution", gBool.class, "bool", "", eCVegetationBrush_PS.class);
		public static final PropertyDescriptor<gFloat> Variation = new PropertyDescriptor<>("Variation", gFloat.class, "float", "", eCVegetationBrush_PS.class);
	}

	public static interface eCVegetationRubber_PS extends eCEntityPropertySet {
		public static final PropertyDescriptor<gFloat> Radius = new PropertyDescriptor<>("Radius", gFloat.class, "float", "", eCVegetationRubber_PS.class);
	}

	public static interface eCVegetation_Mesh extends ClassDescriptor {
		public static final PropertyDescriptor<gBool> DoubleSided = new PropertyDescriptor<>("DoubleSided", gBool.class, "bool", "", eCVegetation_Mesh.class);
		public static final PropertyDescriptor<bCString> MeshFilePath = new PropertyDescriptor<>("MeshFilePath", bCString.class, "bCMeshResourceString", "", eCVegetation_Mesh.class);
		public static final PropertyDescriptor<bTPropertyContainer<eEVegetationMeshShading>> MeshShading = new PropertyDescriptor<>("MeshShading", bTPropertyContainer.class, "bTPropertyContainer<enum eEVegetationMeshShading>", "", eCVegetation_Mesh.class);
		public static final PropertyDescriptor<gFloat> MinSpacing = new PropertyDescriptor<>("MinSpacing", gFloat.class, "float", "", eCVegetation_Mesh.class);
		public static final PropertyDescriptor<gFloat> WindStrength = new PropertyDescriptor<>("WindStrength", gFloat.class, "float", "", eCVegetation_Mesh.class);
	}

	public static interface eCVegetation_PS extends eCEntityPropertySet {
		public static final PropertyDescriptor<gFloat> FadeOutStart = new PropertyDescriptor<>("FadeOutStart", gFloat.class, "float", "", eCVegetation_PS.class);
		public static final PropertyDescriptor<gFloat> GridNodeSize = new PropertyDescriptor<>("GridNodeSize", gFloat.class, "float", "", eCVegetation_PS.class);
		public static final PropertyDescriptor<gBool> UseDefaultViewRange = new PropertyDescriptor<>("UseDefaultViewRange", gBool.class, "bool", "", eCVegetation_PS.class);
		public static final PropertyDescriptor<gBool> UseQuality = new PropertyDescriptor<>("UseQuality", gBool.class, "bool", "", eCVegetation_PS.class);
		public static final PropertyDescriptor<gFloat> ViewRange = new PropertyDescriptor<>("ViewRange", gFloat.class, "float", "", eCVegetation_PS.class);
	}

	public static interface eCVisualAnimation_PS extends eCEntityPropertySet {
		public static final PropertyDescriptor<gFloat> BoneAngularDamping = new PropertyDescriptor<>("BoneAngularDamping", gFloat.class, "float", "", eCVisualAnimation_PS.class);
		public static final PropertyDescriptor<gFloat> BoneJointSpring = new PropertyDescriptor<>("BoneJointSpring", gFloat.class, "float", "", eCVisualAnimation_PS.class);
		public static final PropertyDescriptor<gFloat> BoneJointSpringDamping = new PropertyDescriptor<>("BoneJointSpringDamping", gFloat.class, "float", "", eCVisualAnimation_PS.class);
		public static final PropertyDescriptor<gBool> BoneJointSpringEnabled = new PropertyDescriptor<>("BoneJointSpringEnabled", gBool.class, "bool", "", eCVisualAnimation_PS.class);
		public static final PropertyDescriptor<gFloat> BoneLinearDamping = new PropertyDescriptor<>("BoneLinearDamping", gFloat.class, "float", "", eCVisualAnimation_PS.class);
		public static final PropertyDescriptor<gFloat> BoneProjectionDistance = new PropertyDescriptor<>("BoneProjectionDistance", gFloat.class, "float", "", eCVisualAnimation_PS.class);
		public static final PropertyDescriptor<gBool> BoneRaycastsEnabled = new PropertyDescriptor<>("BoneRaycastsEnabled", gBool.class, "bool", "", eCVisualAnimation_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<eEShapeGroup>> BoneShapeGroup = new PropertyDescriptor<>("BoneShapeGroup", bTPropertyContainer.class, "bTPropertyContainer<enum eEShapeGroup>", "", eCVisualAnimation_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<eEShapeMaterial>> BoneShapeMaterial = new PropertyDescriptor<>("BoneShapeMaterial", bTPropertyContainer.class, "bTPropertyContainer<enum eEShapeMaterial>", "", eCVisualAnimation_PS.class);
		public static final PropertyDescriptor<gFloat> BoneSwingSpring = new PropertyDescriptor<>("BoneSwingSpring", gFloat.class, "float", "", eCVisualAnimation_PS.class);
		public static final PropertyDescriptor<gFloat> BoneSwingSpringDamping = new PropertyDescriptor<>("BoneSwingSpringDamping", gFloat.class, "float", "", eCVisualAnimation_PS.class);
		public static final PropertyDescriptor<gBool> BoneSwingSpringEnabled = new PropertyDescriptor<>("BoneSwingSpringEnabled", gBool.class, "bool", "", eCVisualAnimation_PS.class);
		public static final PropertyDescriptor<gFloat> BoneTwistSpring = new PropertyDescriptor<>("BoneTwistSpring", gFloat.class, "float", "", eCVisualAnimation_PS.class);
		public static final PropertyDescriptor<gFloat> BoneTwistSpringDamping = new PropertyDescriptor<>("BoneTwistSpringDamping", gFloat.class, "float", "", eCVisualAnimation_PS.class);
		public static final PropertyDescriptor<gBool> BoneTwistSpringEnabled = new PropertyDescriptor<>("BoneTwistSpringEnabled", gBool.class, "bool", "", eCVisualAnimation_PS.class);
		public static final PropertyDescriptor<gBool> BonesAreBreakable = new PropertyDescriptor<>("BonesAreBreakable", gBool.class, "bool", "", eCVisualAnimation_PS.class);
		public static final PropertyDescriptor<gFloat> BonesBreakMaxForce = new PropertyDescriptor<>("BonesBreakMaxForce", gFloat.class, "float", "", eCVisualAnimation_PS.class);
		public static final PropertyDescriptor<gFloat> BonesBreakMaxTorque = new PropertyDescriptor<>("BonesBreakMaxTorque", gFloat.class, "float", "", eCVisualAnimation_PS.class);
		public static final PropertyDescriptor<bTPropertyObject> ClothEffector = new PropertyDescriptor<>("ClothEffector", bTPropertyObject.class, "bTPropertyObject<class eCSpringAndDamperEffector,class eCEffector>", "", eCVisualAnimation_PS.class);
		public static final PropertyDescriptor<gBool> ConstrainRootToWorld = new PropertyDescriptor<>("ConstrainRootToWorld", gBool.class, "bool", "", eCVisualAnimation_PS.class);
		public static final PropertyDescriptor<gBool> CreateSpringMassCloth = new PropertyDescriptor<>("CreateSpringMassCloth", gBool.class, "bool", "", eCVisualAnimation_PS.class);
		public static final PropertyDescriptor<gBool> DisablePhysicLodding = new PropertyDescriptor<>("DisablePhysicLodding", gBool.class, "bool", "", eCVisualAnimation_PS.class);
		public static final PropertyDescriptor<bCString> FacialAnimFilePath = new PropertyDescriptor<>("FacialAnimFilePath", bCString.class, "bCAnimationResourceString", "", eCVisualAnimation_PS.class);
		public static final PropertyDescriptor<gBool> InterBoneCollision = new PropertyDescriptor<>("InterBoneCollision", gBool.class, "bool", "", eCVisualAnimation_PS.class);
		public static final PropertyDescriptor<gBool> PhysicsControled = new PropertyDescriptor<>("PhysicsControled", gBool.class, "bool", "", eCVisualAnimation_PS.class);
		public static final PropertyDescriptor<gInt> MaterialSwitch = new PropertyDescriptor<>("MaterialSwitch", gInt.class, "int", "", eCVisualAnimation_PS.class);
		public static final PropertyDescriptor<bCString> ResourceFilePath = new PropertyDescriptor<>("ResourceFilePath", bCString.class, "bCAnimationResourceString", "", eCVisualAnimation_PS.class);
		public static final PropertyDescriptor<gBool> SkeletonCollisionEnabled = new PropertyDescriptor<>("SkeletonCollisionEnabled", gBool.class, "bool", "", eCVisualAnimation_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<eECollisionGroup>> SkeletonShapeGroup = new PropertyDescriptor<>("SkeletonShapeGroup", bTPropertyContainer.class, "bTPropertyContainer<enum eECollisionGroup>", "", eCVisualAnimation_PS.class);
		public static final PropertyDescriptor<gFloat> SpringParentNodesScale = new PropertyDescriptor<>("SpringParentNodesScale", gFloat.class, "float", "", eCVisualAnimation_PS.class);
		public static final PropertyDescriptor<gBool> SpringParentNodesOnly = new PropertyDescriptor<>("SpringParentNodesOnly", gBool.class, "bool", "", eCVisualAnimation_PS.class);
	}

	public static interface eCVisualMeshBase_PS extends eCEntityPropertySet {
		public static final PropertyDescriptor<bTPropertyContainer<eELightmapAmbientOcclusion>> LightmapAmbientOcclusion = new PropertyDescriptor<>("LightmapAmbientOcclusion", bTPropertyContainer.class, "bTPropertyContainer<enum eELightmapAmbientOcclusion>", "", eCVisualMeshBase_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<eELightmapType>> LightmapType = new PropertyDescriptor<>("LightmapType", bTPropertyContainer.class, "bTPropertyContainer<enum eELightmapType>", "", eCVisualMeshBase_PS.class);
		public static final PropertyDescriptor<gInt> MaterialSwitch = new PropertyDescriptor<>("MaterialSwitch", gInt.class, "int", "", eCVisualMeshBase_PS.class);
		public static final PropertyDescriptor<bCString> ResourceFileName = new PropertyDescriptor<>("ResourceFileName", bCString.class, "bCMeshResourceString", "", eCVisualMeshBase_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<eEStaticLighingType>> StaticLightingType = new PropertyDescriptor<>("StaticLightingType", bTPropertyContainer.class, "bTPropertyContainer<enum eEStaticLighingType>", "", eCVisualMeshBase_PS.class);
		public static final PropertyDescriptor<gFloat> UnitsPerLightmapTexel = new PropertyDescriptor<>("UnitsPerLightmapTexel", gFloat.class, "float", "", eCVisualMeshBase_PS.class);
	}

	public static interface eCVisualMeshDynamic_PS extends eCVisualMeshBase_PS {
		public static final PropertyDescriptor<bCString> ResourceFilePath = new PropertyDescriptor<>("ResourceFilePath", bCString.class, "bCMeshResourceString", "Old", eCVisualMeshDynamic_PS.class);
	}

	public static interface eCVisualMeshStatic_PS extends eCVisualMeshBase_PS {
		public static final PropertyDescriptor<bCString> ResourceFilePath = new PropertyDescriptor<>("ResourceFilePath", bCString.class, "bCMeshResourceString", "Old", eCVisualMeshStatic_PS.class);
	}

	public static interface eCWeatherStates extends ClassDescriptor {
		public static final PropertyDescriptor<gInt> CurrentTextureTile = new PropertyDescriptor<>("CurrentTextureTile", gInt.class, "int", "Precipitation", eCWeatherStates.class);
		public static final PropertyDescriptor<gFloat> DirectionScale = new PropertyDescriptor<>("DirectionScale", gFloat.class, "float", "Precipitation", eCWeatherStates.class);
		public static final PropertyDescriptor<gFloat> JitterPower = new PropertyDescriptor<>("JitterPower", gFloat.class, "float", "Precipitation", eCWeatherStates.class);
		public static final PropertyDescriptor<gFloat> JitterSpeed = new PropertyDescriptor<>("JitterSpeed", gFloat.class, "float", "Precipitation", eCWeatherStates.class);
		public static final PropertyDescriptor<bCString> LoopSample = new PropertyDescriptor<>("LoopSample", bCString.class, "bCString", "Audio", eCWeatherStates.class);
		public static final PropertyDescriptor<gFloat> LoopVolume = new PropertyDescriptor<>("LoopVolume", gFloat.class, "float", "Audio", eCWeatherStates.class);
		public static final PropertyDescriptor<gInt> ParticlesPerSecond = new PropertyDescriptor<>("ParticlesPerSecond", gInt.class, "int", "Precipitation", eCWeatherStates.class);
		public static final PropertyDescriptor<bCVector2> Size = new PropertyDescriptor<>("Size", bCVector2.class, "bCVector2", "Precipitation", eCWeatherStates.class);
		public static final PropertyDescriptor<gFloat> SpawnPointDizzer = new PropertyDescriptor<>("SpawnPointDizzer", gFloat.class, "float", "Precipitation", eCWeatherStates.class);
		public static final PropertyDescriptor<bCVector2> SpeedMinMax = new PropertyDescriptor<>("SpeedMinMax", bCVector2.class, "bCVector2", "Precipitation", eCWeatherStates.class);
		public static final PropertyDescriptor<gFloat> SplashDuration = new PropertyDescriptor<>("SplashDuration", gFloat.class, "float", "Precipitation", eCWeatherStates.class);
		public static final PropertyDescriptor<bCVector2> SplashSizeScale = new PropertyDescriptor<>("SplashSizeScale", bCVector2.class, "bCVector2", "Precipitation", eCWeatherStates.class);
		public static final PropertyDescriptor<gInt> SplashTextureTile = new PropertyDescriptor<>("SplashTextureTile", gInt.class, "int", "Precipitation", eCWeatherStates.class);
	}

	public static interface eCWeatherZone_PS extends eCEntityPropertySet {
		public static final PropertyDescriptor<bCFloatColor> AmbientBackLightColor = new PropertyDescriptor<>("AmbientBackLightColor", bCFloatColor.class, "bCFloatColor", "Ambient", eCWeatherZone_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<eEWeatherZoneOverwrite>> AmbientBackLightOverwrite = new PropertyDescriptor<>("AmbientBackLightOverwrite", bTPropertyContainer.class, "bTPropertyContainer<enum eEWeatherZoneOverwrite>", "Ambient", eCWeatherZone_PS.class);
		public static final PropertyDescriptor<bCFloatColor> AmbientGeneralColor = new PropertyDescriptor<>("AmbientGeneralColor", bCFloatColor.class, "bCFloatColor", "Ambient", eCWeatherZone_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<eEWeatherZoneOverwrite>> AmbientGeneralOverwrite = new PropertyDescriptor<>("AmbientGeneralOverwrite", bTPropertyContainer.class, "bTPropertyContainer<enum eEWeatherZoneOverwrite>", "Ambient", eCWeatherZone_PS.class);
		public static final PropertyDescriptor<gFloat> AmbientIntensity = new PropertyDescriptor<>("AmbientIntensity", gFloat.class, "float", "Ambient", eCWeatherZone_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<eEWeatherZoneOverwrite>> AmbientIntensityOverwrite = new PropertyDescriptor<>("AmbientIntensityOverwrite", bTPropertyContainer.class, "bTPropertyContainer<enum eEWeatherZoneOverwrite>", "Ambient", eCWeatherZone_PS.class);
		public static final PropertyDescriptor<bCFloatColor> CloudColor = new PropertyDescriptor<>("CloudColor", bCFloatColor.class, "bCFloatColor", "Sky", eCWeatherZone_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<eEWeatherZoneOverwrite>> CloudColorOverwrite = new PropertyDescriptor<>("CloudColorOverwrite", bTPropertyContainer.class, "bTPropertyContainer<enum eEWeatherZoneOverwrite>", "Sky", eCWeatherZone_PS.class);
		public static final PropertyDescriptor<gFloat> CloudThickness = new PropertyDescriptor<>("CloudThickness", gFloat.class, "float", "Sky", eCWeatherZone_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<eEWeatherZoneOverwrite>> CloudThicknessrOverwrite = new PropertyDescriptor<>("CloudThicknessrOverwrite", bTPropertyContainer.class, "bTPropertyContainer<enum eEWeatherZoneOverwrite>", "Sky", eCWeatherZone_PS.class);
		public static final PropertyDescriptor<bCFloatColor> FogColor = new PropertyDescriptor<>("FogColor", bCFloatColor.class, "bCFloatColor", "Fog", eCWeatherZone_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<eEWeatherZoneOverwrite>> FogColorOverwrite = new PropertyDescriptor<>("FogColorOverwrite", bTPropertyContainer.class, "bTPropertyContainer<enum eEWeatherZoneOverwrite>", "Fog", eCWeatherZone_PS.class);
		public static final PropertyDescriptor<gFloat> FogEnd = new PropertyDescriptor<>("FogEnd", gFloat.class, "float", "Fog", eCWeatherZone_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<eEWeatherZoneOverwrite>> FogEndrOverwrite = new PropertyDescriptor<>("FogEndrOverwrite", bTPropertyContainer.class, "bTPropertyContainer<enum eEWeatherZoneOverwrite>", "Fog", eCWeatherZone_PS.class);
		public static final PropertyDescriptor<gFloat> FogStart = new PropertyDescriptor<>("FogStart", gFloat.class, "float", "Fog", eCWeatherZone_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<eEWeatherZoneOverwrite>> FogStartrOverwrite = new PropertyDescriptor<>("FogStartrOverwrite", bTPropertyContainer.class, "bTPropertyContainer<enum eEWeatherZoneOverwrite>", "Fog", eCWeatherZone_PS.class);
		public static final PropertyDescriptor<bCFloatColor> HazeColor = new PropertyDescriptor<>("HazeColor", bCFloatColor.class, "bCFloatColor", "Sky", eCWeatherZone_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<eEWeatherZoneOverwrite>> HazeColorOverwrite = new PropertyDescriptor<>("HazeColorOverwrite", bTPropertyContainer.class, "bTPropertyContainer<enum eEWeatherZoneOverwrite>", "Sky", eCWeatherZone_PS.class);
		public static final PropertyDescriptor<bCVector> InnerExtends = new PropertyDescriptor<>("InnerExtends", bCVector.class, "bCVector", "Shape", eCWeatherZone_PS.class);
		public static final PropertyDescriptor<gFloat> InnerRadius = new PropertyDescriptor<>("InnerRadius", gFloat.class, "float", "Shape", eCWeatherZone_PS.class);
		public static final PropertyDescriptor<bCFloatColor> LightDiffuseColor = new PropertyDescriptor<>("LightDiffuseColor", bCFloatColor.class, "bCFloatColor", "Light", eCWeatherZone_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<eEWeatherZoneOverwrite>> LightDiffuseOverwrite = new PropertyDescriptor<>("LightDiffuseOverwrite", bTPropertyContainer.class, "bTPropertyContainer<enum eEWeatherZoneOverwrite>", "Light", eCWeatherZone_PS.class);
		public static final PropertyDescriptor<gFloat> LightIntensity = new PropertyDescriptor<>("LightIntensity", gFloat.class, "float", "Light", eCWeatherZone_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<eEWeatherZoneOverwrite>> LightIntensityOverwrite = new PropertyDescriptor<>("LightIntensityOverwrite", bTPropertyContainer.class, "bTPropertyContainer<enum eEWeatherZoneOverwrite>", "Light", eCWeatherZone_PS.class);
		public static final PropertyDescriptor<bCFloatColor> LightSpecularColor = new PropertyDescriptor<>("LightSpecularColor", bCFloatColor.class, "bCFloatColor", "Light", eCWeatherZone_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<eEWeatherZoneOverwrite>> LightSpecularOverwrite = new PropertyDescriptor<>("LightSpecularOverwrite", bTPropertyContainer.class, "bTPropertyContainer<enum eEWeatherZoneOverwrite>", "Light", eCWeatherZone_PS.class);
		public static final PropertyDescriptor<bCString> MusicLocation = new PropertyDescriptor<>("MusicLocation", bCString.class, "bCString", "Music", eCWeatherZone_PS.class);
		public static final PropertyDescriptor<bCVector> OuterExtends = new PropertyDescriptor<>("OuterExtends", bCVector.class, "bCVector", "Shape", eCWeatherZone_PS.class);
		public static final PropertyDescriptor<gFloat> OuterRadius = new PropertyDescriptor<>("OuterRadius", gFloat.class, "float", "Shape", eCWeatherZone_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<eEWeatherZoneShape>> Shape = new PropertyDescriptor<>("Shape", bTPropertyContainer.class, "bTPropertyContainer<enum eEWeatherZoneShape>", "Shape", eCWeatherZone_PS.class);
		public static final PropertyDescriptor<bCFloatColor> SkyColor = new PropertyDescriptor<>("SkyColor", bCFloatColor.class, "bCFloatColor", "Sky", eCWeatherZone_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<eEWeatherZoneOverwrite>> SkyColorOverwrite = new PropertyDescriptor<>("SkyColorOverwrite", bTPropertyContainer.class, "bTPropertyContainer<enum eEWeatherZoneOverwrite>", "Sky", eCWeatherZone_PS.class);
		public static final PropertyDescriptor<bCString> WeatherEnvironment = new PropertyDescriptor<>("WeatherEnvironment", bCString.class, "bCString", "Environment", eCWeatherZone_PS.class);
	}

	public static interface gCAIZone_PS extends eCEntityPropertySet {
		public static final PropertyDescriptor<bTPropertyContainer<gESecurityLevel>> SecurityLevel = new PropertyDescriptor<>("SecurityLevel", bTPropertyContainer.class, "bTPropertyContainer<enum gESecurityLevel>", "", gCAIZone_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<gEZoneType>> Type = new PropertyDescriptor<>("Type", bTPropertyContainer.class, "bTPropertyContainer<enum gEZoneType>", "", gCAIZone_PS.class);
		public static final PropertyDescriptor<eCEntityProxy> Owner = new PropertyDescriptor<>("Owner", eCEntityProxy.class, "eCEntityProxy", "", gCAIZone_PS.class);
	}

	public static interface gCAmbientEnvironment extends ClassDescriptor {
		public static final PropertyDescriptor<gInt> MaxNumSounds = new PropertyDescriptor<>("MaxNumSounds", gInt.class, "int", "Spawning", gCAmbientEnvironment.class);
		public static final PropertyDescriptor<gFloat> MaxSpawnDelay = new PropertyDescriptor<>("MaxSpawnDelay", gFloat.class, "float", "Spawning", gCAmbientEnvironment.class);
		public static final PropertyDescriptor<gInt> MinNumSounds = new PropertyDescriptor<>("MinNumSounds", gInt.class, "int", "Spawning", gCAmbientEnvironment.class);
		public static final PropertyDescriptor<gFloat> MinSpawnDelay = new PropertyDescriptor<>("MinSpawnDelay", gFloat.class, "float", "Spawning", gCAmbientEnvironment.class);
		public static final PropertyDescriptor<gFloat> SpawnProbability = new PropertyDescriptor<>("SpawnProbability", gFloat.class, "float", "Spawning", gCAmbientEnvironment.class);
	}

	public static interface gCAmbientSequencer extends ClassDescriptor {
		public static final PropertyDescriptor<bTPropertyContainer<eEAmbientClimate>> Climate = new PropertyDescriptor<>("Climate", bTPropertyContainer.class, "bTPropertyContainer<enum eEAmbientClimate>", "Situation", gCAmbientSequencer.class);
		public static final PropertyDescriptor<bTPropertyContainer<eEAmbientDayTime>> DayTime = new PropertyDescriptor<>("DayTime", bTPropertyContainer.class, "bTPropertyContainer<enum eEAmbientDayTime>", "Situation", gCAmbientSequencer.class);
		public static final PropertyDescriptor<bTPropertyContainer<eEAmbientEnvironment>> Environment = new PropertyDescriptor<>("Environment", bTPropertyContainer.class, "bTPropertyContainer<enum eEAmbientEnvironment>", "Situation", gCAmbientSequencer.class);
	}

	public static interface gCAmbientSound extends ClassDescriptor {
		public static final PropertyDescriptor<bTPropertyContainer<eEAmbientClimate>> Climate = new PropertyDescriptor<>("Climate", bTPropertyContainer.class, "bTPropertyContainer<enum eEAmbientClimate>", "Situation", gCAmbientSound.class);
		public static final PropertyDescriptor<bTPropertyContainer<eEAmbientDayTime>> DayTime = new PropertyDescriptor<>("DayTime", bTPropertyContainer.class, "bTPropertyContainer<enum eEAmbientDayTime>", "Situation", gCAmbientSound.class);
		public static final PropertyDescriptor<gLong> Duration = new PropertyDescriptor<>("Duration", gLong.class, "unsigned long", "Range", gCAmbientSound.class);
		public static final PropertyDescriptor<bTPropertyContainer<eEAmbientEnvironment>> Environment = new PropertyDescriptor<>("Environment", bTPropertyContainer.class, "bTPropertyContainer<enum eEAmbientEnvironment>", "Situation", gCAmbientSound.class);
		public static final PropertyDescriptor<gFloat> MaxDistance = new PropertyDescriptor<>("MaxDistance", gFloat.class, "float", "Audibility", gCAmbientSound.class);
		public static final PropertyDescriptor<gFloat> MinDistance = new PropertyDescriptor<>("MinDistance", gFloat.class, "float", "Audibility", gCAmbientSound.class);
		public static final PropertyDescriptor<bCString> Sample = new PropertyDescriptor<>("Sample", bCString.class, "bCString", "Sample", gCAmbientSound.class);
		public static final PropertyDescriptor<gFloat> Volume = new PropertyDescriptor<>("Volume", gFloat.class, "float", "Audibility", gCAmbientSound.class);
		public static final PropertyDescriptor<gInt> Weight = new PropertyDescriptor<>("Weight", gInt.class, "int", "Probability", gCAmbientSound.class);
	}

	public static interface gCAnchor_PS extends eCEntityPropertySet {
		public static final PropertyDescriptor<bTPropertyContainer<gEAnchorType>> AnchorType = new PropertyDescriptor<>("AnchorType", bTPropertyContainer.class, "bTPropertyContainer<enum gEAnchorType>", "", gCAnchor_PS.class);
		public static final PropertyDescriptor<gLong> MaxUsers = new PropertyDescriptor<>("MaxUsers", gLong.class, "unsigned long", "", gCAnchor_PS.class);
		public static final PropertyDescriptor<gLong> PatrolIndex = new PropertyDescriptor<>("PatrolIndex", gLong.class, "unsigned long", "", gCAnchor_PS.class);
		public static final PropertyDescriptor<gLong> UserCount = new PropertyDescriptor<>("UserCount", gLong.class, "unsigned long", "", gCAnchor_PS.class);
	}

	public static interface gCArena_PS extends eCEntityPropertySet {
		public static final PropertyDescriptor<bTPropertyContainer<gEArenaStatus>> Status = new PropertyDescriptor<>("Status", bTPropertyContainer.class, "bTPropertyContainer<enum gEArenaStatus>", "", gCArena_PS.class);
	}

	public static interface gCArmorSet_PS extends eCEntityPropertySet {
		public static final PropertyDescriptor<eCEntityProxy> ActivateSkill1 = new PropertyDescriptor<>("ActivateSkill1", eCEntityProxy.class, "eCTemplateEntityProxy", "Benefits", gCArmorSet_PS.class);
		public static final PropertyDescriptor<eCEntityProxy> ActivateSkill2 = new PropertyDescriptor<>("ActivateSkill2", eCEntityProxy.class, "eCTemplateEntityProxy", "Benefits", gCArmorSet_PS.class);
		public static final PropertyDescriptor<eCEntityProxy> Body = new PropertyDescriptor<>("Body", eCEntityProxy.class, "eCTemplateEntityProxy", "Requirements", gCArmorSet_PS.class);
		public static final PropertyDescriptor<eCEntityProxy> Hand1 = new PropertyDescriptor<>("Hand1", eCEntityProxy.class, "eCTemplateEntityProxy", "Requirements", gCArmorSet_PS.class);
		public static final PropertyDescriptor<eCEntityProxy> Hand2 = new PropertyDescriptor<>("Hand2", eCEntityProxy.class, "eCTemplateEntityProxy", "Requirements", gCArmorSet_PS.class);
		public static final PropertyDescriptor<eCEntityProxy> Head = new PropertyDescriptor<>("Head", eCEntityProxy.class, "eCTemplateEntityProxy", "Requirements", gCArmorSet_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<EAttribModOperation>> ModAttrib1Op = new PropertyDescriptor<>("ModAttrib1Op", bTPropertyContainer.class, "bTPropertyContainer<enum EAttribModOperation>", "Benefits", gCArmorSet_PS.class);
		public static final PropertyDescriptor<gInt> ModAttrib1Value = new PropertyDescriptor<>("ModAttrib1Value", gInt.class, "int", "Benefits", gCArmorSet_PS.class);
		public static final PropertyDescriptor<bCString> ModAttrib1Tag = new PropertyDescriptor<>("ModAttrib1Tag", bCString.class, "bCString", "Benefits", gCArmorSet_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<EAttribModOperation>> ModAttrib2Op = new PropertyDescriptor<>("ModAttrib2Op", bTPropertyContainer.class, "bTPropertyContainer<enum EAttribModOperation>", "Benefits", gCArmorSet_PS.class);
		public static final PropertyDescriptor<gInt> ModAttrib2Value = new PropertyDescriptor<>("ModAttrib2Value", gInt.class, "int", "Benefits", gCArmorSet_PS.class);
		public static final PropertyDescriptor<bCString> ModAttrib2Tag = new PropertyDescriptor<>("ModAttrib2Tag", bCString.class, "bCString", "Benefits", gCArmorSet_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<EAttribModOperation>> ModAttrib3Op = new PropertyDescriptor<>("ModAttrib3Op", bTPropertyContainer.class, "bTPropertyContainer<enum EAttribModOperation>", "Benefits", gCArmorSet_PS.class);
		public static final PropertyDescriptor<gInt> ModAttrib3Value = new PropertyDescriptor<>("ModAttrib3Value", gInt.class, "int", "Benefits", gCArmorSet_PS.class);
		public static final PropertyDescriptor<bCString> ModAttrib3Tag = new PropertyDescriptor<>("ModAttrib3Tag", bCString.class, "bCString", "Benefits", gCArmorSet_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<EAttribModOperation>> ModAttrib4Op = new PropertyDescriptor<>("ModAttrib4Op", bTPropertyContainer.class, "bTPropertyContainer<enum EAttribModOperation>", "Benefits", gCArmorSet_PS.class);
		public static final PropertyDescriptor<gInt> ModAttrib4Value = new PropertyDescriptor<>("ModAttrib4Value", gInt.class, "int", "Benefits", gCArmorSet_PS.class);
		public static final PropertyDescriptor<bCString> ModAttrib4Tag = new PropertyDescriptor<>("ModAttrib4Tag", bCString.class, "bCString", "Benefits", gCArmorSet_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<EAttribModOperation>> ModAttrib5Op = new PropertyDescriptor<>("ModAttrib5Op", bTPropertyContainer.class, "bTPropertyContainer<enum EAttribModOperation>", "Benefits", gCArmorSet_PS.class);
		public static final PropertyDescriptor<gInt> ModAttrib5Value = new PropertyDescriptor<>("ModAttrib5Value", gInt.class, "int", "Benefits", gCArmorSet_PS.class);
		public static final PropertyDescriptor<bCString> ModAttrib5Tag = new PropertyDescriptor<>("ModAttrib5Tag", bCString.class, "bCString", "Benefits", gCArmorSet_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<EAttribModOperation>> ModAttrib6Op = new PropertyDescriptor<>("ModAttrib6Op", bTPropertyContainer.class, "bTPropertyContainer<enum EAttribModOperation>", "Benefits", gCArmorSet_PS.class);
		public static final PropertyDescriptor<gInt> ModAttrib6Value = new PropertyDescriptor<>("ModAttrib6Value", gInt.class, "int", "Benefits", gCArmorSet_PS.class);
		public static final PropertyDescriptor<bCString> ModAttrib6Tag = new PropertyDescriptor<>("ModAttrib6Tag", bCString.class, "bCString", "Benefits", gCArmorSet_PS.class);
		public static final PropertyDescriptor<eCEntityProxy> Ring1 = new PropertyDescriptor<>("Ring1", eCEntityProxy.class, "eCTemplateEntityProxy", "Requirements", gCArmorSet_PS.class);
		public static final PropertyDescriptor<eCEntityProxy> Ring2 = new PropertyDescriptor<>("Ring2", eCEntityProxy.class, "eCTemplateEntityProxy", "Requirements", gCArmorSet_PS.class);
	}

	public static interface gCAttribute extends ClassDescriptor {
		public static final PropertyDescriptor<gInt> BaseValue = new PropertyDescriptor<>("BaseValue", gInt.class, "int", "Value", gCAttribute.class);
		public static final PropertyDescriptor<gInt> Modifier = new PropertyDescriptor<>("Modifier", gInt.class, "int", "Value", gCAttribute.class);
		public static final PropertyDescriptor<bCString> Tag = new PropertyDescriptor<>("Tag", bCString.class, "bCString", "Name", gCAttribute.class);
	}

	public static interface gCBook_PS extends eCEntityPropertySet {
		public static final PropertyDescriptor<bCString> Header = new PropertyDescriptor<>("Header", bCString.class, "eCLocString", "", gCBook_PS.class);
		public static final PropertyDescriptor<bCString> LeftText = new PropertyDescriptor<>("LeftText", bCString.class, "eCLocString", "", gCBook_PS.class);
		public static final PropertyDescriptor<bCString> RightText = new PropertyDescriptor<>("RightText", bCString.class, "eCLocString", "", gCBook_PS.class);
	}

	public static interface gCCameraAI_PS extends eCEntityPropertySet {
		public static final PropertyDescriptor<bTPropertyObject> CamModeFirstPersonFight = new PropertyDescriptor<>("CamModeFirstPersonFight", bTPropertyObject.class, "bTPropertyObject<class gCCameraModeParams,class bCObjectRefBase>", "", gCCameraAI_PS.class);
		public static final PropertyDescriptor<bTPropertyObject> CamModeFirstPersonRnged = new PropertyDescriptor<>("CamModeFirstPersonRnged", bTPropertyObject.class, "bTPropertyObject<class gCCameraModeParams,class bCObjectRefBase>", "", gCCameraAI_PS.class);
		public static final PropertyDescriptor<bTPropertyObject> CamModeDeath = new PropertyDescriptor<>("CamModeDeath", bTPropertyObject.class, "bTPropertyObject<class gCCameraModeParams,class bCObjectRefBase>", "", gCCameraAI_PS.class);
		public static final PropertyDescriptor<bTPropertyObject> CamModeDialog = new PropertyDescriptor<>("CamModeDialog", bTPropertyObject.class, "bTPropertyObject<class gCCameraModeParams,class bCObjectRefBase>", "", gCCameraAI_PS.class);
		public static final PropertyDescriptor<bTPropertyObject> CamModeDialogFace2L = new PropertyDescriptor<>("CamModeDialogFace2L", bTPropertyObject.class, "bTPropertyObject<class gCCameraModeParams,class bCObjectRefBase>", "", gCCameraAI_PS.class);
		public static final PropertyDescriptor<bTPropertyObject> CamModeDialogFace2R = new PropertyDescriptor<>("CamModeDialogFace2R", bTPropertyObject.class, "bTPropertyObject<class gCCameraModeParams,class bCObjectRefBase>", "", gCCameraAI_PS.class);
		public static final PropertyDescriptor<bTPropertyObject> CamModeDialogFaceL = new PropertyDescriptor<>("CamModeDialogFaceL", bTPropertyObject.class, "bTPropertyObject<class gCCameraModeParams,class bCObjectRefBase>", "", gCCameraAI_PS.class);
		public static final PropertyDescriptor<bTPropertyObject> CamModeDialogFaceR = new PropertyDescriptor<>("CamModeDialogFaceR", bTPropertyObject.class, "bTPropertyObject<class gCCameraModeParams,class bCObjectRefBase>", "", gCCameraAI_PS.class);
		public static final PropertyDescriptor<bTPropertyObject> CamModeDialogLerp1 = new PropertyDescriptor<>("CamModeDialogLerp1", bTPropertyObject.class, "bTPropertyObject<class gCCameraModeParams,class bCObjectRefBase>", "", gCCameraAI_PS.class);
		public static final PropertyDescriptor<bTPropertyObject> CamModeDialogLerp2 = new PropertyDescriptor<>("CamModeDialogLerp2", bTPropertyObject.class, "bTPropertyObject<class gCCameraModeParams,class bCObjectRefBase>", "", gCCameraAI_PS.class);
		public static final PropertyDescriptor<bTPropertyObject> CamModeDialogLerp3 = new PropertyDescriptor<>("CamModeDialogLerp3", bTPropertyObject.class, "bTPropertyObject<class gCCameraModeParams,class bCObjectRefBase>", "", gCCameraAI_PS.class);
		public static final PropertyDescriptor<bTPropertyObject> CamModeDialogLerp4 = new PropertyDescriptor<>("CamModeDialogLerp4", bTPropertyObject.class, "bTPropertyObject<class gCCameraModeParams,class bCObjectRefBase>", "", gCCameraAI_PS.class);
		public static final PropertyDescriptor<bTPropertyObject> CamModeDialogShoulder1L = new PropertyDescriptor<>("CamModeDialogShoulder1L", bTPropertyObject.class, "bTPropertyObject<class gCCameraModeParams,class bCObjectRefBase>", "", gCCameraAI_PS.class);
		public static final PropertyDescriptor<bTPropertyObject> CamModeDialogShoulder1R = new PropertyDescriptor<>("CamModeDialogShoulder1R", bTPropertyObject.class, "bTPropertyObject<class gCCameraModeParams,class bCObjectRefBase>", "", gCCameraAI_PS.class);
		public static final PropertyDescriptor<bTPropertyObject> CamModeDialogShoulder2L = new PropertyDescriptor<>("CamModeDialogShoulder2L", bTPropertyObject.class, "bTPropertyObject<class gCCameraModeParams,class bCObjectRefBase>", "", gCCameraAI_PS.class);
		public static final PropertyDescriptor<bTPropertyObject> CamModeDialogShoulder2R = new PropertyDescriptor<>("CamModeDialogShoulder2R", bTPropertyObject.class, "bTPropertyObject<class gCCameraModeParams,class bCObjectRefBase>", "", gCCameraAI_PS.class);
		public static final PropertyDescriptor<bTPropertyObject> CamModeDialogSideL = new PropertyDescriptor<>("CamModeDialogSideL", bTPropertyObject.class, "bTPropertyObject<class gCCameraModeParams,class bCObjectRefBase>", "", gCCameraAI_PS.class);
		public static final PropertyDescriptor<bTPropertyObject> CamModeDialogSideR = new PropertyDescriptor<>("CamModeDialogSideR", bTPropertyObject.class, "bTPropertyObject<class gCCameraModeParams,class bCObjectRefBase>", "", gCCameraAI_PS.class);
		public static final PropertyDescriptor<bTPropertyObject> CamModeDive = new PropertyDescriptor<>("CamModeDive", bTPropertyObject.class, "bTPropertyObject<class gCCameraModeParams,class bCObjectRefBase>", "", gCCameraAI_PS.class);
		public static final PropertyDescriptor<bTPropertyObject> CamModeFall = new PropertyDescriptor<>("CamModeFall", bTPropertyObject.class, "bTPropertyObject<class gCCameraModeParams,class bCObjectRefBase>", "", gCCameraAI_PS.class);
		public static final PropertyDescriptor<bTPropertyObject> CamModeFirstPerson = new PropertyDescriptor<>("CamModeFirstPerson", bTPropertyObject.class, "bTPropertyObject<class gCCameraModeParams,class bCObjectRefBase>", "", gCCameraAI_PS.class);
		public static final PropertyDescriptor<bTPropertyObject> CamModeInteraction = new PropertyDescriptor<>("CamModeInteraction", bTPropertyObject.class, "bTPropertyObject<class gCCameraModeParams,class bCObjectRefBase>", "", gCCameraAI_PS.class);
		public static final PropertyDescriptor<bTPropertyObject> CamModeInventory = new PropertyDescriptor<>("CamModeInventory", bTPropertyObject.class, "bTPropertyObject<class gCCameraModeParams,class bCObjectRefBase>", "", gCCameraAI_PS.class);
		public static final PropertyDescriptor<bTPropertyObject> CamModeMagic = new PropertyDescriptor<>("CamModeMagic", bTPropertyObject.class, "bTPropertyObject<class gCCameraModeParams,class bCObjectRefBase>", "", gCCameraAI_PS.class);
		public static final PropertyDescriptor<bTPropertyObject> CamModeMelee = new PropertyDescriptor<>("CamModeMelee", bTPropertyObject.class, "bTPropertyObject<class gCCameraModeParams,class bCObjectRefBase>", "", gCCameraAI_PS.class);
		public static final PropertyDescriptor<bTPropertyObject> CamModeNormal = new PropertyDescriptor<>("CamModeNormal", bTPropertyObject.class, "bTPropertyObject<class gCCameraModeParams,class bCObjectRefBase>", "", gCCameraAI_PS.class);
		public static final PropertyDescriptor<bTPropertyObject> CamModeRanged = new PropertyDescriptor<>("CamModeRanged", bTPropertyObject.class, "bTPropertyObject<class gCCameraModeParams,class bCObjectRefBase>", "", gCCameraAI_PS.class);
		public static final PropertyDescriptor<bTPropertyObject> CamModeShoulder = new PropertyDescriptor<>("CamModeShoulder", bTPropertyObject.class, "bTPropertyObject<class gCCameraModeParams,class bCObjectRefBase>", "", gCCameraAI_PS.class);
		public static final PropertyDescriptor<bTPropertyObject> CamModeSwim = new PropertyDescriptor<>("CamModeSwim", bTPropertyObject.class, "bTPropertyObject<class gCCameraModeParams,class bCObjectRefBase>", "", gCCameraAI_PS.class);
	}

	public static interface gCCharacterControl_PS extends eCEntityPropertySet {
		public static final PropertyDescriptor<bTPropertyContainer<gECharacterControlFOR>> ControlFrameOfReference = new PropertyDescriptor<>("ControlFrameOfReference", bTPropertyContainer.class, "bTPropertyContainer<enum gECharacterControlFOR>", "Common", gCCharacterControl_PS.class);
		public static final PropertyDescriptor<gLong> DurationPressedMSecs = new PropertyDescriptor<>("DurationPressedMSecs", gLong.class, "unsigned long", "ScriptParam", gCCharacterControl_PS.class);
		public static final PropertyDescriptor<gBool> IsPressed = new PropertyDescriptor<>("IsPressed", gBool.class, "bool", "ScriptParam", gCCharacterControl_PS.class);
		public static final PropertyDescriptor<gBool> IsPressedBefore = new PropertyDescriptor<>("IsPressedBefore", gBool.class, "bool", "ScriptParam", gCCharacterControl_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<gESessionKey>> PressedKey = new PropertyDescriptor<>("PressedKey", bTPropertyContainer.class, "bTPropertyContainer<enum gESessionKey>", "ScriptParam", gCCharacterControl_PS.class);
	}

	public static interface gCMovementBase_PS extends eCEntityPropertySet {
	}

	public static interface gCCharacterMovement_PS extends gCMovementBase_PS {
		public static final PropertyDescriptor<gFloat> AniVelocityInertia = new PropertyDescriptor<>("AniVelocityInertia", gFloat.class, "float", "", gCCharacterMovement_PS.class);
		public static final PropertyDescriptor<gBool> ApplyGravity = new PropertyDescriptor<>("ApplyGravity", gBool.class, "bool", "", gCCharacterMovement_PS.class);
		public static final PropertyDescriptor<gBool> CanBePushedWhileIdle = new PropertyDescriptor<>("CanBePushedWhileIdle", gBool.class, "bool", "", gCCharacterMovement_PS.class);
		public static final PropertyDescriptor<gBool> DoHeightCorrection = new PropertyDescriptor<>("DoHeightCorrection", gBool.class, "bool", "", gCCharacterMovement_PS.class);
		public static final PropertyDescriptor<gFloat> FallSteerScaleFactor = new PropertyDescriptor<>("FallSteerScaleFactor", gFloat.class, "float", "", gCCharacterMovement_PS.class);
		public static final PropertyDescriptor<gFloat> FastModifier = new PropertyDescriptor<>("FastModifier", gFloat.class, "float", "", gCCharacterMovement_PS.class);
		public static final PropertyDescriptor<gBool> ForceGroundAlignment = new PropertyDescriptor<>("ForceGroundAlignment", gBool.class, "bool", "", gCCharacterMovement_PS.class);
		public static final PropertyDescriptor<gFloat> GroundSlopeTransInertia = new PropertyDescriptor<>("GroundSlopeTransInertia", gFloat.class, "float", "", gCCharacterMovement_PS.class);
		public static final PropertyDescriptor<gBool> IsQuadruped = new PropertyDescriptor<>("IsQuadruped", gBool.class, "bool", "", gCCharacterMovement_PS.class);
		public static final PropertyDescriptor<gFloat> JumpStartUpVelocity = new PropertyDescriptor<>("JumpStartUpVelocity", gFloat.class, "float", "", gCCharacterMovement_PS.class);
		public static final PropertyDescriptor<gFloat> JumpSteerScaleFactor = new PropertyDescriptor<>("JumpSteerScaleFactor", gFloat.class, "float", "", gCCharacterMovement_PS.class);
		public static final PropertyDescriptor<gFloat> LastFallVelocity = new PropertyDescriptor<>("LastFallVelocity", gFloat.class, "float", "", gCCharacterMovement_PS.class);
		public static final PropertyDescriptor<gFloat> MoveAcceleration = new PropertyDescriptor<>("MoveAcceleration", gFloat.class, "float", "", gCCharacterMovement_PS.class);
		public static final PropertyDescriptor<gFloat> MoveDecceleration = new PropertyDescriptor<>("MoveDecceleration", gFloat.class, "float", "", gCCharacterMovement_PS.class);
		public static final PropertyDescriptor<gFloat> QuadrupedSlopeInertia = new PropertyDescriptor<>("QuadrupedSlopeInertia", gFloat.class, "float", "", gCCharacterMovement_PS.class);
		public static final PropertyDescriptor<gFloat> SensorAdvanceDuration = new PropertyDescriptor<>("SensorAdvanceDuration", gFloat.class, "float", "", gCCharacterMovement_PS.class);
		public static final PropertyDescriptor<gBool> SensorAffectsDirection = new PropertyDescriptor<>("SensorAffectsDirection", gBool.class, "bool", "", gCCharacterMovement_PS.class);
		public static final PropertyDescriptor<gFloat> SensorInertia = new PropertyDescriptor<>("SensorInertia", gFloat.class, "float", "", gCCharacterMovement_PS.class);
		public static final PropertyDescriptor<gFloat> SensorMinSlideAngle = new PropertyDescriptor<>("SensorMinSlideAngle", gFloat.class, "float", "", gCCharacterMovement_PS.class);
		public static final PropertyDescriptor<gFloat> SlideInertia = new PropertyDescriptor<>("SlideInertia", gFloat.class, "float", "", gCCharacterMovement_PS.class);
		public static final PropertyDescriptor<gFloat> SlideSpeedMax = new PropertyDescriptor<>("SlideSpeedMax", gFloat.class, "float", "", gCCharacterMovement_PS.class);
		public static final PropertyDescriptor<gFloat> SlowModifier = new PropertyDescriptor<>("SlowModifier", gFloat.class, "float", "", gCCharacterMovement_PS.class);
		public static final PropertyDescriptor<gFloat> SneakModifier = new PropertyDescriptor<>("SneakModifier", gFloat.class, "float", "", gCCharacterMovement_PS.class);
		public static final PropertyDescriptor<gFloat> BackwardSpeedMax = new PropertyDescriptor<>("BackwardSpeedMax", gFloat.class, "float", "", gCCharacterMovement_PS.class);
		public static final PropertyDescriptor<gFloat> ForwardSpeedMax = new PropertyDescriptor<>("ForwardSpeedMax", gFloat.class, "float", "", gCCharacterMovement_PS.class);
		public static final PropertyDescriptor<gFloat> StrafeSpeedMax = new PropertyDescriptor<>("StrafeSpeedMax", gFloat.class, "float", "", gCCharacterMovement_PS.class);
		public static final PropertyDescriptor<gFloat> TurnSpeedMax = new PropertyDescriptor<>("TurnSpeedMax", gFloat.class, "float", "", gCCharacterMovement_PS.class);
		public static final PropertyDescriptor<gFloat> TurnSpeedModifier = new PropertyDescriptor<>("TurnSpeedModifier", gFloat.class, "float", "", gCCharacterMovement_PS.class);
		public static final PropertyDescriptor<gFloat> SteepGroundAngleMax = new PropertyDescriptor<>("SteepGroundAngleMax", gFloat.class, "float", "", gCCharacterMovement_PS.class);
		public static final PropertyDescriptor<gFloat> SteepGroundAngleMin = new PropertyDescriptor<>("SteepGroundAngleMin", gFloat.class, "float", "", gCCharacterMovement_PS.class);
		public static final PropertyDescriptor<gFloat> StepHeight = new PropertyDescriptor<>("StepHeight", gFloat.class, "float", "", gCCharacterMovement_PS.class);
		public static final PropertyDescriptor<gFloat> SwimDepth = new PropertyDescriptor<>("SwimDepth", gFloat.class, "float", "", gCCharacterMovement_PS.class);
		public static final PropertyDescriptor<gBool> TreatWaterAsSolid = new PropertyDescriptor<>("TreatWaterAsSolid", gBool.class, "bool", "", gCCharacterMovement_PS.class);
		public static final PropertyDescriptor<gFloat> TurnAcceleration = new PropertyDescriptor<>("TurnAcceleration", gFloat.class, "float", "", gCCharacterMovement_PS.class);
		public static final PropertyDescriptor<gFloat> TurnDecceleration = new PropertyDescriptor<>("TurnDecceleration", gFloat.class, "float", "", gCCharacterMovement_PS.class);
		public static final PropertyDescriptor<gFloat> WalkDownSpeedScale = new PropertyDescriptor<>("WalkDownSpeedScale", gFloat.class, "float", "", gCCharacterMovement_PS.class);
	}

	public static interface gCClock_PS extends eCEntityPropertySet {
		public static final PropertyDescriptor<gLong> Day = new PropertyDescriptor<>("Day", gLong.class, "unsigned long", "", gCClock_PS.class);
		public static final PropertyDescriptor<gFloat> Factor = new PropertyDescriptor<>("Factor", gFloat.class, "float", "", gCClock_PS.class);
		public static final PropertyDescriptor<gLong> Hour = new PropertyDescriptor<>("Hour", gLong.class, "unsigned long", "", gCClock_PS.class);
		public static final PropertyDescriptor<gLong> Minute = new PropertyDescriptor<>("Minute", gLong.class, "unsigned long", "", gCClock_PS.class);
		public static final PropertyDescriptor<gLong> Second = new PropertyDescriptor<>("Second", gLong.class, "unsigned long", "", gCClock_PS.class);
		public static final PropertyDescriptor<gLong> Year = new PropertyDescriptor<>("Year", gLong.class, "unsigned long", "", gCClock_PS.class);
	}

	public static interface gCCollisionCircle_PS extends eCEntityPropertySet {
		public static final PropertyDescriptor<gLong> CircleCount = new PropertyDescriptor<>("CircleCount", gLong.class, "unsigned long", "", gCCollisionCircle_PS.class);
		public static final PropertyDescriptor<gFloat> DefaultRadius = new PropertyDescriptor<>("DefaultRadius", gFloat.class, "float", "", gCCollisionCircle_PS.class);
		public static final PropertyDescriptor<bTValArray_bCVector> Offset = new PropertyDescriptor<>("Offset", bTValArray_bCVector.class, "bTValArray<class bCVector>", "", gCCollisionCircle_PS.class);
		public static final PropertyDescriptor<bTValArray_float> Radius = new PropertyDescriptor<>("Radius", bTValArray_float.class, "bTValArray<float>", "", gCCollisionCircle_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<gENavObstacleType>> Type = new PropertyDescriptor<>("Type", bTPropertyContainer.class, "bTPropertyContainer<enum gENavObstacleType>", "", gCCollisionCircle_PS.class);
		public static final PropertyDescriptor<bTValArray_bCPropertyID> ZoneEntityIDs = new PropertyDescriptor<>("ZoneEntityIDs", bTValArray_bCPropertyID.class, "bTValArray<class bCPropertyID>", "", gCCollisionCircle_PS.class);
	}

	public static interface gCDamageReceiver_PS extends eCEntityPropertySet {
		public static final PropertyDescriptor<gLong> DamageAmount = new PropertyDescriptor<>("DamageAmount", gLong.class, "long", "Script", gCDamageReceiver_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<gEDamageType>> DamageType = new PropertyDescriptor<>("DamageType", bTPropertyContainer.class, "bTPropertyContainer<enum gEDamageType>", "Script", gCDamageReceiver_PS.class);
		public static final PropertyDescriptor<gLong> HitPoints = new PropertyDescriptor<>("HitPoints", gLong.class, "long", "Basic", gCDamageReceiver_PS.class);
		public static final PropertyDescriptor<gLong> HitPointsMax = new PropertyDescriptor<>("HitPointsMax", gLong.class, "long", "Basic", gCDamageReceiver_PS.class);
		public static final PropertyDescriptor<eCEntityProxy> LastInflictor = new PropertyDescriptor<>("LastInflictor", eCEntityProxy.class, "eCEntityProxy", "Script", gCDamageReceiver_PS.class);
		public static final PropertyDescriptor<gLong> ManaPoints = new PropertyDescriptor<>("ManaPoints", gLong.class, "long", "Basic", gCDamageReceiver_PS.class);
		public static final PropertyDescriptor<gLong> ManaPointsMax = new PropertyDescriptor<>("ManaPointsMax", gLong.class, "long", "Basic", gCDamageReceiver_PS.class);
		public static final PropertyDescriptor<gLong> StaminaPoints = new PropertyDescriptor<>("StaminaPoints", gLong.class, "long", "Basic", gCDamageReceiver_PS.class);
		public static final PropertyDescriptor<gLong> StaminaPointsMax = new PropertyDescriptor<>("StaminaPointsMax", gLong.class, "long", "Basic", gCDamageReceiver_PS.class);
	}

	public static interface gCDamage_PS extends eCEntityPropertySet {
		public static final PropertyDescriptor<gLong> DamageAmount = new PropertyDescriptor<>("DamageAmount", gLong.class, "long", "", gCDamage_PS.class);
		public static final PropertyDescriptor<gFloat> DamageHitMultiplier = new PropertyDescriptor<>("DamageHitMultiplier", gFloat.class, "float", "Script", gCDamage_PS.class);
		public static final PropertyDescriptor<gFloat> DamageManaMultiplier = new PropertyDescriptor<>("DamageManaMultiplier", gFloat.class, "float", "Magic", gCDamage_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<gEDamageType>> DamageType = new PropertyDescriptor<>("DamageType", bTPropertyContainer.class, "bTPropertyContainer<enum gEDamageType>", "", gCDamage_PS.class);
		public static final PropertyDescriptor<gInt> ManaUsed = new PropertyDescriptor<>("ManaUsed", gInt.class, "int", "Magic", gCDamage_PS.class);
	}

	public static interface gCDatabase extends eCProcessibleElement {
		public static final PropertyDescriptor<Unknown> TemplateLayers = new PropertyDescriptor<>("TemplateLayers", Unknown.class, "bTRefPtrArray<class bCPropertyObjectBase *>", "", gCDatabase.class);
		public static final PropertyDescriptor<Unknown> Workspace = new PropertyDescriptor<>("Workspace", Unknown.class, "bTPOSmartPtr<class gCWorkspace>", "", gCDatabase.class);
	}

	public static interface gCDialog_PS extends eCEntityPropertySet {
		public static final PropertyDescriptor<gFloat> EndDialogTimestamp = new PropertyDescriptor<>("EndDialogTimestamp", gFloat.class, "float", "", gCDialog_PS.class);
		public static final PropertyDescriptor<gBool> MobEnabled = new PropertyDescriptor<>("MobEnabled", gBool.class, "bool", "", gCDialog_PS.class);
		public static final PropertyDescriptor<gBool> PartyEnabled = new PropertyDescriptor<>("PartyEnabled", gBool.class, "bool", "", gCDialog_PS.class);
		public static final PropertyDescriptor<gBool> PickedPocket = new PropertyDescriptor<>("PickedPocket", gBool.class, "bool", "", gCDialog_PS.class);
		public static final PropertyDescriptor<gBool> SlaveryEnabled = new PropertyDescriptor<>("SlaveryEnabled", gBool.class, "bool", "", gCDialog_PS.class);
		public static final PropertyDescriptor<gBool> TalkedToPlayer = new PropertyDescriptor<>("TalkedToPlayer", gBool.class, "bool", "", gCDialog_PS.class);
		public static final PropertyDescriptor<gBool> TeachEnabled = new PropertyDescriptor<>("TeachEnabled", gBool.class, "bool", "", gCDialog_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<gETradeCategory>> TradeCategory = new PropertyDescriptor<>("TradeCategory", bTPropertyContainer.class, "bTPropertyContainer<enum gETradeCategory>", "", gCDialog_PS.class);
		public static final PropertyDescriptor<gBool> TradeEnabled = new PropertyDescriptor<>("TradeEnabled", gBool.class, "bool", "", gCDialog_PS.class);
	}

	public static interface gCDistanceTrigger_PS extends gCEventScript_PS {
		public static final PropertyDescriptor<bTPropertyContainer<gEMaxDistType>> MaxDistType = new PropertyDescriptor<>("MaxDistType", bTPropertyContainer.class, "bTPropertyContainer<enum gEMaxDistType>", "", gCDistanceTrigger_PS.class);
		public static final PropertyDescriptor<gFloat> MaxDistance = new PropertyDescriptor<>("MaxDistance", gFloat.class, "float", "", gCDistanceTrigger_PS.class);
		public static final PropertyDescriptor<gFloat> MinDistance = new PropertyDescriptor<>("MinDistance", gFloat.class, "float", "", gCDistanceTrigger_PS.class);
	}

	public static interface gCDoor_PS extends gCMover_PS {
		public static final PropertyDescriptor<bTPropertyContainer<gEDoorStatus>> Status = new PropertyDescriptor<>("Status", bTPropertyContainer.class, "bTPropertyContainer<enum gEDoorStatus>", "Common", gCDoor_PS.class);
	}

	public static interface gCDynamicCollisionCircle_PS extends eCEntityPropertySet {
		public static final PropertyDescriptor<gFloat> Radius = new PropertyDescriptor<>("Radius", gFloat.class, "float", "", gCDynamicCollisionCircle_PS.class);
	}

	public static interface gCDynamicLayer extends gCLayerBase {
		public static final PropertyDescriptor<bTPropertyContainer<gEEntityType>> EntityType = new PropertyDescriptor<>("EntityType", bTPropertyContainer.class, "bTPropertyContainer<enum gEEntityType>", "", gCDynamicLayer.class);
	}

	public static interface gCEffectCommand extends ClassDescriptor {
		public static final PropertyDescriptor<gFloat> TimeOffset = new PropertyDescriptor<>("TimeOffset", gFloat.class, "float", "Time", gCEffectCommand.class, new gFloat(0));
	}

	public static interface gCEffectCommandCreateDecal extends gCEffectCommand {
		public static final PropertyDescriptor<bCString> BoneName = new PropertyDescriptor<>("BoneName", bCString.class, "bCString", "Position", gCEffectCommandCreateDecal.class);
		public static final PropertyDescriptor<gBool> CreateOnDynamicEntities = new PropertyDescriptor<>("CreateOnDynamicEntities", gBool.class, "bool", "Decal", gCEffectCommandCreateDecal.class);
		public static final PropertyDescriptor<bCEulerAngles> DirectionOffset = new PropertyDescriptor<>("DirectionOffset", bCEulerAngles.class, "bCEulerAngles", "Direction", gCEffectCommandCreateDecal.class);
		public static final PropertyDescriptor<gFloat> FadeInTime = new PropertyDescriptor<>("FadeInTime", gFloat.class, "float", "Fading", gCEffectCommandCreateDecal.class);
		public static final PropertyDescriptor<gFloat> FadeOutTime = new PropertyDescriptor<>("FadeOutTime", gFloat.class, "float", "Fading", gCEffectCommandCreateDecal.class);
		public static final PropertyDescriptor<bCString> ImageOrMaterial = new PropertyDescriptor<>("ImageOrMaterial", bCString.class, "bCImageOrMaterialResourceString", "Decal", gCEffectCommandCreateDecal.class);
		public static final PropertyDescriptor<gFloat> LifeTime = new PropertyDescriptor<>("LifeTime", gFloat.class, "float", "Decal", gCEffectCommandCreateDecal.class);
		public static final PropertyDescriptor<bCVector> Offset = new PropertyDescriptor<>("Offset", bCVector.class, "bCVector", "Position", gCEffectCommandCreateDecal.class);
		public static final PropertyDescriptor<bCVector> Size = new PropertyDescriptor<>("Size", bCVector.class, "bCVector", "Extent", gCEffectCommandCreateDecal.class);
		public static final PropertyDescriptor<gBool> UseTargetDirection = new PropertyDescriptor<>("UseTargetDirection", gBool.class, "bool", "Direction", gCEffectCommandCreateDecal.class);
	}

	public static interface gCEffectCommandEarthquake extends gCEffectCommand {
		public static final PropertyDescriptor<bCVector> Amplitude = new PropertyDescriptor<>("Amplitude", bCVector.class, "bCVector", "Strength", gCEffectCommandEarthquake.class, new bCVector(0.0f, 20.0f, 0.0f));
		public static final PropertyDescriptor<gFloat> Duration = new PropertyDescriptor<>("Duration", gFloat.class, "float", "Time", gCEffectCommandEarthquake.class, new gFloat(2.0f));
		public static final PropertyDescriptor<gFloat> Frequency = new PropertyDescriptor<>("Frequency", gFloat.class, "float", "Strength", gCEffectCommandEarthquake.class, new gFloat(30.0f));
		public static final PropertyDescriptor<gFloat> Radius = new PropertyDescriptor<>("Radius", gFloat.class, "float", "Range", gCEffectCommandEarthquake.class, new gFloat(500.0f));
	}

	public static interface gCEffectCommandKillEntity extends gCEffectCommand {
		public static final PropertyDescriptor<gInt> EntityIndex = new PropertyDescriptor<>("EntityIndex", gInt.class, "int", "General", gCEffectCommandKillEntity.class, new gInt(0));
	}

	public static interface gCEffectCommandModifyEntity extends gCEffectCommand {
		public static final PropertyDescriptor<gInt> EntityIndex = new PropertyDescriptor<>("EntityIndex", gInt.class, "int", "General", gCEffectCommandModifyEntity.class, new gInt(0));
		public static final PropertyDescriptor<bCString> Property = new PropertyDescriptor<>("Property", bCString.class, "bCString", "General", gCEffectCommandModifyEntity.class, new bCString(""));
		public static final PropertyDescriptor<bTPropertyContainer<eEPropertySetType>> PropertySet = new PropertyDescriptor<>("PropertySet", bTPropertyContainer.class, "bTPropertyContainer<enum eEPropertySetType>", "General", gCEffectCommandModifyEntity.class, new bTPropertyContainer<>(eEPropertySetType.eEPropertySetType_Particle));
		public static final PropertyDescriptor<bCString> Value = new PropertyDescriptor<>("Value", bCString.class, "bCString", "General", gCEffectCommandModifyEntity.class, new bCString(""));
	}

	public static interface gCEffectCommandMusicTrigger extends gCEffectCommand {
		public static final PropertyDescriptor<gInt> ConnectIn = new PropertyDescriptor<>("ConnectIn", gInt.class, "int", "Transition", gCEffectCommandMusicTrigger.class, new gInt(0));
		public static final PropertyDescriptor<gInt> ConnectOut = new PropertyDescriptor<>("ConnectOut", gInt.class, "int", "Transition", gCEffectCommandMusicTrigger.class, new gInt(0));
		public static final PropertyDescriptor<bCString> DayTime = new PropertyDescriptor<>("DayTime", bCString.class, "bCString", "Situation", gCEffectCommandMusicTrigger.class, new bCString(""));
		public static final PropertyDescriptor<gBool> DisableTriggers = new PropertyDescriptor<>("DisableTriggers", gBool.class, "bool", "General", gCEffectCommandMusicTrigger.class);
		public static final PropertyDescriptor<bTPropertyContainer<gEMusicFragmentPosition>> Entrance = new PropertyDescriptor<>("Entrance", bTPropertyContainer.class, "bTPropertyContainer<enum gEMusicFragmentPosition>", "Transition", gCEffectCommandMusicTrigger.class, new bTPropertyContainer<>(gEMusicFragmentPosition.gEMusicFragmentPosition_Begin));
		public static final PropertyDescriptor<bTPropertyContainer<gEMusicTriggerTime>> Exit = new PropertyDescriptor<>("Exit", bTPropertyContainer.class, "bTPropertyContainer<enum gEMusicTriggerTime>", "Transition", gCEffectCommandMusicTrigger.class, new bTPropertyContainer<>(gEMusicTriggerTime.gEMusicTriggerTime_NextExit));
		public static final PropertyDescriptor<gBool> IsStopTrigger = new PropertyDescriptor<>("IsStopTrigger", gBool.class, "bool", "General", gCEffectCommandMusicTrigger.class);
		public static final PropertyDescriptor<bCString> Location = new PropertyDescriptor<>("Location", bCString.class, "bCString", "Situation", gCEffectCommandMusicTrigger.class, new bCString(""));
		public static final PropertyDescriptor<bCString> Situation = new PropertyDescriptor<>("Situation", bCString.class, "bCString", "Situation", gCEffectCommandMusicTrigger.class, new bCString(""));
		public static final PropertyDescriptor<bTPropertyContainer<gEMusicTransition>> TransitionMode = new PropertyDescriptor<>("TransitionMode", bTPropertyContainer.class, "bTPropertyContainer<enum gEMusicTransition>", "Transition", gCEffectCommandMusicTrigger.class, new bTPropertyContainer<>(gEMusicTransition.gEMusicTransition_MatchingConnector));
		public static final PropertyDescriptor<bCString> Transition = new PropertyDescriptor<>("Transition", bCString.class, "bCString", "Transition", gCEffectCommandMusicTrigger.class, new bCString(""));
		public static final PropertyDescriptor<bCString> Variation = new PropertyDescriptor<>("Variation", bCString.class, "bCString", "Situation", gCEffectCommandMusicTrigger.class, new bCString(""));
	}

	public static interface gCEffectCommandPlaySound extends gCEffectCommand {
		public static final PropertyDescriptor<gFloat> MaxDistance = new PropertyDescriptor<>("MaxDistance", gFloat.class, "float", "Audibility", gCEffectCommandPlaySound.class, new gFloat(1000000.0f));
		public static final PropertyDescriptor<gFloat> MinDistance = new PropertyDescriptor<>("MinDistance", gFloat.class, "float", "Audibility", gCEffectCommandPlaySound.class, new gFloat(200.0f));
		public static final PropertyDescriptor<gFloat> Volume = new PropertyDescriptor<>("Volume", gFloat.class, "float", "Audibility", gCEffectCommandPlaySound.class, new gFloat(1.0f));
	}

	public static interface gCEffectCommandPlayVoice extends gCEffectCommand {
		public static final PropertyDescriptor<gFloat> MaxDistance = new PropertyDescriptor<>("MaxDistance", gFloat.class, "float", "Audibility", gCEffectCommandPlayVoice.class, new gFloat(1000000.0f));
		public static final PropertyDescriptor<gFloat> MinDistance = new PropertyDescriptor<>("MinDistance", gFloat.class, "float", "Audibility", gCEffectCommandPlayVoice.class, new gFloat(200.0f));
		public static final PropertyDescriptor<bCString> Sample = new PropertyDescriptor<>("Sample", bCString.class, "bCString", "Sample", gCEffectCommandPlayVoice.class, new bCString("(voice).wav"));
		public static final PropertyDescriptor<gFloat> Volume = new PropertyDescriptor<>("Volume", gFloat.class, "float", "Audibility", gCEffectCommandPlayVoice.class, new gFloat(1.0f));
	}

	public static interface gCEffectCommandSpawnEntity extends gCEffectCommand {
		public static final PropertyDescriptor<gBool> AutoKill = new PropertyDescriptor<>("AutoKill", gBool.class, "bool", "General", gCEffectCommandSpawnEntity.class, new gBool(false));
		public static final PropertyDescriptor<bCString> BoneName = new PropertyDescriptor<>("BoneName", bCString.class, "bCString", "General", gCEffectCommandSpawnEntity.class, new bCString(""));
		public static final PropertyDescriptor<gInt> EntityIndex = new PropertyDescriptor<>("EntityIndex", gInt.class, "int", "General", gCEffectCommandSpawnEntity.class, new gInt(-1));
		public static final PropertyDescriptor<bTPropertyContainer<gEEffectLink>> CoordinateSystem = new PropertyDescriptor<>("CoordinateSystem", bTPropertyContainer.class, "bTPropertyContainer<enum gEEffectLink>", "General", gCEffectCommandSpawnEntity.class, new bTPropertyContainer<>(gEEffectLink.gEEffectLink_Independent));
		public static final PropertyDescriptor<bCRange3> Offset = new PropertyDescriptor<>("Offset", bCRange3.class, "bCRange3", "General", gCEffectCommandSpawnEntity.class, new bCRange3(bCVector.nullVector(), bCVector.nullVector()));
		public static final PropertyDescriptor<eCEntityProxy> TemplateEntity = new PropertyDescriptor<>("TemplateEntity", eCEntityProxy.class, "eCTemplateEntityProxy", "General", gCEffectCommandSpawnEntity.class, new eCEntityProxy(null));
	}

	public static interface gCEffectCommandTriggerEntity extends gCEffectCommand {
		public static final PropertyDescriptor<gInt> EntityIndex = new PropertyDescriptor<>("EntityIndex", gInt.class, "int", "General", gCEffectCommandTriggerEntity.class, new gInt(0));
		public static final PropertyDescriptor<bCString> Trigger = new PropertyDescriptor<>("Trigger", bCString.class, "bCString", "General", gCEffectCommandTriggerEntity.class, new bCString(""));
	}

	public static interface gCEffect_PS extends eCEntityPropertySet {
		public static final PropertyDescriptor<bCString> Effect = new PropertyDescriptor<>("Effect", bCString.class, "bCString", "", gCEffect_PS.class);
		public static final PropertyDescriptor<bCVector> Offset = new PropertyDescriptor<>("Offset", bCVector.class, "bCVector", "", gCEffect_PS.class);
		public static final PropertyDescriptor<gFloat> Probability = new PropertyDescriptor<>("Probability", gFloat.class, "float", "", gCEffect_PS.class);
		public static final PropertyDescriptor<gBool> Static = new PropertyDescriptor<>("Static", gBool.class, "bool", "", gCEffect_PS.class);
	}

	public static interface gCEnclave_PS extends eCEntityPropertySet {
		public static final PropertyDescriptor<bTValArray_long> CrimeCount = new PropertyDescriptor<>("CrimeCount", bTValArray_long.class, "bTValArray<long>", "", gCEnclave_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<gEPlayerCrime>> KnownPlayerCrime = new PropertyDescriptor<>("KnownPlayerCrime", bTPropertyContainer.class, "bTPropertyContainer<enum gEPlayerCrime>", "", gCEnclave_PS.class);
		public static final PropertyDescriptor<gBool> MusicPlayed = new PropertyDescriptor<>("MusicPlayed", gBool.class, "bool", "", gCEnclave_PS.class);
		public static final PropertyDescriptor<gLong> PlayerFame = new PropertyDescriptor<>("PlayerFame", gLong.class, "long", "", gCEnclave_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<gEPoliticalAlignment>> PoliticalAlignment = new PropertyDescriptor<>("PoliticalAlignment", bTPropertyContainer.class, "bTPropertyContainer<enum gEPoliticalAlignment>", "", gCEnclave_PS.class);
		public static final PropertyDescriptor<gBool> Raid = new PropertyDescriptor<>("Raid", gBool.class, "bool", "", gCEnclave_PS.class);
		public static final PropertyDescriptor<gBool> Revolution = new PropertyDescriptor<>("Revolution", gBool.class, "bool", "", gCEnclave_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<gEEnclaveStatus>> Status = new PropertyDescriptor<>("Status", bTPropertyContainer.class, "bTPropertyContainer<enum gEEnclaveStatus>", "", gCEnclave_PS.class);
		public static final PropertyDescriptor<bTValArray_long> SuspectComment = new PropertyDescriptor<>("SuspectComment", bTValArray_long.class, "bTValArray<long>", "", gCEnclave_PS.class);
		public static final PropertyDescriptor<gBool> Switched = new PropertyDescriptor<>("Switched", gBool.class, "bool", "", gCEnclave_PS.class);
	}

	public static interface gCEventScript_PS extends eCTrigger_PS {
		public static final PropertyDescriptor<bCString> ScriptConditionFunc = new PropertyDescriptor<>("ScriptConditionFunc", bCString.class, "bCString", "", gCEventScript_PS.class);
		public static final PropertyDescriptor<bCString> ScriptTriggerFunc = new PropertyDescriptor<>("ScriptTriggerFunc", bCString.class, "bCString", "", gCEventScript_PS.class);
		public static final PropertyDescriptor<bCString> ScriptUntriggerFunc = new PropertyDescriptor<>("ScriptUntriggerFunc", bCString.class, "bCString", "", gCEventScript_PS.class);
	}

	public static interface gCFlock_PS extends eCEntityPropertySet {
		public static final PropertyDescriptor<gBool> AmbientAniPlaying = new PropertyDescriptor<>("AmbientAniPlaying", gBool.class, "bool", "Internal", gCFlock_PS.class);
		public static final PropertyDescriptor<gFloat> AmbientDuration = new PropertyDescriptor<>("AmbientDuration", gFloat.class, "float", "Internal", gCFlock_PS.class);
		public static final PropertyDescriptor<gFloat> AmbientTimer = new PropertyDescriptor<>("AmbientTimer", gFloat.class, "float", "Internal", gCFlock_PS.class);
		public static final PropertyDescriptor<bCString> CurrentAni = new PropertyDescriptor<>("CurrentAni", bCString.class, "bCAnimationResourceString", "Internal", gCFlock_PS.class);
		public static final PropertyDescriptor<gFloat> DistanceDampener = new PropertyDescriptor<>("DistanceDampener", gFloat.class, "float", "Internal", gCFlock_PS.class);
		public static final PropertyDescriptor<bCString> FallAni = new PropertyDescriptor<>("FallAni", bCString.class, "bCAnimationResourceString", "", gCFlock_PS.class);
		public static final PropertyDescriptor<gFloat> FollowTimer = new PropertyDescriptor<>("FollowTimer", gFloat.class, "float", "Internal", gCFlock_PS.class);
		public static final PropertyDescriptor<gBool> FoundGround = new PropertyDescriptor<>("FoundGround", gBool.class, "bool", "Internal", gCFlock_PS.class);
		public static final PropertyDescriptor<gFloat> FreeWillPropabilitySecsMax = new PropertyDescriptor<>("FreeWillPropabilitySecsMax", gFloat.class, "float", "", gCFlock_PS.class);
		public static final PropertyDescriptor<gFloat> FreeWillPropabilitySecsMin = new PropertyDescriptor<>("FreeWillPropabilitySecsMin", gFloat.class, "float", "", gCFlock_PS.class);
		public static final PropertyDescriptor<gFloat> FreeWillTimer = new PropertyDescriptor<>("FreeWillTimer", gFloat.class, "float", "Internal", gCFlock_PS.class);
		public static final PropertyDescriptor<gFloat> GroundTimer = new PropertyDescriptor<>("GroundTimer", gFloat.class, "float", "Internal", gCFlock_PS.class);
		public static final PropertyDescriptor<gBool> IsDead = new PropertyDescriptor<>("IsDead", gBool.class, "bool", "Internal", gCFlock_PS.class);
		public static final PropertyDescriptor<gBool> IsGround = new PropertyDescriptor<>("IsGround", gBool.class, "bool", "", gCFlock_PS.class);
		public static final PropertyDescriptor<gBool> IsLeader = new PropertyDescriptor<>("IsLeader", gBool.class, "bool", "", gCFlock_PS.class);
		public static final PropertyDescriptor<bCString> LandAni = new PropertyDescriptor<>("LandAni", bCString.class, "bCAnimationResourceString", "", gCFlock_PS.class);
		public static final PropertyDescriptor<gLong> MaxMembers = new PropertyDescriptor<>("MaxMembers", gLong.class, "unsigned long", "", gCFlock_PS.class);
		public static final PropertyDescriptor<gLong> MinMembers = new PropertyDescriptor<>("MinMembers", gLong.class, "unsigned long", "", gCFlock_PS.class);
		public static final PropertyDescriptor<bCString> MoveAni = new PropertyDescriptor<>("MoveAni", bCString.class, "bCAnimationResourceString", "", gCFlock_PS.class);
		public static final PropertyDescriptor<gFloat> NeighbourDist = new PropertyDescriptor<>("NeighbourDist", gFloat.class, "float", "", gCFlock_PS.class);
		public static final PropertyDescriptor<gFloat> NeighbourVeloScale = new PropertyDescriptor<>("NeighbourVeloScale", gFloat.class, "float", "", gCFlock_PS.class);
		public static final PropertyDescriptor<gFloat> POIVeloScale = new PropertyDescriptor<>("POIVeloScale", gFloat.class, "float", "", gCFlock_PS.class);
		public static final PropertyDescriptor<bCString> PerchAni = new PropertyDescriptor<>("PerchAni", bCString.class, "bCAnimationResourceString", "", gCFlock_PS.class);
		public static final PropertyDescriptor<bCString> Roam2Ani = new PropertyDescriptor<>("Roam2Ani", bCString.class, "bCAnimationResourceString", "", gCFlock_PS.class);
		public static final PropertyDescriptor<bCString> RoamAni = new PropertyDescriptor<>("RoamAni", bCString.class, "bCAnimationResourceString", "", gCFlock_PS.class);
		public static final PropertyDescriptor<gFloat> RoamPropabilitySecsMax = new PropertyDescriptor<>("RoamPropabilitySecsMax", gFloat.class, "float", "", gCFlock_PS.class);
		public static final PropertyDescriptor<gFloat> RoamPropabilitySecsMin = new PropertyDescriptor<>("RoamPropabilitySecsMin", gFloat.class, "float", "", gCFlock_PS.class);
		public static final PropertyDescriptor<bCString> ScatterAni = new PropertyDescriptor<>("ScatterAni", bCString.class, "bCAnimationResourceString", "", gCFlock_PS.class);
		public static final PropertyDescriptor<gFloat> SeekMemberDist = new PropertyDescriptor<>("SeekMemberDist", gFloat.class, "float", "", gCFlock_PS.class);
		public static final PropertyDescriptor<bCString> StartAni = new PropertyDescriptor<>("StartAni", bCString.class, "bCAnimationResourceString", "", gCFlock_PS.class);
		public static final PropertyDescriptor<bCVector> TouchNormal = new PropertyDescriptor<>("TouchNormal", bCVector.class, "bCVector", "Internal", gCFlock_PS.class);
	}

	public static interface gCFocus_PS extends eCEntityPropertySet {
		public static final PropertyDescriptor<gBool> AutoCollectFocus = new PropertyDescriptor<>("AutoCollectFocus", gBool.class, "bool", "Advanced", gCFocus_PS.class);
		public static final PropertyDescriptor<eCEntityProxy> CurrentEntity = new PropertyDescriptor<>("CurrentEntity", eCEntityProxy.class, "eCEntityProxy", "Status", gCFocus_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<gEFocus>> CurrentMode = new PropertyDescriptor<>("CurrentMode", bTPropertyContainer.class, "bTPropertyContainer<enum gEFocus>", "Status", gCFocus_PS.class);
		public static final PropertyDescriptor<gBool> DrawFocusName = new PropertyDescriptor<>("DrawFocusName", gBool.class, "bool", "DisplayType", gCFocus_PS.class);
		public static final PropertyDescriptor<gBool> EnableRangeRating = new PropertyDescriptor<>("EnableRangeRating", gBool.class, "bool", "Status", gCFocus_PS.class);
		public static final PropertyDescriptor<gFloat> FocusLockMaxDistance = new PropertyDescriptor<>("FocusLockMaxDistance", gFloat.class, "float", "Advanced", gCFocus_PS.class);
		public static final PropertyDescriptor<gBool> FocusLocked = new PropertyDescriptor<>("FocusLocked", gBool.class, "bool", "Status", gCFocus_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<gEFocusLookAtKeysFOR>> FocusLookAtKeysFOR = new PropertyDescriptor<>("FocusLookAtKeysFOR", bTPropertyContainer.class, "bTPropertyContainer<enum gEFocusLookAtKeysFOR>", "LookAtMode", gCFocus_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<gEFocusLookAt>> FocusLookAtMode = new PropertyDescriptor<>("FocusLookAtMode", bTPropertyContainer.class, "bTPropertyContainer<enum gEFocusLookAt>", "LookAtMode", gCFocus_PS.class);
		public static final PropertyDescriptor<gFloat> HardFocusMaxAzi = new PropertyDescriptor<>("HardFocusMaxAzi", gFloat.class, "float", "HardMode", gCFocus_PS.class);
		public static final PropertyDescriptor<gFloat> HardFocusMaxElev = new PropertyDescriptor<>("HardFocusMaxElev", gFloat.class, "float", "HardMode", gCFocus_PS.class);
		public static final PropertyDescriptor<gFloat> HardFocusMaxRange = new PropertyDescriptor<>("HardFocusMaxRange", gFloat.class, "float", "HardMode", gCFocus_PS.class);
		public static final PropertyDescriptor<gBool> HighlightFocus = new PropertyDescriptor<>("HighlightFocus", gBool.class, "bool", "DisplayType", gCFocus_PS.class);
		public static final PropertyDescriptor<gBool> HighlightFocusInFight = new PropertyDescriptor<>("HighlightFocusInFight", gBool.class, "bool", "DisplayType", gCFocus_PS.class);
		public static final PropertyDescriptor<gFloat> InteractHighPriorityBonus = new PropertyDescriptor<>("InteractHighPriorityBonus", gFloat.class, "float", "Advanced", gCFocus_PS.class);
		public static final PropertyDescriptor<gFloat> MagicModeCharacterPriority = new PropertyDescriptor<>("MagicModeCharacterPriority", gFloat.class, "float", "MagicMode", gCFocus_PS.class);
		public static final PropertyDescriptor<gFloat> MagicModeCharacterScanAzi = new PropertyDescriptor<>("MagicModeCharacterScanAzi", gFloat.class, "float", "MagicMode", gCFocus_PS.class);
		public static final PropertyDescriptor<gFloat> MagicModeCharacterScanElevDown = new PropertyDescriptor<>("MagicModeCharacterScanElevDown", gFloat.class, "float", "MagicMode", gCFocus_PS.class);
		public static final PropertyDescriptor<gFloat> MagicModeCharacterScanElevUp = new PropertyDescriptor<>("MagicModeCharacterScanElevUp", gFloat.class, "float", "MagicMode", gCFocus_PS.class);
		public static final PropertyDescriptor<gFloat> MagicModeCharacterScanRange = new PropertyDescriptor<>("MagicModeCharacterScanRange", gFloat.class, "float", "MagicMode", gCFocus_PS.class);
		public static final PropertyDescriptor<gFloat> MagicModeInteractPriority = new PropertyDescriptor<>("MagicModeInteractPriority", gFloat.class, "float", "MagicMode", gCFocus_PS.class);
		public static final PropertyDescriptor<gFloat> MagicModeInteractScanAzi = new PropertyDescriptor<>("MagicModeInteractScanAzi", gFloat.class, "float", "MagicMode", gCFocus_PS.class);
		public static final PropertyDescriptor<gFloat> MagicModeInteractScanElevDown = new PropertyDescriptor<>("MagicModeInteractScanElevDown", gFloat.class, "float", "MagicMode", gCFocus_PS.class);
		public static final PropertyDescriptor<gFloat> MagicModeInteractScanElevUp = new PropertyDescriptor<>("MagicModeInteractScanElevUp", gFloat.class, "float", "MagicMode", gCFocus_PS.class);
		public static final PropertyDescriptor<gFloat> MagicModeInteractScanRange = new PropertyDescriptor<>("MagicModeInteractScanRange", gFloat.class, "float", "MagicMode", gCFocus_PS.class);
		public static final PropertyDescriptor<gFloat> MagicModeItemPriority = new PropertyDescriptor<>("MagicModeItemPriority", gFloat.class, "float", "MagicMode", gCFocus_PS.class);
		public static final PropertyDescriptor<gFloat> MagicModeItemScanAzi = new PropertyDescriptor<>("MagicModeItemScanAzi", gFloat.class, "float", "MagicMode", gCFocus_PS.class);
		public static final PropertyDescriptor<gFloat> MagicModeItemScanElevDown = new PropertyDescriptor<>("MagicModeItemScanElevDown", gFloat.class, "float", "MagicMode", gCFocus_PS.class);
		public static final PropertyDescriptor<gFloat> MagicModeItemScanElevUp = new PropertyDescriptor<>("MagicModeItemScanElevUp", gFloat.class, "float", "MagicMode", gCFocus_PS.class);
		public static final PropertyDescriptor<gFloat> MagicModeItemScanRange = new PropertyDescriptor<>("MagicModeItemScanRange", gFloat.class, "float", "MagicMode", gCFocus_PS.class);
		public static final PropertyDescriptor<gFloat> MeleeModeCharacterPriority = new PropertyDescriptor<>("MeleeModeCharacterPriority", gFloat.class, "float", "MeleeMode", gCFocus_PS.class);
		public static final PropertyDescriptor<gFloat> MeleeModeCharacterScanAzi = new PropertyDescriptor<>("MeleeModeCharacterScanAzi", gFloat.class, "float", "MeleeMode", gCFocus_PS.class);
		public static final PropertyDescriptor<gFloat> MeleeModeCharacterScanElevDown = new PropertyDescriptor<>("MeleeModeCharacterScanElevDown", gFloat.class, "float", "MeleeMode", gCFocus_PS.class);
		public static final PropertyDescriptor<gFloat> MeleeModeCharacterScanElevUp = new PropertyDescriptor<>("MeleeModeCharacterScanElevUp", gFloat.class, "float", "MeleeMode", gCFocus_PS.class);
		public static final PropertyDescriptor<gFloat> MeleeModeCharacterScanRange = new PropertyDescriptor<>("MeleeModeCharacterScanRange", gFloat.class, "float", "MeleeMode", gCFocus_PS.class);
		public static final PropertyDescriptor<gFloat> MeleeModeInteractPriority = new PropertyDescriptor<>("MeleeModeInteractPriority", gFloat.class, "float", "MeleeMode", gCFocus_PS.class);
		public static final PropertyDescriptor<gFloat> MeleeModeInteractScanAzi = new PropertyDescriptor<>("MeleeModeInteractScanAzi", gFloat.class, "float", "MeleeMode", gCFocus_PS.class);
		public static final PropertyDescriptor<gFloat> MeleeModeInteractScanElevDown = new PropertyDescriptor<>("MeleeModeInteractScanElevDown", gFloat.class, "float", "MeleeMode", gCFocus_PS.class);
		public static final PropertyDescriptor<gFloat> MeleeModeInteractScanElevUp = new PropertyDescriptor<>("MeleeModeInteractScanElevUp", gFloat.class, "float", "MeleeMode", gCFocus_PS.class);
		public static final PropertyDescriptor<gFloat> MeleeModeInteractScanRange = new PropertyDescriptor<>("MeleeModeInteractScanRange", gFloat.class, "float", "MeleeMode", gCFocus_PS.class);
		public static final PropertyDescriptor<gFloat> MeleeModeItemPriority = new PropertyDescriptor<>("MeleeModeItemPriority", gFloat.class, "float", "MeleeMode", gCFocus_PS.class);
		public static final PropertyDescriptor<gFloat> MeleeModeItemScanAzi = new PropertyDescriptor<>("MeleeModeItemScanAzi", gFloat.class, "float", "MeleeMode", gCFocus_PS.class);
		public static final PropertyDescriptor<gFloat> MeleeModeItemScanElevDown = new PropertyDescriptor<>("MeleeModeItemScanElevDown", gFloat.class, "float", "MeleeMode", gCFocus_PS.class);
		public static final PropertyDescriptor<gFloat> MeleeModeItemScanElevUp = new PropertyDescriptor<>("MeleeModeItemScanElevUp", gFloat.class, "float", "MeleeMode", gCFocus_PS.class);
		public static final PropertyDescriptor<gFloat> MeleeModeItemScanRange = new PropertyDescriptor<>("MeleeModeItemScanRange", gFloat.class, "float", "MeleeMode", gCFocus_PS.class);
		public static final PropertyDescriptor<gFloat> NormalModeCharacterPriority = new PropertyDescriptor<>("NormalModeCharacterPriority", gFloat.class, "float", "NormalMode", gCFocus_PS.class);
		public static final PropertyDescriptor<gFloat> NormalModeCharacterScanAzi = new PropertyDescriptor<>("NormalModeCharacterScanAzi", gFloat.class, "float", "NormalMode", gCFocus_PS.class);
		public static final PropertyDescriptor<gFloat> NormalModeCharacterScanElevDown = new PropertyDescriptor<>("NormalModeCharacterScanElevDown", gFloat.class, "float", "NormalMode", gCFocus_PS.class);
		public static final PropertyDescriptor<gFloat> NormalModeCharacterScanElevUp = new PropertyDescriptor<>("NormalModeCharacterScanElevUp", gFloat.class, "float", "NormalMode", gCFocus_PS.class);
		public static final PropertyDescriptor<gFloat> NormalModeCharacterScanRange = new PropertyDescriptor<>("NormalModeCharacterScanRange", gFloat.class, "float", "NormalMode", gCFocus_PS.class);
		public static final PropertyDescriptor<gFloat> NormalModeInteractPriority = new PropertyDescriptor<>("NormalModeInteractPriority", gFloat.class, "float", "NormalMode", gCFocus_PS.class);
		public static final PropertyDescriptor<gFloat> NormalModeInteractScanAzi = new PropertyDescriptor<>("NormalModeInteractScanAzi", gFloat.class, "float", "NormalMode", gCFocus_PS.class);
		public static final PropertyDescriptor<gFloat> NormalModeInteractScanElevDown = new PropertyDescriptor<>("NormalModeInteractScanElevDown", gFloat.class, "float", "NormalMode", gCFocus_PS.class);
		public static final PropertyDescriptor<gFloat> NormalModeInteractScanElevUp = new PropertyDescriptor<>("NormalModeInteractScanElevUp", gFloat.class, "float", "NormalMode", gCFocus_PS.class);
		public static final PropertyDescriptor<gFloat> NormalModeInteractScanRange = new PropertyDescriptor<>("NormalModeInteractScanRange", gFloat.class, "float", "NormalMode", gCFocus_PS.class);
		public static final PropertyDescriptor<gFloat> NormalModeItemPriority = new PropertyDescriptor<>("NormalModeItemPriority", gFloat.class, "float", "NormalMode", gCFocus_PS.class);
		public static final PropertyDescriptor<gFloat> NormalModeItemScanAzi = new PropertyDescriptor<>("NormalModeItemScanAzi", gFloat.class, "float", "NormalMode", gCFocus_PS.class);
		public static final PropertyDescriptor<gFloat> NormalModeItemScanElevDown = new PropertyDescriptor<>("NormalModeItemScanElevDown", gFloat.class, "float", "NormalMode", gCFocus_PS.class);
		public static final PropertyDescriptor<gFloat> NormalModeItemScanElevUp = new PropertyDescriptor<>("NormalModeItemScanElevUp", gFloat.class, "float", "NormalMode", gCFocus_PS.class);
		public static final PropertyDescriptor<gFloat> NormalModeItemScanRange = new PropertyDescriptor<>("NormalModeItemScanRange", gFloat.class, "float", "NormalMode", gCFocus_PS.class);
		public static final PropertyDescriptor<gFloat> NormalModePreferedRange = new PropertyDescriptor<>("NormalModePreferedRange", gFloat.class, "float", "Advanced", gCFocus_PS.class);
		public static final PropertyDescriptor<gFloat> OverrideCharacterPriority = new PropertyDescriptor<>("OverrideCharacterPriority", gFloat.class, "float", "Status", gCFocus_PS.class);
		public static final PropertyDescriptor<gFloat> OverrideInteractPriority = new PropertyDescriptor<>("OverrideInteractPriority", gFloat.class, "float", "Status", gCFocus_PS.class);
		public static final PropertyDescriptor<gFloat> OverrideItemPriority = new PropertyDescriptor<>("OverrideItemPriority", gFloat.class, "float", "Status", gCFocus_PS.class);
		public static final PropertyDescriptor<gFloat> OverrideRange = new PropertyDescriptor<>("OverrideRange", gFloat.class, "float", "Status", gCFocus_PS.class);
		public static final PropertyDescriptor<gFloat> RangedModeCharacterPriority = new PropertyDescriptor<>("RangedModeCharacterPriority", gFloat.class, "float", "RangedMode", gCFocus_PS.class);
		public static final PropertyDescriptor<gFloat> RangedModeCharacterScanAzi = new PropertyDescriptor<>("RangedModeCharacterScanAzi", gFloat.class, "float", "RangedMode", gCFocus_PS.class);
		public static final PropertyDescriptor<gFloat> RangedModeCharacterScanElevDown = new PropertyDescriptor<>("RangedModeCharacterScanElevDown", gFloat.class, "float", "RangedMode", gCFocus_PS.class);
		public static final PropertyDescriptor<gFloat> RangedModeCharacterScanElevUp = new PropertyDescriptor<>("RangedModeCharacterScanElevUp", gFloat.class, "float", "RangedMode", gCFocus_PS.class);
		public static final PropertyDescriptor<gFloat> RangedModeCharacterScanRange = new PropertyDescriptor<>("RangedModeCharacterScanRange", gFloat.class, "float", "RangedMode", gCFocus_PS.class);
		public static final PropertyDescriptor<gFloat> RangedModeInteractPriority = new PropertyDescriptor<>("RangedModeInteractPriority", gFloat.class, "float", "RangedMode", gCFocus_PS.class);
		public static final PropertyDescriptor<gFloat> RangedModeInteractScanAzi = new PropertyDescriptor<>("RangedModeInteractScanAzi", gFloat.class, "float", "RangedMode", gCFocus_PS.class);
		public static final PropertyDescriptor<gFloat> RangedModeInteractScanElevDown = new PropertyDescriptor<>("RangedModeInteractScanElevDown", gFloat.class, "float", "RangedMode", gCFocus_PS.class);
		public static final PropertyDescriptor<gFloat> RangedModeInteractScanElevUp = new PropertyDescriptor<>("RangedModeInteractScanElevUp", gFloat.class, "float", "RangedMode", gCFocus_PS.class);
		public static final PropertyDescriptor<gFloat> RangedModeInteractScanRange = new PropertyDescriptor<>("RangedModeInteractScanRange", gFloat.class, "float", "RangedMode", gCFocus_PS.class);
		public static final PropertyDescriptor<gFloat> RangedModeItemPriority = new PropertyDescriptor<>("RangedModeItemPriority", gFloat.class, "float", "RangedMode", gCFocus_PS.class);
		public static final PropertyDescriptor<gFloat> RangedModeItemScanAzi = new PropertyDescriptor<>("RangedModeItemScanAzi", gFloat.class, "float", "RangedMode", gCFocus_PS.class);
		public static final PropertyDescriptor<gFloat> RangedModeItemScanElevDown = new PropertyDescriptor<>("RangedModeItemScanElevDown", gFloat.class, "float", "RangedMode", gCFocus_PS.class);
		public static final PropertyDescriptor<gFloat> RangedModeItemScanElevUp = new PropertyDescriptor<>("RangedModeItemScanElevUp", gFloat.class, "float", "RangedMode", gCFocus_PS.class);
		public static final PropertyDescriptor<gFloat> RangedModeItemScanRange = new PropertyDescriptor<>("RangedModeItemScanRange", gFloat.class, "float", "RangedMode", gCFocus_PS.class);
	}

	public static interface gCGeometryLayer extends gCLayerBase {
		public static final PropertyDescriptor<bTPropertyContainer<gEGeometryType>> GeometryType = new PropertyDescriptor<>("GeometryType", bTPropertyContainer.class, "bTPropertyContainer<enum gEGeometryType>", "", gCGeometryLayer.class);
		public static final PropertyDescriptor<bCString> OriginImportName = new PropertyDescriptor<>("OriginImportName", bCString.class, "bCString", "", gCGeometryLayer.class);
	}

	public static interface gCInfoManager_PS extends eCEntityPropertySet {
		public static final PropertyDescriptor<gBool> ChildMode = new PropertyDescriptor<>("ChildMode", gBool.class, "bool", "", gCInfoManager_PS.class);
		public static final PropertyDescriptor<gBool> EndRequested = new PropertyDescriptor<>("EndRequested", gBool.class, "bool", "", gCInfoManager_PS.class);
		public static final PropertyDescriptor<gInt> CurrentInfoIndex = new PropertyDescriptor<>("CurrentInfoIndex", gInt.class, "int", "", gCInfoManager_PS.class);
		public static final PropertyDescriptor<gBool> IsRunning = new PropertyDescriptor<>("IsRunning", gBool.class, "bool", "", gCInfoManager_PS.class);
		public static final PropertyDescriptor<gBool> LastInfoExecuted = new PropertyDescriptor<>("LastInfoExecuted", gBool.class, "bool", "", gCInfoManager_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<gESession_State>> LastSessionState = new PropertyDescriptor<>("LastSessionState", bTPropertyContainer.class, "bTPropertyContainer<enum gESession_State>", "", gCInfoManager_PS.class);
		public static final PropertyDescriptor<gBool> WaitForScript = new PropertyDescriptor<>("WaitForScript", gBool.class, "bool", "", gCInfoManager_PS.class);
	}

	public static interface gCInfoScriptLine_PS extends ClassDescriptor {
		public static final PropertyDescriptor<bCString> ID1 = new PropertyDescriptor<>("ID1", bCString.class, "bCString", "", gCInfoScriptLine_PS.class);
		public static final PropertyDescriptor<bCString> ID2 = new PropertyDescriptor<>("ID2", bCString.class, "bCString", "", gCInfoScriptLine_PS.class);
		public static final PropertyDescriptor<bCString> Entity1 = new PropertyDescriptor<>("Entity1", bCString.class, "bCString", "", gCInfoScriptLine_PS.class);
		public static final PropertyDescriptor<bCString> Entity2 = new PropertyDescriptor<>("Entity2", bCString.class, "bCString", "", gCInfoScriptLine_PS.class);
		public static final PropertyDescriptor<bCString> Command = new PropertyDescriptor<>("Command", bCString.class, "bCString", "", gCInfoScriptLine_PS.class);
		public static final PropertyDescriptor<bCString> Text = new PropertyDescriptor<>("Text", bCString.class, "eCLocString", "", gCInfoScriptLine_PS.class);
	}

	public static interface gCInfoScript_PS extends ClassDescriptor {
		public static final PropertyDescriptor<Unknown> InfoScriptLines = new PropertyDescriptor<>("InfoScriptLines", Unknown.class, "bTObjArray<class bTAutoPOSmartPtr<class gCInfoScriptLine_PS> >", "", gCInfoScript_PS.class);
		public static final PropertyDescriptor<gBool> ScriptIsRunnig = new PropertyDescriptor<>("ScriptIsRunnig", gBool.class, "bool", "", gCInfoScript_PS.class);
		public static final PropertyDescriptor<gInt> CurrentScriptLine = new PropertyDescriptor<>("CurrentScriptLine", gInt.class, "int", "", gCInfoScript_PS.class);
	}

	public static interface gCInfo_PS extends ClassDescriptor {
		public static final PropertyDescriptor<bCString> TeachAttrib = new PropertyDescriptor<>("TeachAttrib", bCString.class, "bCString", "", gCInfo_PS.class);
		public static final PropertyDescriptor<gLong> TeachAttribValue = new PropertyDescriptor<>("TeachAttribValue", gLong.class, "long", "", gCInfo_PS.class);
		public static final PropertyDescriptor<gBool> ClearChildren = new PropertyDescriptor<>("ClearChildren", gBool.class, "bool", "", gCInfo_PS.class);
		public static final PropertyDescriptor<bTObjArray_bCString> CondHasSkill = new PropertyDescriptor<>("CondHasSkill", bTObjArray_bCString.class, "bTObjArray<class bCString>", "", gCInfo_PS.class);
		public static final PropertyDescriptor<bTValArray_long> CondItemAmounts = new PropertyDescriptor<>("CondItemAmounts", bTValArray_long.class, "bTValArray<unsigned long>", "", gCInfo_PS.class);
		public static final PropertyDescriptor<bTObjArray_bCString> CondItems = new PropertyDescriptor<>("CondItems", bTObjArray_bCString.class, "bTObjArray<class bCString>", "", gCInfo_PS.class);
		public static final PropertyDescriptor<bTObjArray_bCString> CondPAL = new PropertyDescriptor<>("CondPAL", bTObjArray_bCString.class, "bTObjArray<class bCString>", "", gCInfo_PS.class);
		public static final PropertyDescriptor<bTObjArray_bCString> ConditionPlayerKnows = new PropertyDescriptor<>("ConditionPlayerKnows", bTObjArray_bCString.class, "bTObjArray<class bCString>", "", gCInfo_PS.class);
		public static final PropertyDescriptor<bTObjArray_bCString> ConditionPlayerKnowsNot = new PropertyDescriptor<>("ConditionPlayerKnowsNot", bTObjArray_bCString.class, "bTObjArray<class bCString>", "", gCInfo_PS.class);
		public static final PropertyDescriptor<bTObjArray_bCString> CondPlayerPartyMember = new PropertyDescriptor<>("CondPlayerPartyMember", bTObjArray_bCString.class, "bTObjArray<class bCString>", "", gCInfo_PS.class);
		public static final PropertyDescriptor<bTValArray_long> CondReputAmount = new PropertyDescriptor<>("CondReputAmount", bTValArray_long.class, "bTObjArray<unsigned long>", "", gCInfo_PS.class);
		public static final PropertyDescriptor<bTObjArray_bCString> CondReputGroup = new PropertyDescriptor<>("CondReputGroup", bTObjArray_bCString.class, "bTObjArray<class bCString>", "", gCInfo_PS.class);
		public static final PropertyDescriptor<bTObjArray_bCString> CondReputRelation = new PropertyDescriptor<>("CondReputRelation", bTObjArray_bCString.class, "bTObjArray<class bCString>", "", gCInfo_PS.class);
		public static final PropertyDescriptor<bTObjArray_bCString> CondSecondaryNPC = new PropertyDescriptor<>("CondSecondaryNPC", bTObjArray_bCString.class, "bTObjArray<class bCString>", "", gCInfo_PS.class);
		public static final PropertyDescriptor<bTValArray_long> CondSecondaryNPCStatus = new PropertyDescriptor<>("CondSecondaryNPCStatus", bTValArray_long.class, "bTValArray<unsigned long>", "", gCInfo_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<gEInfoCondType>> ConditionType = new PropertyDescriptor<>("ConditionType", bTPropertyContainer.class, "bTPropertyContainer<enum gEInfoCondType>", "", gCInfo_PS.class);
		public static final PropertyDescriptor<bTObjArray_bCString> CondWearsItem = new PropertyDescriptor<>("CondWearsItem", bTObjArray_bCString.class, "bTObjArray<class bCString>", "", gCInfo_PS.class);
		public static final PropertyDescriptor<bCString> Folder = new PropertyDescriptor<>("Folder", bCString.class, "bCString", "", gCInfo_PS.class);
		public static final PropertyDescriptor<gLong> SortID = new PropertyDescriptor<>("SortID", gLong.class, "unsigned long", "", gCInfo_PS.class);
		public static final PropertyDescriptor<gBool> InfoGiven = new PropertyDescriptor<>("InfoGiven", gBool.class, "bool", "", gCInfo_PS.class);
		public static final PropertyDescriptor<gLong> GoldCost = new PropertyDescriptor<>("GoldCost", gLong.class, "long", "", gCInfo_PS.class);
		public static final PropertyDescriptor<bCString> ConditionItemContainer = new PropertyDescriptor<>("ConditionItemContainer", bCString.class, "bCString", "", gCInfo_PS.class);
		public static final PropertyDescriptor<bCString> Name = new PropertyDescriptor<>("Name", bCString.class, "bCString", "", gCInfo_PS.class);
		public static final PropertyDescriptor<bCString> ConditionOwnerNearEntity = new PropertyDescriptor<>("ConditionOwnerNearEntity", bCString.class, "bCString", "", gCInfo_PS.class);
		public static final PropertyDescriptor<bCString> Parent = new PropertyDescriptor<>("Parent", bCString.class, "bCString", "", gCInfo_PS.class);
		public static final PropertyDescriptor<gBool> Permanent = new PropertyDescriptor<>("Permanent", gBool.class, "bool", "", gCInfo_PS.class);
		public static final PropertyDescriptor<bCString> Quest = new PropertyDescriptor<>("Quest", bCString.class, "bCString", "", gCInfo_PS.class);
		public static final PropertyDescriptor<bTPropertyObject> InfoScript = new PropertyDescriptor<>("InfoScript", bTPropertyObject.class, "bTPropertyObject<class gCInfoScript_PS,class bCObjectRefBase>", "", gCInfo_PS.class);
		public static final PropertyDescriptor<eCEntityProxy> TeachSkill = new PropertyDescriptor<>("TeachSkill", eCEntityProxy.class, "eCTemplateEntityProxy", "", gCInfo_PS.class);
		public static final PropertyDescriptor<gBool> SuppressLog = new PropertyDescriptor<>("SuppressLog", gBool.class, "bool", "", gCInfo_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<gEInfoType>> Type = new PropertyDescriptor<>("Type", bTPropertyContainer.class, "bTPropertyContainer<enum gEInfoType>", "", gCInfo_PS.class);
		public static final PropertyDescriptor<bCString> Owner = new PropertyDescriptor<>("Owner", bCString.class, "bCString", "", gCInfo_PS.class);
	}

	public static interface gCInteraction_PS extends eCEntityPropertySet {
		public static final PropertyDescriptor<eCEntityProxy> AnchorPoint = new PropertyDescriptor<>("AnchorPoint", eCEntityProxy.class, "eCEntityProxy", "Intern", gCInteraction_PS.class);
		public static final PropertyDescriptor<bCString> EnterROIScript = new PropertyDescriptor<>("EnterROIScript", bCString.class, "bCString", "Script", gCInteraction_PS.class);
		public static final PropertyDescriptor<bCString> ExitROIScript = new PropertyDescriptor<>("ExitROIScript", bCString.class, "bCString", "Script", gCInteraction_PS.class);
		public static final PropertyDescriptor<bCString> FocusNameBone = new PropertyDescriptor<>("FocusNameBone", bCString.class, "bCString", "Focus", gCInteraction_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<gEFocusNameType>> FocusNameType = new PropertyDescriptor<>("FocusNameType", bTPropertyContainer.class, "bTPropertyContainer<enum gEFocusNameType>", "Focus", gCInteraction_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<gEFocusPriority>> FocusPriority = new PropertyDescriptor<>("FocusPriority", bTPropertyContainer.class, "bTPropertyContainer<enum gEFocusPriority>", "Focus", gCInteraction_PS.class);
		public static final PropertyDescriptor<bCVector> FocusViewOffset = new PropertyDescriptor<>("FocusViewOffset", bCVector.class, "bCVector", "Focus", gCInteraction_PS.class);
		public static final PropertyDescriptor<bCVector> FocusWorldOffset = new PropertyDescriptor<>("FocusWorldOffset", bCVector.class, "bCVector", "Focus", gCInteraction_PS.class);
		public static final PropertyDescriptor<eCEntityProxy> Owner = new PropertyDescriptor<>("Owner", eCEntityProxy.class, "eCEntityProxy", "Intern", gCInteraction_PS.class);
		public static final PropertyDescriptor<bCString> ScriptUseFunc = new PropertyDescriptor<>("ScriptUseFunc", bCString.class, "bCString", "Interact", gCInteraction_PS.class);
		public static final PropertyDescriptor<eCEntityProxy> Spell = new PropertyDescriptor<>("Spell", eCEntityProxy.class, "eCTemplateEntityProxy", "Script", gCInteraction_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<gEUseType>> UseType = new PropertyDescriptor<>("UseType", bTPropertyContainer.class, "bTPropertyContainer<enum gEUseType>", "Interact", gCInteraction_PS.class);
		public static final PropertyDescriptor<gBool> UsedByPlayer = new PropertyDescriptor<>("UsedByPlayer", gBool.class, "bool", "Intern", gCInteraction_PS.class);
		public static final PropertyDescriptor<eCEntityProxy> User = new PropertyDescriptor<>("User", eCEntityProxy.class, "eCEntityProxy", "Intern", gCInteraction_PS.class);
	}

	public static interface gCInventorySlot extends ClassDescriptor {
		public static final PropertyDescriptor<eCEntityProxy> Item = new PropertyDescriptor<>("Item", eCEntityProxy.class, "eCEntityProxy", "Instance", gCInventorySlot.class);
		public static final PropertyDescriptor<bTPropertyContainer<gESlot>> Slot = new PropertyDescriptor<>("Slot", bTPropertyContainer.class, "bTPropertyContainer<enum gESlot>", "Instance", gCInventorySlot.class);
		public static final PropertyDescriptor<eCEntityProxy> Template = new PropertyDescriptor<>("Template", eCEntityProxy.class, "eCTemplateEntityProxy", "Template", gCInventorySlot.class);
	}

	public static interface gCInventoryStack extends gCInventorySlot {
		public static final PropertyDescriptor<gInt> ActivationCount = new PropertyDescriptor<>("ActivationCount", gInt.class, "int", "Skill", gCInventoryStack.class);
		public static final PropertyDescriptor<gInt> Amount = new PropertyDescriptor<>("Amount", gInt.class, "int", "Instance", gCInventoryStack.class);
		public static final PropertyDescriptor<gBool> Learned = new PropertyDescriptor<>("Learned", gBool.class, "bool", "Skill", gCInventoryStack.class);
		public static final PropertyDescriptor<gInt> Quality = new PropertyDescriptor<>("Quality", gInt.class, "int", "Instance", gCInventoryStack.class);
		public static final PropertyDescriptor<gInt> QuickSlot = new PropertyDescriptor<>("QuickSlot", gInt.class, "int", "Instance", gCInventoryStack.class);
		public static final PropertyDescriptor<gInt> SortIndex = new PropertyDescriptor<>("SortIndex", gInt.class, "int", "Display", gCInventoryStack.class);
		public static final PropertyDescriptor<gInt> TransactionCount = new PropertyDescriptor<>("TransactionCount", gInt.class, "int", "Trade", gCInventoryStack.class);
		public static final PropertyDescriptor<bTPropertyContainer<gEStackType>> Generated = new PropertyDescriptor<>("Generated", bTPropertyContainer.class, "bTPropertyContainer<enum gEStackType>", "Display", gCInventoryStack.class);
	}

	public static interface gCInventory_PS extends eCEntityPropertySet {
		public static final PropertyDescriptor<gBool> GeneratedPlunder = new PropertyDescriptor<>("GeneratedPlunder", gBool.class, "bool", "", gCInventory_PS.class);
		public static final PropertyDescriptor<gBool> GeneratedTrade = new PropertyDescriptor<>("GeneratedTrade", gBool.class, "bool", "", gCInventory_PS.class);
		public static final PropertyDescriptor<bCString> TreasureSet1 = new PropertyDescriptor<>("TreasureSet1", bCString.class, "bCString", "", gCInventory_PS.class);
		public static final PropertyDescriptor<bCString> TreasureSet2 = new PropertyDescriptor<>("TreasureSet2", bCString.class, "bCString", "", gCInventory_PS.class);
		public static final PropertyDescriptor<bCString> TreasureSet3 = new PropertyDescriptor<>("TreasureSet3", bCString.class, "bCString", "", gCInventory_PS.class);
		public static final PropertyDescriptor<bCString> TreasureSet4 = new PropertyDescriptor<>("TreasureSet4", bCString.class, "bCString", "", gCInventory_PS.class);
		public static final PropertyDescriptor<bCString> TreasureSet5 = new PropertyDescriptor<>("TreasureSet5", bCString.class, "bCString", "", gCInventory_PS.class);
	}

	public static interface gCItem_PS extends eCEntityPropertySet {
		public static final PropertyDescriptor<eCEntityProxy> ActivateSkill1 = new PropertyDescriptor<>("ActivateSkill1", eCEntityProxy.class, "eCTemplateEntityProxy", "Modifiers", gCItem_PS.class);
		public static final PropertyDescriptor<eCEntityProxy> ActivateSkill2 = new PropertyDescriptor<>("ActivateSkill2", eCEntityProxy.class, "eCTemplateEntityProxy", "Modifiers", gCItem_PS.class);
		public static final PropertyDescriptor<gLong> Amount = new PropertyDescriptor<>("Amount", gLong.class, "unsigned long", "General", gCItem_PS.class);
		public static final PropertyDescriptor<eCEntityProxy> ArmorSet = new PropertyDescriptor<>("ArmorSet", eCEntityProxy.class, "eCTemplateEntityProxy", "Behaviour", gCItem_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<gEItemCategory>> Category = new PropertyDescriptor<>("Category", bTPropertyContainer.class, "bTPropertyContainer<enum gEItemCategory>", "Display", gCItem_PS.class);
		public static final PropertyDescriptor<gBool> Dropped = new PropertyDescriptor<>("Dropped", gBool.class, "bool", "Transform", gCItem_PS.class);
		public static final PropertyDescriptor<gBool> FullBody = new PropertyDescriptor<>("FullBody", gBool.class, "bool", "Behaviour", gCItem_PS.class);
		public static final PropertyDescriptor<gLong> GoldValue = new PropertyDescriptor<>("GoldValue", gLong.class, "long", "General", gCItem_PS.class);
		public static final PropertyDescriptor<eCEntityProxy> ItemInventory = new PropertyDescriptor<>("ItemInventory", eCEntityProxy.class, "eCTemplateEntityProxy", "Transform", gCItem_PS.class);
		public static final PropertyDescriptor<gBool> MissionItem = new PropertyDescriptor<>("MissionItem", gBool.class, "bool", "General", gCItem_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<EAttribModOperation>> ModAttrib1Op = new PropertyDescriptor<>("ModAttrib1Op", bTPropertyContainer.class, "bTPropertyContainer<enum EAttribModOperation>", "Modifiers", gCItem_PS.class);
		public static final PropertyDescriptor<gInt> ModAttrib1Value = new PropertyDescriptor<>("ModAttrib1Value", gInt.class, "int", "Modifiers", gCItem_PS.class);
		public static final PropertyDescriptor<bCString> ModAttrib1Tag = new PropertyDescriptor<>("ModAttrib1Tag", bCString.class, "bCString", "Modifiers", gCItem_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<EAttribModOperation>> ModAttrib2Op = new PropertyDescriptor<>("ModAttrib2Op", bTPropertyContainer.class, "bTPropertyContainer<enum EAttribModOperation>", "Modifiers", gCItem_PS.class);
		public static final PropertyDescriptor<gInt> ModAttrib2Value = new PropertyDescriptor<>("ModAttrib2Value", gInt.class, "int", "Modifiers", gCItem_PS.class);
		public static final PropertyDescriptor<bCString> ModAttrib2Tag = new PropertyDescriptor<>("ModAttrib2Tag", bCString.class, "bCString", "Modifiers", gCItem_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<EAttribModOperation>> ModAttrib3Op = new PropertyDescriptor<>("ModAttrib3Op", bTPropertyContainer.class, "bTPropertyContainer<enum EAttribModOperation>", "Modifiers", gCItem_PS.class);
		public static final PropertyDescriptor<gInt> ModAttrib3Value = new PropertyDescriptor<>("ModAttrib3Value", gInt.class, "int", "Modifiers", gCItem_PS.class);
		public static final PropertyDescriptor<bCString> ModAttrib3Tag = new PropertyDescriptor<>("ModAttrib3Tag", bCString.class, "bCString", "Modifiers", gCItem_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<EAttribModOperation>> ModAttrib4Op = new PropertyDescriptor<>("ModAttrib4Op", bTPropertyContainer.class, "bTPropertyContainer<enum EAttribModOperation>", "Modifiers", gCItem_PS.class);
		public static final PropertyDescriptor<gInt> ModAttrib4Value = new PropertyDescriptor<>("ModAttrib4Value", gInt.class, "int", "Modifiers", gCItem_PS.class);
		public static final PropertyDescriptor<bCString> ModAttrib4Tag = new PropertyDescriptor<>("ModAttrib4Tag", bCString.class, "bCString", "Modifiers", gCItem_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<EAttribModOperation>> ModAttrib5Op = new PropertyDescriptor<>("ModAttrib5Op", bTPropertyContainer.class, "bTPropertyContainer<enum EAttribModOperation>", "Modifiers", gCItem_PS.class);
		public static final PropertyDescriptor<gInt> ModAttrib5Value = new PropertyDescriptor<>("ModAttrib5Value", gInt.class, "int", "Modifiers", gCItem_PS.class);
		public static final PropertyDescriptor<bCString> ModAttrib5Tag = new PropertyDescriptor<>("ModAttrib5Tag", bCString.class, "bCString", "Modifiers", gCItem_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<EAttribModOperation>> ModAttrib6Op = new PropertyDescriptor<>("ModAttrib6Op", bTPropertyContainer.class, "bTPropertyContainer<enum EAttribModOperation>", "Modifiers", gCItem_PS.class);
		public static final PropertyDescriptor<gInt> ModAttrib6Value = new PropertyDescriptor<>("ModAttrib6Value", gInt.class, "int", "Modifiers", gCItem_PS.class);
		public static final PropertyDescriptor<bCString> ModAttrib6Tag = new PropertyDescriptor<>("ModAttrib6Tag", bCString.class, "bCString", "Modifiers", gCItem_PS.class);
		public static final PropertyDescriptor<gBool> Permanent = new PropertyDescriptor<>("Permanent", gBool.class, "bool", "General", gCItem_PS.class);
		public static final PropertyDescriptor<gLong> Quality = new PropertyDescriptor<>("Quality", gLong.class, "unsigned long", "General", gCItem_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<EAttribReqOperation>> ReqAttrib1Op = new PropertyDescriptor<>("ReqAttrib1Op", bTPropertyContainer.class, "bTPropertyContainer<enum EAttribReqOperation>", "Requirements", gCItem_PS.class);
		public static final PropertyDescriptor<gInt> ReqAttrib1Value = new PropertyDescriptor<>("ReqAttrib1Value", gInt.class, "int", "Requirements", gCItem_PS.class);
		public static final PropertyDescriptor<bCString> ReqAttrib1Tag = new PropertyDescriptor<>("ReqAttrib1Tag", bCString.class, "bCString", "Requirements", gCItem_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<EAttribReqOperation>> ReqAttrib2Op = new PropertyDescriptor<>("ReqAttrib2Op", bTPropertyContainer.class, "bTPropertyContainer<enum EAttribReqOperation>", "Requirements", gCItem_PS.class);
		public static final PropertyDescriptor<gInt> ReqAttrib2Value = new PropertyDescriptor<>("ReqAttrib2Value", gInt.class, "int", "Requirements", gCItem_PS.class);
		public static final PropertyDescriptor<bCString> ReqAttrib2Tag = new PropertyDescriptor<>("ReqAttrib2Tag", bCString.class, "bCString", "Requirements", gCItem_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<EAttribReqOperation>> ReqAttrib3Op = new PropertyDescriptor<>("ReqAttrib3Op", bTPropertyContainer.class, "bTPropertyContainer<enum EAttribReqOperation>", "Requirements", gCItem_PS.class);
		public static final PropertyDescriptor<gInt> ReqAttrib3Value = new PropertyDescriptor<>("ReqAttrib3Value", gInt.class, "int", "Requirements", gCItem_PS.class);
		public static final PropertyDescriptor<bCString> ReqAttrib3Tag = new PropertyDescriptor<>("ReqAttrib3Tag", bCString.class, "bCString", "Requirements", gCItem_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<EAttribReqOperation>> ReqAttrib4Op = new PropertyDescriptor<>("ReqAttrib4Op", bTPropertyContainer.class, "bTPropertyContainer<enum EAttribReqOperation>", "Requirements", gCItem_PS.class);
		public static final PropertyDescriptor<gInt> ReqAttrib4Value = new PropertyDescriptor<>("ReqAttrib4Value", gInt.class, "int", "Requirements", gCItem_PS.class);
		public static final PropertyDescriptor<bCString> ReqAttrib4Tag = new PropertyDescriptor<>("ReqAttrib4Tag", bCString.class, "bCString", "Requirements", gCItem_PS.class);
		public static final PropertyDescriptor<eCEntityProxy> RequiredSkill1 = new PropertyDescriptor<>("RequiredSkill1", eCEntityProxy.class, "eCTemplateEntityProxy", "Requirements", gCItem_PS.class);
		public static final PropertyDescriptor<eCEntityProxy> RequiredSkill2 = new PropertyDescriptor<>("RequiredSkill2", eCEntityProxy.class, "eCTemplateEntityProxy", "Requirements", gCItem_PS.class);
		public static final PropertyDescriptor<gBool> Robe = new PropertyDescriptor<>("Robe", gBool.class, "bool", "Behaviour", gCItem_PS.class);
		public static final PropertyDescriptor<eCEntityProxy> Skill = new PropertyDescriptor<>("Skill", eCEntityProxy.class, "eCTemplateEntityProxy", "Behaviour", gCItem_PS.class);
		public static final PropertyDescriptor<eCEntityProxy> Spell = new PropertyDescriptor<>("Spell", eCEntityProxy.class, "eCTemplateEntityProxy", "Behaviour", gCItem_PS.class);
		public static final PropertyDescriptor<bCString> Texture = new PropertyDescriptor<>("Texture", bCString.class, "bCString", "Display", gCItem_PS.class);
		public static final PropertyDescriptor<eCEntityProxy> ItemWorld = new PropertyDescriptor<>("ItemWorld", eCEntityProxy.class, "eCTemplateEntityProxy", "Transform", gCItem_PS.class);
	}

	public static interface gCLayerBase extends eCProcessibleElement {
		public static final PropertyDescriptor<Unknown> SectorPtr = new PropertyDescriptor<>("SectorPtr", Unknown.class, "bTPOSmartPtr<class gCSector>", "", gCLayerBase.class);
	}

	public static interface gCLetter_PS extends eCEntityPropertySet {
		public static final PropertyDescriptor<bCString> Header = new PropertyDescriptor<>("Header", bCString.class, "eCLocString", "", gCLetter_PS.class);
		public static final PropertyDescriptor<bCString> Text = new PropertyDescriptor<>("Text", bCString.class, "eCLocString", "", gCLetter_PS.class);
	}

	public static interface gCLock_PS extends eCEntityPropertySet {
		public static final PropertyDescriptor<gInt> Difficulty = new PropertyDescriptor<>("Difficulty", gInt.class, "int", "", gCLock_PS.class);
		public static final PropertyDescriptor<eCEntityProxy> Key = new PropertyDescriptor<>("Key", eCEntityProxy.class, "eCTemplateEntityProxy", "Common", gCLock_PS.class);
		public static final PropertyDescriptor<gInt> KeyAmount = new PropertyDescriptor<>("KeyAmount", gInt.class, "int", "", gCLock_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<gELockStatus>> Status = new PropertyDescriptor<>("Status", bTPropertyContainer.class, "bTPropertyContainer<enum gELockStatus>", "Common", gCLock_PS.class);
		public static final PropertyDescriptor<bTObjArray_bTPropertyContainer<gETurnDirection>> Combination = new PropertyDescriptor<>("Combination", bTObjArray_bTPropertyContainer.class, "bTObjArray<class bTPropertyContainer<enum gETurnDirection> >", "", gCLock_PS.class);
	}

	public static interface gCMagicBarrier_PS extends eCEntityPropertySet {
		public static final PropertyDescriptor<bCString> OnEnterScript = new PropertyDescriptor<>("OnEnterScript", bCString.class, "bCString", "Script", gCMagicBarrier_PS.class);
		public static final PropertyDescriptor<bCString> OnInScript = new PropertyDescriptor<>("OnInScript", bCString.class, "bCString", "Script", gCMagicBarrier_PS.class);
		public static final PropertyDescriptor<bCString> OnLeaveScript = new PropertyDescriptor<>("OnLeaveScript", bCString.class, "bCString", "Script", gCMagicBarrier_PS.class);
		public static final PropertyDescriptor<bCString> OnToucheBarrier = new PropertyDescriptor<>("OnToucheBarrier", bCString.class, "bCString", "Script", gCMagicBarrier_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<gEMagicBarrierShape>> Shape = new PropertyDescriptor<>("Shape", bTPropertyContainer.class, "bTPropertyContainer<enum gEMagicBarrierShape>", "Shape", gCMagicBarrier_PS.class);
		public static final PropertyDescriptor<gFloat> Thickness = new PropertyDescriptor<>("Thickness", gFloat.class, "float", "Shape", gCMagicBarrier_PS.class);
	}

	public static interface gCMagic_PS extends eCEntityPropertySet {
		public static final PropertyDescriptor<bCString> AnimActionCast = new PropertyDescriptor<>("AnimActionCast", bCString.class, "bCString", "Animation", gCMagic_PS.class);
		public static final PropertyDescriptor<bCString> AnimActionPowerCast = new PropertyDescriptor<>("AnimActionPowerCast", bCString.class, "bCString", "Animation", gCMagic_PS.class);
		public static final PropertyDescriptor<gBool> AutoReload = new PropertyDescriptor<>("AutoReload", gBool.class, "bool", "Animation", gCMagic_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<gECastType>> CastType = new PropertyDescriptor<>("CastType", bTPropertyContainer.class, "bTPropertyContainer<enum gECastType>", "Animation", gCMagic_PS.class);
		public static final PropertyDescriptor<bCString> EffectCasterCast = new PropertyDescriptor<>("EffectCasterCast", bCString.class, "bCString", "Effects", gCMagic_PS.class);
		public static final PropertyDescriptor<bCString> EffectCasterHold = new PropertyDescriptor<>("EffectCasterHold", bCString.class, "bCString", "Effects", gCMagic_PS.class);
		public static final PropertyDescriptor<bCString> EffectCasterLoad = new PropertyDescriptor<>("EffectCasterLoad", bCString.class, "bCString", "Effects", gCMagic_PS.class);
		public static final PropertyDescriptor<bCString> EffectCasterPower = new PropertyDescriptor<>("EffectCasterPower", bCString.class, "bCString", "Effects", gCMagic_PS.class);
		public static final PropertyDescriptor<bCString> EffectItemCast = new PropertyDescriptor<>("EffectItemCast", bCString.class, "bCString", "Effects", gCMagic_PS.class);
		public static final PropertyDescriptor<bCString> EffectItemHold = new PropertyDescriptor<>("EffectItemHold", bCString.class, "bCString", "Effects", gCMagic_PS.class);
		public static final PropertyDescriptor<bCString> EffectItemLoad = new PropertyDescriptor<>("EffectItemLoad", bCString.class, "bCString", "Effects", gCMagic_PS.class);
		public static final PropertyDescriptor<bCString> EffectItemPower = new PropertyDescriptor<>("EffectItemPower", bCString.class, "bCString", "Effects", gCMagic_PS.class);
		public static final PropertyDescriptor<bCString> EffectTargetCast = new PropertyDescriptor<>("EffectTargetCast", bCString.class, "bCString", "Effects", gCMagic_PS.class);
		public static final PropertyDescriptor<bCString> EffectTargetLoad = new PropertyDescriptor<>("EffectTargetLoad", bCString.class, "bCString", "Effects", gCMagic_PS.class);
		public static final PropertyDescriptor<bCString> EffectTargetPower = new PropertyDescriptor<>("EffectTargetPower", bCString.class, "bCString", "Effects", gCMagic_PS.class);
		public static final PropertyDescriptor<gInt> LearnPoints = new PropertyDescriptor<>("LearnPoints", gInt.class, "int", "Requirements", gCMagic_PS.class);
		public static final PropertyDescriptor<gInt> MaxManaCost = new PropertyDescriptor<>("MaxManaCost", gInt.class, "int", "General", gCMagic_PS.class);
		public static final PropertyDescriptor<gInt> MinManaCost = new PropertyDescriptor<>("MinManaCost", gInt.class, "int", "General", gCMagic_PS.class);
		public static final PropertyDescriptor<gFloat> ReloadDelaySeconds = new PropertyDescriptor<>("ReloadDelaySeconds", gFloat.class, "float", "Animation", gCMagic_PS.class);
		public static final PropertyDescriptor<gInt> ReqAttrib1Value = new PropertyDescriptor<>("ReqAttrib1Value", gInt.class, "int", "Requirements", gCMagic_PS.class);
		public static final PropertyDescriptor<bCString> ReqAttrib1Tag = new PropertyDescriptor<>("ReqAttrib1Tag", bCString.class, "bCString", "Requirements", gCMagic_PS.class);
		public static final PropertyDescriptor<gInt> ReqAttrib2Value = new PropertyDescriptor<>("ReqAttrib2Value", gInt.class, "int", "Requirements", gCMagic_PS.class);
		public static final PropertyDescriptor<bCString> ReqAttrib2Tag = new PropertyDescriptor<>("ReqAttrib2Tag", bCString.class, "bCString", "Requirements", gCMagic_PS.class);
		public static final PropertyDescriptor<gInt> ReqAttrib3Value = new PropertyDescriptor<>("ReqAttrib3Value", gInt.class, "int", "Requirements", gCMagic_PS.class);
		public static final PropertyDescriptor<bCString> ReqAttrib3Tag = new PropertyDescriptor<>("ReqAttrib3Tag", bCString.class, "bCString", "Requirements", gCMagic_PS.class);
		public static final PropertyDescriptor<gInt> ReqAttrib4Value = new PropertyDescriptor<>("ReqAttrib4Value", gInt.class, "int", "Requirements", gCMagic_PS.class);
		public static final PropertyDescriptor<bCString> ReqAttrib4Tag = new PropertyDescriptor<>("ReqAttrib4Tag", bCString.class, "bCString", "Requirements", gCMagic_PS.class);
		public static final PropertyDescriptor<eCEntityProxy> ReqSkill1 = new PropertyDescriptor<>("ReqSkill1", eCEntityProxy.class, "eCTemplateEntityProxy", "Requirements", gCMagic_PS.class);
		public static final PropertyDescriptor<bCString> FuncOnCast = new PropertyDescriptor<>("FuncOnCast", bCString.class, "bCString", "Script", gCMagic_PS.class);
		public static final PropertyDescriptor<bCString> FuncOnTargetHit = new PropertyDescriptor<>("FuncOnTargetHit", bCString.class, "bCString", "Script", gCMagic_PS.class);
		public static final PropertyDescriptor<eCEntityProxy> Spawn = new PropertyDescriptor<>("Spawn", eCEntityProxy.class, "eCTemplateEntityProxy", "Spawn", gCMagic_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<gESpellDeity>> SpellDeity = new PropertyDescriptor<>("SpellDeity", bTPropertyContainer.class, "bTPropertyContainer<enum gESpellDeity>", "General", gCMagic_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<gESpellTarget>> SpellTarget = new PropertyDescriptor<>("SpellTarget", bTPropertyContainer.class, "bTPropertyContainer<enum gESpellTarget>", "Target", gCMagic_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<gESpellType>> SpellType = new PropertyDescriptor<>("SpellType", bTPropertyContainer.class, "bTPropertyContainer<enum gESpellType>", "General", gCMagic_PS.class);
	}

	public static interface gCMap_PS extends eCEntityPropertySet {
		public static final PropertyDescriptor<bCString> Bitmap = new PropertyDescriptor<>("Bitmap", bCString.class, "bCString", "", gCMap_PS.class);
		public static final PropertyDescriptor<bCString> Header = new PropertyDescriptor<>("Header", bCString.class, "eCLocString", "", gCMap_PS.class);
		public static final PropertyDescriptor<bCVector> WorldBottomRight = new PropertyDescriptor<>("WorldBottomRight", bCVector.class, "bCVector", "", gCMap_PS.class);
		public static final PropertyDescriptor<bCVector> WorldTopLeft = new PropertyDescriptor<>("WorldTopLeft", bCVector.class, "bCVector", "", gCMap_PS.class);
	}

	public static interface gCMover_PS extends gCTouchDamage_PS {
		public static final PropertyDescriptor<bCString> CloseAni = new PropertyDescriptor<>("CloseAni", bCString.class, "bCAnimationResourceString", "", gCMover_PS.class);
		public static final PropertyDescriptor<gBool> ForceAlwaysVisible = new PropertyDescriptor<>("ForceAlwaysVisible", gBool.class, "bool", "", gCMover_PS.class);
		public static final PropertyDescriptor<gBool> IgnoreOpenTouches = new PropertyDescriptor<>("IgnoreOpenTouches", gBool.class, "bool", "", gCMover_PS.class);
		public static final PropertyDescriptor<gBool> LoopAni = new PropertyDescriptor<>("LoopAni", gBool.class, "bool", "", gCMover_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<gEMoverBehavior>> MoverBehavior = new PropertyDescriptor<>("MoverBehavior", bTPropertyContainer.class, "bTPropertyContainer<enum gEMoverBehavior>", "", gCMover_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<gEMoverState>> MoverState = new PropertyDescriptor<>("MoverState", bTPropertyContainer.class, "bTPropertyContainer<enum gEMoverState>", "", gCMover_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<gEMoverTouchBehavior>> MoverTouchBehavior = new PropertyDescriptor<>("MoverTouchBehavior", bTPropertyContainer.class, "bTPropertyContainer<enum gEMoverTouchBehavior>", "", gCMover_PS.class);
		public static final PropertyDescriptor<bCString> OpenAni = new PropertyDescriptor<>("OpenAni", bCString.class, "bCAnimationResourceString", "", gCMover_PS.class);
	}

	public static interface gCMusicFragment extends gCMusicStream {
		public static final PropertyDescriptor<bCString> DayTime = new PropertyDescriptor<>("DayTime", bCString.class, "bCString", "Condition", gCMusicFragment.class);
		public static final PropertyDescriptor<bCString> Location = new PropertyDescriptor<>("Location", bCString.class, "bCString", "Condition", gCMusicFragment.class);
		public static final PropertyDescriptor<bCString> NextVariation = new PropertyDescriptor<>("NextVariation", bCString.class, "bCString", "Sequence", gCMusicFragment.class);
		public static final PropertyDescriptor<bCString> Situation = new PropertyDescriptor<>("Situation", bCString.class, "bCString", "Condition", gCMusicFragment.class);
		public static final PropertyDescriptor<bCString> Variation = new PropertyDescriptor<>("Variation", bCString.class, "bCString", "Sequence", gCMusicFragment.class);
	}

	public static interface gCMusicLink extends ClassDescriptor {
		public static final PropertyDescriptor<gInt> ConnectIn = new PropertyDescriptor<>("ConnectIn", gInt.class, "int", "Connector", gCMusicLink.class);
		public static final PropertyDescriptor<gInt> ConnectOut = new PropertyDescriptor<>("ConnectOut", gInt.class, "int", "Connector", gCMusicLink.class);
		public static final PropertyDescriptor<gBool> IsPriority = new PropertyDescriptor<>("IsPriority", gBool.class, "bool", "General", gCMusicLink.class);
		public static final PropertyDescriptor<gLong> Offset = new PropertyDescriptor<>("Offset", gLong.class, "unsigned long", "Position", gCMusicLink.class);
		public static final PropertyDescriptor<bTPropertyContainer<gEMusicLink>> Type = new PropertyDescriptor<>("Type", bTPropertyContainer.class, "bTPropertyContainer<enum gEMusicLink>", "General", gCMusicLink.class);
	}

	public static interface gCMusicSequencer extends ClassDescriptor {
		public static final PropertyDescriptor<gBool> AcceptTriggers = new PropertyDescriptor<>("AcceptTriggers", gBool.class, "bool", "General", gCMusicSequencer.class);
		public static final PropertyDescriptor<bCString> DayTime = new PropertyDescriptor<>("DayTime", bCString.class, "bCString", "Situation", gCMusicSequencer.class);
		public static final PropertyDescriptor<bTPropertyContainer<gEMusicSequence>> FragmentSequence = new PropertyDescriptor<>("FragmentSequence", bTPropertyContainer.class, "bTPropertyContainer<enum gEMusicSequence>", "Shuffle", gCMusicSequencer.class);
		public static final PropertyDescriptor<bCString> Location = new PropertyDescriptor<>("Location", bCString.class, "bCString", "Situation", gCMusicSequencer.class);
		public static final PropertyDescriptor<bCString> Situation = new PropertyDescriptor<>("Situation", bCString.class, "bCString", "Situation", gCMusicSequencer.class);
		public static final PropertyDescriptor<bTPropertyContainer<gEMusicSequence>> TransitionSequence = new PropertyDescriptor<>("TransitionSequence", bTPropertyContainer.class, "bTPropertyContainer<enum gEMusicSequence>", "Shuffle", gCMusicSequencer.class);
	}

	public static interface gCMusicStream extends ClassDescriptor {
		public static final PropertyDescriptor<gLong> Duration = new PropertyDescriptor<>("Duration", gLong.class, "unsigned long", "Range", gCMusicStream.class);
		public static final PropertyDescriptor<gLong> LeadInEnd = new PropertyDescriptor<>("LeadInEnd", gLong.class, "unsigned long", "Range", gCMusicStream.class);
		public static final PropertyDescriptor<gLong> LeadOutBegin = new PropertyDescriptor<>("LeadOutBegin", gLong.class, "unsigned long", "Range", gCMusicStream.class);
		public static final PropertyDescriptor<bCString> Sample = new PropertyDescriptor<>("Sample", bCString.class, "bCString", "Sample", gCMusicStream.class);
		public static final PropertyDescriptor<gBool> Shuffled = new PropertyDescriptor<>("Shuffled", gBool.class, "bool", "Playback", gCMusicStream.class);
		public static final PropertyDescriptor<gFloat> Volume = new PropertyDescriptor<>("Volume", gFloat.class, "float", "Audibility", gCMusicStream.class);
	}

	public static interface gCMusicTransition extends gCMusicStream {
		public static final PropertyDescriptor<gInt> ConnectIn = new PropertyDescriptor<>("ConnectIn", gInt.class, "int", "Connector", gCMusicTransition.class);
		public static final PropertyDescriptor<gInt> ConnectOut = new PropertyDescriptor<>("ConnectOut", gInt.class, "int", "Connector", gCMusicTransition.class);
		public static final PropertyDescriptor<gLong> FadeInBegin = new PropertyDescriptor<>("FadeInBegin", gLong.class, "unsigned long", "Fading", gCMusicTransition.class);
		public static final PropertyDescriptor<gLong> FadeInEnd = new PropertyDescriptor<>("FadeInEnd", gLong.class, "unsigned long", "Fading", gCMusicTransition.class);
		public static final PropertyDescriptor<gLong> FadeOutBegin = new PropertyDescriptor<>("FadeOutBegin", gLong.class, "unsigned long", "Fading", gCMusicTransition.class);
		public static final PropertyDescriptor<gLong> FadeOutEnd = new PropertyDescriptor<>("FadeOutEnd", gLong.class, "unsigned long", "Fading", gCMusicTransition.class);
		public static final PropertyDescriptor<bCString> ID = new PropertyDescriptor<>("ID", bCString.class, "bCString", "General", gCMusicTransition.class);
		public static final PropertyDescriptor<gLong> Offset = new PropertyDescriptor<>("Offset", gLong.class, "unsigned long", "Timing", gCMusicTransition.class);
		public static final PropertyDescriptor<bTPropertyContainer<gEMusicTransitionTiming>> Timing = new PropertyDescriptor<>("Timing", bTPropertyContainer.class, "bTPropertyContainer<enum gEMusicTransitionTiming>", "Timing", gCMusicTransition.class);
	}

	public static interface gCMusicTrigger extends ClassDescriptor {
		public static final PropertyDescriptor<gInt> ConnectIn = new PropertyDescriptor<>("ConnectIn", gInt.class, "int", "Transition", gCMusicTrigger.class);
		public static final PropertyDescriptor<gInt> ConnectOut = new PropertyDescriptor<>("ConnectOut", gInt.class, "int", "Transition", gCMusicTrigger.class);
		public static final PropertyDescriptor<bCString> DayTime = new PropertyDescriptor<>("DayTime", bCString.class, "bCString", "Situation", gCMusicTrigger.class);
		public static final PropertyDescriptor<gBool> DisableTriggers = new PropertyDescriptor<>("DisableTriggers", gBool.class, "bool", "General", gCMusicTrigger.class);
		public static final PropertyDescriptor<bTPropertyContainer<gEMusicFragmentPosition>> Entrance = new PropertyDescriptor<>("Entrance", bTPropertyContainer.class, "bTPropertyContainer<enum gEMusicFragmentPosition>", "Transition", gCMusicTrigger.class);
		public static final PropertyDescriptor<bTPropertyContainer<gEMusicTriggerTime>> Exit = new PropertyDescriptor<>("Exit", bTPropertyContainer.class, "bTPropertyContainer<enum gEMusicTriggerTime>", "Transition", gCMusicTrigger.class);
		public static final PropertyDescriptor<bCString> ID = new PropertyDescriptor<>("ID", bCString.class, "bCString", "General", gCMusicTrigger.class);
		public static final PropertyDescriptor<gBool> IsStopTrigger = new PropertyDescriptor<>("IsStopTrigger", gBool.class, "bool", "General", gCMusicTrigger.class);
		public static final PropertyDescriptor<bCString> Location = new PropertyDescriptor<>("Location", bCString.class, "bCString", "Situation", gCMusicTrigger.class);
		public static final PropertyDescriptor<bCString> Situation = new PropertyDescriptor<>("Situation", bCString.class, "bCString", "Situation", gCMusicTrigger.class);
		public static final PropertyDescriptor<bTPropertyContainer<gEMusicTransition>> TransitionMode = new PropertyDescriptor<>("TransitionMode", bTPropertyContainer.class, "bTPropertyContainer<enum gEMusicTransition>", "Transition", gCMusicTrigger.class);
		public static final PropertyDescriptor<bCString> Transition = new PropertyDescriptor<>("Transition", bCString.class, "bCString", "Transition", gCMusicTrigger.class);
		public static final PropertyDescriptor<bCString> Variation = new PropertyDescriptor<>("Variation", bCString.class, "bCString", "Situation", gCMusicTrigger.class);
	}

	public static interface gCNPC_PS extends eCEntityPropertySet {
		public static final PropertyDescriptor<eCEntityProxy> AlternativeTargetEntity = new PropertyDescriptor<>("AlternativeTargetEntity", eCEntityProxy.class, "eCEntityProxy", "", gCNPC_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<gEAttackReason>> AttackReason = new PropertyDescriptor<>("AttackReason", bTPropertyContainer.class, "bTPropertyContainer<enum gEAttackReason>", "", gCNPC_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<gEAttitude>> AttitudeToPlayer2 = new PropertyDescriptor<>("AttitudeToPlayer2", bTPropertyContainer.class, "bTPropertyContainer<enum gEAttitude>", "", gCNPC_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<gEBearing>> Bearing = new PropertyDescriptor<>("Bearing", bTPropertyContainer.class, "bTPropertyContainer<enum gEBearing>", "", gCNPC_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<gEClass>> Class = new PropertyDescriptor<>("Class", bTPropertyContainer.class, "bTPropertyContainer<enum gEClass>", "", gCNPC_PS.class);
		public static final PropertyDescriptor<gLong> CombatState = new PropertyDescriptor<>("CombatState", gLong.class, "unsigned long", "", gCNPC_PS.class);
		public static final PropertyDescriptor<eCEntityProxy> CurrentAttackerEntity = new PropertyDescriptor<>("CurrentAttackerEntity", eCEntityProxy.class, "eCEntityProxy", "", gCNPC_PS.class);
		public static final PropertyDescriptor<eCEntityProxy> CurrentTargetEntity = new PropertyDescriptor<>("CurrentTargetEntity", eCEntityProxy.class, "eCEntityProxy", "", gCNPC_PS.class);
		public static final PropertyDescriptor<gBool> DefeatedByPlayer = new PropertyDescriptor<>("DefeatedByPlayer", gBool.class, "bool", "", gCNPC_PS.class);
		public static final PropertyDescriptor<bCString> Description = new PropertyDescriptor<>("Description", bCString.class, "bCString", "", gCNPC_PS.class);
		public static final PropertyDescriptor<gBool> Discovered = new PropertyDescriptor<>("Discovered", gBool.class, "bool", "", gCNPC_PS.class);
		public static final PropertyDescriptor<gFloat> LastDistToGuardPoint = new PropertyDescriptor<>("LastDistToGuardPoint", gFloat.class, "float", "", gCNPC_PS.class);
		public static final PropertyDescriptor<gFloat> DistToNearestMist = new PropertyDescriptor<>("DistToNearestMist", gFloat.class, "float", "", gCNPC_PS.class);
		public static final PropertyDescriptor<bCPropertyID> Enclave = new PropertyDescriptor<>("Enclave", bCPropertyID.class, "bCPropertyID", "", gCNPC_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<gEGender>> Gender = new PropertyDescriptor<>("Gender", bTPropertyContainer.class, "bTPropertyContainer<enum gEGender>", "", gCNPC_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<gEGuardStatus>> GuardStatus = new PropertyDescriptor<>("GuardStatus", bTPropertyContainer.class, "bTPropertyContainer<enum gEGuardStatus>", "", gCNPC_PS.class);
		public static final PropertyDescriptor<gBool> Immortal = new PropertyDescriptor<>("Immortal", gBool.class, "bool", "", gCNPC_PS.class);
		public static final PropertyDescriptor<eCEntityProxy> LastAttackerEntity = new PropertyDescriptor<>("LastAttackerEntity", eCEntityProxy.class, "eCEntityProxy", "", gCNPC_PS.class);
		public static final PropertyDescriptor<gLong> LastCastTimestamp = new PropertyDescriptor<>("LastCastTimestamp", gLong.class, "unsigned long", "", gCNPC_PS.class);
		public static final PropertyDescriptor<gFloat> LastDistToTarget = new PropertyDescriptor<>("LastDistToTarget", gFloat.class, "float", "", gCNPC_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<gELastFightAgainstPlayer>> LastFightAgainstPlayer = new PropertyDescriptor<>("LastFightAgainstPlayer", bTPropertyContainer.class, "bTPropertyContainer<enum gELastFightAgainstPlayer>", "", gCNPC_PS.class);
		public static final PropertyDescriptor<gBool> LastFightComment = new PropertyDescriptor<>("LastFightComment", gBool.class, "bool", "", gCNPC_PS.class);
		public static final PropertyDescriptor<gFloat> LastFightTimestamp = new PropertyDescriptor<>("LastFightTimestamp", gFloat.class, "float", "", gCNPC_PS.class);
		public static final PropertyDescriptor<gLong> LastHitTimestamp = new PropertyDescriptor<>("LastHitTimestamp", gLong.class, "unsigned long", "", gCNPC_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<gEAttackReason>> LastPlayerAR = new PropertyDescriptor<>("LastPlayerAR", bTPropertyContainer.class, "bTPropertyContainer<enum gEAttackReason>", "", gCNPC_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<gEPlayerCrime>> LastPlayerCrime = new PropertyDescriptor<>("LastPlayerCrime", bTPropertyContainer.class, "bTPropertyContainer<enum gEPlayerCrime>", "", gCNPC_PS.class);
		public static final PropertyDescriptor<bCString> LastSpell = new PropertyDescriptor<>("LastSpell", bCString.class, "bCString", "Magic", gCNPC_PS.class);
		public static final PropertyDescriptor<gLong> Level = new PropertyDescriptor<>("Level", gLong.class, "unsigned long", "", gCNPC_PS.class);
		public static final PropertyDescriptor<gLong> LevelMax = new PropertyDescriptor<>("LevelMax", gLong.class, "unsigned long", "", gCNPC_PS.class);
		public static final PropertyDescriptor<gInt> ManaUsed = new PropertyDescriptor<>("ManaUsed", gInt.class, "int", "Magic", gCNPC_PS.class);
		public static final PropertyDescriptor<eCEntityProxy> GuardPoint = new PropertyDescriptor<>("GuardPoint", eCEntityProxy.class, "eCEntityProxy", "", gCNPC_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<gEPoliticalAlignment>> PoliticalAlignment = new PropertyDescriptor<>("PoliticalAlignment", bTPropertyContainer.class, "bTPropertyContainer<enum gEPoliticalAlignment>", "", gCNPC_PS.class);
		public static final PropertyDescriptor<gBool> Ransacked = new PropertyDescriptor<>("Ransacked", gBool.class, "bool", "", gCNPC_PS.class);
		public static final PropertyDescriptor<bCString> RoleDescription = new PropertyDescriptor<>("RoleDescription", bCString.class, "bCString", "", gCNPC_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<gESpecies>> Species = new PropertyDescriptor<>("Species", bTPropertyContainer.class, "bTPropertyContainer<enum gESpecies>", "", gCNPC_PS.class);
		public static final PropertyDescriptor<eCEntityProxy> SpellTarget = new PropertyDescriptor<>("SpellTarget", eCEntityProxy.class, "eCEntityProxy", "Magic", gCNPC_PS.class);
		public static final PropertyDescriptor<gLong> StatusEffects = new PropertyDescriptor<>("StatusEffects", gLong.class, "unsigned long", "Status", gCNPC_PS.class);
		public static final PropertyDescriptor<bTObjArray_bCString> TeachAttribs = new PropertyDescriptor<>("TeachAttribs", bTObjArray_bCString.class, "bTObjArray<class bCString>", "", gCNPC_PS.class);
		public static final PropertyDescriptor<bTObjArray_eCEntityProxy> TeachSkills = new PropertyDescriptor<>("TeachSkills", bTObjArray_eCEntityProxy.class, "bTObjArray<class eCTemplateEntityProxy>", "", gCNPC_PS.class);
		public static final PropertyDescriptor<gLong> TimeStampBurning = new PropertyDescriptor<>("TimeStampBurning", gLong.class, "unsigned long", "Status", gCNPC_PS.class);
		public static final PropertyDescriptor<gLong> TimeStampDiseased = new PropertyDescriptor<>("TimeStampDiseased", gLong.class, "unsigned long", "Status", gCNPC_PS.class);
		public static final PropertyDescriptor<gLong> TimeStampFrozen = new PropertyDescriptor<>("TimeStampFrozen", gLong.class, "unsigned long", "Status", gCNPC_PS.class);
		public static final PropertyDescriptor<gLong> TimeStampPoisoned = new PropertyDescriptor<>("TimeStampPoisoned", gLong.class, "unsigned long", "Status", gCNPC_PS.class);
		public static final PropertyDescriptor<gLong> TimeStampTransformed = new PropertyDescriptor<>("TimeStampTransformed", gLong.class, "unsigned long", "Status", gCNPC_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<gENPCType>> Type = new PropertyDescriptor<>("Type", bTPropertyContainer.class, "bTPropertyContainer<enum gENPCType>", "", gCNPC_PS.class);
		public static final PropertyDescriptor<bCString> Voice = new PropertyDescriptor<>("Voice", bCString.class, "bCString", "", gCNPC_PS.class);
	}

	public static interface gCNavHelper_PS extends eCEntityPropertySet {
		public static final PropertyDescriptor<bCPropertyID> LinkedToEntityID = new PropertyDescriptor<>("LinkedToEntityID", bCPropertyID.class, "bCPropertyID", "", gCNavHelper_PS.class);
		public static final PropertyDescriptor<bCPropertyID> LinkedToSecondEntityID = new PropertyDescriptor<>("LinkedToSecondEntityID", bCPropertyID.class, "bCPropertyID", "", gCNavHelper_PS.class);
	}

	public static interface gCNavOffset_PS extends eCEntityPropertySet {
		public static final PropertyDescriptor<bTValArray_gEDirection> AniDirection = new PropertyDescriptor<>("AniDirection", bTValArray_gEDirection.class, "bTValArray<enum gEDirection>", "", gCNavOffset_PS.class);
		public static final PropertyDescriptor<gBool> OffsetCircle = new PropertyDescriptor<>("OffsetCircle", gBool.class, "bool", "", gCNavOffset_PS.class);
		public static final PropertyDescriptor<bTValArray_bCMotion> OffsetPose = new PropertyDescriptor<>("OffsetPose", bTValArray_bCMotion.class, "bTValArray<class bCMotion>", "", gCNavOffset_PS.class);
	}

	public static interface gCNavPath_PS extends eCEntityPropertySet {
		public static final PropertyDescriptor<gFloat> BottomToleranz = new PropertyDescriptor<>("BottomToleranz", gFloat.class, "float", "", gCNavPath_PS.class);
		public static final PropertyDescriptor<gBool> LinkInnerArea = new PropertyDescriptor<>("LinkInnerArea", gBool.class, "bool", "", gCNavPath_PS.class);
		public static final PropertyDescriptor<gBool> LinkInnerBottomArea = new PropertyDescriptor<>("LinkInnerBottomArea", gBool.class, "bool", "", gCNavPath_PS.class);
		public static final PropertyDescriptor<gBool> LinkInnerTopArea = new PropertyDescriptor<>("LinkInnerTopArea", gBool.class, "bool", "", gCNavPath_PS.class);
		public static final PropertyDescriptor<bTValArray_bCVector> Point = new PropertyDescriptor<>("Point", bTValArray_bCVector.class, "bTValArray<class bCVector>", "", gCNavPath_PS.class);
		public static final PropertyDescriptor<bTValArray_float> Radius = new PropertyDescriptor<>("Radius", bTValArray_float.class, "bTValArray<float>", "", gCNavPath_PS.class);
		public static final PropertyDescriptor<gFloat> TopToleranz = new PropertyDescriptor<>("TopToleranz", gFloat.class, "float", "", gCNavPath_PS.class);
		public static final PropertyDescriptor<gBool> UnlimitedHeight = new PropertyDescriptor<>("UnlimitedHeight", gBool.class, "bool", "", gCNavPath_PS.class);
		public static final PropertyDescriptor<bCPropertyID> ZoneAEntityID = new PropertyDescriptor<>("ZoneAEntityID", bCPropertyID.class, "bCPropertyID", "", gCNavPath_PS.class);
		public static final PropertyDescriptor<bCVector> ZoneAIntersectionCenter = new PropertyDescriptor<>("ZoneAIntersectionCenter", bCVector.class, "bCVector", "", gCNavPath_PS.class);
		public static final PropertyDescriptor<bCVector> ZoneAIntersectionMargin1 = new PropertyDescriptor<>("ZoneAIntersectionMargin1", bCVector.class, "bCVector", "", gCNavPath_PS.class);
		public static final PropertyDescriptor<bCVector> ZoneAIntersectionMargin2 = new PropertyDescriptor<>("ZoneAIntersectionMargin2", bCVector.class, "bCVector", "", gCNavPath_PS.class);
		public static final PropertyDescriptor<bCPropertyID> ZoneBEntityID = new PropertyDescriptor<>("ZoneBEntityID", bCPropertyID.class, "bCPropertyID", "", gCNavPath_PS.class);
		public static final PropertyDescriptor<bCVector> ZoneBIntersectionCenter = new PropertyDescriptor<>("ZoneBIntersectionCenter", bCVector.class, "bCVector", "", gCNavPath_PS.class);
		public static final PropertyDescriptor<bCVector> ZoneBIntersectionMargin1 = new PropertyDescriptor<>("ZoneBIntersectionMargin1", bCVector.class, "bCVector", "", gCNavPath_PS.class);
		public static final PropertyDescriptor<bCVector> ZoneBIntersectionMargin2 = new PropertyDescriptor<>("ZoneBIntersectionMargin2", bCVector.class, "bCVector", "", gCNavPath_PS.class);
	}

	public static interface gCNavZone_PS extends eCEntityPropertySet {
		public static final PropertyDescriptor<gFloat> BottomToleranz = new PropertyDescriptor<>("BottomToleranz", gFloat.class, "float", "", gCNavZone_PS.class);
		public static final PropertyDescriptor<gBool> LinkInnerArea = new PropertyDescriptor<>("LinkInnerArea", gBool.class, "bool", "", gCNavZone_PS.class);
		public static final PropertyDescriptor<gBool> LinkInnerBottomArea = new PropertyDescriptor<>("LinkInnerBottomArea", gBool.class, "bool", "", gCNavZone_PS.class);
		public static final PropertyDescriptor<gBool> LinkInnerTopArea = new PropertyDescriptor<>("LinkInnerTopArea", gBool.class, "bool", "", gCNavZone_PS.class);
		public static final PropertyDescriptor<bTValArray_bCVector> Point = new PropertyDescriptor<>("Point", bTValArray_bCVector.class, "bTValArray<class bCVector>", "", gCNavZone_PS.class);
		public static final PropertyDescriptor<gFloat> Radius = new PropertyDescriptor<>("Radius", gFloat.class, "float", "", gCNavZone_PS.class);
		public static final PropertyDescriptor<bCVector> RadiusOffset = new PropertyDescriptor<>("RadiusOffset", bCVector.class, "bCVector", "", gCNavZone_PS.class);
		public static final PropertyDescriptor<gFloat> TopToleranz = new PropertyDescriptor<>("TopToleranz", gFloat.class, "float", "", gCNavZone_PS.class);
		public static final PropertyDescriptor<gBool> ZoneIsCCW = new PropertyDescriptor<>("ZoneIsCCW", gBool.class, "bool", "", gCNavZone_PS.class);
	}

	public static interface gCNavigation_PS extends eCEntityPropertySet {
		public static final PropertyDescriptor<eCEntityProxy> CurrentDestinationPointProxy = new PropertyDescriptor<>("CurrentDestinationPointProxy", eCEntityProxy.class, "eCEntityProxy", "", gCNavigation_PS.class);
		public static final PropertyDescriptor<eCEntityProxy> CurrentZoneEntityProxy = new PropertyDescriptor<>("CurrentZoneEntityProxy", eCEntityProxy.class, "eCEntityProxy", "", gCNavigation_PS.class);
		public static final PropertyDescriptor<bCVector> LastUseableNavigationPosition = new PropertyDescriptor<>("LastUseableNavigationPosition", bCVector.class, "bCVector", "", gCNavigation_PS.class);
		public static final PropertyDescriptor<bCPropertyID> LastUseableNavigationZoneID = new PropertyDescriptor<>("LastUseableNavigationZoneID", bCPropertyID.class, "bCPropertyID", "", gCNavigation_PS.class);
		public static final PropertyDescriptor<gBool> LastUseableNavigationZoneIsPath = new PropertyDescriptor<>("LastUseableNavigationZoneIsPath", gBool.class, "bool", "", gCNavigation_PS.class);
		public static final PropertyDescriptor<eCEntityProxy> LastZoneEntityProxy = new PropertyDescriptor<>("LastZoneEntityProxy", eCEntityProxy.class, "eCEntityProxy", "", gCNavigation_PS.class);
		public static final PropertyDescriptor<bCPropertyID> RelaxingPoint = new PropertyDescriptor<>("RelaxingPoint", bCPropertyID.class, "bCPropertyID", "", gCNavigation_PS.class);
		public static final PropertyDescriptor<bTValArray_bCPropertyID> RelaxingPoints = new PropertyDescriptor<>("RelaxingPoints", bTValArray_bCPropertyID.class, "bTValArray<class bCPropertyID>", "", gCNavigation_PS.class);
		public static final PropertyDescriptor<bCString> Routine = new PropertyDescriptor<>("Routine", bCString.class, "bCString", "", gCNavigation_PS.class);
		public static final PropertyDescriptor<bTObjArray_bCString> RoutineNames = new PropertyDescriptor<>("RoutineNames", bTObjArray_bCString.class, "bTObjArray<class bCString>", "", gCNavigation_PS.class);
		public static final PropertyDescriptor<bCPropertyID> SleepingPoint = new PropertyDescriptor<>("SleepingPoint", bCPropertyID.class, "bCPropertyID", "", gCNavigation_PS.class);
		public static final PropertyDescriptor<bTValArray_bCPropertyID> SleepingPoints = new PropertyDescriptor<>("SleepingPoints", bTValArray_bCPropertyID.class, "bTValArray<class bCPropertyID>", "", gCNavigation_PS.class);
		public static final PropertyDescriptor<bCVector> StartPosition = new PropertyDescriptor<>("StartPosition", bCVector.class, "bCVector", "", gCNavigation_PS.class);
		public static final PropertyDescriptor<bCPropertyID> WorkingPoint = new PropertyDescriptor<>("WorkingPoint", bCPropertyID.class, "bCPropertyID", "", gCNavigation_PS.class);
		public static final PropertyDescriptor<bTValArray_bCPropertyID> WorkingPoints = new PropertyDescriptor<>("WorkingPoints", bTValArray_bCPropertyID.class, "bTValArray<class bCPropertyID>", "", gCNavigation_PS.class);
	}

	public static interface gCNegZone_PS extends eCEntityPropertySet {
		public static final PropertyDescriptor<bTValArray_bCVector> Point = new PropertyDescriptor<>("Point", bTValArray_bCVector.class, "bTValArray<class bCVector>", "", gCNegZone_PS.class);
		public static final PropertyDescriptor<gFloat> Radius = new PropertyDescriptor<>("Radius", gFloat.class, "float", "", gCNegZone_PS.class);
		public static final PropertyDescriptor<bCVector> RadiusOffset = new PropertyDescriptor<>("RadiusOffset", bCVector.class, "bCVector", "", gCNegZone_PS.class);
		public static final PropertyDescriptor<bCPropertyID> ZoneEntityID = new PropertyDescriptor<>("ZoneEntityID", bCPropertyID.class, "bCPropertyID", "", gCNegZone_PS.class);
		public static final PropertyDescriptor<gBool> ZoneIsCCW = new PropertyDescriptor<>("ZoneIsCCW", gBool.class, "bool", "", gCNegZone_PS.class);
	}

	public static interface gCParty_PS extends eCEntityPropertySet {
		public static final PropertyDescriptor<eCEntityProxy> PartyLeaderEntity = new PropertyDescriptor<>("PartyLeaderEntity", eCEntityProxy.class, "eCEntityProxy", "", gCParty_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<gEPartyMemberType>> PartyMemberType = new PropertyDescriptor<>("PartyMemberType", bTPropertyContainer.class, "bTPropertyContainer<enum gEPartyMemberType>", "", gCParty_PS.class);
		public static final PropertyDescriptor<gBool> Waiting = new PropertyDescriptor<>("Waiting", gBool.class, "bool", "", gCParty_PS.class);
	}

	public static interface gCPlayerMemory_PS extends eCEntityPropertySet {
		public static final PropertyDescriptor<gLong> BookOfFlood = new PropertyDescriptor<>("BookOfFlood", gLong.class, "long", "Internal", gCPlayerMemory_PS.class);
		public static final PropertyDescriptor<gLong> BookOfRhobar = new PropertyDescriptor<>("BookOfRhobar", gLong.class, "long", "Internal", gCPlayerMemory_PS.class);
		public static final PropertyDescriptor<gLong> BookOfZuben = new PropertyDescriptor<>("BookOfZuben", gLong.class, "long", "Internal", gCPlayerMemory_PS.class);
		public static final PropertyDescriptor<gLong> Chapter = new PropertyDescriptor<>("Chapter", gLong.class, "long", "Internal", gCPlayerMemory_PS.class);
		public static final PropertyDescriptor<gLong> DuskToDawnStartHour = new PropertyDescriptor<>("DuskToDawnStartHour", gLong.class, "long", "Internal", gCPlayerMemory_PS.class);
		public static final PropertyDescriptor<gBool> HideTips = new PropertyDescriptor<>("HideTips", gBool.class, "bool", "Common", gCPlayerMemory_PS.class);
		public static final PropertyDescriptor<gBool> IsConsumingItem = new PropertyDescriptor<>("IsConsumingItem", gBool.class, "bool", "Internal", gCPlayerMemory_PS.class);
		public static final PropertyDescriptor<gLong> LPAttribs = new PropertyDescriptor<>("LPAttribs", gLong.class, "long", "Common", gCPlayerMemory_PS.class);
		public static final PropertyDescriptor<gLong> LPPerks = new PropertyDescriptor<>("LPPerks", gLong.class, "long", "Common", gCPlayerMemory_PS.class);
		public static final PropertyDescriptor<bCPropertyID> LastSpell = new PropertyDescriptor<>("LastSpell", bCPropertyID.class, "bCPropertyID", "Internal", gCPlayerMemory_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<gEWeaponConfig>> LastWeaponConfig = new PropertyDescriptor<>("LastWeaponConfig", bTPropertyContainer.class, "bTPropertyContainer<enum gEWeaponConfig>", "Internal", gCPlayerMemory_PS.class);
		public static final PropertyDescriptor<bTObjArray_bCString> PlayerKnows = new PropertyDescriptor<>("PlayerKnows", bTObjArray_bCString.class, "bTObjArray<class bCString>", "Common", gCPlayerMemory_PS.class);
		public static final PropertyDescriptor<bTValArray_long> PoliticalCrimeCount = new PropertyDescriptor<>("PoliticalCrimeCount", bTValArray_long.class, "bTValArray<long>", "Common", gCPlayerMemory_PS.class);
		public static final PropertyDescriptor<bTValArray_long> PoliticalFame = new PropertyDescriptor<>("PoliticalFame", bTValArray_long.class, "bTValArray<long>", "Common", gCPlayerMemory_PS.class);
		public static final PropertyDescriptor<bTValArray_bool> PoliticalPlayerCrime = new PropertyDescriptor<>("PoliticalPlayerCrime", bTValArray_bool.class, "bTValArray<bool>", "Common", gCPlayerMemory_PS.class);
		public static final PropertyDescriptor<bTValArray_long> PoliticalSuspectComment = new PropertyDescriptor<>("PoliticalSuspectComment", bTValArray_long.class, "bTValArray<long>", "Common", gCPlayerMemory_PS.class);
		public static final PropertyDescriptor<gFloat> SecondsMistRemain = new PropertyDescriptor<>("SecondsMistRemain", gFloat.class, "float", "Internal", gCPlayerMemory_PS.class);
		public static final PropertyDescriptor<gFloat> SecondsTransformRemain = new PropertyDescriptor<>("SecondsTransformRemain", gFloat.class, "float", "Internal", gCPlayerMemory_PS.class);
		public static final PropertyDescriptor<gBool> TalkedToDiego = new PropertyDescriptor<>("TalkedToDiego", gBool.class, "bool", "Internal", gCPlayerMemory_PS.class);
		public static final PropertyDescriptor<gBool> TalkedToGorn = new PropertyDescriptor<>("TalkedToGorn", gBool.class, "bool", "Internal", gCPlayerMemory_PS.class);
		public static final PropertyDescriptor<gBool> TalkedToLester = new PropertyDescriptor<>("TalkedToLester", gBool.class, "bool", "Internal", gCPlayerMemory_PS.class);
		public static final PropertyDescriptor<gBool> TalkedToMilten = new PropertyDescriptor<>("TalkedToMilten", gBool.class, "bool", "Internal", gCPlayerMemory_PS.class);
		public static final PropertyDescriptor<gFloat> TimeStampStart = new PropertyDescriptor<>("TimeStampStart", gFloat.class, "float", "Internal", gCPlayerMemory_PS.class);
		public static final PropertyDescriptor<gLong> TutorialFlags = new PropertyDescriptor<>("TutorialFlags", gLong.class, "unsigned long", "Internal", gCPlayerMemory_PS.class);
		public static final PropertyDescriptor<gLong> XP = new PropertyDescriptor<>("XP", gLong.class, "long", "Common", gCPlayerMemory_PS.class);
	}

	public static interface gCPrefPath_PS extends eCEntityPropertySet {
		public static final PropertyDescriptor<bTValArray_bCVector> Point = new PropertyDescriptor<>("Point", bTValArray_bCVector.class, "bTValArray<class bCVector>", "", gCPrefPath_PS.class);
		public static final PropertyDescriptor<bTValArray_float> PointRadius = new PropertyDescriptor<>("PointRadius", bTValArray_float.class, "bTValArray<float>", "", gCPrefPath_PS.class);
		public static final PropertyDescriptor<gFloat> Radius = new PropertyDescriptor<>("Radius", gFloat.class, "float", "", gCPrefPath_PS.class);
		public static final PropertyDescriptor<bCVector> RadiusOffset = new PropertyDescriptor<>("RadiusOffset", bCVector.class, "bCVector", "", gCPrefPath_PS.class);
		public static final PropertyDescriptor<bCPropertyID> ZoneEntityID = new PropertyDescriptor<>("ZoneEntityID", bCPropertyID.class, "bCPropertyID", "", gCPrefPath_PS.class);
	}

	public static interface gCProject extends eCProcessibleElement {
		public static final PropertyDescriptor<bCString> ActiveWorld = new PropertyDescriptor<>("ActiveWorld", bCString.class, "bCString", "", gCProject.class);
		public static final PropertyDescriptor<Unknown> Workspace = new PropertyDescriptor<>("Workspace", Unknown.class, "bTPOSmartPtr<class gCWorkspace>", "", gCProject.class);
		public static final PropertyDescriptor<Unknown> Worlds = new PropertyDescriptor<>("Worlds", Unknown.class, "bTRefPtrArray<class bCPropertyObjectBase *>", "", gCProject.class);
	}

	public static interface gCProjectile_PS extends eCEntityPropertySet {
		public static final PropertyDescriptor<gFloat> DecayDuration = new PropertyDescriptor<>("DecayDuration", gFloat.class, "float", "", gCProjectile_PS.class);
		public static final PropertyDescriptor<bCString> EffectPointImpact = new PropertyDescriptor<>("EffectPointImpact", bCString.class, "bCString", "Collision", gCProjectile_PS.class);
		public static final PropertyDescriptor<bCString> EffectTargetHit = new PropertyDescriptor<>("EffectTargetHit", bCString.class, "bCString", "Collision", gCProjectile_PS.class);
		public static final PropertyDescriptor<gBool> FadeOnDecay = new PropertyDescriptor<>("FadeOnDecay", gBool.class, "bool", "", gCProjectile_PS.class);
		public static final PropertyDescriptor<gBool> LinkToBones = new PropertyDescriptor<>("LinkToBones", gBool.class, "bool", "", gCProjectile_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<gEProjectilePath>> PathStyle = new PropertyDescriptor<>("PathStyle", bTPropertyContainer.class, "bTPropertyContainer<enum gEProjectilePath>", "", gCProjectile_PS.class);
		public static final PropertyDescriptor<bCString> FuncOnTargetHit = new PropertyDescriptor<>("FuncOnTargetHit", bCString.class, "bCString", "Collision", gCProjectile_PS.class);
		public static final PropertyDescriptor<gFloat> ShootVelocity = new PropertyDescriptor<>("ShootVelocity", gFloat.class, "float", "", gCProjectile_PS.class);
		public static final PropertyDescriptor<bCVector> TargetDirection = new PropertyDescriptor<>("TargetDirection", bCVector.class, "bCVector", "", gCProjectile_PS.class);
		public static final PropertyDescriptor<eCEntityProxy> TargetEntity = new PropertyDescriptor<>("TargetEntity", eCEntityProxy.class, "eCEntityProxy", "", gCProjectile_PS.class);
		public static final PropertyDescriptor<bCVector> TargetPosition = new PropertyDescriptor<>("TargetPosition", bCVector.class, "bCVector", "", gCProjectile_PS.class);
		public static final PropertyDescriptor<gLong> TargetUpdateMSec = new PropertyDescriptor<>("TargetUpdateMSec", gLong.class, "unsigned long", "", gCProjectile_PS.class);
		public static final PropertyDescriptor<gFloat> TouchAngleTreshold = new PropertyDescriptor<>("TouchAngleTreshold", gFloat.class, "float", "", gCProjectile_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<gEProjectileTouchBehavior>> TouchBehavior = new PropertyDescriptor<>("TouchBehavior", bTPropertyContainer.class, "bTPropertyContainer<enum gEProjectileTouchBehavior>", "", gCProjectile_PS.class);
	}

	public static interface gCQuestManager_PS extends eCEntityPropertySet {
		public static final PropertyDescriptor<bTObjArray_bTAutoPOSmartPtr_gCQuest_PS> Quests = new PropertyDescriptor<>("Quests", bTObjArray_bTAutoPOSmartPtr_gCQuest_PS.class, "bTObjArray<class bTAutoPOSmartPtr<class gCQuest_PS> >", "", gCQuestManager_PS.class);
	}

	public static interface gCQuest_PS extends ClassDescriptor {
		public static final PropertyDescriptor<bTValArray_long> DeliveryAmounts = new PropertyDescriptor<>("DeliveryAmounts", bTValArray_long.class, "bTValArray<unsigned long>", "", gCQuest_PS.class);
		public static final PropertyDescriptor<bTValArray_long> DeliveryCounter = new PropertyDescriptor<>("DeliveryCounter", bTValArray_long.class, "bTValArray<long>", "", gCQuest_PS.class);
		public static final PropertyDescriptor<bTObjArray_bCString> DeliveryEntities = new PropertyDescriptor<>("DeliveryEntities", bTObjArray_bCString.class, "bTObjArray<class bCString>", "", gCQuest_PS.class);
		public static final PropertyDescriptor<bCString> DestinationEntity = new PropertyDescriptor<>("DestinationEntity", bCString.class, "bCString", "", gCQuest_PS.class);
		public static final PropertyDescriptor<bCString> EnclaveFailure = new PropertyDescriptor<>("EnclaveFailure", bCString.class, "bCString", "", gCQuest_PS.class);
		public static final PropertyDescriptor<gLong> EnclaveFailureAmount = new PropertyDescriptor<>("EnclaveFailureAmount", gLong.class, "long", "", gCQuest_PS.class);
		public static final PropertyDescriptor<gLong> PoliticalFailureAmount = new PropertyDescriptor<>("PoliticalFailureAmount", gLong.class, "long", "", gCQuest_PS.class);
		public static final PropertyDescriptor<bTObjArray_bCString> FinishedQuests = new PropertyDescriptor<>("FinishedQuests", bTObjArray_bCString.class, "bTObjArray<class bCString>", "", gCQuest_PS.class);
		public static final PropertyDescriptor<bCString> Folder = new PropertyDescriptor<>("Folder", bCString.class, "bCString", "", gCQuest_PS.class);
		public static final PropertyDescriptor<Unknown> LogText = new PropertyDescriptor<>("LogText", Unknown.class, "bTObjArray<struct gCQuest_PS::SLogEntry>", "", gCQuest_PS.class);
		public static final PropertyDescriptor<bCString> LogTopic = new PropertyDescriptor<>("LogTopic", bCString.class, "eCLocString", "", gCQuest_PS.class);
		public static final PropertyDescriptor<bCString> Name = new PropertyDescriptor<>("Name", bCString.class, "bCString", "", gCQuest_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<gEPoliticalAlignment>> PoliticalFailure = new PropertyDescriptor<>("PoliticalFailure", bTPropertyContainer.class, "bTPropertyContainer<enum gEPoliticalAlignment>", "", gCQuest_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<gEPoliticalAlignment>> PoliticalSuccess = new PropertyDescriptor<>("PoliticalSuccess", bTPropertyContainer.class, "bTPropertyContainer<enum gEPoliticalAlignment>", "", gCQuest_PS.class);
		public static final PropertyDescriptor<gLong> RunningTimeDays = new PropertyDescriptor<>("RunningTimeDays", gLong.class, "unsigned long", "", gCQuest_PS.class);
		public static final PropertyDescriptor<gLong> RunningTimeHours = new PropertyDescriptor<>("RunningTimeHours", gLong.class, "unsigned long", "", gCQuest_PS.class);
		public static final PropertyDescriptor<gLong> RunningTimeYears = new PropertyDescriptor<>("RunningTimeYears", gLong.class, "unsigned long", "", gCQuest_PS.class);
		public static final PropertyDescriptor<bCString> AttribSuccess = new PropertyDescriptor<>("AttribSuccess", bCString.class, "bCString", "", gCQuest_PS.class);
		public static final PropertyDescriptor<bCString> EnclaveSuccess = new PropertyDescriptor<>("EnclaveSuccess", bCString.class, "bCString", "", gCQuest_PS.class);
		public static final PropertyDescriptor<gLong> AttribSuccessAmount = new PropertyDescriptor<>("AttribSuccessAmount", gLong.class, "long", "", gCQuest_PS.class);
		public static final PropertyDescriptor<gLong> EnclaveSuccessAmount = new PropertyDescriptor<>("EnclaveSuccessAmount", gLong.class, "long", "", gCQuest_PS.class);
		public static final PropertyDescriptor<gLong> PoliticalSuccessAmount = new PropertyDescriptor<>("PoliticalSuccessAmount", gLong.class, "long", "", gCQuest_PS.class);
		public static final PropertyDescriptor<gLong> ActivationTime_Day = new PropertyDescriptor<>("ActivationTime_Day", gLong.class, "unsigned long", "", gCQuest_PS.class);
		public static final PropertyDescriptor<gLong> ActivationTime_Hour = new PropertyDescriptor<>("ActivationTime_Hour", gLong.class, "unsigned long", "", gCQuest_PS.class);
		public static final PropertyDescriptor<gLong> ActivationTime_Year = new PropertyDescriptor<>("ActivationTime_Year", gLong.class, "unsigned long", "", gCQuest_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<gEQuestType>> Type = new PropertyDescriptor<>("Type", bTPropertyContainer.class, "bTPropertyContainer<enum gEQuestType>", "", gCQuest_PS.class);
		public static final PropertyDescriptor<gLong> ExperiencePoints = new PropertyDescriptor<>("ExperiencePoints", gLong.class, "unsigned long", "", gCQuest_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<gEQuestStatus>> Status = new PropertyDescriptor<>("Status", bTPropertyContainer.class, "bTPropertyContainer<enum gEQuestStatus>", "", gCQuest_PS.class);
	}

	public static interface gCRecipe_PS extends eCEntityPropertySet {
		public static final PropertyDescriptor<bTPropertyContainer<gESkillCategory>> Craft = new PropertyDescriptor<>("Craft", bTPropertyContainer.class, "bTPropertyContainer<enum gESkillCategory>", "General", gCRecipe_PS.class);
		public static final PropertyDescriptor<gLong> Ingredient1Amount = new PropertyDescriptor<>("Ingredient1Amount", gLong.class, "unsigned long", "Ingredients", gCRecipe_PS.class);
		public static final PropertyDescriptor<eCEntityProxy> Ingredient1Item = new PropertyDescriptor<>("Ingredient1Item", eCEntityProxy.class, "eCTemplateEntityProxy", "Ingredients", gCRecipe_PS.class);
		public static final PropertyDescriptor<gLong> Ingredient2Amount = new PropertyDescriptor<>("Ingredient2Amount", gLong.class, "unsigned long", "Ingredients", gCRecipe_PS.class);
		public static final PropertyDescriptor<eCEntityProxy> Ingredient2Item = new PropertyDescriptor<>("Ingredient2Item", eCEntityProxy.class, "eCTemplateEntityProxy", "Ingredients", gCRecipe_PS.class);
		public static final PropertyDescriptor<gLong> Ingredient3Amount = new PropertyDescriptor<>("Ingredient3Amount", gLong.class, "unsigned long", "Ingredients", gCRecipe_PS.class);
		public static final PropertyDescriptor<eCEntityProxy> Ingredient3Item = new PropertyDescriptor<>("Ingredient3Item", eCEntityProxy.class, "eCTemplateEntityProxy", "Ingredients", gCRecipe_PS.class);
		public static final PropertyDescriptor<gLong> Ingredient4Amount = new PropertyDescriptor<>("Ingredient4Amount", gLong.class, "unsigned long", "Ingredients", gCRecipe_PS.class);
		public static final PropertyDescriptor<eCEntityProxy> Ingredient4Item = new PropertyDescriptor<>("Ingredient4Item", eCEntityProxy.class, "eCTemplateEntityProxy", "Ingredients", gCRecipe_PS.class);
		public static final PropertyDescriptor<bCString> ReqAttrib1Tag = new PropertyDescriptor<>("ReqAttrib1Tag", bCString.class, "bCString", "Requirements", gCRecipe_PS.class);
		public static final PropertyDescriptor<gLong> ReqAttrib1Value = new PropertyDescriptor<>("ReqAttrib1Value", gLong.class, "unsigned long", "Requirements", gCRecipe_PS.class);
		public static final PropertyDescriptor<eCEntityProxy> ReqSkill1 = new PropertyDescriptor<>("ReqSkill1", eCEntityProxy.class, "eCTemplateEntityProxy", "Requirements", gCRecipe_PS.class);
		public static final PropertyDescriptor<gLong> ResultAmount = new PropertyDescriptor<>("ResultAmount", gLong.class, "unsigned long", "Result", gCRecipe_PS.class);
		public static final PropertyDescriptor<eCEntityProxy> ResultItem = new PropertyDescriptor<>("ResultItem", eCEntityProxy.class, "eCTemplateEntityProxy", "Result", gCRecipe_PS.class);
		public static final PropertyDescriptor<gLong> ResultQuality = new PropertyDescriptor<>("ResultQuality", gLong.class, "unsigned long", "Result", gCRecipe_PS.class);
	}

	public static interface gCScriptRoutine_PS extends eCEntityPropertySet {
		public static final PropertyDescriptor<bTPropertyContainer<gEAIMode>> AIMode = new PropertyDescriptor<>("AIMode", bTPropertyContainer.class, "bTPropertyContainer<enum gEAIMode>", "", gCScriptRoutine_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<gEAction>> Action = new PropertyDescriptor<>("Action", bTPropertyContainer.class, "bTPropertyContainer<enum gEAction>", "", gCScriptRoutine_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<gEAmbientAction>> AmbientAction = new PropertyDescriptor<>("AmbientAction", bTPropertyContainer.class, "bTPropertyContainer<enum gEAmbientAction>", "", gCScriptRoutine_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<gEAniState>> AniState = new PropertyDescriptor<>("AniState", bTPropertyContainer.class, "bTPropertyContainer<enum gEAniState>", "", gCScriptRoutine_PS.class);
		public static final PropertyDescriptor<gLong> CommandTime = new PropertyDescriptor<>("CommandTime", gLong.class, "long", "", gCScriptRoutine_PS.class);
		public static final PropertyDescriptor<gLong> CurrentBreakBlock = new PropertyDescriptor<>("CurrentBreakBlock", gLong.class, "long", "", gCScriptRoutine_PS.class);
		public static final PropertyDescriptor<bCString> CurrentState = new PropertyDescriptor<>("CurrentState", bCString.class, "bCString", "", gCScriptRoutine_PS.class);
		public static final PropertyDescriptor<bCString> CurrentTask = new PropertyDescriptor<>("CurrentTask", bCString.class, "bCString", "", gCScriptRoutine_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<gEHitDirection>> HitDirection = new PropertyDescriptor<>("HitDirection", bTPropertyContainer.class, "bTPropertyContainer<enum gEHitDirection>", "", gCScriptRoutine_PS.class);
		public static final PropertyDescriptor<bCString> LastTask = new PropertyDescriptor<>("LastTask", bCString.class, "bCString", "", gCScriptRoutine_PS.class);
		public static final PropertyDescriptor<bCString> Routine = new PropertyDescriptor<>("Routine", bCString.class, "bCScriptString", "", gCScriptRoutine_PS.class);
		public static final PropertyDescriptor<gLong> StatePosition = new PropertyDescriptor<>("StatePosition", gLong.class, "long", "", gCScriptRoutine_PS.class);
		public static final PropertyDescriptor<gFloat> StateTime = new PropertyDescriptor<>("StateTime", gFloat.class, "float", "", gCScriptRoutine_PS.class);
		public static final PropertyDescriptor<gLong> TaskPosition = new PropertyDescriptor<>("TaskPosition", gLong.class, "long", "", gCScriptRoutine_PS.class);
		public static final PropertyDescriptor<gFloat> TaskTime = new PropertyDescriptor<>("TaskTime", gFloat.class, "float", "", gCScriptRoutine_PS.class);
	}

	public static interface gCSector extends eCProcessibleElement {
		public static final PropertyDescriptor<Unknown> EntityLayers = new PropertyDescriptor<>("EntityLayers", Unknown.class, "bTRefPtrArray<class bCPropertyObjectBase *>", "", gCSector.class);
		public static final PropertyDescriptor<gBool> Freezed = new PropertyDescriptor<>("Freezed", gBool.class, "bool", "", gCSector.class);
		public static final PropertyDescriptor<Unknown> GeometryLayers = new PropertyDescriptor<>("GeometryLayers", Unknown.class, "bTRefPtrArray<class bCPropertyObjectBase *>", "", gCSector.class);
		public static final PropertyDescriptor<gBool> Locked = new PropertyDescriptor<>("Locked", gBool.class, "bool", "", gCSector.class);
		public static final PropertyDescriptor<gBool> Marked = new PropertyDescriptor<>("Marked", gBool.class, "bool", "", gCSector.class);
		public static final PropertyDescriptor<gFloat> ObjectCullFactor = new PropertyDescriptor<>("ObjectCullFactor", gFloat.class, "float", "", gCSector.class);
		public static final PropertyDescriptor<gFloat> VisualLoDFactor = new PropertyDescriptor<>("VisualLoDFactor", gFloat.class, "float", "", gCSector.class);
		public static final PropertyDescriptor<Unknown> World = new PropertyDescriptor<>("World", Unknown.class, "bTPOSmartPtr<class gCWorld>", "", gCSector.class);
	}

	public static interface gCSession extends eCEngineComponentBase {
		public static final PropertyDescriptor<bTPropertyContainer<gESession_State>> State = new PropertyDescriptor<>("State", bTPropertyContainer.class, "bTPropertyContainer<enum gESession_State>", "", gCSession.class);
	}

	public static interface gCSessionCheats extends ClassDescriptor {
		public static final PropertyDescriptor<gBool> CheatGodEnabled = new PropertyDescriptor<>("CheatGodEnabled", gBool.class, "bool", "", gCSessionCheats.class);
		public static final PropertyDescriptor<gBool> CheatInvisibleEnabled = new PropertyDescriptor<>("CheatInvisibleEnabled", gBool.class, "bool", "", gCSessionCheats.class);
	}

	public static interface gCSessionEditor extends eCInputReceiver {
		public static final PropertyDescriptor<bTPropertyContainer<gESessionEditorState>> State = new PropertyDescriptor<>("State", bTPropertyContainer.class, "bTPropertyContainer<enum gESessionEditorState>", "", gCSessionEditor.class);
	}

	public static interface gCSessionInfo extends ClassDescriptor {
		public static final PropertyDescriptor<bCString> Name = new PropertyDescriptor<>("Name", bCString.class, "bCString", "", gCSessionInfo.class);
		public static final PropertyDescriptor<gFloat> NumHoursPlayed = new PropertyDescriptor<>("NumHoursPlayed", gFloat.class, "float", "", gCSessionInfo.class);
		public static final PropertyDescriptor<gBool> PlayerHasCheated = new PropertyDescriptor<>("PlayerHasCheated", gBool.class, "bool", "", gCSessionInfo.class);
	}

	public static interface gCSkill_PS extends eCEntityPropertySet {
		public static final PropertyDescriptor<bTPropertyContainer<gESkillCategory>> Category = new PropertyDescriptor<>("Category", bTPropertyContainer.class, "bTPropertyContainer<enum gESkillCategory>", "Display", gCSkill_PS.class);
		public static final PropertyDescriptor<gInt> LearnPoints = new PropertyDescriptor<>("LearnPoints", gInt.class, "int", "Requirements", gCSkill_PS.class);
		public static final PropertyDescriptor<gInt> ReqAttrib1Value = new PropertyDescriptor<>("ReqAttrib1Value", gInt.class, "int", "Requirements", gCSkill_PS.class);
		public static final PropertyDescriptor<bCString> ReqAttrib1Tag = new PropertyDescriptor<>("ReqAttrib1Tag", bCString.class, "bCString", "Requirements", gCSkill_PS.class);
		public static final PropertyDescriptor<gInt> ReqAttrib2Value = new PropertyDescriptor<>("ReqAttrib2Value", gInt.class, "int", "Requirements", gCSkill_PS.class);
		public static final PropertyDescriptor<bCString> ReqAttrib2Tag = new PropertyDescriptor<>("ReqAttrib2Tag", bCString.class, "bCString", "Requirements", gCSkill_PS.class);
		public static final PropertyDescriptor<gInt> ReqAttrib3Value = new PropertyDescriptor<>("ReqAttrib3Value", gInt.class, "int", "Requirements", gCSkill_PS.class);
		public static final PropertyDescriptor<bCString> ReqAttrib3Tag = new PropertyDescriptor<>("ReqAttrib3Tag", bCString.class, "bCString", "Requirements", gCSkill_PS.class);
		public static final PropertyDescriptor<gInt> ReqAttrib4Value = new PropertyDescriptor<>("ReqAttrib4Value", gInt.class, "int", "Requirements", gCSkill_PS.class);
		public static final PropertyDescriptor<bCString> ReqAttrib4Tag = new PropertyDescriptor<>("ReqAttrib4Tag", bCString.class, "bCString", "Requirements", gCSkill_PS.class);
		public static final PropertyDescriptor<eCEntityProxy> ReqSkill1 = new PropertyDescriptor<>("ReqSkill1", eCEntityProxy.class, "eCTemplateEntityProxy", "Requirements", gCSkill_PS.class);
		public static final PropertyDescriptor<gInt> SortOrder = new PropertyDescriptor<>("SortOrder", gInt.class, "int", "Display", gCSkill_PS.class);
		public static final PropertyDescriptor<bCString> Tag = new PropertyDescriptor<>("Tag", bCString.class, "bCString", "Name", gCSkill_PS.class);
	}

	public static interface gCStat extends gCAttribute {
		public static final PropertyDescriptor<gInt> BaseMaximum = new PropertyDescriptor<>("BaseMaximum", gInt.class, "int", "Value", gCStat.class);
		public static final PropertyDescriptor<gInt> MaximumModifier = new PropertyDescriptor<>("MaximumModifier", gInt.class, "int", "Value", gCStat.class);
	}

	public static interface gCTeleporter_PS extends eCTrigger_PS {
		public static final PropertyDescriptor<bCString> DestinationEntity = new PropertyDescriptor<>("DestinationEntity", bCString.class, "bCString", "", gCTeleporter_PS.class);
	}

	public static interface gCTimeZone_PS extends gCDistanceTrigger_PS {
		public static final PropertyDescriptor<gFloat> TimeScale = new PropertyDescriptor<>("TimeScale", gFloat.class, "float", "", gCTimeZone_PS.class);
	}

	public static interface gCTouchDamage_PS extends eCTrigger_PS {
		public static final PropertyDescriptor<gBool> DamageDisabled = new PropertyDescriptor<>("DamageDisabled", gBool.class, "bool", "", gCTouchDamage_PS.class);
		public static final PropertyDescriptor<gBool> ResetOnUntouch = new PropertyDescriptor<>("ResetOnUntouch", gBool.class, "bool", "", gCTouchDamage_PS.class);
		public static final PropertyDescriptor<bCString> ScriptTouchFunc = new PropertyDescriptor<>("ScriptTouchFunc", bCString.class, "bCString", "", gCTouchDamage_PS.class);
	}

	public static interface gCTreasureSet_PS extends eCEntityPropertySet {
		public static final PropertyDescriptor<gLong> MaxTransferStacks = new PropertyDescriptor<>("MaxTransferStacks", gLong.class, "unsigned long", "", gCTreasureSet_PS.class);
		public static final PropertyDescriptor<gLong> MinTransferStacks = new PropertyDescriptor<>("MinTransferStacks", gLong.class, "unsigned long", "", gCTreasureSet_PS.class);
		public static final PropertyDescriptor<bTPropertyContainer<gETreasureDistribution>> TreasureDistribution = new PropertyDescriptor<>("TreasureDistribution", bTPropertyContainer.class, "bTPropertyContainer<enum gETreasureDistribution>", "", gCTreasureSet_PS.class);
	}

	public static interface gCWaterZone_PS extends eCEntityPropertySet {
		public static final PropertyDescriptor<bTValArray_bCVector> CoveringPoint = new PropertyDescriptor<>("CoveringPoint", bTValArray_bCVector.class, "bTValArray<class bCVector>", "", gCWaterZone_PS.class);
		public static final PropertyDescriptor<bCVector> FaceNormal = new PropertyDescriptor<>("FaceNormal", bCVector.class, "bCVector", "", gCWaterZone_PS.class);
		public static final PropertyDescriptor<gFloat> MaxX = new PropertyDescriptor<>("MaxX", gFloat.class, "float", "", gCWaterZone_PS.class);
		public static final PropertyDescriptor<gFloat> MaxZ = new PropertyDescriptor<>("MaxZ", gFloat.class, "float", "", gCWaterZone_PS.class);
		public static final PropertyDescriptor<gFloat> MinX = new PropertyDescriptor<>("MinX", gFloat.class, "float", "", gCWaterZone_PS.class);
		public static final PropertyDescriptor<gFloat> MinZ = new PropertyDescriptor<>("MinZ", gFloat.class, "float", "", gCWaterZone_PS.class);
		public static final PropertyDescriptor<bCVector> RadiusOffset = new PropertyDescriptor<>("RadiusOffset", bCVector.class, "bCVector", "", gCWaterZone_PS.class);
		public static final PropertyDescriptor<gFloat> SquareRadius = new PropertyDescriptor<>("SquareRadius", gFloat.class, "float", "", gCWaterZone_PS.class);
	}

	public static interface gCWorkspace extends eCProcessibleElement {
		public static final PropertyDescriptor<bCString> ActiveDatabase = new PropertyDescriptor<>("ActiveDatabase", bCString.class, "bCString", "", gCWorkspace.class);
		public static final PropertyDescriptor<bCString> ActiveProject = new PropertyDescriptor<>("ActiveProject", bCString.class, "bCString", "", gCWorkspace.class);
		public static final PropertyDescriptor<Unknown> Databases = new PropertyDescriptor<>("Databases", Unknown.class, "bTRefPtrArray<class bCPropertyObjectBase *>", "", gCWorkspace.class);
		public static final PropertyDescriptor<Unknown> Projects = new PropertyDescriptor<>("Projects", Unknown.class, "bTRefPtrArray<class bCPropertyObjectBase *>", "", gCWorkspace.class);
	}

	public static interface gCAIHelper_Label_PS extends eCEntityPropertySet {
	}

	public static interface gCAIHelper_FreePoint_PS extends gCAIHelper_Label_PS {
	}
	//@fon
}

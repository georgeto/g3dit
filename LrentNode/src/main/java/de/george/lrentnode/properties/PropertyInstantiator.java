package de.george.lrentnode.properties;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.george.g3utils.io.G3Serializable;
import de.george.g3utils.structure.bCBox;
import de.george.g3utils.structure.bCEulerAngles;
import de.george.g3utils.structure.bCMatrix;
import de.george.g3utils.structure.bCRect;
import de.george.g3utils.structure.bCVector;
import de.george.g3utils.structure.bCVector2;
import de.george.lrentnode.structures.bCFloatAlphaColor;
import de.george.lrentnode.structures.bCFloatColor;
import de.george.lrentnode.structures.bCGuid;

public class PropertyInstantiator {
	private static final Logger logger = LoggerFactory.getLogger(PropertyInstantiator.class);

	private static ConcurrentHashMap<String, Class<?>> classMap = new ConcurrentHashMap<>();
	private static Objenesis objenesis = new ObjenesisStd(true);

	private static void add(Class<?> clazz, String... types) {
		for (String type : types) {
			try {
				classMap.putIfAbsent(type, clazz);
			} catch (Exception e) {
				logger.warn("Error while getting constructor for property {}: ", clazz.getName(), e);
			}
		}
	}

	public static boolean isPropertyContainer(String name) {
		return name.startsWith("bTPropertyContainer<enum");
	}

	public static boolean isPropertyContainerArray(String name) {
		return name.startsWith("bTObjArray<class bTPropertyContainer<enum");
	}

	public static boolean isPropertyObject(String name) {
		return name.startsWith("bTPropertyObject<class");
	}

	@SuppressWarnings("unchecked")
	public static <T extends G3Serializable> T getPropertyInstance(String name, String type) {
		Class<?> clazz = classMap.get(type);
		if (clazz == null) {
			if (isPropertyContainer(type)) {
				clazz = bTPropertyContainer.class;
			} else if (isPropertyContainerArray(type)) {
				clazz = bTObjArray_bTPropertyContainer.class;
			} else if (isPropertyObject(type)) {
				return (T) new bTPropertyObject(type.substring(0, type.indexOf(",")).replace("bTPropertyObject<class", "").trim());
			} else {
				clazz = Unknown.class;
				logger.info("Unknon Property: {} - {}", name, type);
			}

			add(clazz, type);
		}

		try {
			return (T) objenesis.getInstantiatorOf(clazz).newInstance();
		} catch (Exception e) {
			logger.warn("Error while instantiating Property: {} - {}", name, type, e);
			throw new RuntimeException("Error while instantiating Property: " + name + " - " + type, e);
		}
	}

	public static <T extends G3Serializable> Optional<T> getPropertyDefaultValue(String type) {
		return getPropertyDefaultValue("<No name>", type);
	}

	@SuppressWarnings("unchecked")
	public static <T extends G3Serializable> Optional<T> getPropertyDefaultValue(String name, String type) {
		Class<?> clazz = classMap.get(type);
		if (clazz == null) {
			if (isPropertyContainer(type)) {
				clazz = bTPropertyContainer.class;
			} else if (isPropertyContainerArray(type)) {
				clazz = bTObjArray_bTPropertyContainer.class;
			} else if (isPropertyObject(type)) {
				return Optional
						.of((T) new bTPropertyObject(type.substring(0, type.indexOf(",")).replace("bTPropertyObject<class", "").trim()));
			} else {
				logger.info("Unknown Property: {} - {}", name, type);
				return Optional.empty();
			}
		}

		try {
			return Optional.of((T) clazz.getConstructor().newInstance());
		} catch (ReflectiveOperationException | SecurityException e) {
			logger.warn("Error while instantiating property with default value: {} - {}", name, type, e);
			return Optional.empty();
		}
	}

	public static <T extends G3Serializable> Optional<T> getPropertyDefaultValue(String name, Class<T> clazz) {
		try {
			return Optional.of(clazz.getConstructor().newInstance());
		} catch (ReflectiveOperationException | SecurityException e) {
			logger.warn("Error while instantiating property with default value: {} - {}", name, clazz.getSimpleName(), e);
			return Optional.empty();
		}
	}

	public static Set<String> getKnownTypes() {
		return Collections.unmodifiableSet(classMap.keySet());
	}

	static {
		add(bCBox.class, "bCBox");
		add(bCEulerAngles.class, "bCEulerAngles");
		add(bCFloatAlphaColor.class, "bCFloatAlphaColor");
		add(bCFloatColor.class, "bCFloatColor");
		add(bCGuid.class, "bCGuid");
		add(bCMatrix.class, "bCMatrix");
		add(bCPropertyID.class, "bCPropertyID");
		add(bCRange1.class, "bCRange1");
		add(bCRange3.class, "bCRange3");
		add(bCRect.class, "bCRect");
		add(bCString.class, "bCAnimationResourceString", "bCImageOrMaterialResourceString", "bCImageResourceString",
				"bCMeshResourceString", "bCScriptString", "bCSpeedTreeResourceString", "bCString", "eCLocString");
		add(bCVector.class, "bCVector");
		add(bCVector2.class, "bCVector2");
		add(bTObjArray_bCString.class, "bTObjArray<class bCString>");
		add(bTObjArray_eCEntityProxy.class, "bTObjArray<class eCEntityProxy>", "bTObjArray<class eCTemplateEntityProxy>");
		add(bTValArray_bCMotion.class, "bTValArray<class bCMotion>");
		add(bTValArray_bCPropertyID.class, "bTValArray<class bCPropertyID>");
		add(bTValArray_bCVector.class, "bTValArray<class bCVector>");
		add(bTValArray_bool.class, "bTValArray<bool>");
		add(bTValArray_float.class, "bTValArray<float>");
		add(bTValArray_gEDirection.class, "bTValArray<enum gEDirection>");
		add(bTValArray_long.class, "bTObjArray<unsigned long>", "bTValArray<long>", "bTValArray<unsigned long>");
		add(bTValArray_unsigned_short.class, "bTValArray<unsigned short>");
		add(eCEntityProxy.class, "eCEntityProxy", "eCTemplateEntityProxy");
		add(eCPropertySetProxy.class, "eCPropertySetProxy");
		add(gBool.class, "bool");
		add(gChar.class, "unsigned char", "char");
		add(gFloat.class, "float");
		add(gInt.class, "int");
		add(gLong.class, "long", "unsigned long");
		add(gShort.class, "short");
		add(gUnsignedShort.class, "unsigned short");
		add(bTObjArray_bTAutoPOSmartPtr_gCQuest_PS.class, "bTObjArray<class bTAutoPOSmartPtr<class gCQuest_PS> >");
	}
}

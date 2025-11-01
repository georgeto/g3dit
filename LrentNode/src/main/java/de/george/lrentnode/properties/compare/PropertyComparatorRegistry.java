package de.george.lrentnode.properties.compare;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import com.google.common.base.Strings;

import de.george.g3utils.io.G3Serializable;
import de.george.g3utils.structure.bCMotion;
import de.george.g3utils.structure.bCVector;
import de.george.lrentnode.properties.bCPropertyID;
import de.george.lrentnode.properties.bCString;
import de.george.lrentnode.properties.bTAutoPOSmartPtr_gCQuest_PS;
import de.george.lrentnode.properties.bTObjArray_bCString;
import de.george.lrentnode.properties.bTObjArray_bTAutoPOSmartPtr_gCQuest_PS;
import de.george.lrentnode.properties.bTObjArray_bTPropertyContainer;
import de.george.lrentnode.properties.bTObjArray_eCEntityProxy;
import de.george.lrentnode.properties.bTPropertyContainer;
import de.george.lrentnode.properties.bTValArray_bCMotion;
import de.george.lrentnode.properties.bTValArray_bCPropertyID;
import de.george.lrentnode.properties.bTValArray_bCVector;
import de.george.lrentnode.properties.bTValArray_bool;
import de.george.lrentnode.properties.bTValArray_float;
import de.george.lrentnode.properties.bTValArray_gEDirection;
import de.george.lrentnode.properties.bTValArray_long;
import de.george.lrentnode.properties.bTValArray_short;
import de.george.lrentnode.properties.bTValArray_unsigned_short;
import de.george.lrentnode.properties.eCEntityProxy;
import de.george.lrentnode.properties.gBool;
import de.george.lrentnode.properties.gChar;
import de.george.lrentnode.properties.gFloat;
import de.george.lrentnode.properties.gInt;
import de.george.lrentnode.properties.gLong;
import de.george.lrentnode.properties.gShort;
import de.george.lrentnode.properties.gUnsignedShort;
import de.george.lrentnode.structures.bCGuid;

public final class PropertyComparatorRegistry {
	private static final class Holder {
		private static final PropertyComparatorRegistry INSTANCE = new PropertyComparatorRegistry();
	}

	public static PropertyComparatorRegistry getInstance() {
		return Holder.INSTANCE;
	}

	private static final PropertyComparator<G3Serializable> FALLBACK_COMPARATOR = new BinaryPropertyComparator<>();

	private ConcurrentHashMap<Class<? extends G3Serializable>, PropertyComparator<? extends G3Serializable>> registeredComparators = new ConcurrentHashMap<>();

	@SuppressWarnings("unchecked")
	public PropertyComparator<G3Serializable> getComparator(Class<?> type) {
		return (PropertyComparator<G3Serializable>) registeredComparators.getOrDefault(type, FALLBACK_COMPARATOR);
	}

	private <T extends G3Serializable> void registerComparator(Class<T> propertyType, PropertyComparator<T> comparator) {
		registeredComparators.put(propertyType, comparator);
	}

	private <T extends G3Serializable, V extends G3Serializable> void registerArrayComparator(Class<T> propertyType, Class<V> valueType) {
		registeredComparators.put(propertyType, new ArrayPropertyComparator<>(getComparator(valueType)));
	}

	private PropertyComparatorRegistry() {
		registerDefaults();
	}

	private void registerDefaults() {
		registerComparator(bCString.class, new StringPropertyComparator<>(bCString::getString));
		registerComparator(eCEntityProxy.class,
				new StringPropertyComparator<>(((Function<eCEntityProxy, String>) eCEntityProxy::getGuid).andThen(Strings::nullToEmpty)));
		registerComparator(bCPropertyID.class,
				new StringPropertyComparator<>(((Function<bCPropertyID, String>) bCPropertyID::getGuid).andThen(Strings::nullToEmpty)));
		registerComparator(bCGuid.class,
				new StringPropertyComparator<>(((Function<bCGuid, String>) bCGuid::getGuid).andThen(Strings::nullToEmpty)));

		registerComparator(gBool.class, new TransformingPropertyComparator<>(gBool::isBool, Boolean::compare));
		registerComparator(gChar.class, new TransformingPropertyComparator<>(gChar::getChar, Byte::compare));
		registerComparator(gShort.class, new TransformingPropertyComparator<>(gShort::getShort, Short::compare));
		registerComparator(gUnsignedShort.class, new TransformingPropertyComparator<>(gUnsignedShort::getUnsignedShort, Integer::compare));
		registerComparator(gInt.class, new TransformingPropertyComparator<>(gInt::getInt, Integer::compare));
		registerComparator(gLong.class, new TransformingPropertyComparator<>(gLong::getLong, Integer::compare));
		registerComparator(gFloat.class, new TransformingPropertyComparator<>(gFloat::getFloat, Float::compare));

		registerArrayComparator(bTObjArray_bCString.class, bCString.class);
		registerArrayComparator(bTObjArray_bTAutoPOSmartPtr_gCQuest_PS.class, bTAutoPOSmartPtr_gCQuest_PS.class);
		registerArrayComparator(bTObjArray_bTPropertyContainer.class, bTPropertyContainer.class);
		registerArrayComparator(bTObjArray_eCEntityProxy.class, eCEntityProxy.class);
		registerArrayComparator(bTValArray_bCMotion.class, bCMotion.class);
		registerArrayComparator(bTValArray_bCPropertyID.class, bCPropertyID.class);
		registerArrayComparator(bTValArray_bCVector.class, bCVector.class);
		registerArrayComparator(bTValArray_bool.class, gBool.class);
		registerArrayComparator(bTValArray_float.class, gFloat.class);
		registerArrayComparator(bTValArray_gEDirection.class, gInt.class);
		registerArrayComparator(bTValArray_long.class, gLong.class);
		registerArrayComparator(bTValArray_short.class, gShort.class);
		registerArrayComparator(bTValArray_unsigned_short.class, gUnsignedShort.class);

		registerComparator(bCVector.class, new FloatPropertyComparator<>() {
			@Override
			public boolean equals(bCVector o1, bCVector o2) {
				return o1.equals(o2);
			}

			@Override
			public boolean similiar(bCVector o1, bCVector o2) {
				return o1.simliar(o2, 0.001f);
			}
		});
	}
}

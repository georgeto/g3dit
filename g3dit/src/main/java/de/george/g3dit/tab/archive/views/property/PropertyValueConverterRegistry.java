package de.george.g3dit.tab.archive.views.property;

import java.awt.Color;
import java.util.concurrent.ConcurrentHashMap;

import de.george.g3utils.io.G3Serializable;
import de.george.g3utils.structure.GuidUtil;
import de.george.g3utils.structure.bCBox;
import de.george.g3utils.structure.bCVector;
import de.george.lrentnode.properties.bCPropertyID;
import de.george.lrentnode.properties.bCRange1;
import de.george.lrentnode.properties.bCRange3;
import de.george.lrentnode.properties.bCString;
import de.george.lrentnode.properties.bTObjArray_bCString;
import de.george.lrentnode.properties.bTObjArray_eCEntityProxy;
import de.george.lrentnode.properties.bTValArray_bCPropertyID;
import de.george.lrentnode.properties.bTValArray_bCVector;
import de.george.lrentnode.properties.bTValArray_bool;
import de.george.lrentnode.properties.bTValArray_float;
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
import de.george.lrentnode.structures.eCColorScale;
import de.george.lrentnode.structures.eCFloatScale;
import de.george.lrentnode.structures.eCVectorScale;

public final class PropertyValueConverterRegistry {
	private static final class Holder {
		private static final PropertyValueConverterRegistry INSTANCE = new PropertyValueConverterRegistry();
	}

	public static PropertyValueConverterRegistry getInstance() {
		return Holder.INSTANCE;
	}

	private ConcurrentHashMap<Class<? extends G3Serializable>, PropertyValueConverter<? extends G3Serializable, ?>> registeredConverters = new ConcurrentHashMap<>();

	@SuppressWarnings("unchecked")
	public PropertyValueConverter<G3Serializable, Object> getConverter(Class<?> type) {
		return (PropertyValueConverter<G3Serializable, Object>) registeredConverters.get(type);
	}

	public <T extends G3Serializable> void registerConverter(PropertyValueConverter<T, ?> converter) {
		registeredConverters.put(converter.getPropertyType(), converter);
	}

	public <T extends G3Serializable> void registerConverterJsonFallback(PropertyValueConverter<T, ?> converter) {
		registerConverter(new FallbackConverter<>(converter, new JsonPropertyValueConverter<>(converter.getPropertyType())));
	}

	private PropertyValueConverterRegistry() {
		registerDefaults();
	}

	private void registerDefaults() {
		registerConverter(
				DictPropertyValueConverter.builder(bCBox.class).with("Min", bCVector.class, bCVector::toString, bCVector::fromString)
						.with("Max", bCVector.class, bCVector::toString, bCVector::fromString).build());
		/*
		 * registerConverter(bCEulerAngles.class,
		 * DictPropertyValueConverter.builder(bCEulerAngles.class) .with("Yaw", Float.TYPE, f ->
		 * f.toString(), Float::valueOf) .with("Pitch", Float.TYPE, f -> f.toString(),
		 * Float::valueOf) .with("Roll", Float.TYPE, f -> f.toString(), Float::valueOf).build());
		 */
		registerConverterJsonFallback(
				LambdaConverter.of(bCFloatAlphaColor.class, Color.class, bCFloatAlphaColor::toAwt, bCFloatAlphaColor::fromAwt));
		registerConverterJsonFallback(LambdaConverter.of(bCFloatColor.class, Color.class, bCFloatColor::toAwt, bCFloatColor::fromAwt));
		registerConverter(
				LambdaConverter.ofInplace(bCGuid.class, String.class, bCGuid::getGuid, (g, v) -> g.setGuid(GuidUtil.parseGuid(v))));
		// add(bCMatrix.class, "bCMatrix");
		registerConverter(LambdaConverter.ofInplace(bCPropertyID.class, String.class, bCPropertyID::getGuid,
				(g, v) -> g.setGuid(GuidUtil.parseGuid(v))));
		registerConverterJsonFallback(
				DictPropertyValueConverter.builder(bCRange1.class).with("Min", Float.TYPE, Object::toString, Float::valueOf)
						.with("Max", Float.TYPE, Object::toString, Float::valueOf).build());
		registerConverterJsonFallback(
				DictPropertyValueConverter.builder(bCRange3.class).with("Min", bCVector.class, bCVector::toString, bCVector::fromString)
						.with("Max", bCVector.class, bCVector::toString, bCVector::fromString).build());
		// add(bCRect.class, "bCRect");
		registerConverter(BeanPropertyValueConverter.with(bCString.class, String.class, "String"));
		registerConverterJsonFallback(LambdaConverter.of(bCVector.class, String.class, bCVector::toString, bCVector::fromString));
		// add(bCVector2.class, "bCVector2");
		registerConverter(ArrayPropertyValueConverter.of(bTObjArray_bCString.class, bCString::getString, bCString::new));
		registerConverter(ArrayPropertyValueConverter.of(bTObjArray_eCEntityProxy.class, eCEntityProxy::getGuid,
				g -> new eCEntityProxy(GuidUtil.parseGuid(g))));
		// add(bTValArray_bCMotion.class, "bTValArray<class bCMotion>");
		registerConverter(ArrayPropertyValueConverter.of(bTValArray_bCPropertyID.class, bCPropertyID::getGuid,
				g -> new bCPropertyID(GuidUtil.parseGuid(g))));
		registerConverter(ArrayPropertyValueConverter.of(bTValArray_bCVector.class, bCVector::toString, bCVector::fromString));
		registerConverter(ArrayPropertyValueConverter.of(bTValArray_bool.class, b -> Boolean.toString(b.isBool()),
				s -> new gBool(Boolean.parseBoolean(s))));
		registerConverter(ArrayPropertyValueConverter.of(bTValArray_float.class, f -> Float.toString(f.getFloat()),
				s -> new gFloat(Float.parseFloat(s))));
		// add(bTValArray_gEDirection.class, "bTValArray<enum gEDirection>");
		registerConverter(ArrayPropertyValueConverter.of(bTValArray_long.class, l -> Integer.toString(l.getLong()),
				s -> new gLong(Integer.parseInt(s))));
		registerConverter(ArrayPropertyValueConverter.of(bTValArray_unsigned_short.class, u -> Integer.toString(u.getUnsignedShort()),
				s -> new gUnsignedShort(Integer.parseInt(s))));
		registerConverter(LambdaConverter.ofInplace(eCEntityProxy.class, String.class, eCEntityProxy::getGuid,
				(g, v) -> g.setGuid(GuidUtil.parseGuid(v))));
		registerConverter(BeanPropertyValueConverter.with(gBool.class, Boolean.TYPE, "Bool"));
		registerConverter(BeanPropertyValueConverter.with(gChar.class, Character.TYPE, "Char"));
		registerConverter(BeanPropertyValueConverter.with(gFloat.class, Float.TYPE, "Float"));
		registerConverter(BeanPropertyValueConverter.with(gInt.class, Integer.TYPE, "Int"));
		registerConverter(BeanPropertyValueConverter.with(gLong.class, Integer.TYPE, "Long"));
		registerConverter(BeanPropertyValueConverter.with(gShort.class, Short.TYPE, "Short"));
		registerConverter(BeanPropertyValueConverter.with(gUnsignedShort.class, Integer.TYPE, "UnsignedShort"));

		registerConverter(new JsonPropertyValueConverter<>(eCFloatScale.class));
		registerConverter(new JsonPropertyValueConverter<>(eCVectorScale.class));
		registerConverter(new JsonPropertyValueConverter<>(eCColorScale.class));
	}
}

package de.george.lrentnode.classes.desc;

import static de.george.g3utils.util.ReflectionUtils.withModifier;
import static de.george.g3utils.util.ReflectionUtils.withType;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import de.george.g3utils.util.ReflectionUtils;
import one.util.streamex.StreamEx;

public interface ClassDescriptor {
	static final ConcurrentMap<Class<? extends ClassDescriptor>, String> NAME_MAP = new ConcurrentHashMap<>();

	public static String getName(Class<? extends ClassDescriptor> descriptor) {
		return NAME_MAP.computeIfAbsent(descriptor, Class::getSimpleName);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public static StreamEx<PropertyDescriptor<?>> getProperties(Class<? extends ClassDescriptor> descriptor) {
		return (StreamEx) StreamEx.of(descriptor.getDeclaredFields()).filterBy(Field::getType, PropertyDescriptor.class)
				.filter(f -> Modifier.isStatic(f.getModifiers())).map(ReflectionUtils::getStaticFieldValue);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public static StreamEx<PropertyDescriptor<?>> getAllProperties(Class<? extends ClassDescriptor> descriptor) {
		return (StreamEx) StreamEx
				.of(ReflectionUtils.getAllFields(descriptor, withType(PropertyDescriptor.class), withModifier(Modifier.STATIC)))
				.map(ReflectionUtils::getStaticFieldValue);
	}
}

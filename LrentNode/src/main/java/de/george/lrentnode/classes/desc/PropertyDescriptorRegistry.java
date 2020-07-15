package de.george.lrentnode.classes.desc;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSetMultimap;

public class PropertyDescriptorRegistry {
	// Initialization-on-demand holder idiom
	private static class Holder {
		static final PropertyDescriptorRegistry INSTANCE = new PropertyDescriptorRegistry();
	}

	public static PropertyDescriptorRegistry getInstance() {
		return Holder.INSTANCE;
	}

	private ImmutableMap<String, Class<? extends ClassDescriptor>> mapStringToClassDescriptor;
	private ImmutableMap<Class<? extends ClassDescriptor>, Map<String, PropertyDescriptor<?>>> mapNameToPropertyDescriptor;
	private ImmutableSetMultimap<Class<? extends ClassDescriptor>, Class<? extends ClassDescriptor>> derviedPropertySets;
	private ImmutableSetMultimap<Class<? extends ClassDescriptor>, String> derviedPropertySetNames;

	private PropertyDescriptorRegistry() {
		init();
	}

	@SuppressWarnings("unchecked")
	private void init() {
		ImmutableMap.Builder<String, Class<? extends ClassDescriptor>> mapStringToClassDescriptor = ImmutableMap.builder();
		ImmutableMap.Builder<Class<? extends ClassDescriptor>, Map<String, PropertyDescriptor<?>>> mapNameToPropertyDescriptor = ImmutableMap
				.builder();
		ImmutableSetMultimap.Builder<Class<? extends ClassDescriptor>, Class<? extends ClassDescriptor>> derviedPropertySets = ImmutableSetMultimap
				.builder();
		for (Class<ClassDescriptor> clazz : CD.getClassDescriptors()) {
			mapStringToClassDescriptor.put(clazz.getSimpleName(), clazz);
			Map<String, PropertyDescriptor<?>> properties = new HashMap<>();
			for (PropertyDescriptor<?> propertyDescriptor : ClassDescriptor.getAllProperties(clazz)) {
				properties.put(propertyDescriptor.getName(), propertyDescriptor);
			}
			mapNameToPropertyDescriptor.put(clazz, properties);
			for (Class<?> iface : clazz.getInterfaces()) {
				if (ClassDescriptor.class.isAssignableFrom(iface)) {
					derviedPropertySets.put((Class<? extends ClassDescriptor>) iface, clazz);
				}
			}
		}
		this.mapStringToClassDescriptor = mapStringToClassDescriptor.build();
		this.mapNameToPropertyDescriptor = mapNameToPropertyDescriptor.build();
		this.derviedPropertySets = derviedPropertySets.build();
		derviedPropertySetNames = this.derviedPropertySets.asMap().entrySet().stream().collect(ImmutableSetMultimap
				.flatteningToImmutableSetMultimap(Map.Entry::getKey, e -> e.getValue().stream().map(ClassDescriptor::getName)));
	}

	public Optional<Class<? extends ClassDescriptor>> lookupPropertySet(String propertySetName) {
		return Optional.ofNullable(mapStringToClassDescriptor.get(propertySetName));
	}

	public Optional<PropertyDescriptor<?>> lookupProperty(String propertySetName, String propertyName) {
		return Optional.ofNullable(mapStringToClassDescriptor.get(propertySetName)).map(mapNameToPropertyDescriptor::get)
				.map(m -> m.get(propertyName));
	}

	public Optional<PropertyDescriptor<?>> lookupProperty(Class<? extends ClassDescriptor> propertySet, String propertyName) {
		return Optional.ofNullable(mapNameToPropertyDescriptor.get(propertySet).get(propertyName));
	}

	public Set<Class<? extends ClassDescriptor>> derivedPropertySets(Class<? extends ClassDescriptor> propertySet) {
		return derviedPropertySets.get(propertySet);
	}

	public Set<String> derivedPropertySetNames(Class<? extends ClassDescriptor> propertySet) {
		return derviedPropertySetNames.get(propertySet);
	}
}

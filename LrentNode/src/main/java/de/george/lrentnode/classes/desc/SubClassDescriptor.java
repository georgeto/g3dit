package de.george.lrentnode.classes.desc;

import java.util.function.Function;

import de.george.lrentnode.classes.G3Class;

public class SubClassDescriptor {
	private final String name;
	private final String elementName;
	private final Class<? extends ClassDescriptor> subPropertySet;
	private final Class<? extends ClassDescriptor> propertySet;
	private Function<G3Class, Object> accessor;

	@SuppressWarnings("unchecked")
	public <P extends G3Class, V extends G3Class> SubClassDescriptor(String name, Class<? extends ClassDescriptor> subPropertySet,
			Class<? extends ClassDescriptor> propertySet, Function<P, V> accessor) {
		this(name, null, subPropertySet, propertySet, (Function) accessor);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public <P extends G3Class, V extends G3Class> SubClassDescriptor(String name, String elementName,
			Class<? extends ClassDescriptor> subPropertySet, Class<? extends ClassDescriptor> propertySet,
			Function<P, Iterable<V>> accessor) {
		this.name = name;
		this.elementName = elementName;
		this.subPropertySet = subPropertySet;
		this.propertySet = propertySet;
		this.accessor = (Function) accessor;
	}

	public String getName() {
		return name;
	}

	@SuppressWarnings("unchecked")
	public <P extends G3Class, V extends G3Class> V get(P propertySet) {
		return (V) accessor.apply(propertySet);
	}

	public boolean isList() {
		return elementName != null;
	}

	@SuppressWarnings("unchecked")
	public <P extends G3Class, V extends G3Class> Iterable<V> getList(P propertySet) {
		return (Iterable<V>) accessor.apply(propertySet);
	}

	/**
	 * @return Name of a single element in case {@link #isList()}{@code  == true}, otherwise
	 *         {@code null}.
	 */
	public String getElementName() {
		return elementName;
	}

	public Class<? extends ClassDescriptor> getSubPropertySet() {
		return subPropertySet;
	}

	public Class<? extends ClassDescriptor> getPropertySet() {
		return propertySet;
	}
}

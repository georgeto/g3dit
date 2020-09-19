package de.george.g3dit.gui.edit.adapter;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import de.george.g3utils.io.G3Serializable;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.classes.G3Class;
import de.george.lrentnode.classes.desc.ClassDescriptor;

public class GetterSetterPropertyAdapter<C extends G3Class, V extends G3Serializable> implements PropertyAdapter<V> {
	private final Class<? extends ClassDescriptor> propertySet;
	private final Function<C, V> getter;
	private final BiConsumer<C, V> setter;
	private final Supplier<V> defaultSupplier;
	private final Class<V> dataType;
	private final String dataTypeName;

	public GetterSetterPropertyAdapter(Class<? extends ClassDescriptor> propertySet, Function<C, V> getter, BiConsumer<C, V> setter,
			Supplier<V> defaultSupplier, Class<V> dataType, String dataTypeName) {
		this.propertySet = propertySet;
		this.getter = getter;
		this.setter = setter;
		this.defaultSupplier = defaultSupplier;
		this.dataType = dataType;
		this.dataTypeName = dataTypeName;
	}

	@Override
	public V getValue(eCEntity entity) {
		// If property not present in property set, fall back to default value.
		return entity.<C>getClassOptional(propertySet).map(this.getter).orElseGet(defaultSupplier);
	}

	@Override
	public void setValue(eCEntity entity, V newValue) {
		setter.accept(entity.getClass(propertySet), newValue);
	}

	@Override
	public Class<V> getDataType() {
		return dataType;
	}

	@Override
	public String getDataTypeName() {
		return dataTypeName;
	}

	@Override
	public V getDefaultValue() {
		return defaultSupplier.get();
	}
}

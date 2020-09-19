package de.george.g3dit.gui.edit.adapter;

import java.util.function.Function;

import de.george.g3utils.io.G3Serializable;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.classes.G3Class;
import de.george.lrentnode.classes.desc.PropertyDescriptor;
import de.george.lrentnode.properties.PropertyInstantiator;
import de.george.lrentnode.properties.compare.PropertyComparator;
import de.george.lrentnode.properties.compare.PropertyComparatorRegistry;

public class DescriptorPropertyAdapter implements PropertyAdapter<G3Serializable> {
	private final PropertyDescriptor<G3Serializable> descriptor;
	private final Function<eCEntity, G3Class> propertySetExtractor;

	public DescriptorPropertyAdapter(PropertyDescriptor<?> descriptor) {
		this(descriptor, null);
	}

	@SuppressWarnings("unchecked")
	public DescriptorPropertyAdapter(PropertyDescriptor<?> descriptor, Function<eCEntity, G3Class> propertySetExtractor) {
		this.descriptor = (PropertyDescriptor<G3Serializable>) descriptor;
		if (propertySetExtractor != null) {
			this.propertySetExtractor = propertySetExtractor;
		} else {
			this.propertySetExtractor = entity -> entity.getClass(descriptor.getPropertySet());
		}
	}

	@Override
	public G3Serializable getValue(eCEntity entity) {
		// If property not present in property set, fall back to default value.
		return propertySetExtractor.apply(entity).propertyNoThrow(descriptor).orElseGet(descriptor::getDefaultValue);
	}

	@Override
	public void setValue(eCEntity entity, G3Serializable newValue) {
		G3Class propertySet = propertySetExtractor.apply(entity);
		if (!propertySet.hasProperty(descriptor)) {
			// Property is not yet present in property set
			PropertyComparator<G3Serializable> comparator = PropertyComparatorRegistry.getInstance()
					.getComparator(descriptor.getDataType());

			if (comparator.equals(newValue, descriptor.getDefaultValue())) {
				// Don't create property if its value hasn't changed from the default value.
				return;
			}
		}
		propertySet.setPropertyData(descriptor, newValue);
	}

	@Override
	public Class<G3Serializable> getDataType() {
		return descriptor.getDataType();
	}

	@Override
	public String getDataTypeName() {
		return descriptor.getDataTypeName();
	}

	@Override
	public G3Serializable getDefaultValue() {
		return PropertyInstantiator.getPropertyInstance(descriptor.getName(), descriptor.getDataTypeName());
	}
}

package de.george.g3dit.gui.components.search;

import java.util.regex.Pattern;

import de.george.g3dit.tab.archive.views.property.PropertyValueConverterRegistry;
import de.george.g3utils.io.G3Serializable;
import de.george.lrentnode.archive.G3ClassContainer;
import de.george.lrentnode.classes.G3Class;
import de.george.lrentnode.classes.desc.PropertyDescriptor;
import de.george.lrentnode.classes.desc.SubClassDescriptor;
import de.george.lrentnode.properties.ClassProperty;
import de.george.lrentnode.properties.compare.CompareOperation;
import de.george.lrentnode.properties.compare.PropertyComparator;
import de.george.lrentnode.util.PropertyUtil;

public class PropertySearchFilter<T extends G3ClassContainer> implements SearchFilter<T> {
	private final SubClassDescriptor subClassDescriptor;
	private final PropertyDescriptor<?> propertyDesc;
	private final ClassProperty<G3Serializable> property;
	private final PropertyComparator<G3Serializable> comparator;
	private final CompareOperation operation;
	private Pattern cachedPattern;

	public PropertySearchFilter(SubClassDescriptor subClassDescriptor, PropertyDescriptor<?> propertyDesc,
			ClassProperty<G3Serializable> property, PropertyComparator<G3Serializable> comparator, CompareOperation operation) {
		this.subClassDescriptor = subClassDescriptor;
		this.propertyDesc = propertyDesc;
		this.property = property != null ? PropertyUtil.clone(property) : null;
		this.comparator = comparator;
		this.operation = operation;

		// Cache regex value
		if (property != null && operation != null && operation.isRegex()) {
			try {
				String raw = (String) PropertyValueConverterRegistry.getInstance().getConverter(propertyDesc.getDataType())
						.convertTo(property.getValue());
				cachedPattern = Pattern.compile(raw, operation == CompareOperation.RegexIgnoreCase ? Pattern.CASE_INSENSITIVE : 0);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean matches(T container) {
		if (propertyDesc == null) {
			return false;
		}

		Object matchValue = operation.isRegex() ? cachedPattern : property.getValue();
		if (subClassDescriptor != null) {
			if (!container.hasClass(subClassDescriptor.getPropertySet())) {
				return false;
			}

			G3Class propertySet = container.getClass(subClassDescriptor.getPropertySet());
			if (subClassDescriptor.isList()) {
				for (G3Class subPropertySet : subClassDescriptor.getList(propertySet)) {
					if (subPropertySet.hasProperty(propertyDesc)
							&& comparator.satisfies(operation, subPropertySet.property(propertyDesc), matchValue)) {
						return true;
					}
				}
				return false;
			} else {
				G3Class subPropertySet = subClassDescriptor.get(propertySet);
				return subPropertySet.hasProperty(propertyDesc)
						&& comparator.satisfies(operation, subPropertySet.property(propertyDesc), matchValue);
			}
		} else {
			return container.hasProperty(propertyDesc) && comparator.satisfies(operation, container.getProperty(propertyDesc), matchValue);
		}
	}

	@Override
	public boolean isValid() {
		return propertyDesc != null && property != null && comparator != null && (!operation.isRegex() || cachedPattern != null);
	}

	public SubClassDescriptor getSubClassDescriptor() {
		return subClassDescriptor;
	}

	public PropertyDescriptor<?> getPropertyDesc() {
		return propertyDesc;
	}

	public ClassProperty<G3Serializable> getProperty() {
		return property;
	}

	public PropertyComparator<G3Serializable> getComparator() {
		return comparator;
	}

	public CompareOperation getOperation() {
		return operation;
	}
}

package de.george.g3dit.gui.components.search;

import de.george.lrentnode.archive.G3ClassContainer;
import de.george.lrentnode.classes.desc.ClassDescriptor;

public class PropertySetSearchFilter<T extends G3ClassContainer> implements SearchFilter<T> {
	private final Class<? extends ClassDescriptor> propertySet;

	public PropertySetSearchFilter(Class<? extends ClassDescriptor> propertySet) {
		this.propertySet = propertySet;
	}

	@Override
	public boolean matches(T container) {
		return container.hasClass(propertySet);
	}

	@Override
	public boolean isValid() {
		return propertySet != null;
	}
}

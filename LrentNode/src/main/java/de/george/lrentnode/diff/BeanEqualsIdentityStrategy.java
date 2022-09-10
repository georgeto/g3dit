package de.george.lrentnode.diff;

import de.danielbechler.diff.access.PropertyAwareAccessor;
import de.danielbechler.diff.identity.IdentityStrategy;
import de.danielbechler.diff.instantiation.TypeInfo;
import de.george.lrentnode.diff.introspection.FieldIntrospector;

public class BeanEqualsIdentityStrategy implements IdentityStrategy {
	private FieldIntrospector introspector = new FieldIntrospector();

	@Override
	public boolean equals(Object working, Object base) {
		if (working == null || base == null || working.getClass() != base.getClass()) {
			return false;
		}

		try {
			if (base.getClass().getDeclaredMethod("equals", Object.class) != null) {
				return base.equals(working);
			} else if (base.getClass().getDeclaredMethod("equals", base.getClass()) != null) {
				return base.equals(working);
			}
		} catch (NoSuchMethodException | SecurityException e) {
			// Ignore
		}

		TypeInfo typeInfo = introspector.introspect(base.getClass());
		if (typeInfo.getAccessors().isEmpty()) {
			return base.equals(working);
		}

		for (final PropertyAwareAccessor propertyAccessor : typeInfo.getAccessors()) {
			if (!equals(propertyAccessor.get(working), propertyAccessor.get(base))) {
				return false;
			}
		}

		return true;
	}
}

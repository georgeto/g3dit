package de.george.lrentnode.diff.introspection;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import de.danielbechler.diff.instantiation.TypeInfo;
import de.danielbechler.diff.introspection.Introspector;
import de.george.g3utils.util.ReflectionUtils;

public class FieldIntrospector implements Introspector {
	private boolean returnFinalFields;

	@Override
	public TypeInfo introspect(final Class<?> type) {
		final TypeInfo typeInfo = new TypeInfo(type);
		for (final Field field : ReflectionUtils.getAllFields(type)) {
			if (shouldSkip(field)) {
				continue;
			}
			typeInfo.addPropertyAccessor(new FieldAccessor(field));
		}
		return typeInfo;
	}

	private boolean shouldSkip(final Field field) {
		return Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers()) && !returnFinalFields;
	}

	public void setReturnFinalFields(final boolean returnFinalFields) {
		this.returnFinalFields = returnFinalFields;
	}
}

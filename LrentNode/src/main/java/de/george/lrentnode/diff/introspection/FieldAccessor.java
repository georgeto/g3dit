package de.george.lrentnode.diff.introspection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import de.danielbechler.diff.access.PropertyAwareAccessor;
import de.danielbechler.diff.introspection.ObjectDiffProperty;
import de.danielbechler.diff.introspection.PropertyReadException;
import de.danielbechler.diff.introspection.PropertyWriteException;
import de.danielbechler.diff.selector.BeanPropertyElementSelector;
import de.danielbechler.diff.selector.ElementSelector;
import de.danielbechler.util.Assert;

public class FieldAccessor implements PropertyAwareAccessor {
	private final Field field;

	public FieldAccessor(final Field field) {
		Assert.notNull(field, "field");
		this.field = field;
	}

	@Override
	public Class<?> getType() {
		return field.getType();
	}

	@Override
	public Set<String> getCategoriesFromAnnotation() {
		return Collections.emptySet();
	}

	@Override
	public ElementSelector getElementSelector() {
		return new BeanPropertyElementSelector(getPropertyName());
	}

	@Override
	public Object get(Object target) {
		if (target == null) {
			return null;
		}

		try {
			field.setAccessible(true);
			return field.get(target);
		} catch (IllegalAccessException | RuntimeException e) {
			throw new PropertyReadException(getPropertyName(), getType(), e);
		} finally {
			field.setAccessible(false);
		}
	}

	@Override
	public void set(Object target, Object value) {
		try {
			field.setAccessible(true);
			field.set(target, value);
		} catch (IllegalAccessException | RuntimeException e) {
			throw new PropertyWriteException(getPropertyName(), getType(), value, e);
		} finally {
			field.setAccessible(false);
		}
	}

	@Override
	public void unset(Object target) {}

	@Override
	public String getPropertyName() {
		return field.getName();
	}

	@Override
	public Set<Annotation> getFieldAnnotations() {
		final Set<Annotation> fieldAnnotations = new HashSet<>(field.getAnnotations().length);
		fieldAnnotations.addAll(Arrays.asList(field.getAnnotations()));
		return fieldAnnotations;
	}

	@Override
	public <T extends Annotation> T getFieldAnnotation(Class<T> annotationClass) {
		return field.getAnnotation(annotationClass);
	}

	@Override
	public Set<Annotation> getReadMethodAnnotations() {
		return Collections.emptySet();
	}

	@Override
	public <T extends Annotation> T getReadMethodAnnotation(Class<T> annotationClass) {
		return null;
	}

	@Override
	public boolean isExcludedByAnnotation() {
		ObjectDiffProperty annotation = getFieldAnnotation(ObjectDiffProperty.class);
		return annotation != null && annotation.excluded();
	}
}

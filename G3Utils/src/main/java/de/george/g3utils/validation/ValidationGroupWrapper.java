package de.george.g3utils.validation;

import java.awt.Component;
import java.lang.reflect.Field;
import java.util.LinkedList;

import org.netbeans.validation.api.ui.ValidationGroup;
import org.netbeans.validation.api.ui.ValidationListener;

import de.george.g3utils.util.ReflectionUtils;

public class ValidationGroupWrapper {
	private static final Field GROUPITEMS_FIELD = ReflectionUtils.getField(ValidationGroup.class, "validationItems");
	private static final Field LISTENER_TARGET = ReflectionUtils.getField(ValidationListener.class, "target");

	private final ValidationGroup group;

	public ValidationGroupWrapper(ValidationGroup group) {
		this.group = group;
	}

	public ValidationGroup getValidationGroup() {
		return group;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public boolean contains(ValidationListener validator) {
		return ((LinkedList<ValidationListener>) ReflectionUtils.getFieldValue(GROUPITEMS_FIELD, group)).contains(validator);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public ValidationListener remove(Component comp) {
		LinkedList<ValidationListener> list = ReflectionUtils.getFieldValue(GROUPITEMS_FIELD, group);
		for (ValidationListener listener : list) {
			Object target = ReflectionUtils.getFieldValue(LISTENER_TARGET, listener);
			if (comp.equals(target)) {
				group.remove(listener);
				return listener;
			}
		}
		return null;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public void removeAll() {
		LinkedList<ValidationListener> list = (LinkedList<ValidationListener>) ((LinkedList<ValidationListener>) ReflectionUtils
				.getFieldValue(GROUPITEMS_FIELD, group)).clone();
		for (ValidationListener listener : list) {
			group.remove(listener);
		}
	}
}

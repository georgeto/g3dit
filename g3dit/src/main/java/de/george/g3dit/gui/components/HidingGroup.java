package de.george.g3dit.gui.components;

import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.netbeans.validation.api.ui.ValidationGroup;
import org.netbeans.validation.api.ui.ValidationListener;

import de.george.g3utils.validation.ValidationGroupWrapper;

public class HidingGroup {
	private List<Component> elements = new ArrayList<>();
	@SuppressWarnings("rawtypes")
	private Map<Component, ValidationListener> validators = new HashMap<>();

	public static HidingGroup create(Component... comps) {
		HidingGroup group = new HidingGroup();
		for (Component comp : comps) {
			group.add(comp);
		}
		return group;
	}

	public void add(Component comp) {
		if (comp != null) {
			elements.add(comp);
		}
	}

	public void remove(Component comp) {
		if (comp != null) {
			elements.remove(comp);
			validators.remove(comp);
		}

	}

	public void setVisible(boolean b) {
		elements.forEach(e -> e.setVisible(b));
	}

	@SuppressWarnings("rawtypes")
	public void setVisible(boolean b, ValidationGroup group) {
		setVisible(b);
		if (b) {
			for (Component element : elements) {
				ValidationListener validator = validators.get(element);
				if (validator != null) {
					ValidationGroupWrapper wrapper = new ValidationGroupWrapper(group);
					if (!wrapper.contains(validator)) {
						group.addItem(validator, false);
					}
				}

			}
		} else {
			ValidationGroupWrapper wrapper = new ValidationGroupWrapper(group);
			for (Component element : elements) {
				ValidationListener validator = wrapper.remove(element);
				if (validator != null) {
					validators.put(element, validator);
				}
			}
		}

	}
}

package de.george.g3dit.gui.components;

import java.awt.Component;
import java.util.LinkedHashSet;

import javax.swing.Action;

public class EnableGroup {
	private LinkedHashSet<Component> elements = new LinkedHashSet<>();
	private LinkedHashSet<Action> actions = new LinkedHashSet<>();

	public static EnableGroup create(Component... comps) {
		return new EnableGroup().add(comps);
	}

	public static EnableGroup create(EnableGroup group) {
		return new EnableGroup().add(group);
	}

	public EnableGroup add(Component... comps) {
		for (Component comp : comps) {
			if (comp != null) {
				elements.add(comp);
			}
		}
		return this;
	}

	public EnableGroup remove(Component... comps) {
		for (Component comp : comps) {
			if (comp != null) {
				elements.remove(comp);
			}
		}
		return this;
	}

	public EnableGroup add(Action... actions) {
		for (Action action : actions) {
			if (action != null) {
				this.actions.add(action);
			}
		}
		return this;
	}

	public EnableGroup remove(Action... actions) {
		for (Action action : actions) {
			if (action != null) {
				this.actions.remove(action);
			}
		}
		return this;
	}

	public EnableGroup add(EnableGroup group) {
		group.elements.forEach(this::add);
		group.actions.forEach(this::add);
		return this;
	}

	public EnableGroup remove(EnableGroup group) {
		group.elements.forEach(this::remove);
		group.actions.forEach(this::remove);
		return this;
	}

	public void setEnabled(boolean b) {
		elements.forEach(e -> e.setEnabled(b));
		actions.forEach(e -> e.setEnabled(b));
	}

	public boolean isEnabled() {
		return elements.stream().allMatch(e -> e.isEnabled()) && actions.stream().allMatch(e -> e.isEnabled());
	}
}

package de.george.g3dit.gui.components;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;

public class EnableGroup {
	private List<Component> elements = new ArrayList<>();
	private List<Action> actions = new ArrayList<>();

	public static EnableGroup create(Component... comps) {
		EnableGroup group = new EnableGroup();
		for (Component comp : comps) {
			group.add(comp);
		}
		return group;
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

	public void setEnabled(boolean b) {
		elements.forEach(e -> e.setEnabled(b));
		actions.forEach(e -> e.setEnabled(b));
	}

	public boolean isEnabled() {
		return elements.stream().allMatch(e -> e.isEnabled()) && actions.stream().allMatch(e -> e.isEnabled());
	}
}

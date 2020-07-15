package de.george.g3dit.gui.components.tab;

import java.awt.Color;
import java.awt.Component;

import javax.swing.Icon;

public interface ITypedTab {

	public String getTabTitle();

	default Color getTitleColor() {
		return null;
	}

	public Icon getTabIcon();

	default String getTooltip() {
		return null;
	}

	public Component getTabContent();
}

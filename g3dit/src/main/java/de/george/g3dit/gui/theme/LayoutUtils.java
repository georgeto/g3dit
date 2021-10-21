package de.george.g3dit.gui.theme;

import one.util.streamex.StreamEx;

public class LayoutUtils {
	public static final String sqrBtn(String... layoutConstraints) {
		return StreamEx.of("width sqb!, height sqb!").append(layoutConstraints).joining(", ");
	}
}

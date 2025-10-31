package de.george.g3dit.entitytree.filter;

import com.teamunify.i18n.I;

import de.george.g3utils.structure.bCVector;
import de.george.lrentnode.archive.eCEntity;

public class PositionEntityFilter extends AbstractEntityFilter {

	private bCVector positonToMatch;
	private float positionTolerance;
	private boolean ignoreY;

	public PositionEntityFilter(String text, String toleranceText) {
		try {
			if (text.startsWith("#l#")) {
				ignoreY = true;
				text = text.substring(3);
			}
			positonToMatch = bCVector.fromString(text.replaceFirst("//.*", "//"));
			if (ignoreY) {
				positonToMatch.setY(0.0f);
			}
		} catch (IllegalArgumentException e) {
			positonToMatch = null;
		}
		try {
			positionTolerance = Float.parseFloat(toleranceText);
		} catch (NumberFormatException e) {
			positionTolerance = 0;
		}
	}

	public PositionEntityFilter(bCVector positonToMatch, float positionTolerance, boolean ignoreY) {
		this.positonToMatch = positonToMatch.clone();
		if (ignoreY) {
			positonToMatch.setY(0.0f);
		}
		this.positionTolerance = positionTolerance;
		this.ignoreY = ignoreY;
	}

	@Override
	public boolean matches(eCEntity entity) {
		bCVector worldPosition = entity.getWorldPosition();
		if (ignoreY) {
			worldPosition = new bCVector(worldPosition.getX(), 0.0f, worldPosition.getZ());
		}
		return positonToMatch.getRelative(worldPosition).length() <= positionTolerance;
	}

	@Override
	public boolean isValid() {
		return positonToMatch != null;
	}

	public bCVector getPositonToMatch() {
		return positonToMatch;
	}

	public float getPositionTolerance() {
		return positionTolerance;
	}

	public boolean isIgnoreY() {
		return ignoreY;
	}

	public static String getToolTipText() {
		// @foff
		return I.tr("<html>"
				+ "Coordinates in the form x/y/z//<br>"
				+ "The position can be prefixed with one of the following prefixes."
				+ "<ul>"
				+ "<li><b>No prefix</b>: Distance</li>"
				+ "<li><b>#l#</b>: Distance ignoring the Y component</li>"
				+ "</ul></html>");
		// @fon
	}
}

package de.george.g3dit.entitytree.filter;

import com.teamunify.i18n.I;

import de.george.g3utils.structure.bCVector;
import de.george.lrentnode.archive.eCEntity;

public class PositionEntityFilter extends AbstractEntityFilter {

	private bCVector positonToMatch;
	private float positionTolerance;
	private boolean ignoreY;
	private boolean cube;

	public PositionEntityFilter(String text, String toleranceText) {
		try {
			if (text.startsWith("#l#")) {
				ignoreY = true;
				text = text.substring(3);
			} else if (text.startsWith("#c#")) {
				cube = true;
				text = text.substring(3);
			} else if (text.startsWith("#s#")) {
				ignoreY = true;
				cube = true;
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

	public PositionEntityFilter(bCVector positonToMatch, float positionTolerance, boolean ignoreY, boolean cube) {
		this.positonToMatch = positonToMatch.clone();
		if (ignoreY) {
			this.positonToMatch.setY(0.0f);
		}
		this.positionTolerance = positionTolerance;
		this.ignoreY = ignoreY;
		this.cube = cube;
	}

	@Override
	public boolean matches(eCEntity entity) {
		bCVector worldPosition = entity.getWorldPosition();
		if (ignoreY) {
			worldPosition = new bCVector(worldPosition.getX(), 0.0f, worldPosition.getZ());
		}
		bCVector relative = positonToMatch.getRelative(worldPosition);
		if (cube) {
			relative.absolute();
			return relative.getX() <= positionTolerance && relative.getY() <= positionTolerance && relative.getZ() <= positionTolerance;
		} else
			return relative.length() <= positionTolerance;
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

	public boolean isCube() {
		return cube;
	}

	public static String getToolTipText() {
		// @foff
		return I.tr("<html>"
				+ "Coordinates in the form x/y/z//<br>"
				+ "The position can be prefixed with one of the following prefixes."
				+ "<ul>"
				+ "<li><b>No prefix</b>: Distance</li>"
				+ "<li><b>#l#</b>: Distance ignoring the Y component</li>"
				+ "<li><b>#c#</b>: Cube side length (instead of sphere radius)</li>"
				+ "<li><b>#s#</b>: Square side length ignoring the Y component (instead of circle radius)</li>"
				+ "</ul></html>");
		// @fon
	}
}

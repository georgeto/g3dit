package de.george.g3dit.gui.theme;

import java.util.Arrays;

import net.miginfocom.layout.ComponentWrapper;
import net.miginfocom.layout.ContainerWrapper;
import net.miginfocom.layout.UnitConverter;
import net.miginfocom.layout.UnitValue;

public class EditorUnitConverter extends UnitConverter {
	private int squareButtonSize;

	private EditorUnitConverter(int squareButtonSize) {
		this.squareButtonSize = squareButtonSize;
	}

	@Override
	public int convertToPixels(float value, String unit, boolean isHor, float refValue, ContainerWrapper parent, ComponentWrapper comp) {
		if (unit.equals("sqb")) {
			return new UnitValue(value * squareButtonSize, UnitValue.PIXEL, null).getPixels(refValue, parent, comp);
		} else {
			return UnitConverter.UNABLE;
		}
	}

	public static final void register() {
		EditorUnitConverter unitConverter;
		unitConverter = new EditorUnitConverter(27);

		// Remove previous EditorUnitConverter instance
		Arrays.stream(UnitValue.getGlobalUnitConverters()).filter(c -> c instanceof EditorUnitConverter)
				.forEach(UnitValue::removeGlobalUnitConverter);

		// Add current EditorUnitConverter instance
		UnitValue.addGlobalUnitConverter(unitConverter);
	}
}

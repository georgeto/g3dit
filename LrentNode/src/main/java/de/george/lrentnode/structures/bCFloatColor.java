package de.george.lrentnode.structures;

import java.awt.Color;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;

public class bCFloatColor extends FloatColor {
	int vtable;

	public bCFloatColor() {
		this(0.0f, 0.0f, 0.0f);
	}

	public bCFloatColor(float red, float green, float blue) {
		super(red, green, blue);
		vtable = -1;
	}

	@Override
	public void read(G3FileReader reader) {
		vtable = reader.readInt(); // vtable is written and ignored on read by g3
		super.read(reader);
	}

	@Override
	public void write(G3FileWriter writer) {
		writer.writeInt(vtable);
		super.write(writer);
	}

	public static bCFloatColor fromAwt(Color color) {
		return new bCFloatColor(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f);
	}
}

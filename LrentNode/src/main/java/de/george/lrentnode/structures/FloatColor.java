package de.george.lrentnode.structures;

import java.awt.Color;

import com.fasterxml.jackson.annotation.JsonProperty;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.io.G3Serializable;

public class FloatColor implements G3Serializable {

	protected float red;
	protected float green;
	protected float blue;

	public FloatColor(float red, float green, float blue) {
		this.red = red;
		this.green = green;
		this.blue = blue;
	}

	@JsonProperty
	public float getRed() {
		return red;
	}

	public void setRed(float red) {
		this.red = red;
	}

	@JsonProperty
	public float getGreen() {
		return green;
	}

	public void setGreen(float green) {
		this.green = green;
	}

	@JsonProperty
	public float getBlue() {
		return blue;
	}

	public void setBlue(float blue) {
		this.blue = blue;
	}

	@Override
	public void read(G3FileReader reader) {
		red = reader.readFloat();
		green = reader.readFloat();
		blue = reader.readFloat();
	}

	@Override
	public void write(G3FileWriter writer) {
		writer.writeFloat(red).writeFloat(green).writeFloat(blue);
	}

	@Override
	public String toString() {
		return "red=" + red + ", green=" + green + ", blue=" + blue;
	}

	public static FloatColor fromAwt(Color color) {
		return new FloatColor(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f);
	}

	public Color toAwt() {
		return new Color(getRed(), getGreen(), getBlue());
	}
}

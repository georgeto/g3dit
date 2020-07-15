package de.george.lrentnode.structures;

import java.awt.Color;

import com.fasterxml.jackson.annotation.JsonProperty;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;

public class bCFloatAlphaColor extends bCFloatColor {
	private float alpha;

	public bCFloatAlphaColor() {
		this(0.0f, 0.0f, 0.0f, 0.0f);
	}

	public bCFloatAlphaColor(float red, float green, float blue, float alpha) {
		super(red, green, blue);
		this.alpha = alpha;
	}

	@JsonProperty
	public float getAlpha() {
		return alpha;
	}

	public void setAlpha(float alpha) {
		this.alpha = alpha;
	}

	@Override
	public void read(G3FileReader reader) {
		super.read(reader);
		alpha = reader.readFloat();
	}

	@Override
	public void write(G3FileWriter writer) {
		super.write(writer);
		writer.writeFloat(alpha);
	}

	@Override
	public String toString() {
		return super.toString() + ", alpha=" + alpha;
	}

	public static bCFloatAlphaColor fromAwt(Color color) {
		return new bCFloatAlphaColor(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f,
				color.getAlpha() / 255.0f);
	}

	@Override
	public Color toAwt() {
		return new Color(getRed(), getGreen(), getBlue(), getAlpha());
	}
}

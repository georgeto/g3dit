package de.george.lrentnode.classes;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.lrentnode.structures.eCColorScale;
import de.george.lrentnode.structures.eCFloatScale;
import de.george.lrentnode.structures.eCParticleSoundArray;
import de.george.lrentnode.structures.eCVectorScale;

public class eCParticle_PS extends G3Class {
	private eCFloatScale subdivisionScale;
	private eCVectorScale revolutionScale;
	private eCVectorScale velocityScale;
	private eCColorScale colorScale;
	private eCVectorScale sizeScale;
	private eCParticleSoundArray sounds;

	public eCParticle_PS(String className, G3FileReader reader) {
		super(className, reader);
	}

	public eCParticle_PS(String className, int version) {
		super(className, version);
	}

	public eCFloatScale getSubdivisionScale() {
		return subdivisionScale;
	}

	public void setSubdivisionScale(eCFloatScale subdivisionScale) {
		this.subdivisionScale = subdivisionScale;
	}

	public eCVectorScale getRevolutionScale() {
		return revolutionScale;
	}

	public void setRevolutionScale(eCVectorScale revolutionScale) {
		this.revolutionScale = revolutionScale;
	}

	public eCVectorScale getVelocityScale() {
		return velocityScale;
	}

	public void setVelocityScale(eCVectorScale velocityScale) {
		this.velocityScale = velocityScale;
	}

	public eCColorScale getColorScale() {
		return colorScale;
	}

	public void setColorScale(eCColorScale colorScale) {
		this.colorScale = colorScale;
	}

	public eCVectorScale getSizeScale() {
		return sizeScale;
	}

	public void setSizeScale(eCVectorScale sizeScale) {
		this.sizeScale = sizeScale;
	}

	public eCParticleSoundArray getSounds() {
		return sounds;
	}

	public void setSounds(eCParticleSoundArray sounds) {
		this.sounds = sounds;
	}

	@Override
	protected void readPostClassVersion(G3FileReader reader) {
		if (classVersion == 2) {
			subdivisionScale = reader.read(eCFloatScale.class);
			revolutionScale = reader.read(eCVectorScale.class);
			velocityScale = reader.read(eCVectorScale.class);
			colorScale = reader.read(eCColorScale.class);
			sizeScale = reader.read(eCVectorScale.class);
			sounds = reader.read(eCParticleSoundArray.class);
		} else if (classVersion < 2) {
			subdivisionScale = new eCFloatScale();
			revolutionScale = new eCVectorScale();
			velocityScale = new eCVectorScale();
			colorScale = new eCColorScale();
			sizeScale = new eCVectorScale();
			sounds = new eCParticleSoundArray();
			classVersion = 2;
		} else {
			throw new UnsupportedOperationException("Version > 2 is not supported.");
		}
	}

	@Override
	protected void writePostClassVersion(G3FileWriter writer) {
		writer.write(subdivisionScale);
		writer.write(revolutionScale);
		writer.write(velocityScale);
		writer.write(colorScale);
		writer.write(sizeScale);
		writer.write(sounds);
	}
}

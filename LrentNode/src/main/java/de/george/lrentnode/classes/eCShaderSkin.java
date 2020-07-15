package de.george.lrentnode.classes;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.lrentnode.structures.eCColorSrcProxy;

public final class eCShaderSkin extends eCShaderBase {
	private eCColorSrcProxy colorSrcDiffuse;
	private eCColorSrcProxy colorSrcOpacity;
	private eCColorSrcProxy colorSrcSelfIllumination;
	private eCColorSrcProxy colorSrcSpecular;
	private eCColorSrcProxy colorSrcSpecularPower;
	private eCColorSrcProxy colorSrcNormal;
	private eCColorSrcProxy colorSrcSubSurface;

	public eCShaderSkin(String className, G3FileReader reader) {
		super(className, reader);
	}

	@Override
	protected void readPostClassVersion(G3FileReader reader) {
		// if (classVersion != 2)
		// reader.warn(logger, "eCShaderSkin unbekannte Version.");

		colorSrcDiffuse = reader.read(eCColorSrcProxy.class);
		colorSrcOpacity = reader.read(eCColorSrcProxy.class);
		colorSrcSelfIllumination = reader.read(eCColorSrcProxy.class);
		colorSrcSpecular = reader.read(eCColorSrcProxy.class);
		colorSrcSpecularPower = reader.read(eCColorSrcProxy.class);
		colorSrcNormal = reader.read(eCColorSrcProxy.class);
		colorSrcSubSurface = reader.read(eCColorSrcProxy.class);

		super.readPostClassVersion(reader);
	}

	@Override
	protected void writePostClassVersion(G3FileWriter writer) {
		writer.write(colorSrcDiffuse);
		writer.write(colorSrcOpacity);
		writer.write(colorSrcSelfIllumination);
		writer.write(colorSrcSpecular);
		writer.write(colorSrcSpecularPower);
		writer.write(colorSrcNormal);
		writer.write(colorSrcSubSurface);

		super.writePostClassVersion(writer);
	}

	public eCColorSrcProxy getColorSrcDiffuse() {
		return colorSrcDiffuse;
	}

	public void setColorSrcDiffuse(eCColorSrcProxy colorSrcDiffuse) {
		this.colorSrcDiffuse = colorSrcDiffuse;
	}

	public eCColorSrcProxy getColorSrcOpacity() {
		return colorSrcOpacity;
	}

	public void setColorSrcOpacity(eCColorSrcProxy colorSrcOpacity) {
		this.colorSrcOpacity = colorSrcOpacity;
	}

	public eCColorSrcProxy getColorSrcSelfIllumination() {
		return colorSrcSelfIllumination;
	}

	public void setColorSrcSelfIllumination(eCColorSrcProxy colorSrcSelfIllumination) {
		this.colorSrcSelfIllumination = colorSrcSelfIllumination;
	}

	public eCColorSrcProxy getColorSrcSpecular() {
		return colorSrcSpecular;
	}

	public void setColorSrcSpecular(eCColorSrcProxy colorSrcSpecular) {
		this.colorSrcSpecular = colorSrcSpecular;
	}

	public eCColorSrcProxy getColorSrcSpecularPower() {
		return colorSrcSpecularPower;
	}

	public void setColorSrcSpecularPower(eCColorSrcProxy colorSrcSpecularPower) {
		this.colorSrcSpecularPower = colorSrcSpecularPower;
	}

	public eCColorSrcProxy getColorSrcNormal() {
		return colorSrcNormal;
	}

	public void setColorSrcNormal(eCColorSrcProxy colorSrcNormal) {
		this.colorSrcNormal = colorSrcNormal;
	}

	public eCColorSrcProxy getColorSrcSubSurface() {
		return colorSrcSubSurface;
	}

	public void setColorSrcSubSurface(eCColorSrcProxy colorSrcSubSurface) {
		this.colorSrcSubSurface = colorSrcSubSurface;
	}
}

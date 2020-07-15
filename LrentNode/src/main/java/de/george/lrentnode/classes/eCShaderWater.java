package de.george.lrentnode.classes;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.lrentnode.structures.eCColorSrcProxy;

public final class eCShaderWater extends eCShaderBase {
	private eCColorSrcProxy colorSrcDiffuse;
	private eCColorSrcProxy colorSrcStaticBump;
	private eCColorSrcProxy colorSrcFlowingBump;
	private eCColorSrcProxy colorSrcSpecular;
	private eCColorSrcProxy colorSrcSpecularPower;
	private eCColorSrcProxy colorSrcReflection;
	private eCColorSrcProxy colorSrcDistortion;

	public eCShaderWater(String className, G3FileReader reader) {
		super(className, reader);
	}

	@Override
	protected void readPostClassVersion(G3FileReader reader) {
		colorSrcDiffuse = reader.read(eCColorSrcProxy.class);
		colorSrcStaticBump = reader.read(eCColorSrcProxy.class);
		colorSrcFlowingBump = reader.read(eCColorSrcProxy.class);
		colorSrcSpecular = reader.read(eCColorSrcProxy.class);
		colorSrcSpecularPower = reader.read(eCColorSrcProxy.class);
		colorSrcReflection = reader.read(eCColorSrcProxy.class);

		if (classVersion > 1) {
			colorSrcDistortion = reader.read(eCColorSrcProxy.class);
		}

		super.readPostClassVersion(reader);
	}

	@Override
	protected void writePostClassVersion(G3FileWriter writer) {
		// writer.appendShort(2);

		writer.write(colorSrcDiffuse);
		writer.write(colorSrcStaticBump);
		writer.write(colorSrcFlowingBump);
		writer.write(colorSrcSpecular);
		writer.write(colorSrcSpecularPower);
		writer.write(colorSrcReflection);

		if (classVersion > 1) {
			writer.write(colorSrcDistortion);
		}

		super.writePostClassVersion(writer);
	}

	public eCColorSrcProxy getColorSrcDiffuse() {
		return colorSrcDiffuse;
	}

	public void setColorSrcDiffuse(eCColorSrcProxy colorSrcDiffuse) {
		this.colorSrcDiffuse = colorSrcDiffuse;
	}

	public eCColorSrcProxy getColorSrcStaticBump() {
		return colorSrcStaticBump;
	}

	public void setColorSrcStaticBump(eCColorSrcProxy colorSrcStaticBump) {
		this.colorSrcStaticBump = colorSrcStaticBump;
	}

	public eCColorSrcProxy getColorSrcFlowingBump() {
		return colorSrcFlowingBump;
	}

	public void setColorSrcFlowingBump(eCColorSrcProxy colorSrcFlowingBump) {
		this.colorSrcFlowingBump = colorSrcFlowingBump;
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

	public eCColorSrcProxy getColorSrcReflection() {
		return colorSrcReflection;
	}

	public void setColorSrcReflection(eCColorSrcProxy colorSrcReflection) {
		this.colorSrcReflection = colorSrcReflection;
	}

	public eCColorSrcProxy getColorSrcDistortion() {
		return colorSrcDistortion;
	}

	public void setColorSrcDistortion(eCColorSrcProxy colorSrcDistortion) {
		this.colorSrcDistortion = colorSrcDistortion;
	}
}

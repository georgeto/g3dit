package de.george.lrentnode.classes;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.lrentnode.structures.eCColorSrcProxy;

public final class eCShaderLeaf extends eCShaderBase {
	private eCColorSrcProxy colorSrcDiffuse;
	private eCColorSrcProxy colorSrcSpecular;
	private eCColorSrcProxy colorSrcSpecularPower;
	private eCColorSrcProxy colorSrcNormal;

	public eCShaderLeaf(String className, G3FileReader reader) {
		super(className, reader);
	}

	@Override
	protected void readPostClassVersion(G3FileReader reader) {
		colorSrcDiffuse = reader.read(eCColorSrcProxy.class);
		colorSrcSpecular = reader.read(eCColorSrcProxy.class);
		colorSrcSpecularPower = reader.read(eCColorSrcProxy.class);
		colorSrcNormal = reader.read(eCColorSrcProxy.class);

		super.readPostClassVersion(reader);
	}

	@Override
	protected void writePostClassVersion(G3FileWriter writer) {
		// writer.appendShort(2);

		writer.write(colorSrcDiffuse);
		writer.write(colorSrcSpecular);
		writer.write(colorSrcSpecularPower);
		writer.write(colorSrcNormal);

		super.writePostClassVersion(writer);
	}

	public eCColorSrcProxy getColorSrcDiffuse() {
		return colorSrcDiffuse;
	}

	public void setColorSrcDiffuse(eCColorSrcProxy colorSrcDiffuse) {
		this.colorSrcDiffuse = colorSrcDiffuse;
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
}

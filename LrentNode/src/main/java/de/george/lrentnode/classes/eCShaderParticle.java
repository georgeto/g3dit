package de.george.lrentnode.classes;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.lrentnode.structures.eCColorSrcProxy;

public final class eCShaderParticle extends eCShaderBase {
	private eCColorSrcProxy colorSrcDiffuse;
	private eCColorSrcProxy colorSrcDistortion;

	public eCShaderParticle(String className, G3FileReader reader) {
		super(className, reader);
	}

	@Override
	protected void readPostClassVersion(G3FileReader reader) {
		colorSrcDiffuse = reader.read(eCColorSrcProxy.class);

		if (classVersion > 1) {
			colorSrcDistortion = reader.read(eCColorSrcProxy.class);
		}

		super.readPostClassVersion(reader);
	}

	@Override
	protected void writePostClassVersion(G3FileWriter writer) {
		// writer.appendShort(2);

		writer.write(colorSrcDiffuse);

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

	public eCColorSrcProxy getColorSrcDistortion() {
		return colorSrcDistortion;
	}

	public void setColorSrcDistortion(eCColorSrcProxy colorSrcDistortion) {
		this.colorSrcDistortion = colorSrcDistortion;
	}
}

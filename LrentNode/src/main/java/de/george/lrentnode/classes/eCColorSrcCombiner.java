package de.george.lrentnode.classes;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.lrentnode.structures.eCColorSrcProxy;

public final class eCColorSrcCombiner extends eCColorSrcBase {
	private eCColorSrcProxy colorSrc1;
	private eCColorSrcProxy colorSrc2;

	public eCColorSrcCombiner(String className, G3FileReader reader) {
		super(className, reader);
	}

	public eCColorSrcProxy getColorSrc1() {
		return colorSrc1;
	}

	public void setColorSrc1(eCColorSrcProxy colorSrc1) {
		this.colorSrc1 = colorSrc1;
	}

	public eCColorSrcProxy getColorSrc2() {
		return colorSrc2;
	}

	public void setColorSrc2(eCColorSrcProxy colorSrc2) {
		this.colorSrc2 = colorSrc2;
	}

	@Override
	protected void readPostClassVersion(G3FileReader reader) {
		colorSrc1 = reader.read(eCColorSrcProxy.class);
		colorSrc2 = reader.read(eCColorSrcProxy.class);

		super.readPostClassVersion(reader);
	}

	@Override
	protected void writePostClassVersion(G3FileWriter writer) {
		writer.write(colorSrc1).write(colorSrc2);
		super.writePostClassVersion(writer);
	}
}

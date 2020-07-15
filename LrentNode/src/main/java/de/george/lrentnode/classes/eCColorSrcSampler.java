package de.george.lrentnode.classes;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.lrentnode.structures.eCTexCoordSrcProxy;

public class eCColorSrcSampler extends eCColorSrcBase {
	private eCTexCoordSrcProxy texCoordSrc;
	private int samplerType;// eEColorSrcSamplerType

	public eCColorSrcSampler(String className, G3FileReader reader) {
		super(className, reader);
	}

	public eCTexCoordSrcProxy getTexCoordSrc() {
		return texCoordSrc;
	}

	public void setTexCoordSrc(eCTexCoordSrcProxy texCoordSrc) {
		this.texCoordSrc = texCoordSrc;
	}

	@Override
	protected void readPostClassVersion(G3FileReader reader) {
		texCoordSrc = reader.read(eCTexCoordSrcProxy.class);
		samplerType = reader.readInt();

		super.readPostClassVersion(reader);
	}

	@Override
	protected void writePostClassVersion(G3FileWriter writer) {
		writer.write(texCoordSrc).writeInt(samplerType);
		super.writePostClassVersion(writer);
	}
}

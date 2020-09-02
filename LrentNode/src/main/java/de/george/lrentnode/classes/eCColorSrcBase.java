package de.george.lrentnode.classes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;

public abstract class eCColorSrcBase extends eCShaderEllementBase {
	private static final Logger logger = LoggerFactory.getLogger(eCColorSrcBase.class);

	public eCColorSrcBase(String className, G3FileReader reader) {
		super(className, reader);
	}

	@Override
	protected void readPostClassVersion(G3FileReader reader) {
		if (reader.readShort() != 1) {
			reader.warn(logger, "Unsupported eCColorSrcBase version.");
		}

		super.readPostClassVersion(reader);
	}

	@Override
	protected void writePostClassVersion(G3FileWriter writer) {
		writer.writeUnsignedShort(1);

		super.writePostClassVersion(writer);
	}
}

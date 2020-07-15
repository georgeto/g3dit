package de.george.lrentnode.structures;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.io.G3Serializable;

public class eCParticleSoundArray implements G3Serializable {
	private static final Logger logger = LoggerFactory.getLogger(eCParticleSoundArray.class);

	@Override
	public void read(G3FileReader reader) {
		int version = reader.readInt();
		int size = reader.readInt();
		if (version < 1) {
			reader.warn(logger, "Detected unknown version. Expected: {}. Found: {}. Skipping content.", 1, version);
			reader.skip(size);
		} else if (size > 0) {
			throw new UnsupportedOperationException("Nicht leeres eCParticleSoundArray wird nicht unterstützt.");
		}
	}

	@Override
	public void write(G3FileWriter writer) {
		writer.writeInt(2).writeInt(0);
	}
}

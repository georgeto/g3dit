package de.george.lrentnode.classes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;

public class DefaultClass extends G3Class {
	private static final Logger logger = LoggerFactory.getLogger(DefaultClass.class);

	private byte[] raw;

	public DefaultClass(String className, G3FileReader reader) {
		super(className, reader);
	}

	public DefaultClass(String className, int version) {
		super(className, version);
	}

	@Override
	protected void readPostClassVersion(G3FileReader reader) {
		if (reader.getPos() < deadcodePosition) {
			// Klassen ohne relevante Erweiterungen
			// if(!className.equals("gCProjectile_PS") && !className.equals("eCParticle_PS"))
			reader.debug(logger, "{} is handled by DefaultClass but contains Subclass(es) - Size: {}", className,
					deadcodePosition - 4 - reader.getPos());
			// Leider gibt es im CSP viele fehlerhafte InventorySlot Klassen, dass ist ein
			// Workaround
			if (!className.equals("gCInventorySlot")) {
				raw = reader.readByteArray(deadcodePosition - reader.getPos());
			}
		}
	}

	@Override
	protected void writePostClassVersion(G3FileWriter writer) {
		if (raw != null) {
			writer.write(raw);
		}
	}

	public byte[] getRaw() {
		return raw;
	}

	// @foff
	/* gCProjectile_PS
	 *  bool isFlying
		G3Vector 88 ???
		G3Vector 7C ???
		float 94 ???
		float 98 ???
		bool hasCollided
	 */
	// @fon
}

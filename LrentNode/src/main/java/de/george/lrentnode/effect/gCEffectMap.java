package de.george.lrentnode.effect;

import java.io.IOException;
import java.util.List;

import de.george.g3utils.io.G3FileReaderEx;
import de.george.g3utils.io.G3FileWriterEx;
import de.george.g3utils.io.GenomeFile;

public class gCEffectMap extends GenomeFile {
	public List<gCEffectCommandSequence> effects;

	public gCEffectMap(G3FileReaderEx reader) throws IOException {
		read(reader);
	}

	@Override
	protected void readInternal(G3FileReaderEx reader) throws IOException {
		if (reader.readUnsignedShort() != 2) {
			throw new IOException("Unsupported EffectMap version.");
		}
		effects = reader.readList(gCEffectCommandSequence.class);
	}

	@Override
	protected void writeInternal(G3FileWriterEx writer) throws IOException {
		writer.writeUnsignedShort(2);
		writer.writeList(effects);
	}

	public List<gCEffectCommandSequence> getEffects() {
		return effects;
	}

	public void setEffects(List<gCEffectCommandSequence> effects) {
		this.effects = effects;
	}
}

package de.george.lrentnode.effect;

import java.util.ArrayList;
import java.util.List;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.io.G3Serializable;

public class gCEffectCommandPlaySound extends gCEffectCommand {
	public static class Sample implements G3Serializable {
		public String name;
		public float probability;

		public Sample(String name, float probability) {
			this.name = name;
			this.probability = probability;
		}

		@Override
		public void read(G3FileReader reader) {
			name = reader.readEntry();
			probability = reader.readFloat();
		}

		@Override
		public void write(G3FileWriter writer) {
			writer.writeEntry(name).writeFloat(probability);
		}
	}

	public List<Sample> samples;

	public gCEffectCommandPlaySound(String className, G3FileReader reader) {
		super(className, reader);
	}

	public gCEffectCommandPlaySound(String className, int classVersion) {
		super(className, classVersion);
		samples = new ArrayList<>();
	}

	@Override
	protected void readPostClassVersion(G3FileReader reader) {
		super.readPostClassVersion(reader);
		samples = reader.readList(Sample.class);
	}

	@Override
	protected void writePostClassVersion(G3FileWriter writer) {
		super.writePostClassVersion(writer);
		writer.writeList(samples);
	}
}

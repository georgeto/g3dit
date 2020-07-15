package de.george.lrentnode.structures;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.io.G3Serializable;

public class eCColorScale implements G3Serializable {
	private static final Logger logger = LoggerFactory.getLogger(eCColorScale.class);

	private List<eSColorScaleItem> entries;

	@JsonProperty
	public List<eSColorScaleItem> getEntries() {
		return entries;
	}

	public void setEntries(List<eSColorScaleItem> entries) {
		this.entries = entries;
	}

	@Override
	public void read(G3FileReader reader) {
		entries = new ArrayList<>();
		int version = reader.readInt();
		int size = reader.readInt();
		if (version != 1) {
			reader.warn(logger, "Detected unknown version. Expected: {}. Found: {}. Skipping content.", 1, version);
			reader.skip(size);
		} else if (size > 0) {
			reader.readList(eSColorScaleItem.class, entries::add);
		}
	}

	@Override
	public void write(G3FileWriter writer) {
		writer.writeInt(1).writeInt(entries.size() * 20);
		if (!entries.isEmpty()) {
			writer.writeList(entries);
		}
	}

	public static class eSColorScaleItem extends eSScaleItem<bCFloatAlphaColor> {
		public eSColorScaleItem() {
			super(new bCFloatAlphaColor());
		}

		public eSColorScaleItem(float scale, bCFloatAlphaColor value) {
			super(scale, value);
		}

		@Override
		protected bCFloatAlphaColor readValue(G3FileReader reader) {
			return new bCFloatAlphaColor(reader.readFloat(), reader.readFloat(), reader.readFloat(), reader.readFloat());
		}

		@Override
		protected void writeValue(G3FileWriter writer, bCFloatAlphaColor value) {
			writer.writeFloat(value.getRed()).writeFloat(value.getGreen()).writeFloat(value.getBlue()).writeFloat(value.getAlpha());
		}
	}
}

package de.george.lrentnode.structures;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.io.G3Serializable;

public class eCFloatScale implements G3Serializable {
	private static final Logger logger = LoggerFactory.getLogger(eCFloatScale.class);

	private List<eSFloatScaleItem> entries;

	@JsonProperty
	public List<eSFloatScaleItem> getEntries() {
		return entries;
	}

	public void setEntries(List<eSFloatScaleItem> entries) {
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
			reader.readList(eSFloatScaleItem.class, entries::add);
		}

	}

	@Override
	public void write(G3FileWriter writer) {
		writer.writeInt(1).writeInt(entries.size() * 8);
		if (!entries.isEmpty()) {
			writer.writeList(entries);
		}
	}

	public static class eSFloatScaleItem extends eSScaleItem<Float> {
		public eSFloatScaleItem() {
			super(0.0f);
		}

		public eSFloatScaleItem(float scale, Float value) {
			super(scale, value);
		}

		@Override
		protected Float readValue(G3FileReader reader) {
			return reader.readFloat();
		}

		@Override
		protected void writeValue(G3FileWriter writer, Float value) {
			writer.writeFloat(value);
		}
	}
}

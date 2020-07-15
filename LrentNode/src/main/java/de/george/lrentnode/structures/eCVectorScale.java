package de.george.lrentnode.structures;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.io.G3Serializable;
import de.george.g3utils.structure.bCVector;

public class eCVectorScale implements G3Serializable {
	private static final Logger logger = LoggerFactory.getLogger(eCVectorScale.class);

	private List<eSVectorScaleItem> entries;

	@JsonProperty
	public List<eSVectorScaleItem> getEntries() {
		return entries;
	}

	public void setEntries(List<eSVectorScaleItem> entries) {
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
			reader.readList(eSVectorScaleItem.class, entries::add);
		}
	}

	@Override
	public void write(G3FileWriter writer) {
		writer.writeInt(1).writeInt(entries.size() * 16);
		if (!entries.isEmpty()) {
			writer.writeList(entries);
		}
	}

	public static class eSVectorScaleItem extends eSScaleItem<bCVector> {
		public eSVectorScaleItem() {
			super(bCVector.nullVector());
		}

		public eSVectorScaleItem(float scale, bCVector value) {
			super(scale, value);
		}

		@Override
		protected bCVector readValue(G3FileReader reader) {
			return reader.read(bCVector.class);
		}

		@Override
		protected void writeValue(G3FileWriter writer, bCVector value) {
			writer.write(value);
		}
	}
}

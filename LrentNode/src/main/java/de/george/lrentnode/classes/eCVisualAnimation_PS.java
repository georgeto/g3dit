package de.george.lrentnode.classes;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.structure.bCVector;
import de.george.lrentnode.enums.G3Enums.gESlot;

public class eCVisualAnimation_PS extends eCEntityPropertySet {
	private static final Logger logger = LoggerFactory.getLogger(eCVisualAnimation_PS.class);

	public MaterialSwitchSlot fxaSlot;
	public List<MaterialSwitchSlot> bodyParts;
	public List<String> stEntries;
	public List<ExtraSlot> attachments;
	public bCVector minVec, maxVec;

	public eCVisualAnimation_PS(String className, G3FileReader reader) {
		super(className, reader);
	}

	@Override
	protected void readPostClassVersion(G3FileReader reader) {
		// TODO: Improve
		reader.skip(2); // Always 0500
		fxaSlot = new MaterialSwitchSlot(reader, false);
		int slotCount = reader.readInt();
		bodyParts = new ArrayList<>(slotCount);
		for (int i = 0; i < slotCount; i++) {
			bodyParts.add(new MaterialSwitchSlot(reader));
			if (reader.readUnsignedByte() != 1) {
				reader.warn(logger, "(1) eCVisualAnimation_PS unexpected filed structure.");
			}
		}
		if (reader.readUnsignedByte() != 1) {
			reader.warn(logger, "(2) eCVisualAnimation_PS unexpected file structure.");
		}
		int stCount = reader.readInt();
		stEntries = new ArrayList<>(stCount);
		for (int i = 0; i < stCount; i++) {
			stEntries.add(reader.readEntry());
		}
		int extraCount = reader.readInt();
		attachments = new ArrayList<>(extraCount);
		for (int i = 0; i < extraCount; i++) {
			attachments.add(new ExtraSlot(reader));
		}
		minVec = reader.readVector();
		maxVec = reader.readVector();
		super.readPostClassVersion(reader);
	}

	@Override

	protected void writePostClassVersion(G3FileWriter writer) {
		writer.write("0500");
		fxaSlot.write(writer);
		writer.writeInt(bodyParts.size());
		for (MaterialSwitchSlot slot : bodyParts) {
			slot.write(writer);
			writer.writeBool(true);
		}

		writer.writeBool(true).writeInt(stEntries.size());
		for (String stEntry : stEntries) {
			writer.writeEntry(stEntry);
		}
		writer.writeInt(attachments.size());
		for (ExtraSlot slot : attachments) {
			slot.write(writer);
		}
		writer.writeVector(minVec).writeVector(maxVec);
		super.writePostClassVersion(writer);
	}

	public void removeExtraSlot(int slotType) {
		String slotName = gESlot.getSlotName(slotType);
		for (ExtraSlot slot : attachments) {
			if (slotName.equals(slot.name)) {
				attachments.remove(slot);
				break;
			}
		}
	}

	public void removeMaterialSwitchSlot(int slotType) {
		String slotName = gESlot.getSlotName(slotType);
		for (MaterialSwitchSlot slot : bodyParts) {
			if (slotName.equals(slot.name)) {
				bodyParts.remove(slot);
				break;
			}
		}
	}

	public static class MaterialSwitchSlot {
		public String name, fxaFile, fxaFile2;
		public int fxaSwitch, fxaSwitch2;

		public MaterialSwitchSlot(G3FileReader reader) {
			this(reader, true);
		}

		public MaterialSwitchSlot(G3FileReader reader, boolean hasName) {
			if (hasName) {
				name = reader.readEntry();
			}
			reader.skip(2);
			fxaFile = reader.readEntry();
			fxaSwitch = reader.readInt();
			if (reader.readBool()) {
				reader.skip(2);
				fxaFile2 = reader.readEntry();
				fxaSwitch2 = reader.readInt();
			}

		}

		public MaterialSwitchSlot(int type, String fxaFile, String fxaFile2, int fxaSwitch, int fxaSwitch2) {
			name = gESlot.getSlotName(type);
			this.fxaFile = fxaFile;
			this.fxaFile2 = fxaFile2;
			this.fxaSwitch = fxaSwitch;
			this.fxaSwitch2 = fxaSwitch2;
		}

		public void write(G3FileWriter writer) {
			if (name != null) {
				writer.writeEntry(name);
			}
			writer.writeUnsignedShort(4);
			writer.writeEntry(fxaFile).writeInt(fxaSwitch);
			writer.writeBool(fxaFile2 != null);
			if (fxaFile2 != null) {
				writer.writeUnsignedShort(4);
				writer.writeEntry(fxaFile2);
				writer.writeInt(fxaSwitch2);
			}
		}

		public int getSize() {
			return (name != null ? 2 : 0) + 8 + (fxaFile2 != null ? 9 : 1);
		}
	}

	public static class ExtraSlot {
		public String guid, name, hexOffset;

		public ExtraSlot(G3FileReader reader) {
			reader.skip(2); // Always 0100
			if (reader.readBool()) {
				guid = reader.readGUID();
			}
			name = reader.readEntry();
			hexOffset = reader.read(72);
		}

		public ExtraSlot(String guid, int type) {
			this.guid = guid;
			name = gESlot.getSlotName(type);
			hexOffset = "010000000000803F000000000000000000000000000000000000803F000000000000000000000000000000000000803F000000000000000000000000000000000000803F6378FBFE";
		}

		public void write(G3FileWriter writer) {
			writer.writeUnsignedShort(1);
			writer.writeBool(guid != null);
			if (guid != null) {
				writer.write(guid);
			}
			writer.writeEntry(name);
			writer.write(hexOffset);
		}

		public int getSize() {
			return 76 + (guid != null ? 21 : 1);
		}
	}
}

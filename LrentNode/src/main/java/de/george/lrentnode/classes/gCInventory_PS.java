package de.george.lrentnode.classes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.util.ClassUtil;

public class gCInventory_PS extends G3Class {
	private static final Logger logger = LoggerFactory.getLogger(gCInventory_PS.class);

	public List<G3Class> stacks;
	public List<G3Class> slots;

	private HashMap<G3Class, Integer> errSlots;

	public gCInventory_PS(String className, G3FileReader reader) {
		super(className, reader);
	}

	@Override
	protected void readPostClassVersion(G3FileReader reader) {
		// TODO: Improve
		slots = new ArrayList<>();
		errSlots = new HashMap<>();

		// Inventory Stacks
		reader.skip(2);
		stacks = reader.readList(ClassUtil::readSubClass);

		// Inventory Slots
		reader.skip(2);
		int slotCount = reader.readInt();
		for (int i = 0; i < slotCount; i++) {
			if (reader.readSilent(2, 1).equals("01")) {
				G3Class slot = ClassUtil.readSubClass(reader);
				int slotInClass = slot.property(CD.gCInventorySlot.Slot).getEnumValue();
				if (i != slotInClass) {
					errSlots.put(slot, i);
					reader.info(logger, "Possible Slot Error (OuterSlot: {} - InnerSlot: {})", i, slotInClass);
				}

				slots.add(slot);
			} else {
				reader.skip(3);
			}
		}
		reader.skip(15);
	}

	@Override
	protected void writePostClassVersion(G3FileWriter writer) {
		// StringBuffer buffer = new StringBuffer();
		// Stacks
		writer.write("0100");
		writer.writeList(stacks, ClassUtil::writeSubClass);

		// Slots
		writer.write("010013000000");
		for (int i = 0; i < 19; i++) {
			boolean found = false;
			for (G3Class slot : slots) {
				if (errSlots.containsKey(slot)) {
					if (errSlots.get(slot) == i) {
						found = true;
						ClassUtil.writeSubClass(writer, slot);
						break;
					}
				} else if (slot.property(CD.gCInventorySlot.Slot).getEnumValue() == i) {
					found = true;
					ClassUtil.writeSubClass(writer, slot);
					break;
				}
			}
			if (!found) {
				writer.write("010000");
			}
		}
		writer.write("010000010000010000010000010000");
	}

	/**
	 * Gibt den Slot des Typs <code>slotType</code> zurück
	 *
	 * @param slotType Typ des Slots
	 * @return Slot vom Typ <code>slotType</code> falls vorhanden, sonst null
	 */
	public G3Class getValidSlot(int slotType) {
		for (G3Class slot : slots) {
			if (slot.property(CD.gCInventorySlot.Slot).getEnumValue() == slotType) {
				return slot;
			}
		}
		return null;
	}

	/**
	 * Gibt den Slot des Typs <code>slotType</code> zurück
	 *
	 * @param slotType Typ des Slots
	 * @return Slot vom Typ <code>slotType</code> falls vorhanden, sonst null
	 */
	public G3Class getErrorSlot(int slotType) {
		for (G3Class slot : slots) {
			Integer errSlot = errSlots.get(slot);
			if (errSlot != null && errSlot == slotType) {
				return slot;
			}
		}

		return null;
	}

	public void removeSlot(int slotType) {
		G3Class slot = getValidSlot(slotType);
		if (slot == null) {
			slot = getErrorSlot(slotType);
		}

		slots.remove(slot);
		errSlots.remove(slot);
	}

	public List<G3Class> getStacks() {
		return stacks;
	}

	public List<G3Class> getSlots() {
		return slots;
	}
}

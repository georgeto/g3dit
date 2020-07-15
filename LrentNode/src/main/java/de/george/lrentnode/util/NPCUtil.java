package de.george.lrentnode.util;

import java.util.Optional;

import de.george.g3utils.structure.GuidUtil;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.classes.G3Class;
import de.george.lrentnode.classes.eCVisualAnimation_PS;
import de.george.lrentnode.classes.eCVisualAnimation_PS.ExtraSlot;
import de.george.lrentnode.classes.eCVisualAnimation_PS.MaterialSwitchSlot;
import de.george.lrentnode.classes.eCVisualMeshDynamic_PS;
import de.george.lrentnode.classes.gCInventory_PS;
import de.george.lrentnode.classes.gCItem_PS;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.classes.desc.CD.eCVisualMeshBase_PS;
import de.george.lrentnode.classes.desc.PropertyDescriptor;
import de.george.lrentnode.enums.G3Enums.gESlot;
import de.george.lrentnode.properties.bCString;
import de.george.lrentnode.properties.bTObjArray_bCString;
import de.george.lrentnode.properties.bTObjArray_eCEntityProxy;
import de.george.lrentnode.properties.eCEntityProxy;
import de.george.lrentnode.template.TemplateEntity;
import de.george.lrentnode.template.TemplateFile;

public class NPCUtil {

	/**
	 * Fügt {@link item} als Slot von {@link npc} ein, d.h. es wird ein InventorySlot erstellt und
	 * eventuell ein ExtraSlot am Ende von eCVisualAnimation_PS für Bart, Haar oder Helm
	 *
	 * @param npc
	 * @param item
	 */
	public static void addSlot(eCEntity npc, eCEntity item) {
		gCInventory_PS npcInventory = npc.getClass(CD.gCInventory_PS.class);
		int useType = EntityUtil.getUseType(item);
		int slotType = gESlot.fromUseType(useType);

		// NPC als Owner von Entity eintragen
		item.getClass(CD.gCInteraction_PS.class).property(CD.gCInteraction_PS.Owner).setGuid(npc.getGuid());

		// InventorySlot erstellen
		G3Class clazz = ClassUtil.createInventorySlot(item.getCreator(), item.getGuid(), slotType);
		npcInventory.slots.add(clazz);

		// Item Eigenschaften anpassen
		gCItem_PS itemPS = item.getClass(CD.gCItem_PS.class);
		itemPS.setVisible(slotType == gESlot.gESlot_Beard || slotType == gESlot.gESlot_Hair || slotType == gESlot.gESlot_Helmet);
		itemPS.setSlot(slotType);

		eCVisualAnimation_PS vis = npc.getClass(CD.eCVisualAnimation_PS.class);
		// ExtraSlot am Ende von eCVisualAnimation_PS für Bart, Haar oder Helm
		if (slotType == gESlot.gESlot_Beard || slotType == gESlot.gESlot_Hair || slotType == gESlot.gESlot_Helmet) {
			vis.attachments.add(new ExtraSlot(item.getGuid(), slotType));
		} else {
			vis.bodyParts.add(new MaterialSwitchSlot(slotType, "", slotType == gESlot.gESlot_Head ? "" : null, 0, 0));
		}
	}

	/**
	 * Fügt {@link item} als Slot von {@link npc} ein, d.h. es wird ein InventorySlot erstellt und
	 * eventuell ein ExtraSlot am Ende von eCVisualAnimation_PS für Bart, Haar oder Helm
	 *
	 * @param npc
	 * @param slotType
	 */
	public static void removeSlot(eCEntity npc, eCEntity item, int slotType) {
		gCInventory_PS npcInventory = npc.getClass(CD.gCInventory_PS.class);
		npcInventory.removeSlot(slotType);

		// Item Eigenschaften anpassen
		if (item != null) {
			gCItem_PS itemPS = item.getClass(CD.gCItem_PS.class);
			itemPS.setVisible(false);
			itemPS.setSlot(gESlot.gESlot_None);
		}

		eCVisualAnimation_PS vis = npc.getClass(CD.eCVisualAnimation_PS.class);
		// ExtraSlot am Ende von eCVisualAnimation_PS für Bart, Haar oder Helm
		if (slotType == gESlot.gESlot_Beard || slotType == gESlot.gESlot_Hair || slotType == gESlot.gESlot_Helmet) {
			vis.removeExtraSlot(slotType);
		} else {
			vis.removeMaterialSwitchSlot(slotType);
		}
	}

	/**
	 * Setzt die Eigenschaften eines animierten Körperteils
	 *
	 * @param item Item-Entity
	 * @param tple Template aus der die Eigenschaften extrahiert werden
	 */
	public static void setAnimatedProperties(eCEntity item, TemplateFile tple) {
		String newName = tple.getItemHeader().getName();
		TemplateEntity refHeader = tple.getReferenceHeader();
		eCVisualAnimation_PS visAnim = refHeader.getClass(CD.eCVisualAnimation_PS.class);
		setAnimatedProperties(item, newName, visAnim.fxaSlot.fxaFile, visAnim.fxaSlot.fxaFile2, visAnim.fxaSlot.fxaSwitch,
				refHeader.getGuid(), refHeader.getDataChangedTimeStamp());
	}

	/**
	 * Setzt die Eigenschaften eines animierten Körperteils
	 *
	 * @param item Item-Entity
	 * @param newName Neuer Name
	 * @param fxaFile Mesh im Format *.fxa
	 * @param fxaFile2 Animated Mesh im Format *.fxa, wenn nicht vorhanden <code>null</code>
	 * @param fxaSwitch Material Switch
	 * @param creator Reference Guid der Template, wenn nicht vorhanden <code>null</code>
	 * @param dataChangedTimeStamp ChangeTime der Template, wenn nicht vorhanden <code>0</code>
	 */
	public static void setAnimatedProperties(eCEntity item, String newName, String fxaFile, String fxaFile2, int fxaSwitch, String refGuid,
			long changeTime) {
		item.setName(newName);
		item.setCreator(refGuid);
		item.setDataChangedTimeStamp(changeTime);

		eCVisualAnimation_PS visualAnim = item.getClass(CD.eCVisualAnimation_PS.class);
		visualAnim.property(CD.eCVisualAnimation_PS.ResourceFilePath).setString(fxaFile.replace(".fxa", ".FXA"));
		visualAnim.property(CD.eCVisualAnimation_PS.FacialAnimFilePath)
				.setString(fxaFile2 == null ? "" : fxaFile2.replace(".fxa", ".FXA"));
		visualAnim.property(CD.eCVisualAnimation_PS.MaterialSwitch).setInt(fxaSwitch);

		visualAnim.fxaSlot.fxaFile = fxaFile;
		visualAnim.fxaSlot.fxaFile2 = fxaFile2;
		visualAnim.fxaSlot.fxaSwitch = fxaSwitch;
		visualAnim.fxaSlot.fxaSwitch2 = fxaSwitch;
	}

	/**
	 * Überträgt die Eigenschaften von animierten Körperteil <code>item</code> nach <code>npc</code>
	 *
	 * @param npc NPC-Entity
	 * @param item Item-Entity
	 */
	public static void syncAnimatedProperties(eCEntity npc, eCEntity item) {
		int slotType = gESlot.fromUseType(EntityUtil.getUseType(item));
		String slotName = gESlot.getSlotName(slotType);

		// Inventory Slot
		gCInventory_PS inv = npc.getClass(CD.gCInventory_PS.class);
		G3Class slot = inv.getValidSlot(slotType);
		slot.property(CD.gCInventorySlot.Template).setGuid(item.getCreator());

		// VisualAnimation
		eCVisualAnimation_PS visNPC = npc.getClass(CD.eCVisualAnimation_PS.class);
		eCVisualAnimation_PS visItem = item.getClass(CD.eCVisualAnimation_PS.class);
		for (MaterialSwitchSlot fxaSlot : visNPC.bodyParts) {
			if (fxaSlot.name.equals(slotName)) {
				fxaSlot.fxaFile = visItem.fxaSlot.fxaFile;
				fxaSlot.fxaFile2 = visItem.fxaSlot.fxaFile2;
				fxaSlot.fxaSwitch = visItem.fxaSlot.fxaSwitch;
				fxaSlot.fxaSwitch2 = visItem.fxaSlot.fxaSwitch2;
				break;
			}
		}
	}

	/**
	 * Setzt die Eigenschaften eines statischen Körperteils
	 *
	 * @param item Item-Entity
	 * @param tple Template aus der die Eigenschaften extrahiert werden
	 */
	public static void setWearableProperties(eCEntity item, TemplateFile tple) {
		String newName = tple.getItemHeader().getName();
		TemplateEntity refHeader = tple.getReferenceHeader();
		eCVisualMeshDynamic_PS visualMesh = refHeader.getClass(CD.eCVisualMeshDynamic_PS.class);
		String meshPath = visualMesh.property(CD.eCVisualMeshDynamic_PS.ResourceFilePath).getString();
		String meshName = visualMesh.property(eCVisualMeshBase_PS.ResourceFileName).getString();
		int mSwitch = visualMesh.property(eCVisualMeshBase_PS.MaterialSwitch).getInt();
		setWearableProperties(item, newName, meshPath, meshName, mSwitch, refHeader.getGuid(), refHeader.getDataChangedTimeStamp());
	}

	/**
	 * Setzt die Eigenschaften eines statischen Körperteils
	 *
	 * @param item Item-Entity
	 * @param newName Neuer Name
	 * @param meshPath Mesh im Format *.xcmsh
	 * @param meshName = <code>meshPath</code>
	 * @param mSwitch Material Switch
	 * @param creator Reference Guid der Template, wenn nicht vorhanden <code>null</code>
	 * @param dataChangedTimeStamp ChangeTime der Template, wenn nicht vorhanden <code>0</code>
	 */
	public static void setWearableProperties(eCEntity item, String newName, String meshPath, String meshName, int mSwitch, String refGuid,
			long changeTime) {
		item.setName(newName);
		item.setCreator(refGuid);
		item.setDataChangedTimeStamp(changeTime);

		eCVisualMeshDynamic_PS visualMesh = item.getClass(CD.eCVisualMeshDynamic_PS.class);
		visualMesh.property(CD.eCVisualMeshDynamic_PS.ResourceFilePath).setString(meshPath);
		visualMesh.property(eCVisualMeshBase_PS.ResourceFileName).setString(meshName);
		visualMesh.property(eCVisualMeshBase_PS.MaterialSwitch).setInt(mSwitch);
	}

	/**
	 * Überträgt die Eigenschaften eines statischen Körperteil <code>item</code> nach
	 * <code>npc</code>
	 *
	 * @param npc NPC-Entity
	 * @param item Item-Entity
	 */
	public static void syncWearableProperties(eCEntity npc, eCEntity item) {
		int slotType = gESlot.fromUseType(EntityUtil.getUseType(item));
		String slotName = gESlot.getSlotName(slotType);

		// Inventory Slot
		gCInventory_PS inv = npc.getClass(CD.gCInventory_PS.class);
		G3Class slot = inv.getValidSlot(slotType);
		slot.property(CD.gCInventorySlot.Template).setGuid(item.getCreator());

		// VisualAnimation
		eCVisualAnimation_PS visNPC = npc.getClass(CD.eCVisualAnimation_PS.class);
		for (ExtraSlot extraSlot : visNPC.attachments) {
			if (extraSlot.name.equals(slotName)) {
				extraSlot.guid = item.getGuid();
				break;
			}
		}
	}

	public static eCEntity cloneNPC(eCEntity sNpc) {
		eCEntity npc = sNpc.clone();
		npc.setGuid(GuidUtil.randomGUID());

		gCInventory_PS inv = npc.getClass(CD.gCInventory_PS.class);
		for (G3Class slot : inv.slots) {
			eCEntityProxy itemGuid = slot.property(CD.gCInventorySlot.Item);
			Optional<eCEntity> declaredItem = sNpc.getChildByGuid(itemGuid.getGuid());
			if (declaredItem.isPresent()) {
				eCEntity item = declaredItem.get().clone();
				item.setGuid(GuidUtil.randomGUID());
				itemGuid.setGuid(item.getGuid());
				eCEntityProxy proxy = item.getClass(CD.gCInteraction_PS.class).property(CD.gCInteraction_PS.Owner);
				proxy.setGuid(npc.getGuid());

				npc.attachChild(item);

				int slotType = gESlot.fromUseType(EntityUtil.getUseType(item));
				if (slotType == gESlot.gESlot_Head || slotType == gESlot.gESlot_Body) {
					NPCUtil.syncAnimatedProperties(npc, item);
				} else {
					NPCUtil.syncWearableProperties(npc, item);
				}
			}
		}

		return npc;
	}

	public static eCEntity copyNPC(eCEntity sNpc) {
		eCEntity npc = sNpc.clone();
		gCInventory_PS inv = npc.getClass(CD.gCInventory_PS.class);
		for (G3Class slot : inv.slots) {
			eCEntityProxy itemGuid = slot.property(CD.gCInventorySlot.Item);
			Optional<eCEntity> declaredItem = sNpc.getChildByGuid(itemGuid.getGuid());
			if (declaredItem.isPresent()) {
				eCEntity item = declaredItem.get().clone();
				npc.attachChild(item);
			}
		}

		return npc;
	}

	public static void clearTeaching(eCEntity npc) {

		bTObjArray_bCString attribs = npc.getClass(CD.gCNPC_PS.class).property(CD.gCNPC_PS.TeachAttribs);
		attribs.clear();

		bTObjArray_eCEntityProxy skills = npc.getClass(CD.gCNPC_PS.class).property(CD.gCNPC_PS.TeachSkills);
		skills.clear();
	}

	public static void setWeaponry(eCEntity entity, String weaponry) {
		boolean set = false;
		gCInventory_PS inv = entity.getClass(CD.gCInventory_PS.class);
		for (PropertyDescriptor<bCString> tsProp : PropertyUtil.GetTreasureSetProperties()) {
			bCString entry = inv.property(tsProp);
			if (entry.getString().contains("TS_Weaponry") || entry.getString().isEmpty()) {
				entry.setString("");
				if (!set) {
					entry.setString(weaponry);
					set = true;
				}
			}
		}
	}

	public static boolean isNPC(eCEntity entity) {
		return entity.hasClass(CD.gCNPC_PS.class) && entity.hasClass(CD.gCInventory_PS.class);
	}

	public void setVoice(eCEntity entity, String voice) {
		G3Class npc = entity.getClass(CD.gCNPC_PS.class);
		npc.property(CD.gCNPC_PS.Voice).setString(voice);
	}

	public void setLevel(eCEntity entity, int levelStart, int levelMax) {
		G3Class npc = entity.getClass(CD.gCNPC_PS.class);
		npc.property(CD.gCNPC_PS.Level).setLong(levelStart);
		npc.property(CD.gCNPC_PS.LevelMax).setLong(levelMax);
	}
}

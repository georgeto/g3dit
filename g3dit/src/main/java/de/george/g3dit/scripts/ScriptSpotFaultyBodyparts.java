package de.george.g3dit.scripts;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.google.common.base.Strings;

import de.george.lrentnode.archive.ArchiveFile;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.archive.lrentdat.LrentdatEntity;
import de.george.lrentnode.classes.G3Class;
import de.george.lrentnode.classes.eCVisualAnimation_PS;
import de.george.lrentnode.classes.eCVisualAnimation_PS.ExtraSlot;
import de.george.lrentnode.classes.eCVisualAnimation_PS.MaterialSwitchSlot;
import de.george.lrentnode.classes.gCInventory_PS;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.enums.G3Enums;
import de.george.lrentnode.enums.G3Enums.gESlot;
import de.george.lrentnode.enums.G3Enums.gEUseType;
import de.george.lrentnode.iterator.ArchiveFileIterator;
import de.george.lrentnode.iterator.TemplateFileIterator;
import de.george.lrentnode.template.TemplateEntity;
import de.george.lrentnode.template.TemplateFile;
import de.george.lrentnode.util.FileUtil;
import de.george.lrentnode.util.NPCUtil;

public class ScriptSpotFaultyBodyparts implements IScript {

	@Override
	public String getTitle() {
		return "Fehlerhafte Körperteile ermitteln";
	}

	@Override
	public String getDescription() {
		return "Überprüft für alle Körperteile, ob diese Inkonsistenzen mit ihrer Template oder ihrem NPC aufweisen.";
	}

	@Override
	public boolean execute(IScriptEnvironment env) {
		TemplateFileIterator tpleFilesIterator = env.getFileManager().templateFilesIterator();
		Map<String, File> tpleGuidMap = new HashMap<>();

		while (tpleFilesIterator.hasNext()) {
			TemplateFile tple = tpleFilesIterator.next();
			tpleGuidMap.put(tple.getReferenceHeader().getGuid(), tpleFilesIterator.nextFile());
		}

		ArchiveFileIterator worldFilesIterator = env.getFileManager().worldFilesIterator();
		while (worldFilesIterator.hasNext()) {
			ArchiveFile archiveFile = worldFilesIterator.next();

			for (eCEntity entity : archiveFile.getEntities()) {
				if (entity.hasClass(CD.gCInteraction_PS.class)) {
					G3Class inter = entity.getClass(CD.gCInteraction_PS.class);
					int useType = inter.property(CD.gCInteraction_PS.UseType).getEnumValue();
					if (entity instanceof LrentdatEntity && (useType == gEUseType.gEUseType_Head || useType == gEUseType.gEUseType_Body
							|| useType == gEUseType.gEUseType_Beard || useType == gEUseType.gEUseType_Helmet
							|| useType == gEUseType.gEUseType_Hair)) {
						List<String> messages = new ArrayList<>();
						int slot = gESlot.fromUseType(useType);

						eCVisualAnimation_PS visEntity = entity.getClass(CD.eCVisualAnimation_PS.class);
						if (visEntity != null) {
							int materialSwitch = visEntity.property(CD.eCVisualAnimation_PS.MaterialSwitch).getInt();
							String filePath = visEntity.property(CD.eCVisualAnimation_PS.ResourceFilePath).getString().replace(".FXA",
									".fxa");
							String animFilePath = visEntity.property(CD.eCVisualAnimation_PS.FacialAnimFilePath).getString()
									.replace(".FXA", ".fxa");
							if (!Objects.equals(filePath, visEntity.fxaSlot.fxaFile)) {
								messages.add("ResourceFilePath weicht von Anhang 1 ab: '" + filePath + "' vs. '"
										+ visEntity.fxaSlot.fxaFile + "'");
							}

							if (!Objects.equals(materialSwitch, visEntity.fxaSlot.fxaSwitch)) {
								messages.add("MaterialSwitch weicht von Anhang 1 ab: " + materialSwitch + " vs. "
										+ visEntity.fxaSlot.fxaSwitch);
							}

							if (!Objects.equals(animFilePath, Strings.nullToEmpty(visEntity.fxaSlot.fxaFile2))) {
								messages.add("FacialAnimFilePath weicht von Anhang 2 ab: '" + filePath + "' vs. '"
										+ Strings.nullToEmpty(visEntity.fxaSlot.fxaFile2) + "'");
							}

							if (!Objects.isNull(visEntity.fxaSlot.fxaFile2)
									&& !Objects.equals(materialSwitch, visEntity.fxaSlot.fxaSwitch2)) {
								messages.add("MaterialSwitch weicht von Anhang 2 ab: " + materialSwitch + " vs. "
										+ visEntity.fxaSlot.fxaSwitch2);
							}
						}

						String creator = ((LrentdatEntity) entity).getCreator();
						if (creator != null) {
							File file = tpleGuidMap.get(creator);
							if (file != null) {
								Optional<eCEntity> owner = archiveFile
										.getEntityByGuid(inter.property(CD.gCInteraction_PS.Owner).getGuid());
								if (owner.isPresent()) {
									if (owner.get() != entity.getParent()) {
										messages.add(String.format(
												"Körperteil ist kein Child seines Owners '%s' (%s), sondern ein Child von '%s' (%s)",
												owner.get().getName(), owner.get().getGuid(), entity.getParent().getName(),
												entity.getParent().getGuid()));
									} else {
										gCInventory_PS inv = owner.get().getClass(CD.gCInventory_PS.class);

										G3Class invSlot = inv.getValidSlot(slot);
										if (invSlot != null) {
											String slotItem = invSlot.property(CD.gCInventorySlot.Item).getGuid();
											if (!entity.getGuid().equals(slotItem)) {
												messages.add("Körperteil Slot des zugehörigen NPCs referenziert nicht auf das Körperteil: "
														+ slotItem);
											}

											eCVisualAnimation_PS visNPC = owner.get().getClass(CD.eCVisualAnimation_PS.class);
											String slotName = gESlot.getSlotName(slot);
											if ((slot == gESlot.gESlot_Head || slot == gESlot.gESlot_Body) && visEntity != null) {
												java.util.Optional<MaterialSwitchSlot> slotNPC = visNPC.bodyParts.stream()
														.filter(p -> p.name.equals(slotName)).findFirst();
												if (!slotNPC.isPresent()) {
													messages.add("Zugehörige NPC enthält keinen passenden MaterialSwitchSlot.");
												} else {
													compareMaterialSwitchSlot(visEntity.fxaSlot, slotNPC.get(), "(Item vs. NPC)",
															messages);
												}
											}

											if (slot != gESlot.gESlot_Head && slot != gESlot.gESlot_Body) {
												java.util.Optional<ExtraSlot> extraSlotNPC = visNPC.attachments.stream()
														.filter(p -> p.name.equals(slotName)).findFirst();
												if (!extraSlotNPC.isPresent()) {
													messages.add("Zugehörige NPC enthält keinen passenden ExtraSlot.");
												} else if (!entity.getGuid().equals(extraSlotNPC.get().guid)) {
													messages.add(
															"ExtraSlot Guid des zugehörigen NPCs referenziert nicht auf das Körperteil: "
																	+ extraSlotNPC.get().guid);
												}
											}

											String slotTple = invSlot.property(CD.gCInventorySlot.Template).getGuid();
											if (creator.equals(slotTple)) {
												try {
													int messageCount = messages.size();
													TemplateEntity tple = FileUtil.openTemplate(file).getReferenceHeader();
													compareItemToTemplate(entity, visEntity, tple, messages);

													if (messageCount < messages.size()) {
														messages.add(messageCount,
																"Template: " + tple.getName() + " (" + tple.getFileName() + ")");
													}
												} catch (Exception e) {
													e.printStackTrace();
												}
											} else {
												messages.add("Körperteil referenziert andere Template als Head Slot des zugehörigen NPCs: "
														+ creator + " vs. " + slotTple);
											}
										} else {
											messages.add("Owner hat keinen " + G3Enums.asString(gESlot.class, slot) + " Slot.");
										}
									}
								} else if (NPCUtil.isNPC(entity.getParent())) {
									messages.add("Hat einen ungültigen Owner eingetragen: "
											+ inter.property(CD.gCInteraction_PS.Owner).getGuid());
								}
							} else {
								messages.add("Hat eine nicht existente Reference Guid eingetragen: " + creator);
							}
						} else {
							messages.add("Hat keine Reference Guid eingetragen.");
						}

						if (!messages.isEmpty()) {
							String entityIdentifier = G3Enums.asString(gESlot.class, slot) + ": " + entity.toString() + " | "
									+ entity.getGuid() + " | " + worldFilesIterator.nextFile().getName();
							env.log(entityIdentifier);
							messages.stream().map(s -> "  " + s).forEach(env::log);
							env.log("");
						}
					}
				}
			}
		}

		return true;
	}

	private void compareItemToTemplate(eCEntity entity, eCVisualAnimation_PS visEntity, TemplateEntity tple, List<String> messages) {
		eCVisualAnimation_PS visTple = tple.getClass(CD.eCVisualAnimation_PS.class);

		if (!entity.getName().equals(tple.getName())) {
			messages.add("Name (Item vs. Tple): " + entity.getName() + " vs. " + tple.getName());
		}

		if (entity.getDataChangedTimeStamp() != tple.getDataChangedTimeStamp()) {
			messages.add("DataChangedTimeStamp (Item vs. Tple): " + entity.getDataChangedTimeStamp() + " vs. "
					+ tple.getDataChangedTimeStamp());
		}

		if (visEntity != null) {
			if (!visEntity.property(CD.eCVisualAnimation_PS.ResourceFilePath).getString()
					.equals(visTple.property(CD.eCVisualAnimation_PS.ResourceFilePath).getString())) {
				messages.add(
						"FacialAnimFilePath (Item vs. Tple): " + visEntity.property(CD.eCVisualAnimation_PS.ResourceFilePath).getString()
								+ " vs. " + visTple.property(CD.eCVisualAnimation_PS.ResourceFilePath).getString());
			}

			if (!visEntity.property(CD.eCVisualAnimation_PS.FacialAnimFilePath).getString()
					.equals(visTple.property(CD.eCVisualAnimation_PS.FacialAnimFilePath).getString())) {
				messages.add(
						"ResourceFilePath (Item vs. Tple): " + visEntity.property(CD.eCVisualAnimation_PS.FacialAnimFilePath).getString()
								+ " vs. " + visTple.property(CD.eCVisualAnimation_PS.FacialAnimFilePath).getString());
			}

			if (visEntity.property(CD.eCVisualAnimation_PS.MaterialSwitch).getInt() != visTple
					.property(CD.eCVisualAnimation_PS.MaterialSwitch).getInt()) {
				messages.add("MaterialSwitch (Item vs. Tple): " + visEntity.property(CD.eCVisualAnimation_PS.MaterialSwitch).getInt()
						+ " vs. " + visTple.property(CD.eCVisualAnimation_PS.MaterialSwitch).getInt());
			}

			MaterialSwitchSlot fxaSlotEntity = visEntity.fxaSlot;
			MaterialSwitchSlot fxaSlotTple = visTple.fxaSlot;
			compareMaterialSwitchSlot(fxaSlotEntity, fxaSlotTple, "(Item vs. Tple)", messages);
		}
	}

	private void compareMaterialSwitchSlot(MaterialSwitchSlot fxaSlot1, MaterialSwitchSlot fxaSlot2, String prefix,
			List<String> messages) {
		if (!fxaSlot1.fxaFile.equals(fxaSlot2.fxaFile)) {
			messages.add("FxaFile " + prefix + ": " + fxaSlot1.fxaFile + " vs. " + fxaSlot2.fxaFile);
		}

		if (fxaSlot1.fxaSwitch != fxaSlot2.fxaSwitch) {
			messages.add("FxaSwitch " + prefix + ": " + fxaSlot1.fxaSwitch + " vs. " + fxaSlot2.fxaSwitch);
		}

		if (!Objects.equals(fxaSlot1.fxaFile2, fxaSlot2.fxaFile2)) {
			messages.add("FxaFile2 " + prefix + ": " + fxaSlot1.fxaFile2 + " vs. " + fxaSlot2.fxaFile2);
		}

		if (fxaSlot1.fxaSwitch2 != fxaSlot2.fxaSwitch2) {
			messages.add("FxaSwitch2 " + prefix + ": " + fxaSlot1.fxaSwitch2 + " vs. " + fxaSlot2.fxaSwitch2);
		}
	}
}

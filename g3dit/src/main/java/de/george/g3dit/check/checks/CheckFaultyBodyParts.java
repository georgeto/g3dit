package de.george.g3dit.check.checks;

import static j2html.TagCreator.a;
import static j2html.TagCreator.join;
import static j2html.TagCreator.text;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import com.google.common.base.Strings;

import de.george.g3dit.check.EntityDescriptor;
import de.george.g3dit.check.FileDescriptor;
import de.george.g3dit.check.FileDescriptor.FileType;
import de.george.g3dit.check.problem.ProblemConsumer;
import de.george.g3dit.check.problem.Severity;
import de.george.g3dit.util.UriUtil;
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
import de.george.lrentnode.template.TemplateEntity;
import de.george.lrentnode.template.TemplateFile;
import de.george.lrentnode.util.FileUtil;
import de.george.lrentnode.util.NPCUtil;

public class CheckFaultyBodyParts extends AbstractEntityCheck {
	public CheckFaultyBodyParts() {
		super("Fehlerhafte Körperteile ermitteln",
				"Überprüft für alle Körperteile, ob diese Inkonsistenzen mit ihrer Template oder ihrem NPC aufweisen.", 1, 1);
	}

	private Map<String, File> tpleGuidMap;

	@Override
	public void reset() {
		tpleGuidMap = new HashMap<>();
	}

	@Override
	public PassStatus processTemplate(TemplateFile tple, File dataFile, int pass, ProblemConsumer problemConsumer) {
		tpleGuidMap.put(tple.getReferenceHeader().getGuid(), dataFile);
		return PassStatus.Next;
	}

	@Override
	protected EntityPassStatus processEntity(ArchiveFile archiveFile, File dataFile, eCEntity entity, int entityPosition, int pass,
			Supplier<EntityDescriptor> descriptor, StringProblemConsumer problemConsumer) {
		if (entity.hasClass(CD.gCInteraction_PS.class)) {
			G3Class inter = entity.getClass(CD.gCInteraction_PS.class);
			int useType = inter.property(CD.gCInteraction_PS.UseType).getEnumValue();
			if (entity instanceof LrentdatEntity
					&& (useType == gEUseType.gEUseType_Head || useType == gEUseType.gEUseType_Body || useType == gEUseType.gEUseType_Beard
							|| useType == gEUseType.gEUseType_Helmet || useType == gEUseType.gEUseType_Hair)) {
				int slot = gESlot.fromUseType(useType);

				eCVisualAnimation_PS visEntity = entity.getClass(CD.eCVisualAnimation_PS.class);
				if (visEntity != null) {
					int materialSwitch = visEntity.property(CD.eCVisualAnimation_PS.MaterialSwitch).getInt();
					String filePath = visEntity.property(CD.eCVisualAnimation_PS.ResourceFilePath).getString().replace(".FXA", ".fxa");
					String animFilePath = visEntity.property(CD.eCVisualAnimation_PS.FacialAnimFilePath).getString().replace(".FXA",
							".fxa");
					if (!Objects.equals(filePath, visEntity.fxaSlot.fxaFile)) {
						problemConsumer.fatal(
								"ResourceFilePath weicht von Anhang 1 ab: '" + filePath + "' vs. '" + visEntity.fxaSlot.fxaFile + "'");
					}

					if (!Objects.equals(materialSwitch, visEntity.fxaSlot.fxaSwitch)) {
						problemConsumer
								.fatal("MaterialSwitch weicht von Anhang 1 ab: " + materialSwitch + " vs. " + visEntity.fxaSlot.fxaSwitch);
					}

					if (!Objects.equals(animFilePath, Strings.nullToEmpty(visEntity.fxaSlot.fxaFile2))) {
						problemConsumer.fatal("FacialAnimFilePath weicht von Anhang 2 ab: '" + filePath + "' vs. '"
								+ Strings.nullToEmpty(visEntity.fxaSlot.fxaFile2) + "'");
					}

					if (!Objects.isNull(visEntity.fxaSlot.fxaFile2) && !Objects.equals(materialSwitch, visEntity.fxaSlot.fxaSwitch2)) {
						problemConsumer.fatal(
								"MaterialSwitch weicht von Anhang 2 ab: " + materialSwitch + " vs. " + visEntity.fxaSlot.fxaSwitch2);
					}
				}

				String creator = ((LrentdatEntity) entity).getCreator();
				if (creator != null) {
					File tpleFile = tpleGuidMap.get(creator);
					if (tpleFile != null) {
						Optional<eCEntity> owner = archiveFile.getEntityByGuid(inter.property(CD.gCInteraction_PS.Owner).getGuid());
						if (owner.isPresent()) {
							if (owner.get() != entity.getParent()) {
								problemConsumer.fatal(
										String.format("Körperteil ist kein Child seines Owners '%s' (%s), sondern ein Child von '%s' (%s)",
												owner.get().getName(), owner.get().getGuid(), entity.getParent().getName(),
												entity.getParent().getGuid()));
							} else {
								float distanceToOwner = entity.getWorldPosition().getInvTranslated(owner.get().getWorldPosition())
										.length();
								if (distanceToOwner > 1000.0f) {
									problemConsumer.warning(
											String.format("Körperteil ist zu weit (%d) von NPC entfernt.", (int) distanceToOwner));
								}

								gCInventory_PS inv = owner.get().getClass(CD.gCInventory_PS.class);
								G3Class invSlot = inv.getValidSlot(slot);
								if (invSlot != null) {
									String slotItem = invSlot.property(CD.gCInventorySlot.Item).getGuid();
									if (!entity.getGuid().equals(slotItem)) {
										problemConsumer.fatal(
												"Körperteil Slot des zugehörigen NPCs referenziert nicht auf das Körperteil: " + slotItem);
									}

									eCVisualAnimation_PS visNPC = owner.get().getClass(CD.eCVisualAnimation_PS.class);
									String slotName = gESlot.getSlotName(slot);
									if ((slot == gESlot.gESlot_Head || slot == gESlot.gESlot_Body) && visEntity != null) {
										java.util.Optional<MaterialSwitchSlot> slotNPC = visNPC.bodyParts.stream()
												.filter(p -> p.name.equals(slotName)).findFirst();
										if (!slotNPC.isPresent()) {
											problemConsumer.fatal("Zugehörige NPC enthält keinen passenden MaterialSwitchSlot.");
										} else {
											problemConsumer.postIfDetailsNotEmpy(Severity.Fatal,
													"MaterialSwitchSlot des zugehörigen NPCs fehlerhaft.",
													compareMaterialSwitchSlot(visEntity.fxaSlot, slotNPC.get(), "(Item vs. NPC)"));
										}
									}

									if (slot != gESlot.gESlot_Head && slot != gESlot.gESlot_Body) {
										java.util.Optional<ExtraSlot> extraSlotNPC = visNPC.attachments.stream()
												.filter(p -> p.name.equals(slotName)).findFirst();
										if (!extraSlotNPC.isPresent()) {
											problemConsumer.fatal("Zugehörige NPC enthält keinen passenden ExtraSlot.");
										} else if (!entity.getGuid().equals(extraSlotNPC.get().guid)) {
											problemConsumer
													.fatal("ExtraSlot Guid des zugehörigen NPCs referenziert nicht auf das Körperteil: "
															+ extraSlotNPC.get().guid);
										}
									}

									String slotTple = invSlot.property(CD.gCInventorySlot.Template).getGuid();
									if (creator.equals(slotTple)) {
										try {
											TemplateEntity tple = FileUtil.openTemplate(tpleFile).getReferenceHeader();
											problemConsumer.postIfDetailsNotEmpy(Severity.Fatal,
													join(text("Weicht von Template ab: "),
															a(tple.getName()).withHref(
																	UriUtil.encodeFile(new FileDescriptor(tpleFile, FileType.Template))))
																			.render(),
													compareItemToTemplate(entity, visEntity, tple));
										} catch (Exception e) {
											problemConsumer.fatal("Fehler beim Öffnen von Template.");

										}
									} else {
										problemConsumer
												.fatal("Körperteil referenziert andere Template als Head Slot des zugehörigen NPCs: "
														+ creator + " vs. " + slotTple);
									}
								} else {
									problemConsumer.fatal("Owner hat keinen " + G3Enums.asString(gESlot.class, slot) + " Slot.");
								}
							}
						} else if (NPCUtil.isNPC(entity.getParent())) {
							problemConsumer.fatal(
									"Hat einen ungültigen Owner eingetragen: " + inter.property(CD.gCInteraction_PS.Owner).getGuid());
						}
					} else {
						problemConsumer.fatal("Hat eine nicht existente Reference Guid eingetragen: " + creator);
					}
				} else {
					problemConsumer.fatal("Hat keine Reference Guid eingetragen.");
				}
			}
		}

		return EntityPassStatus.Next;
	}

	private List<String> compareItemToTemplate(eCEntity entity, eCVisualAnimation_PS visEntity, TemplateEntity tple) {
		List<String> messages = new ArrayList<>();

		eCVisualAnimation_PS visTple = tple.getClass(CD.eCVisualAnimation_PS.class);
		if (!entity.getName().equals(tple.getName())) {
			messages.add("Name (Item vs. Tple): " + entity.getName() + " vs. " + tple.getName());
		}

		if (entity.getDataChangedTimeStamp() != tple.getDataChangedTimeStamp()) {
			messages.add("DataChangedTimeStamp (Item vs. Tple): " + entity.getDataChangedTimeStamp() + " vs. "
					+ tple.getDataChangedTimeStamp());
		}

		if (visEntity != null) {
			String entityResourceFilePath = visEntity.property(CD.eCVisualAnimation_PS.ResourceFilePath).getString().replace(".FXA",
					".fxa");
			String tpleResourceFilePath = visTple.property(CD.eCVisualAnimation_PS.ResourceFilePath).getString().replace(".FXA", ".fxa");
			if (!entityResourceFilePath.equals(tpleResourceFilePath)) {
				messages.add("ResourceFilePath (Item vs. Tple): " + entityResourceFilePath + " vs. " + tpleResourceFilePath);
			}

			String entityFacialAnimPath = visEntity.property(CD.eCVisualAnimation_PS.FacialAnimFilePath).getString().replace(".FXA",
					".fxa");
			String tpleFacialAnimPath = visTple.property(CD.eCVisualAnimation_PS.FacialAnimFilePath).getString().replace(".FXA", ".fxa");
			if (!entityFacialAnimPath.equals(tpleFacialAnimPath)) {
				messages.add("FacialAnimFilePath (Item vs. Tple): " + entityFacialAnimPath + " vs. " + tpleFacialAnimPath);
			}

			if (visEntity.property(CD.eCVisualAnimation_PS.MaterialSwitch).getInt() != visTple
					.property(CD.eCVisualAnimation_PS.MaterialSwitch).getInt()) {
				messages.add("MaterialSwitch (Item vs. Tple): " + visEntity.property(CD.eCVisualAnimation_PS.MaterialSwitch).getInt()
						+ " vs. " + visTple.property(CD.eCVisualAnimation_PS.MaterialSwitch).getInt());
			}

			MaterialSwitchSlot fxaSlotEntity = visEntity.fxaSlot;
			MaterialSwitchSlot fxaSlotTple = visTple.fxaSlot;
			messages.addAll(compareMaterialSwitchSlot(fxaSlotEntity, fxaSlotTple, "(Item vs. Tple)"));
		}
		return messages;
	}

	private List<String> compareMaterialSwitchSlot(MaterialSwitchSlot fxaSlot1, MaterialSwitchSlot fxaSlot2, String prefix) {
		List<String> messages = new ArrayList<>();

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
		return messages;
	}
}

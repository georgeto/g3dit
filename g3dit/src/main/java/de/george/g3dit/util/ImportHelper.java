package de.george.g3dit.util;

import java.awt.Window;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ezware.dialog.task.TaskDialogs;
import com.teamunify.i18n.I;

import de.george.g3dit.EditorContext;
import de.george.g3dit.gui.dialogs.AbstractSelectDialog;
import de.george.g3dit.gui.dialogs.TreeEntitySelectDialog;
import de.george.g3utils.structure.GuidUtil;
import de.george.lrentnode.archive.ArchiveEntity;
import de.george.lrentnode.archive.ArchiveFile;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.archive.lrentdat.LrentdatEntity;
import de.george.lrentnode.archive.node.NodeEntity;
import de.george.lrentnode.util.EntityUtil;
import de.george.lrentnode.util.FileUtil;
import de.george.lrentnode.util.NPCUtil;
import net.miginfocom.swing.MigLayout;

public class ImportHelper {
	private static final Logger logger = LoggerFactory.getLogger(ImportHelper.class);

	public static final boolean importFromFile(File file, ArchiveFile currentFile, EditorContext ctx) {
		try {
			return importFromArchiveFile(FileUtil.openArchive(file, false), currentFile, ctx);
		} catch (Exception e) {
			logger.warn("Import failed.", e);
			TaskDialogs.showException(e);
			return false;
		}
	}

	public static final boolean importFromArchiveFile(ArchiveFile aFile, ArchiveFile currentFile, EditorContext ctx) {
		try {
			boolean convert = false;
			String message = null;

			if (currentFile.isLrentdat()) {
				if (!aFile.isLrentdat()) {
					message = I.tr("Entities aus einer *.node können nicht in eine *.lrentdat importiert werden.");
				}
			} else if (aFile.isLrentdat()) {
				message = I.tr("Entities aus einer *.lrentdat können nicht in eine *.node importiert werden.");
			}

			if (message != null) {
				convert = TaskDialogs.ask(ctx.getParentWindow(), I.tr("Inkompatible Dateitypen"),
						message + "\n" + I.tr("Sollen die Entities konvertiert werden?"));
				if (!convert) {
					return false;
				}
			}

			ImportTreeEntitySelectDialog dialog = new ImportTreeEntitySelectDialog(ctx, I.tr("Entities importieren"),
					AbstractSelectDialog.SELECTION_MULTIPLE, aFile);
			if (dialog.openAndWasSuccessful()) {
				List<eCEntity> entities = dialog.getSelectedEntries();
				Function<eCEntity, eCEntity> cloneEntity = eCEntity::clone;
				if (convert) {
					if (currentFile.isLrentdat()) {
						cloneEntity = cloneEntity
								.andThen((Function) (Function<ArchiveEntity, LrentdatEntity>) EntityUtil::convertToLrentdatEntity);
					} else {
						cloneEntity = cloneEntity
								.andThen((Function) (Function<ArchiveEntity, NodeEntity>) EntityUtil::convertToNodeEntity);
					}
				}

				if (dialog.isGenerateRandomGuids()) {
					cloneEntity = cloneEntity.andThen((entity) -> {
						entity.setGuid(GuidUtil.randomGUID());
						return entity;
					});
				}

				Set<eCEntity> alreadyCopied = new HashSet<>();

				final boolean convertFinal = convert;
				Function<eCEntity, eCEntity> cloneNpc = (entity) -> {
					if (!convertFinal && NPCUtil.isNPC(entity)) {
						alreadyCopied.addAll(entity.getChilds());
						return dialog.isGenerateRandomGuids() ? NPCUtil.cloneNPC(entity) : NPCUtil.copyNPC(entity);
					} else {
						return null;
					}
				};

				for (eCEntity importEntity : entities) {
					eCEntity clonedEntity;
					if (dialog.isImportChilds()) {
						clonedEntity = EntityUtil.cloneEntityRecursive(importEntity, cloneEntity, entity -> {
							// Mark entity as copied
							alreadyCopied.add(entity);

							return cloneNpc.apply(entity);
						}, alreadyCopied::contains);
					} else {
						if (!alreadyCopied.add(importEntity)) {
							continue;
						}

						clonedEntity = cloneNpc.apply(importEntity);
						if (clonedEntity == null) {
							clonedEntity = cloneEntity.apply(importEntity);
						}
					}

					if (clonedEntity != null) {
						clonedEntity.moveToWorldNode(currentFile.getGraph());

						// Set new file
						clonedEntity.getIndirectChilds().append(clonedEntity).forEach(entity -> {
							if (entity instanceof ArchiveEntity) {
								((ArchiveEntity) entity).setFile(currentFile);
							}
						});
					}
				}
				return true;
			}
		} catch (Exception ex) {
			logger.warn("Import failed.", ex);
			TaskDialogs.showException(ex);

		}
		return false;
	}

	public static final boolean importFromList(List<ArchiveEntity> entities, ArchiveFile currentFile, Window owner,
			boolean generateRandomGuids) {
		try {
			for (ArchiveEntity entity : entities) {
				// Convert Entity
				if (currentFile.isLrentdat()) {
					if (!(entity instanceof LrentdatEntity)) {
						entity = EntityUtil.convertToLrentdatEntity(entity);
					}
				} else if (!(entity instanceof NodeEntity)) {
					entity = EntityUtil.convertToNodeEntity(entity);
				}

				entity.setFile(currentFile);
				if (generateRandomGuids) {
					entity.setGuid(GuidUtil.randomGUID());
				}
				entity.moveToWorldNode(currentFile.getGraph());
			}
			return true;
		} catch (Exception ex) {
			logger.warn("Import failed.", ex);
			TaskDialogs.showException(ex);

		}
		return false;
	}

	private static class ImportTreeEntitySelectDialog extends TreeEntitySelectDialog {
		private JCheckBox cbRandomGuids, cbImportChilds;

		public ImportTreeEntitySelectDialog(EditorContext ctx, String title, int selectionType, ArchiveFile file) {
			super(ctx, title, selectionType, file);
		}

		@Override
		public JComponent createContentPanel() {
			JPanel contentPanel = new JPanel(new MigLayout("ins 0, fill", "[]", "[fill][]"));
			contentPanel.add(super.createContentPanel(), "grow, wrap");
			cbImportChilds = new JCheckBox(I.tr("Childs-Entities importieren"));
			cbImportChilds.setSelected(true);
			contentPanel.add(cbImportChilds, "gapleft 5, split 2");
			cbRandomGuids = new JCheckBox(I.tr("Zufällige Guids generieren"));
			cbRandomGuids.setSelected(true);
			contentPanel.add(cbRandomGuids);
			return contentPanel;
		}

		public boolean isGenerateRandomGuids() {
			return cbRandomGuids.isSelected();
		}

		public boolean isImportChilds() {
			return cbImportChilds.isSelected();
		}
	}
}

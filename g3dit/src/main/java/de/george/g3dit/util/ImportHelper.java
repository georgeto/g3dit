package de.george.g3dit.util;

import java.awt.Window;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ezware.dialog.task.TaskDialogs;

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
			logger.warn("Import fehlgeschalgen: ", e);
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
					message = "Entities aus einer *.node können nicht in eine *.lrentdat importiert werden.";
				}
			} else if (aFile.isLrentdat()) {
				message = "Entities aus einer *.lrentdat können nicht in eine *.node importiert werden.";
			}

			if (message != null) {
				convert = TaskDialogs.ask(ctx.getParentWindow(), "Inkompatible Dateitypen",
						message + "\nSollen die Entities konvertiert werden?");
				if (!convert) {
					return false;
				}
			}

			ImportTreeEntitySelectDialog dialog = new ImportTreeEntitySelectDialog(ctx, "Entities importieren",
					AbstractSelectDialog.SELECTION_MULTIPLE, aFile);
			if (dialog.openAndWasSuccessful()) {
				List<eCEntity> entities = dialog.getSelectedEntries();
				if (convert) {
					// Convert Entities
					List<eCEntity> cEntities = new ArrayList<>(entities.size());
					for (eCEntity entity : entities) {
						cEntities.add(currentFile.isLrentdat() ? EntityUtil.convertToLrentdatEntity((ArchiveEntity) entity)
								: EntityUtil.convertToNodeEntity((ArchiveEntity) entity));
					}

					entities = cEntities;

					// Import Entities
					for (eCEntity entity : entities) {
						if (entity instanceof ArchiveEntity) {
							((ArchiveEntity) entity).setFile(currentFile);
						}
						if (dialog.isGenerateRandomGuids()) {
							entity.setGuid(GuidUtil.randomGUID());
						}
						entity.moveToWorldNode(currentFile.getGraph());
					}

				} else {
					// TODO: Set new file
					Set<eCEntity> alreadyCopied = new HashSet<>();
					for (eCEntity entity : entities) {
						if (NPCUtil.isNPC(entity)) {
							eCEntity npc = dialog.isGenerateRandomGuids() ? NPCUtil.cloneNPC(entity) : NPCUtil.copyNPC(entity);
							npc.moveToWorldNode(currentFile.getGraph());
							alreadyCopied.addAll(entity.getChilds());
						} else if (!alreadyCopied.contains(entity)) {
							eCEntity clonedEntity = entity.clone();
							if (dialog.isGenerateRandomGuids()) {
								clonedEntity.setGuid(GuidUtil.randomGUID());
							}
							clonedEntity.moveToWorldNode(currentFile.getGraph());
						}
					}
				}
				return true;
			}
		} catch (Exception ex) {
			logger.warn("Import fehlgeschalgen: ", ex);
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
			logger.warn("Import fehlgeschalgen: ", ex);
			TaskDialogs.showException(ex);

		}
		return false;
	}

	private static class ImportTreeEntitySelectDialog extends TreeEntitySelectDialog {
		private JCheckBox cbRandomGuids;

		public ImportTreeEntitySelectDialog(EditorContext ctx, String title, int selectionType, ArchiveFile file) {
			super(ctx, title, selectionType, file);
		}

		@Override
		public JComponent createContentPanel() {
			JPanel contentPanel = new JPanel(new MigLayout("ins 0, fill", "[]", "[fill][]"));
			contentPanel.add(super.createContentPanel(), "grow, wrap");
			cbRandomGuids = new JCheckBox("Zufällige Guids generieren");
			contentPanel.add(cbRandomGuids, "gapleft 5");
			return contentPanel;
		}

		public boolean isGenerateRandomGuids() {
			return cbRandomGuids.isSelected();
		}
	};
}

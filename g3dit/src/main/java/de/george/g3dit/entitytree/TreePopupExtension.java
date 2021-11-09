package de.george.g3dit.entitytree;

import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import com.teamunify.i18n.I;

import de.george.g3dit.util.Icons;
import de.george.lrentnode.archive.ArchiveFile;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.util.EntityUtil;
import de.george.lrentnode.util.NPCUtil;

public class TreePopupExtension implements ITreePopupExtension {

	@Override
	public void showMenu(final List<eCEntity> selEntities, final eCEntity clickedEntity, final EntityTree tree, int x, int y) {
		int elCount = selEntities.size();
		final List<eCEntity> markedEntities = tree.getMarkedEntities();
		final ArchiveFile archiveFile = tree.getArchiveFile();

		JPopupMenu menu = new JPopupMenu();
		// Entity verschieben
		if (elCount == 1 && markedEntities.size() > 0) {
			boolean show = true;
			for (eCEntity markedEntity : markedEntities) {
				if (markedEntity.equals(clickedEntity) // Ziel-Entity gleich der Quell-Entity
						|| markedEntity.isIndirectChild(clickedEntity) // Ziel-Entity ist SubEntity
																		// der Quell-Entity
						|| clickedEntity.isChild(markedEntity) // Quell-Entity ist bereits direkte
																// SubEntity der Ziel-Entity
				) {
					show = false;
					break;
				}
			}

			if (show) {
				String moveTitle;
				if (markedEntities.size() == 1) {
					moveTitle = I.trf("Make ''{0}'' a child entity of ''{1}''", markedEntities.get(0), clickedEntity);
				} else {
					moveTitle = I.trf("Make {0, number} entities child entities of ''{1}''", markedEntities.size(), clickedEntity);
				}
				JMenuItem miMove = new JMenuItem(moveTitle, Icons.getImageIcon(Icons.Arrow.CURVE));
				miMove.addActionListener(e -> {
					for (eCEntity markedEntity : markedEntities) {
						// TODO: Optional wählbar ob moveWorld oder moveLocal?!
						markedEntity.moveToWorldNode(clickedEntity);
					}
					tree.refreshTree(false, true);
				});
				menu.add(miMove);
			}
		}

		// Entity klonen
		JMenuItem miClone = new JMenuItem(I.trf("Clone '{0}", clickedEntity), Icons.getImageIcon(Icons.Action.CLONE));
		miClone.addActionListener(e -> {
			if (NPCUtil.isNPC(clickedEntity)) {
				eCEntity cloneNPC = NPCUtil.cloneNPC(clickedEntity);
				cloneNPC.moveToWorldNode(archiveFile.getGraph());
			} else {
				eCEntity clonedEntity = EntityUtil.cloneEntity(clickedEntity);
				clonedEntity.moveToWorldNode(archiveFile.getGraph());
			}
			tree.refreshTree(false, false);
		});
		menu.add(miClone);

		if (!NPCUtil.isNPC(clickedEntity) && !clickedEntity.getChilds().isEmpty()) {
			JMenuItem miCloneChilds = new JMenuItem(I.trf("Clone ''{0}'' and child entities", clickedEntity),
					Icons.getImageIcon(Icons.Action.CLONE));
			miCloneChilds.addActionListener(e -> {
				EntityUtil.cloneEntityRecursive(clickedEntity).moveToWorldNode(archiveFile.getGraph());

				tree.refreshTree(false, false);
			});
			menu.add(miCloneChilds);
		}

		// Entity für verschieben markieren
		boolean show = true;
		checkFailed: for (eCEntity markedEntity : selEntities) {
			for (eCEntity otherMarkedEntity : selEntities) {
				if (otherMarkedEntity.isIndirectChild(markedEntity)) {
					show = false;
					break checkFailed;
				}
			}
		}

		if (show) {
			String markTitle = I.trf("Mark '{0}", clickedEntity);
			if (elCount > 1) {
				markTitle = I.trf("Mark {0, number} entities", elCount);
			}
			JMenuItem miMark = new JMenuItem(markTitle, Icons.getImageIcon(Icons.Select.SELECT));
			miMark.addActionListener(e -> tree.setMarkedEntities(selEntities));
			menu.add(miMark);
		}

		// Entity löschen
		if (elCount > 1 || clickedEntity != archiveFile.getGraph()) {
			String title = I.trf("Delete '{0}", clickedEntity);
			if (elCount > 1) {
				title = I.trf("Delete {0, number} entities", elCount);
			}
			JMenuItem miDelete = new JMenuItem(title, Icons.getImageIcon(Icons.Action.DELETE));
			miDelete.addActionListener(e -> {
				// TODO: Das geht besser!
				for (eCEntity entity : selEntities) {
					if (entity == archiveFile.getGraph()) {
						continue;
					}
					// Entity (und Childs) entfernen
					entity.removeFromParent(true);
				}
				tree.refreshTree(false, true);
			});
			menu.add(miDelete);
		}

		menu.show(tree.getTree(), x, y);
	}

}

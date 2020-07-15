package de.george.g3dit.gui.dialogs;

import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.tree.TreeSelectionModel;

import com.jidesoft.dialog.ButtonPanel;

import de.george.g3dit.EditorContext;
import de.george.g3dit.entitytree.EntityTree;
import de.george.g3utils.gui.SwingUtils;
import de.george.lrentnode.archive.ArchiveFile;
import de.george.lrentnode.archive.eCEntity;

public class TreeEntitySelectDialog extends AbstractSelectDialog<eCEntity> {
	private EntityTree entityTree;

	public TreeEntitySelectDialog(EditorContext ctx, String title, int selectionType, ArchiveFile file) {
		super(ctx.getParentWindow(), title);
		setSize(400, 450);

		entityTree = new EntityTree(ctx);
		if (selectionType == SELECTION_SINGLE) {
			entityTree.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		} else {
			entityTree.setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
		}

		entityTree.setArchiveFile(file);
		entityTree.refreshTree(true, true);

	}

	@Override
	public List<eCEntity> getSelectedEntries() {
		return entityTree.getSelectedEntities();
	}

	@Override
	public JComponent createContentPanel() {
		return entityTree;
	}

	@Override
	public ButtonPanel createButtonPanel() {
		ButtonPanel buttonPanel = newButtonPanel();

		Action okAction = SwingUtils.createAction("OK", () -> {
			if (!entityTree.getSelectedEntities().isEmpty()) {
				setDialogResult(RESULT_AFFIRMED);
			}
			dispose();
		});
		okAction.setEnabled(false);

		addButton(buttonPanel, okAction, ButtonPanel.AFFIRMATIVE_BUTTON);
		addDefaultCancelButton(buttonPanel);

		entityTree.addTreeSelectionListener(e -> {
			if (!entityTree.getSelectedEntities().isEmpty()) {
				okAction.setEnabled(true);
			} else {
				okAction.setEnabled(false);
			}
		});

		return buttonPanel;
	}
}

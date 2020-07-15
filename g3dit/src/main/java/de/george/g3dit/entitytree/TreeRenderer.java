package de.george.g3dit.entitytree;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import de.george.g3dit.entitytree.filter.ITreeExtension;
import de.george.lrentnode.archive.ArchiveFile;
import de.george.lrentnode.archive.eCEntity;

public class TreeRenderer extends DefaultTreeCellRenderer {
	private ITreeExtension extension;

	private ArchiveFile file;

	private boolean showEntityNumbers;

	// Color color1 = Color.PURPLE;
	public TreeRenderer() {
		// leafIcon = new ImageIcon(url);
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {

		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

		eCEntity entity = ((EntityTreeNode) value).getEntity();

		if (showEntityNumbers) {
			setText(getText() + " (" + file.getEntityPosition(entity) + ")");
		}

		if (extension != null) {
			extension.renderElement(this, entity, file);
		}

		return this;
	}

	public void setArchiveFile(ArchiveFile file) {
		this.file = file;
	}

	public void setExtension(ITreeExtension extension) {
		this.extension = extension;
	}

	public void showEntityNumbers(boolean showEntityNumbers) {
		this.showEntityNumbers = showEntityNumbers;
	}
}

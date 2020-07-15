package de.george.g3dit.entitytree.filter;

import de.george.g3dit.entitytree.EntityTreeNode;
import de.george.lrentnode.archive.eCEntity;

public class TreeNodeBuilder {
	private ITreeExtension extension;
	private AbstractEntityFilter filter;

	public TreeNodeBuilder(AbstractEntityFilter filterMode, ITreeExtension extension) {
		filter = filterMode;
		this.extension = extension;
	}

	public EntityTreeNode prune(EntityTreeNode root) {
		removeBadNodes(root);

		return root;
	}

	/**
	 * @param node
	 * @return true, wenn Node nicht gelöscht werden darf
	 */
	private boolean removeBadNodes(EntityTreeNode node) {
		boolean keep = node.isRoot();

		if (!keep) {
			eCEntity entity = node.getEntity();

			keep = filter != null ? filter.matches(entity) : true;

			if (keep && extension != null) {
				keep = extension.filterLeave(entity);
			}

			if (keep && filter != null && filter.keepChilds()) {
				return true;
			}
		}

		int childCount = node.getChildCount();
		for (int i = childCount - 1; i >= 0; i--) {
			boolean keepChild = removeBadNodes((EntityTreeNode) node.getChildAt(i));
			if (!keepChild) {
				node.remove(i);
			}
			keep |= keepChild;
		}

		return keep;
	}
}

package de.george.g3dit.entitytree;

import java.lang.ref.WeakReference;

import javax.swing.tree.DefaultMutableTreeNode;

import de.george.lrentnode.archive.eCEntity;

public class EntityTreeNode extends DefaultMutableTreeNode {
	public EntityTreeNode() {}

	public EntityTreeNode(eCEntity entity, boolean allowsChildren) {
		super(new WeakReference<>(entity), allowsChildren);
	}

	public EntityTreeNode(eCEntity entity) {
		super(new WeakReference<>(entity));
	}

	public void setEntity(eCEntity entity) {
		setUserObject(new WeakReference<>(entity));
	}

	@SuppressWarnings("unchecked")
	public <T extends eCEntity> T getEntity() {
		return ((WeakReference<T>) getUserObject()).get();
	}

	@Override
	public String toString() {
		eCEntity entity = getEntity();
		if (entity == null) {
			return "";
		} else {
			return entity.toString();
		}
	}
}

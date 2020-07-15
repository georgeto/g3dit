package de.george.g3dit.entitytree;

import java.util.List;

import de.george.lrentnode.archive.eCEntity;

public interface ITreePopupExtension {
	public void showMenu(List<eCEntity> selEntities, eCEntity clickedEntity, EntityTree tree, int x, int y);
}

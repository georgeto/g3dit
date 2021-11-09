package de.george.g3dit.scripts;

import java.util.Objects;

import com.ezware.dialog.task.TaskDialogs;
import com.google.common.collect.Lists;
import com.teamunify.i18n.I;

import de.george.g3utils.structure.bCBox;
import de.george.g3utils.structure.bCVector;
import de.george.lrentnode.archive.ArchiveFile;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.iterator.ArchiveFileIterator;
import de.george.lrentnode.util.AABBTree;
import de.george.lrentnode.util.AABBTreePrimitive;
import de.george.lrentnode.util.EntityUtil;

public class ScriptFindMisplacedEntities implements IScript {

	@Override
	public String getTitle() {
		return I.tr("Find incorrectly placed entities");
	}

	@Override
	public String getDescription() {
		return I.tr(
				"Finds entities that have no or only a few neighbor entities within a threshold distance. In most cases, this is due to incorrect positioning of the entity.");
	}

	private static class EntityAABBTreePrimitive implements AABBTreePrimitive {
		private bCBox bounds;
		private bCVector referencePoint;
		private String guid;
		private String name;
		private bCVector position;

		public EntityAABBTreePrimitive(bCBox bounds, bCVector referencePoint, String guid, String name, bCVector position) {
			this.bounds = bounds;
			this.referencePoint = referencePoint;
			this.guid = guid;
			this.name = name;
			this.position = position;
		}

		@Override
		public bCBox getBounds() {
			return bounds;
		}

		@Override
		public bCVector getReferencePoint() {
			return referencePoint;
		}

		public String getGuid() {
			return guid;
		}

		public String getName() {
			return name;
		}

		public bCVector getPosition() {
			return position;
		}
	}

	@Override
	public boolean execute(IScriptEnvironment env) {
		Integer k = TaskDialogs.input(env.getParentWindow(), I.tr("Number of nearest neighbours considered"),
				I.tr("Number of neighbor entities that must be within the threshold distance."), 1);
		if (Objects.isNull(k)) {
			return false;
		}

		Integer threshold = TaskDialogs.input(env.getParentWindow(), I.tr("Threshold distance"), I.tr(
				"All entities that have no or not enough neighbors within the threshold distance,\nare classified as 'misplaced'."),
				2000);
		if (Objects.isNull(threshold)) {
			return false;
		}

		boolean solidOnly = TaskDialogs.ask(env.getParentWindow(), I.tr("Solid entities"),
				I.tr("Should only solid entities, i.e. those that have a mesh, be considered?"));

		env.log(I.trf("Number of nearest neighbors considered: {0, number}", k));
		env.log(I.trf("Threshold: {0, number}", threshold));

		ArchiveFileIterator worldFilesIterator = env.getFileManager().worldFilesIterator();
		AABBTree<EntityAABBTreePrimitive> aabbTree = new AABBTree<>();

		while (worldFilesIterator.hasNext()) {
			ArchiveFile archiveFile = worldFilesIterator.next();
			for (eCEntity entity : archiveFile.getEntities()) {
				if (!entity.getName().equals("RootEntity") && entity.getWorldNodeBoundary().isValid()
						&& entity.getWorldNodeBoundary().getExtent().length() < 100000
						&& (!solidOnly || EntityUtil.getMesh(entity).isPresent() || EntityUtil.getTreeMesh(entity).isPresent())) {
					aabbTree.insert(new EntityAABBTreePrimitive(entity.getWorldNodeBoundary(), entity.getWorldNodeBoundary().getCenter(),
							entity.getGuid(), entity.toString(), entity.getWorldPosition()));
				}
			}
		}

		aabbTree.complete();
		env.log(I.trf("{0, number} entities collected.", aabbTree.getPrimitives().size()));

		for (EntityAABBTreePrimitive primitive : aabbTree.getPrimitives()) {
			EntityAABBTreePrimitive neighbour = Lists.reverse(aabbTree.closestPrimitives(k + 1, primitive.getBounds())).get(0);
			float distance = primitive.getBounds().distance(neighbour.getBounds());
			if (distance > threshold) {
				env.log(I.trf("{0} ({1}) at position {2} alone in the radius of {3, number}.", primitive.getName(), primitive.getGuid(),
						primitive.getPosition().toMarvinString(), distance));
			}
		}

		return true;
	}
}

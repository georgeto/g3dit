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
		return I.tr("Falsch platzierte Entities ermitteln");
	}

	@Override
	public String getDescription() {
		return I.tr(
				"Ermittelt Entities die innerhalb einer Schwellwertdistanz keine bzw. wenige Nachbarentities haben. In den meisten Fällen liegt dann eine fehlerhafte Positionierung der Entity vor.");
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
		Integer k = TaskDialogs.input(env.getParentWindow(), I.tr("Anzahl der betrachteten nächsten Nachbarn"),
				I.tr("Anzahl der Nachbarentities die innerhalb der Schwellwertdistanz liegen müssen."), 1);
		if (Objects.isNull(k)) {
			return false;
		}

		Integer threshold = TaskDialogs.input(env.getParentWindow(), I.tr("Schwellwertdistanz"), I.tr(
				"Alle Entities die innerhalb der Schwellwertdistanz keine bzw. nicht genug Nachbarn haben,\nwerden als 'falsch' platziert klassifiziert."),
				2000);
		if (Objects.isNull(threshold)) {
			return false;
		}

		boolean solidOnly = TaskDialogs.ask(env.getParentWindow(), I.tr("Solide Entities"),
				I.tr("Sollen ausschließlich solide Entities, also solche die ein Mesh haben, betrachtet werden?"));

		env.log(I.trf("Anzahl der betrachteten nächsten Nachbarn: {0, number}", k));
		env.log(I.trf("Schwellwert: {0, number}", threshold));

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
		env.log(I.trf("{0, number} Entities erfasst.", aabbTree.getPrimitives().size()));

		for (EntityAABBTreePrimitive primitive : aabbTree.getPrimitives()) {
			EntityAABBTreePrimitive neighbour = Lists.reverse(aabbTree.closestPrimitives(k + 1, primitive.getBounds())).get(0);
			float distance = primitive.getBounds().distance(neighbour.getBounds());
			if (distance > threshold) {
				env.log(I.trf("{0} ({1}) an Position {2} alleine im Radius von {3, number}.", primitive.getName(), primitive.getGuid(),
						primitive.getPosition().toMarvinString(), distance));
			}
		}

		return true;
	}
}

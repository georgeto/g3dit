package de.george.g3dit.scripts;

import java.util.Objects;

import com.ezware.dialog.task.TaskDialogs;
import com.google.common.collect.Lists;

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
		return "Falsch platzierte Entities ermitteln";
	}

	@Override
	public String getDescription() {
		return "Ermittelt Entities die innerhalb einer Schwellwertdistanz keine bzw. wenige Nachbarentities haben. In den meisten Fällen liegt dann eine fehlerhafte Positionierung der Entity vor.";
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
		Integer k = TaskDialogs.input(env.getParentWindow(), "Anzahl der betrachteten nächsten Nachbarn",
				"Anzahl der Nachbarentities die innerhalb der Schwellwertdistanz liegen müssen.", 1);
		if (Objects.isNull(k)) {
			return false;
		}

		Integer threshold = TaskDialogs.input(env.getParentWindow(), "Schwellwertdistanz",
				"Alle Entities die innerhalb der Schwellwertdistanz keine bzw. nicht genug Nachbarn haben,\nwerden als 'falsch' platziert klassifiziert.",
				2000);
		if (Objects.isNull(threshold)) {
			return false;
		}

		boolean solidOnly = TaskDialogs.ask(env.getParentWindow(), "Solide Entities",
				"Sollen ausschließlich solide Entities, also solche die ein Mesh haben, betrachtet werden?");

		env.log("Anzahl der betrachteten nächsten Nachbarn: " + k);
		env.log("Schwellwert: " + threshold);

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
		env.log(aabbTree.getPrimitives().size() + " Entities erfasst.");

		for (EntityAABBTreePrimitive primitive : aabbTree.getPrimitives()) {
			EntityAABBTreePrimitive neighbour = Lists.reverse(aabbTree.closestPrimitives(k + 1, primitive.getBounds())).get(0);
			float distance = primitive.getBounds().distance(neighbour.getBounds());
			if (distance > threshold) {
				env.log(String.format("%s (%s) an Position %s alleine im Radius von %f.", primitive.getName(), primitive.getGuid(),
						primitive.getPosition().toMarvinString(), distance));
			}
		}

		return true;
	}
}

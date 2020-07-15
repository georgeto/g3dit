package de.george.g3dit.check.checks;

import java.io.File;
import java.util.function.Supplier;

import com.google.common.collect.Lists;

import de.george.g3dit.check.EntityDescriptor;
import de.george.g3dit.check.problem.ProblemConsumer;
import de.george.g3dit.check.problem.Severity;
import de.george.g3utils.structure.bCBox;
import de.george.g3utils.structure.bCVector;
import de.george.lrentnode.archive.ArchiveFile;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.util.AABBTree;
import de.george.lrentnode.util.AABBTreePrimitive;
import de.george.lrentnode.util.EntityUtil;

public class CheckFindMisplacedEntities extends AbstractEntityCheck {

	private Integer k;
	private Integer threshold;
	private AABBTree<EntityAABBTreePrimitive> aabbTree;
	private boolean solidOnly;

	public CheckFindMisplacedEntities() {
		super("Falsch platzierte Entities ermitteln",
				"Ermittelt Entities die innerhalb einer Schwellwertdistanz keine bzw. wenige Nachbarentities haben. In den meisten Fällen liegt dann eine fehlerhafte Positionierung der Entity vor.",
				0, 1);
	}

	private static class EntityAABBTreePrimitive implements AABBTreePrimitive {
		private final bCBox bounds;
		private final bCVector referencePoint;
		private final bCVector position;
		private final EntityDescriptor descriptor;

		public EntityAABBTreePrimitive(bCBox bounds, bCVector referencePoint, bCVector position, EntityDescriptor descriptor) {
			this.bounds = bounds;
			this.referencePoint = referencePoint;
			this.position = position;
			this.descriptor = descriptor;
		}

		@Override
		public bCBox getBounds() {
			return bounds;
		}

		@Override
		public bCVector getReferencePoint() {
			return referencePoint;
		}

		public bCVector getPosition() {
			return position;
		}

		public EntityDescriptor getDescriptor() {
			return descriptor;
		}
	}

	@Override
	public void reset() {
		// Integer k = TaskDialogs.input(, "Anzahl der betrachteten nächsten Nachbarn",
		// "Anzahl der Nachbarentities die innerhalb der Schwellwertdistanz liegen müssen.", 1);
		// if (Objects.isNull(k))
		// return false;
		//
		// Integer threshold = TaskDialogs.input(env.getParentWindow(), "Schwellwertdistanz",
		// "Alle Entities die innerhalb der Schwellwertdistanz keine bzw. nicht genug Nachbarn
		// haben,\nwerden als 'falsch' platziert klassifiziert.",
		// 2000);
		// if (Objects.isNull(threshold))
		// return false;
		//
		// boolean solidOnly = TaskDialogs.ask(env.getParentWindow(), "Solide Entities",
		// "Sollen ausschließlich solide Entities, also solche die ein Mesh haben, betrachtet
		// werden?");

		// env.log("Anzahl der betrachteten nächsten Nachbarn: " + k);
		// env.log("Schwellwert: " + threshold);

		k = 1;
		threshold = 2000;
		solidOnly = true;
		aabbTree = new AABBTree<>();
	}

	@Override
	protected EntityPassStatus processEntity(ArchiveFile archiveFile, File dataFile, eCEntity entity, int entityPosition, int pass,
			Supplier<EntityDescriptor> descriptor, StringProblemConsumer problemConsumer) {
		if (!entity.getName().equals("RootEntity") && entity.getWorldNodeBoundary().isValid()
				&& entity.getWorldNodeBoundary().getExtent().length() < 100000
				&& (!solidOnly || EntityUtil.getMesh(entity).isPresent() || EntityUtil.getTreeMesh(entity).isPresent())) {
			aabbTree.insert(new EntityAABBTreePrimitive(entity.getWorldNodeBoundary(), entity.getWorldNodeBoundary().getCenter(),
					entity.getWorldPosition(), descriptor.get()));
		}
		return EntityPassStatus.Next;
	}

	@Override
	public void reportProblems(ProblemConsumer problemConsumer) {
		aabbTree.complete();
		// env.log(aabbTree.getPrimitives().size() + " Entities erfasst.");

		for (EntityAABBTreePrimitive primitive : aabbTree.getPrimitives()) {
			EntityAABBTreePrimitive neighbour = Lists.reverse(aabbTree.closestPrimitives(k + 1, primitive.getBounds())).get(0);
			float distance = primitive.getBounds().distance(neighbour.getBounds());
			if (distance > threshold) {
				String message = String.format("An Position %s alleine im Radius von %f.", primitive.getPosition().toMarvinString(),
						distance);
				postEntityProblem(problemConsumer, primitive.getDescriptor(), Severity.Info, message, null);
			}
		}
	}
}

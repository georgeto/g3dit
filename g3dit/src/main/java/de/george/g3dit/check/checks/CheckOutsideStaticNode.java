package de.george.g3dit.check.checks;

import java.io.File;
import java.util.function.Supplier;

import com.teamunify.i18n.I;

import de.george.g3dit.check.EntityDescriptor;
import de.george.g3utils.structure.bCBox;
import de.george.g3utils.structure.bCVector;
import de.george.lrentnode.archive.ArchiveFile;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.util.EntityUtil;

public class CheckOutsideStaticNode extends AbstractEntityCheck {
	private bCBox staticNodeBoundary;

	public CheckOutsideStaticNode() {
		super(I.tr("Entity outside scope of static .node"), "", 0, 1);
	}

	@Override
	public void reset() {
		staticNodeBoundary = null;
	}

	@Override
	protected boolean onProcessArchive(ArchiveFile archiveFile, File dataFile, int pass) {
		staticNodeBoundary = EntityUtil.getStaticNodeCoordinates(dataFile.getName())
				.map(p -> new bCBox(new bCVector(p.getX() - 5000, -Float.MAX_VALUE, p.getZ() - 5000),
						new bCVector(p.getX() + 5000, Float.MAX_VALUE, p.getZ() + 5000)))
				.orElse(null);
		return staticNodeBoundary != null;
	}

	@Override
	protected EntityPassStatus processEntity(ArchiveFile archiveFile, File dataFile, eCEntity entity, int entityPosition, int pass,
			Supplier<EntityDescriptor> descriptor, StringProblemConsumer problemConsumer) {
		if (entity.getWorldNodeBoundary().isValid() && !entity.hasClass(CD.eCVegetation_PS.class)
				&& !entity.getName().contains("_Landscape_") && !entity.getWorldNodeBoundary().intersects(staticNodeBoundary)) {
			problemConsumer.fatal(I.tr("Lies outside the scope of its static .node"));
		}

		return EntityPassStatus.Next;
	}

}

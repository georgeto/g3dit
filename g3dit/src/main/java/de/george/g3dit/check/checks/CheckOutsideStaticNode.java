package de.george.g3dit.check.checks;

import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.teamunify.i18n.I;

import de.george.g3dit.EditorContext;
import de.george.g3dit.check.EntityDescriptor;
import de.george.g3dit.check.FileDescriptor;
import de.george.g3dit.check.FileDescriptor.FileType;
import de.george.g3dit.util.FileManager;
import de.george.g3utils.structure.bCBox;
import de.george.g3utils.structure.bCVector;
import de.george.g3utils.util.FilesEx;
import de.george.lrentnode.archive.ArchiveEntity;
import de.george.lrentnode.archive.ArchiveFile;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.util.EntityUtil;

public class CheckOutsideStaticNode extends AbstractEntityCheck {
	private static final Logger logger = LoggerFactory.getLogger(CheckOutsideStaticNode.class);

	private final EditorContext ctx;
	private bCBox staticNodeBoundary;

	public CheckOutsideStaticNode(EditorContext ctx) {
		super(I.tr("Entity outside scope of static .node"), "", 0, 1);
		this.ctx = ctx;
	}

	@Override
	public void reset() {
		staticNodeBoundary = null;
	}

	@Override
	protected boolean onProcessArchive(ArchiveFile archiveFile, Path dataFile, int pass) {
		staticNodeBoundary = EntityUtil.getStaticNodeCoordinates(FilesEx.getFileName(dataFile))
				.map(p -> new bCBox(new bCVector(p.getX() - 5000, -Float.MAX_VALUE, p.getZ() - 5000),
						new bCVector(p.getX() + 5000, Float.MAX_VALUE, p.getZ() + 5000)))
				.orElse(null);
		return staticNodeBoundary != null;
	}

	@Override
	protected EntityPassStatus processEntity(ArchiveFile archiveFile, Path dataFile, eCEntity entity, int entityPosition, int pass,
			Supplier<EntityDescriptor> descriptor, StringProblemConsumer problemConsumer) {
		if (entity.getWorldNodeBoundary().isValid() && !entity.hasClass(CD.eCVegetation_PS.class)
				&& !entity.getName().contains("_Landscape_") && !entity.getWorldNodeBoundary().intersects(staticNodeBoundary)) {
			EntityDescriptor entityDesc = descriptor.get();
			problemConsumer.fatal(I.tr("Lies outside the scope of its static .node"), null, () -> onFix(entityDesc));
		}

		return EntityPassStatus.Next;
	}

	private boolean onFix(EntityDescriptor entityDesc) {
		return ctx.getEditor().modifyArchive(entityDesc.getFile(), archive -> {
			Optional<eCEntity> entity = archive.getEntityByGuid(entityDesc.getGuid());
			if (!entity.isPresent()) {
				logger.warn("Entity not found: {}", entityDesc.getGuid());
				return false;
			}

			String nodeName = EntityUtil.getStaticNodeName(entity.get().getWorldPosition());
			if (FilesEx.getFileName(entityDesc.getFile().getPath()).equals(nodeName)) {
				logger.warn("Entity already in correct static .node: {}", entityDesc.getGuid());
				return false;
			}

			Optional<Path> nodePath = ctx.getFileManager().searchFile(FileManager.RP_PROJECTS_COMPILED, nodeName);
			if (!nodePath.isPresent()) {
				logger.warn("Target file {} not found.", nodeName);
				return false;
			}

			return ctx.getEditor().modifyArchive(new FileDescriptor(nodePath.get(), FileType.Node), targetArchive -> {
				entity.get().moveToWorldNode(targetArchive.getGraph());
				// Set new file
				entity.get().getIndirectChilds().append(entity.get()).forEach(e -> {
					if (e instanceof ArchiveEntity) {
						((ArchiveEntity) e).setFile(targetArchive);
					}
				});
				return true;
			});
		});
	}
}

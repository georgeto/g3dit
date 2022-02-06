package de.george.g3dit.check.checks;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Supplier;

import com.teamunify.i18n.I;

import de.george.g3dit.EditorContext;
import de.george.g3dit.check.EntityDescriptor;
import de.george.g3dit.check.FileDescriptor;
import de.george.g3dit.util.AssetResolver;
import de.george.g3dit.util.FileManager;
import de.george.g3utils.io.CompositeFileLocator;
import de.george.g3utils.io.FileLocator;
import de.george.g3utils.structure.bCVector;
import de.george.g3utils.util.Misc;
import de.george.lrentnode.archive.ArchiveFile;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.classes.eCCollisionShape;
import de.george.lrentnode.classes.eCCollisionShape.FileShape;
import de.george.lrentnode.classes.eCCollisionShape_PS;
import de.george.lrentnode.classes.eCResourceCollisionMesh_PS;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.enums.G3Enums.eECollisionShapeType;
import de.george.lrentnode.template.TemplateFile;
import de.george.lrentnode.util.FileUtil;

public class CheckMissingResources extends AbstractEntityCheck {
	private final EditorContext ctx;
	private AssetResolver assetResolver;
	private FileLocator colMeshLocator;

	public CheckMissingResources(EditorContext ctx) {
		super(I.tr("Find missing resources"), I.tr(
				"Checks all entities and templates for references to non-existent resources (meshes, material, textures, collision shapes)."),
				1, 1);
		this.ctx = ctx;
	}

	@Override
	protected EntityPassStatus processEntity(ArchiveFile archiveFile, File dataFile, eCEntity entity, int entityPosition, int pass,
			Supplier<EntityDescriptor> descriptor, StringProblemConsumer problemConsumer) {
		processEntity(entity, problemConsumer);
		return EntityPassStatus.Next;
	}

	@Override
	public PassStatus processTemplateEntity(TemplateFile tple, File dataFile, eCEntity entity, int pass, FileDescriptor descriptor,
			StringProblemConsumer problemConsumer) {
		processEntity(entity, problemConsumer);
		return PassStatus.Next;
	}

	private void processEntity(eCEntity entity, StringProblemConsumer problemConsumer) {
		// Meshes, Materials and Textures
		processMesh(entity, problemConsumer);

		// Collision meshes
		processCollisionMesh(entity, problemConsumer);
	}

	private void processMesh(eCEntity entity, StringProblemConsumer problemConsumer) {
		AssetResolver.MeshAsset mesh = assetResolver.resolveContainer(entity);
		if (mesh.getName().isEmpty()) {
			// Entity has no Mesh
			return;
		}

		if (!mesh.isFound())
			problemConsumer.fatal(I.trf("Problem with mesh ''{0}''.", mesh.getName()), mesh.getError());

		for (AssetResolver.MaterialAsset material : mesh.getMaterials()) {
			if (!material.isFound())
				problemConsumer.fatal(I.trf("Problem with material ''{0}'' (MaterialSwitch: {1, number}).", material.getName(),
						material.getMaterialSwitch()), material.getError());

			for (AssetResolver.TextureAsset texture : material.getTextures()) {
				if (!texture.isFound())
					problemConsumer.fatal(I.trf("Problem with texture ''{0}''.", texture.getName()), texture.getError());
			}
		}
	}

	private void processCollisionMesh(eCEntity entity, StringProblemConsumer problemConsumer) {
		eCCollisionShape_PS collision = entity.getClass(CD.eCCollisionShape_PS.class);
		if (collision == null) {
			return;
		}

		for (eCCollisionShape shape : collision.getShapes()) {
			if (!(shape.getShape()instanceof FileShape fileShape)) {
				continue;
			}

			String colMesh = fileShape.getShapeFile();
			if (!processCollisionMeshFile(fileShape, colMesh, problemConsumer)) {
				continue;
			}

			int shapeType = shape.property(CD.eCCollisionShape.ShapeType).getEnumValue();
			if (shapeType == eECollisionShapeType.eECollisionShapeType_ConvexHull
					|| shapeType == eECollisionShapeType.eECollisionShapeType_TriMesh) {
				bCVector scaling = entity.getWorldMatrix().getPureScaling();
				boolean uniform = Misc.compareFloat(scaling.getX(), scaling.getY(), 0.0001f)
						&& Misc.compareFloat(scaling.getX(), scaling.getZ(), 0.0001f);
				String scalingFormatted = null;
				// Convex collision meshes can only be scaled with a uniform factor (artificial
				// limitation in Gothic 3 collision shape loader).
				if (shapeType == eECollisionShapeType.eECollisionShapeType_ConvexHull || uniform) {
					if (!Misc.compareFloat(scaling.getX(), 1.0f, 0.0001f)) {
						// Uniform scaling
						scalingFormatted = String.format("_SC_%.4f", scaling.getX()).replace(".", "_");
					}
				} else {
					scalingFormatted = String.format("_SCX_%.4f_SCY_%.4f_SCZ_%.4f", scaling.getX(), scaling.getY(), scaling.getZ())
							.replace(".", "_");
				}

				if (scalingFormatted != null) {
					String colMeshScaled = colMesh.replace(".xnvmsh", scalingFormatted + ".xnvmsh");
					processCollisionMeshFile(fileShape, colMeshScaled, problemConsumer);
				}
			}
		}
	}

	private boolean processCollisionMeshFile(FileShape fileShape, String colMeshName, StringProblemConsumer problemConsumer) {
		Optional<File> colMeshFile = colMeshLocator.locate(colMeshName);
		if (!colMeshFile.isPresent()) {
			reportCollisionMesh(fileShape, colMeshName, I.tr("No collision mesh with this name found."), problemConsumer);
			return false;
		}

		try {
			eCResourceCollisionMesh_PS colMesh = FileUtil.openCollisionMesh(colMeshFile.get());
			if (fileShape.getResourceIndex() >= colMesh.getNxsBoundaries().size()) {
				reportCollisionMesh(fileShape, colMeshName, I.tr("Invalid resource index."), problemConsumer);
			}
			return true;
		} catch (IOException | RuntimeException e) {
			reportCollisionMesh(fileShape, colMeshName, I.trf("Error while opening the collision mesh: {0}", e.getMessage()),
					problemConsumer);
			return false;
		}
	}

	private void reportCollisionMesh(FileShape fileShape, String colMesh, String error, StringProblemConsumer problemConsumer) {
		problemConsumer.fatal(
				I.trf("Problem with collision mesh ''{0}'' (ResourceIndex: {1, number}).", colMesh, fileShape.getResourceIndex()), error);
	}

	@Override
	public void reset() {
		assetResolver = AssetResolver.with(ctx).build();
		colMeshLocator = new CompositeFileLocator(ctx.getFileManager().getFileLocator(FileManager.RP_COMPILED_PHYSIC, false),
				ctx.getFileManager().getFileLocator(FileManager.RP_COMPILED_MESH, false));
	}
}

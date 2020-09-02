package de.george.g3dit.scripts;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.base.Predicates;
import com.teamunify.i18n.I;

import de.george.g3dit.util.FileManager;
import de.george.g3utils.structure.bCVector;
import de.george.g3utils.util.Misc;
import de.george.lrentnode.archive.ArchiveFile;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.classes.eCCollisionShape;
import de.george.lrentnode.classes.eCCollisionShape.FileShape;
import de.george.lrentnode.classes.eCCollisionShape_PS;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.enums.G3Enums.eECollisionShapeType;
import de.george.lrentnode.iterator.ArchiveFileIterator;
import de.george.lrentnode.iterator.TemplateFileIterator;
import de.george.lrentnode.template.TemplateFile;
import one.util.streamex.StreamEx;

public class ScriptListUnusedCollisionMesh implements IScript {

	@Override
	public String getTitle() {
		return I.tr("Nicht verwendete oder fehlende CollisionMeshes auflisten");
	}

	@Override
	public String getDescription() {
		return I.tr("Erstellt eine Liste aller nicht verwendeten oder fehlende CollisionMeshes.");
	}

	@Override
	public boolean execute(IScriptEnvironment env) {
		Map<String, File> meshes = listCollisionMeshes(env, FileManager.RP_COMPILED_PHYSIC);
		meshes.putAll(listCollisionMeshes(env, FileManager.RP_COMPILED_MESH));

		Set<String> unseenMeshes = new HashSet<>(meshes.keySet());
		Set<String> missingMeshes = new TreeSet<>();

		Consumer<String> meshConsumer = mesh -> {
			unseenMeshes.remove(mesh.toLowerCase());
			if (!meshes.containsKey(mesh.toLowerCase())) {
				missingMeshes.add(mesh);
			}
		};

		Consumer<eCEntity> containerConsumer = entity -> {
			eCCollisionShape_PS collision = entity.getClass(CD.eCCollisionShape_PS.class);
			if (collision == null) {
				return;
			}

			for (eCCollisionShape shape : collision.getShapes()) {
				if (!(shape.getShape() instanceof FileShape fileShape)) {
					continue;
				}

				String mesh = fileShape.getShapeFile();
				meshConsumer.accept(mesh);

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
						meshConsumer.accept(mesh.replace(".xnvmsh", scalingFormatted + ".xnvmsh"));
					}
				}
			}
		};

		ArchiveFileIterator worldFilesIterator = env.getFileManager().worldFilesIterator();
		while (worldFilesIterator.hasNext()) {
			ArchiveFile aFile = worldFilesIterator.next();
			aFile.forEach(containerConsumer);
		}

		TemplateFileIterator tpleFilesIterator = env.getFileManager().templateFilesIterator();
		while (tpleFilesIterator.hasNext()) {
			TemplateFile aFile = tpleFilesIterator.next();
			containerConsumer.accept(aFile.getReferenceHeader());
		}

		env.log("=== " + I.tr("Unused collision meshes") + " ===");
		StreamEx.of(unseenMeshes).map(meshes::get).map(File::getName).sorted().forEach(env::log);

		env.log("\n\n=== " + I.tr("Missing collision meshes") + " ===");
		missingMeshes.forEach(env::log);

		env.log("\n\n=== " + I.tr("Used collision meshes") + " ===");
		StreamEx.ofValues(meshes, Predicates.not(unseenMeshes::contains)).map(File::getName).sorted().forEach(env::log);

		return true;
	}

	public Map<String, File> listCollisionMeshes(IScriptEnvironment env, String relativePath) {
		return env.getFileManager().listFiles(relativePath, (file) -> file.getName().endsWith(".xnvmsh")).stream()
				.collect(Collectors.toMap(f -> f.getName().toLowerCase(), Function.identity()));
	}
}

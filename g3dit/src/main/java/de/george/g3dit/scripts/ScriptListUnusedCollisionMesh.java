package de.george.g3dit.scripts;

import java.nio.file.Path;
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
import de.george.g3utils.util.FilesEx;
import de.george.g3utils.util.PathFilter;
import de.george.lrentnode.archive.ArchiveFile;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.classes.eCCollisionShape;
import de.george.lrentnode.classes.eCCollisionShape.FileShape;
import de.george.lrentnode.classes.eCCollisionShape_PS;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.iterator.ArchiveFileIterator;
import de.george.lrentnode.iterator.TemplateFileIterator;
import de.george.lrentnode.template.TemplateFile;
import de.george.lrentnode.util.EntityUtil;
import one.util.streamex.StreamEx;

public class ScriptListUnusedCollisionMesh implements IScript {

	@Override
	public String getTitle() {
		return I.tr("List unused or missing collision meshes");
	}

	@Override
	public String getDescription() {
		return I.tr("Creates a list of all unused or missing collision meshes.");
	}

	@Override
	public boolean execute(IScriptEnvironment env) {
		Map<String, Path> meshes = listCollisionMeshes(env, FileManager.RP_COMPILED_PHYSIC);
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
				EntityUtil.getScaledCollisionMesh(entity, mesh, shapeType).ifPresent(meshConsumer::accept);
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
		StreamEx.of(unseenMeshes).map(meshes::get).map(FilesEx::getFileName).sorted().forEach(env::log);

		env.log("\n\n=== " + I.tr("Missing collision meshes") + " ===");
		missingMeshes.forEach(env::log);

		env.log("\n\n=== " + I.tr("Used collision meshes") + " ===");
		StreamEx.ofValues(meshes, Predicates.not(unseenMeshes::contains)).map(FilesEx::getFileName).sorted().forEach(env::log);

		return true;
	}

	public Map<String, Path> listCollisionMeshes(IScriptEnvironment env, String relativePath) {
		return env.getFileManager().listFiles(relativePath, PathFilter.withExt("xnvmsh")).stream()
				.collect(Collectors.toMap(FilesEx::getFileNameLowerCase, Function.identity()));
	}
}

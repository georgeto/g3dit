package de.george.g3dit.scripts;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;
import com.teamunify.i18n.I;

import de.george.g3utils.util.FilesEx;
import de.george.lrentnode.archive.ArchiveFile;
import de.george.lrentnode.archive.G3ClassContainer;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.classes.eCResourceMeshLoD_PS;
import de.george.lrentnode.classes.eCVisualAnimation_PS;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.iterator.ArchiveFileIterator;
import de.george.lrentnode.iterator.TemplateFileIterator;
import de.george.lrentnode.template.TemplateFile;
import de.george.lrentnode.util.EntityUtil;
import de.george.lrentnode.util.FileUtil;

public class ScriptListUnusedMeshes implements IScript {

	@Override
	public String getTitle() {
		return I.tr("List unused meshes");
	}

	@Override
	public String getDescription() {
		return I.tr("Creates a list of all unused meshes.");
	}

	@Override
	public boolean execute(IScriptEnvironment env) {
		Map<String, Path> meshes = env.getFileManager().listStaticMeshes().stream()
				.collect(Collectors.toMap(FilesEx::getFileNameLowerCase, f -> f));

		meshes.putAll(env.getFileManager().listAnimatedMeshes().stream().collect(Collectors.toMap(FilesEx::getFileNameLowerCase, f -> f)));

		List<Path> lodMeshes = env.getFileManager().listLodMeshes();

		meshes.putAll(lodMeshes.stream().collect(Collectors.toMap(FilesEx::getFileNameLowerCase, f -> f)));

		for (Path lodMeshFile : lodMeshes) {
			try {
				eCResourceMeshLoD_PS lodMesh = FileUtil.openLodMesh(lodMeshFile);
				lodMesh.getMeshes().forEach(m -> meshes.remove(m.toLowerCase()));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}

		Consumer<String> meshConsumer = mesh -> {
			if (mesh != null) {
				meshes.remove(EntityUtil.cleanAnimatedMeshName(mesh.toLowerCase()));
			}
		};

		Consumer<G3ClassContainer> containerConsumer = container -> {
			meshConsumer.accept(EntityUtil.getMesh(container).orElse(null));

			eCVisualAnimation_PS animation = EntityUtil.getAnimatedMeshClass(container);
			if (animation != null) {
				meshConsumer.accept(animation.fxaSlot.fxaFile);
				meshConsumer.accept(animation.fxaSlot.fxaFile2);
				meshConsumer.accept(animation.property(CD.eCVisualAnimation_PS.ResourceFilePath).getString());
				meshConsumer.accept(animation.property(CD.eCVisualAnimation_PS.FacialAnimFilePath).getString());
			}
		};

		ArchiveFileIterator worldFilesIterator = env.getFileManager().worldFilesIterator();
		while (worldFilesIterator.hasNext()) {
			ArchiveFile aFile = worldFilesIterator.next();
			for (eCEntity entity : aFile.getEntities()) {
				containerConsumer.accept(entity);
			}
		}

		TemplateFileIterator tpleFilesIterator = env.getFileManager().templateFilesIterator();
		while (tpleFilesIterator.hasNext()) {
			TemplateFile aFile = tpleFilesIterator.next();
			containerConsumer.accept(aFile.getReferenceHeader());
		}

		env.log("----------\n" + I.tr("Overview") + "\n----------");

		String path = null;
		for (Path e : Sets.newTreeSet(meshes.values())) {
			String tempPath = FilesEx.getAbsolutePath(e).replaceFirst(".*\\\\_compiled", "_compiled");
			tempPath = tempPath.substring(0, tempPath.lastIndexOf("\\"));

			if (path == null || !path.equals(tempPath)) {
				path = tempPath;
				env.log("\n" + path);
			}

			env.log("\t" + e.getFileName());
		}

		env.log("\n\n----------\n" + I.tr("List") + "\n----------");
		Sets.newTreeSet(meshes.values()).forEach(e -> env.log(FilesEx.getAbsolutePath(e)));

		return true;
	}

}

package de.george.g3dit.scripts;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import com.teamunify.i18n.I;

import de.george.g3dit.util.FileManager;
import de.george.g3utils.structure.bCBox;
import de.george.g3utils.util.IOUtils;
import de.george.lrentnode.archive.ArchiveFile;
import de.george.lrentnode.archive.ArchiveFile.ArchiveType;
import de.george.lrentnode.classes.eCGeometrySpatialContext;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.iterator.ArchiveFileIterator;
import de.george.lrentnode.util.FileUtil;

public class ScriptUpdateContextBox implements IScript {

	@Override
	public String getTitle() {
		return I.tr("Update ContextBox");
	}

	@Override
	public String getDescription() {
		return I.tr("Update ContextBox of eCEntityDynamicContexts and eCGeometrySpatialContexts in primary data folder.");
	}

	@Override
	public boolean execute(IScriptEnvironment env) {
		ArchiveFileIterator worldFilesIterator = new ArchiveFileIterator(
				env.getFileManager().listPrimaryFiles(FileManager.RP_PROJECTS_COMPILED, IOUtils.archiveFileFilter));
		while (worldFilesIterator.hasNext()) {
			ArchiveFile aFile = worldFilesIterator.next();
			if (aFile.getArchiveType() == ArchiveType.Lrentdat) {
				try {
					aFile.save(worldFilesIterator.nextFile());
				} catch (IOException e) {
					env.log(e.getMessage());
				}
			} else {
				try {
					File geometryLayerDat = new File(IOUtils.changeExtension(worldFilesIterator.nextFile().getAbsolutePath(), "lrgeodat"));
					bCBox newContextBox = aFile.getGraph() != null ? aFile.getGraph().getWorldTreeBoundary() : new bCBox();

					eCGeometrySpatialContext lrgeodat;
					if (geometryLayerDat.exists()) {
						lrgeodat = FileUtil.openLrgeodat(geometryLayerDat);
					} else {
						Optional<File> origGeometryLayerDat = env.getEditorContext().getFileManager()
								.moveFromPrimaryToSecondary(geometryLayerDat);
						if (origGeometryLayerDat.map(File::exists).orElse(false)) {
							lrgeodat = FileUtil.openLrgeodat(origGeometryLayerDat.get());
						} else {
							lrgeodat = FileUtil.createLrgeodat();
						}
					}

					bCBox contextBox = lrgeodat.property(CD.eCContextBase.ContextBox);
					if (!contextBox.isEqual(newContextBox)) {
						lrgeodat.setPropertyData(CD.eCContextBase.ContextBox, newContextBox);
						FileUtil.saveLrgeodat(lrgeodat, geometryLayerDat);
					}
				} catch (IOException e) {
					env.log(e.getMessage());
				}
			}
		}
		return true;
	}
}

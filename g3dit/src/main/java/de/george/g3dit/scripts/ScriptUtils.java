package de.george.g3dit.scripts;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.BiFunction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.teamunify.i18n.I;

import de.george.g3dit.util.FileDialogWrapper;
import de.george.g3dit.util.FileManager;
import de.george.g3dit.util.StringtableHelper;
import de.george.g3utils.structure.GuidUtil;
import de.george.g3utils.util.IOUtils;
import de.george.lrentnode.archive.ArchiveFile;
import de.george.lrentnode.iterator.ArchiveFileIterator;
import one.util.streamex.StreamEx;

public class ScriptUtils {
	private static final Logger logger = LoggerFactory.getLogger(ScriptUtils.class);

	public static final boolean processAndSaveWorldFiles(IScriptEnvironment env, BiFunction<ArchiveFile, Path, Integer> processor,
			String formatString) {
		Path saveDir = FileDialogWrapper.chooseDirectory(I.tr("Select save path"), env.getParentWindow());
		if (saveDir == null) {
			return false;
		}

		int totalChanged = 0;
		ArchiveFileIterator worldFilesIterator = env.getFileManager().worldFilesIterator();
		while (worldFilesIterator.hasNext()) {
			ArchiveFile archive = worldFilesIterator.next();
			Path file = worldFilesIterator.nextFile();
			int changed = processor.apply(archive, file);

			// Es wurden Meshes gelöscht
			if (changed > 0) {
				totalChanged += changed;
				archive.getGraph().updateGeometry();
				StringtableHelper.clearStringtableSafe(archive.getEntities().toList(), archive.getStringtable(), true,
						env.getParentWindow());

				FileManager fileManager = env.getFileManager();
				Optional<String> relativePath = fileManager.getRelativePath(file);
				if (!relativePath.isPresent()) {
					env.log(I.trf("Relative path of {0} could not be determined, applying changes not possible.", file.toAbsolutePath()));
					continue;
				}
				try {
					Files.createDirectories(saveDir);
					archive.save(saveDir.resolve(relativePath.get()));
				} catch (IOException e) {
					env.log(I.trf("Failed to save {0}: {1}", file.toAbsolutePath(), e.getMessage()));
					logger.warn("Error while saving file {}.", file.toAbsolutePath(), e);
				}
			}
		}
		env.log(I.format(formatString, totalChanged));
		return true;
	}

	public static final StreamEx<String> extractGuids(String raw) {
		return StreamEx.split(raw, "\r\n|\n|\\s+|\"").map(GuidUtil::parseGuid).nonNull();
	}

	public static final StreamEx<String> extractGuidFromClipboard() {
		return extractGuids(IOUtils.getClipboardContent());
	}
}

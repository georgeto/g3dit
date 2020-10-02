package de.george.g3dit.scripts;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.function.BiFunction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	public static final boolean processAndSaveWorldFiles(IScriptEnvironment env, BiFunction<ArchiveFile, File, Integer> processor,
			String formatString) {
		File saveDir = FileDialogWrapper.chooseDirectory("Speicherpfad auswählen", env.getParentWindow());
		if (saveDir == null) {
			return false;
		}

		int totalChanged = 0;
		ArchiveFileIterator worldFilesIterator = env.getFileManager().worldFilesIterator();
		while (worldFilesIterator.hasNext()) {
			ArchiveFile archive = worldFilesIterator.next();
			File file = worldFilesIterator.nextFile();
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
					env.log("Relativer Pfad von " + file.getAbsolutePath()
							+ " konnte nicht ermittelt werden, übernehmen der Änderungen nicht möglich.");
					continue;
				}
				try {
					File out = new File(saveDir, relativePath.get());
					out.getParentFile().mkdirs();
					archive.save(out);
				} catch (IOException e) {
					env.log("Speichern von " + file.getAbsolutePath() + " fehlgeschlagen: " + e.getMessage());
					logger.warn("Speichern fehlgeschlagen", e);
				}
			}
		}
		env.log(String.format(formatString, totalChanged));
		return true;
	}

	public static final StreamEx<String> extractGuids(String raw) {
		return StreamEx.split(raw, "\r\n|\n|\\s+|\"").map(GuidUtil::parseGuid).nonNull();
	}

	public static final StreamEx<String> extractGuidFromClipboard() {
		return extractGuids(IOUtils.getClipboardContent());
	}
}

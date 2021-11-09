package de.george.g3dit.scripts;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.teamunify.i18n.I;

import de.george.lrentnode.archive.ArchiveFile;
import de.george.lrentnode.template.TemplateFile;
import de.george.lrentnode.util.FileUtil;

public class ScriptTestReadSaveIntegrity implements IScript {
	private static final Logger logger = LoggerFactory.getLogger(ScriptTestReadSaveIntegrity.class);

	@Override
	public String getTitle() {
		return I.tr("Test for changes during loading and saving");
	}

	@Override
	public String getDescription() {
		return I.tr(
				"Checks which files change when they are read in and then saved again. No changes are made on the disk, the check is done in memory.");
	}

	@Override
	public boolean execute(IScriptEnvironment env) {
		int totalArchiveFiles = 0;
		for (File file : env.getFileManager().listWorldFiles()) {
			try {
				ArchiveFile aFile = FileUtil.openArchive(file, false);
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				try {
					aFile.save(out);

					byte[] inBytes = Files.readAllBytes(Paths.get(file.toURI()));
					if (!Arrays.equals(inBytes, out.toByteArray())) {
						env.log(file.getAbsolutePath());
					}
				} catch (IOException e) {
					logger.warn("Error while comparing archive file.", e);
					env.log(I.trf("Error while comparing: {0}", e.getMessage()));
				}
			} catch (IOException e) {
				logger.warn("Error while loading archive file.", e);
				env.log(I.trf("Error while loading: {0}", e.getMessage()));
			}
			totalArchiveFiles++;
		}
		env.log(totalArchiveFiles + " .node/.lrentdat überprüft.");

		int totalTemplateFiles = 0;
		for (File file : env.getFileManager().listTemplateFiles()) {
			try {
				TemplateFile aFile = FileUtil.openTemplate(file);
				try {
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					aFile.save(out);

					byte[] inBytes = Files.readAllBytes(Paths.get(file.toURI()));
					if (!Arrays.equals(inBytes, out.toByteArray())) {
						env.log(file.getAbsolutePath());
					}
				} catch (IOException e) {
					logger.warn("Error while comparing template file.", e);
					env.log(I.trf("Error while comparing: {0}", e.getMessage()));
				}
			} catch (IOException e) {
				logger.warn("Error while loading template file.", e);
				env.log(I.trf("Error while loading: {0}", e.getMessage()));
			}
			totalTemplateFiles++;
		}
		env.log(I.trf("{0} .tple checked.", totalTemplateFiles));

		return true;
	}
}

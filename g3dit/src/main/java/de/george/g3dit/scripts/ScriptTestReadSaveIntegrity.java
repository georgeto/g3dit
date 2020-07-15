package de.george.g3dit.scripts;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.george.lrentnode.archive.ArchiveFile;
import de.george.lrentnode.template.TemplateFile;
import de.george.lrentnode.util.FileUtil;

public class ScriptTestReadSaveIntegrity implements IScript {
	private static final Logger logger = LoggerFactory.getLogger(ScriptTestReadSaveIntegrity.class);

	@Override
	public String getTitle() {
		return "Teste auf Veränderungen beim Laden und Speichern";
	}

	@Override
	public String getDescription() {
		return "Überprüft welche Dateien sich ändern, wenn sie eigelesen und anschließend wieder gespeichert werden. Es werden keine Änderungen auf dem Datenträger vorgenommen, die Überprüfung geschieht im Arbeitsspeicher.";
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
					logger.warn("Fehler beim Vergleich", e);
					env.log("Fehler beim Vergleich: " + e.getMessage());
				}
			} catch (IOException e) {
				logger.warn("Fehler beim Laden", e);
				env.log("Fehler beim Laden: " + e.getMessage());
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
					logger.warn("Fehler beim Vergleich", e);
					env.log("Fehler beim Vergleich: " + e.getMessage());
				}
			} catch (IOException e) {
				logger.warn("Fehler beim Laden", e);
				env.log("Fehler beim Laden: " + e.getMessage());
			}
			totalTemplateFiles++;
		}
		env.log(totalTemplateFiles + " .tple überprüft.");

		return true;
	}
}

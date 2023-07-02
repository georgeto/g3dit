package de.george.g3dit.scripts;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.teamunify.i18n.I;

import de.george.g3dit.settings.BooleanOptionHandler;
import de.george.g3dit.settings.LambdaOption;
import de.george.g3dit.settings.Option;
import de.george.g3dit.settings.OptionPanel;
import de.george.g3dit.util.FileDialogWrapper;
import de.george.g3dit.util.json.JsonUtil;
import de.george.g3utils.util.FilesEx;
import de.george.g3utils.util.IOUtils;
import de.george.lrentnode.effect.gCSVMManager;
import one.util.streamex.StreamEx;

public class ScriptDumpSvmAdmin implements IScript {
	private static final Logger logger = LoggerFactory.getLogger(ScriptDumpSvmAdmin.class);

	private static final Option<Boolean> AS_JSON = new LambdaOption<>(false, (parent) -> new BooleanOptionHandler(parent, "Json"),
			"ScriptDumpSvmAdmin.AS_JSON", "Json");

	@Override
	public String getTitle() {
		return I.tr("Export SVMAdmin.dat");
	}

	@Override
	public String getDescription() {
		return I.tr("Converts the SVMAdmin.dat into a text file.");
	}

	@Override
	public boolean execute(IScriptEnvironment env) {
		Path loadFile = FileDialogWrapper.openFile(I.tr("Open SVMAdmin.dat..."), env.getParentWindow());
		if (loadFile == null) {
			return false;
		}

		Path saveFile;
		boolean asJson = env.getOption(AS_JSON).booleanValue();
		if (!asJson) {
			saveFile = FileDialogWrapper.saveFile(I.tr("Save SVMAdmin.dat in text form"),
					FilesEx.changeExtension(FilesEx.getFileName(loadFile), "txt"), env.getParentWindow(), FileDialogWrapper.TXT_FILTER);
		} else {
			saveFile = FileDialogWrapper.saveFile(I.tr("Save SVMAdmin.dat as Json"),
					FilesEx.changeExtension(FilesEx.getFileName(loadFile), "json"), env.getParentWindow(), FileDialogWrapper.JSON_FILTER);
		}

		if (saveFile == null) {
			return false;
		}

		try {
			gCSVMManager manager = new gCSVMManager(loadFile);
			if (!asJson) {
				List<String> lines = new ArrayList<>();
				lines.add("Voices:");
				StreamEx.of(manager.voiceList.entrySet()).sortedBy(Map.Entry::getKey)
						.map(e -> "  " + e.getKey() + ": " + e.getValue().entries.stream().sorted().collect(Collectors.joining(", ")))
						.forEach(lines::add);
				lines.add("");
				lines.add("Block:");
				StreamEx.of(manager.blockList.entrySet()).sortedBy(Map.Entry::getKey)
						.map(e -> "  " + e.getKey() + ": "
								+ e.getValue().entries.stream().map(ed -> ed.id).sorted().collect(Collectors.joining(", ")))
						.forEach(lines::add);
				IOUtils.writeTextFile(lines, saveFile, StandardCharsets.UTF_8);
			} else {
				JsonUtil.fieldAutodetectMapper().disable(SerializationFeature.FAIL_ON_EMPTY_BEANS).writerWithDefaultPrettyPrinter()
						.writeValue(saveFile.toFile(), manager);
			}
		} catch (IOException e) {
			logger.warn("Error during export.", e);
			env.log(I.trf("Error during export: {0}", e.getMessage()));

		}

		return true;
	}

	@Override
	public void installOptions(OptionPanel optionPanel) {
		optionPanel.addOption(AS_JSON);
	}
}

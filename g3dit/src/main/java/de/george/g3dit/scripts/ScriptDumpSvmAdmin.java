package de.george.g3dit.scripts;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.SerializationFeature;

import de.george.g3dit.settings.BooleanOptionHandler;
import de.george.g3dit.settings.LambdaOption;
import de.george.g3dit.settings.Option;
import de.george.g3dit.settings.OptionPanel;
import de.george.g3dit.util.FileDialogWrapper;
import de.george.g3dit.util.JsonUtil;
import de.george.g3utils.util.IOUtils;
import de.george.lrentnode.effect.gCSVMManager;
import one.util.streamex.StreamEx;

public class ScriptDumpSvmAdmin implements IScript {
	private static final Logger logger = LoggerFactory.getLogger(ScriptDumpSvmAdmin.class);

	private static final Option<Boolean> AS_JSON = new LambdaOption<>(false, (parent) -> new BooleanOptionHandler(parent, "Json"),
			"ScriptDumpSvmAdmin.AS_JSON", "Json");

	@Override
	public String getTitle() {
		return "SVMAdmin.dat exportieren";
	}

	@Override
	public String getDescription() {
		return "Wandelt die SVMAdmin.dat in eine Textdatei um.";
	}

	@Override
	public boolean execute(IScriptEnvironment env) {
		File loadFile = FileDialogWrapper.openFile("SVMAdmin.dat öffnen...", env.getParentWindow());
		if (loadFile == null) {
			return false;
		}

		File saveFile;
		boolean asJson = env.getOption(AS_JSON).booleanValue();
		if (!asJson) {
			saveFile = FileDialogWrapper.saveFile("SVMAdmin.dat in Textform speichern", IOUtils.changeExtension(loadFile.getName(), "txt"),
					env.getParentWindow(), FileDialogWrapper.TXT_FILTER);
		} else {
			saveFile = FileDialogWrapper.saveFile("SVMAdmin.dat als Json speichern", IOUtils.changeExtension(loadFile.getName(), "json"),
					env.getParentWindow(), FileDialogWrapper.JSON_FILTER);
		}

		if (saveFile == null) {
			return false;
		}

		try {
			gCSVMManager manager = new gCSVMManager(loadFile);
			if (!asJson) {
				List<String> lines = new ArrayList<>();
				lines.add("Voices:");
				StreamEx.of(manager.voiceList.entrySet()).sortedBy(e -> e.getKey())
						.map(e -> "  " + e.getKey() + ": " + e.getValue().entries.stream().sorted().collect(Collectors.joining(", ")))
						.forEach(lines::add);
				lines.add("");
				lines.add("Block:");
				StreamEx.of(manager.blockList.entrySet()).sortedBy(e -> e.getKey())
						.map(e -> "  " + e.getKey() + ": "
								+ e.getValue().entries.stream().map(ed -> ed.id).sorted().collect(Collectors.joining(", ")))
						.forEach(lines::add);
				IOUtils.writeTextFile(lines, saveFile, StandardCharsets.UTF_8);
			} else {
				JsonUtil.fieldAutodetectMapper().disable(SerializationFeature.FAIL_ON_EMPTY_BEANS).writerWithDefaultPrettyPrinter()
						.writeValue(saveFile, manager);
			}
		} catch (IOException e) {
			logger.warn("Fehler beim Export.", e);
			env.log("Fehler beim Export:" + e.getMessage());

		}

		return true;
	}

	@Override
	public void installOptions(OptionPanel optionPanel) {
		optionPanel.addOption(AS_JSON);
	}
}

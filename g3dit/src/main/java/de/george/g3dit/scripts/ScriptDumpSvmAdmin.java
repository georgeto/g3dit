package de.george.g3dit.scripts;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
import de.george.g3utils.util.IOUtils;
import de.george.lrentnode.effect.gCSVMManager;
import one.util.streamex.StreamEx;

public class ScriptDumpSvmAdmin implements IScript {
	private static final Logger logger = LoggerFactory.getLogger(ScriptDumpSvmAdmin.class);

	private static final Option<Boolean> AS_JSON = new LambdaOption<>(false, (parent) -> new BooleanOptionHandler(parent, "Json"),
			"ScriptDumpSvmAdmin.AS_JSON", "Json");

	@Override
	public String getTitle() {
		return I.tr("SVMAdmin.dat exportieren");
	}

	@Override
	public String getDescription() {
		return I.tr("Wandelt die SVMAdmin.dat in eine Textdatei um.");
	}

	@Override
	public boolean execute(IScriptEnvironment env) {
		File loadFile = FileDialogWrapper.openFile(I.tr("SVMAdmin.dat öffnen..."), env.getParentWindow());
		if (loadFile == null) {
			return false;
		}

		File saveFile;
		boolean asJson = env.getOption(AS_JSON).booleanValue();
		if (!asJson) {
			saveFile = FileDialogWrapper.saveFile(I.tr("SVMAdmin.dat in Textform speichern"),
					IOUtils.changeExtension(loadFile.getName(), "txt"), env.getParentWindow(), FileDialogWrapper.TXT_FILTER);
		} else {
			saveFile = FileDialogWrapper.saveFile(I.tr("SVMAdmin.dat als Json speichern"),
					IOUtils.changeExtension(loadFile.getName(), "json"), env.getParentWindow(), FileDialogWrapper.JSON_FILTER);
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
						.writeValue(saveFile, manager);
			}
		} catch (IOException e) {
			logger.warn("Error during export.", e);
			env.log(I.trf("Fehler beim Export: {0}", e.getMessage()));

		}

		return true;
	}

	@Override
	public void installOptions(OptionPanel optionPanel) {
		optionPanel.addOption(AS_JSON);
	}
}

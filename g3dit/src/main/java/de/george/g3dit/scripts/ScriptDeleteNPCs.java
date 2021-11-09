package de.george.g3dit.scripts;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.base.Splitter;
import com.teamunify.i18n.I;

import de.george.g3dit.settings.LambdaOption;
import de.george.g3dit.settings.Option;
import de.george.g3dit.settings.OptionPanel;
import de.george.g3dit.settings.TextFieldOptionHandler;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.classes.desc.CD;

public class ScriptDeleteNPCs implements IScript {
	private static final Option<String> WHITELIST = new LambdaOption<>("",
			(parent) -> new TextFieldOptionHandler(parent, I.tr("Whitelist"), I.tr("List of NPC names separated with commas")),
			"ScriptDeleteNPCs.WHITELIST", I.tr("Whitelist"));

	@Override
	public String getTitle() {
		return I.tr("Delete NPCs");
	}

	@Override
	public String getDescription() {
		return I.tr("Deletes all NPCs from the world data.");
	}

	@Override
	public boolean execute(IScriptEnvironment env) {
		Set<String> whitelist = Splitter.on(',').omitEmptyStrings().trimResults().splitToStream(env.getOption(WHITELIST))
				.collect(Collectors.toSet());

		return ScriptUtils.processAndSaveWorldFiles(env, (archive, file) -> {
			List<eCEntity> toBeDeleted = new ArrayList<>();
			for (eCEntity entity : archive.getEntities()) {
				if (entity.hasClass(CD.gCNPC_PS.class) && !entity.getName().equals("PC_Hero") && !whitelist.contains(entity.getName())) {
					toBeDeleted.add(entity);
				}
			}
			toBeDeleted.forEach(e -> e.removeFromParent(false));
			return toBeDeleted.size();
		}, I.tr("A total of {0} NPCs were removed."));
	}

	@Override
	public void installOptions(OptionPanel optionPanel) {
		optionPanel.addOption(WHITELIST);
	}

}

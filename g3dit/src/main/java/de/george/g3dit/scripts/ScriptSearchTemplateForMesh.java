package de.george.g3dit.scripts;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.IntStream;

import com.ezware.dialog.task.TaskDialogs;
import com.google.common.base.Strings;
import com.teamunify.i18n.I;

import de.george.g3utils.util.Pair;
import de.george.lrentnode.iterator.TemplateFileIterator;
import de.george.lrentnode.template.TemplateEntity;
import de.george.lrentnode.template.TemplateFile;
import de.george.lrentnode.util.EntityUtil;

public class ScriptSearchTemplateForMesh implements IScript {

	@Override
	public String getTitle() {
		return I.tr("Search templates for mesh");
	}

	@Override
	public String getDescription() {
		return I.tr("Find templates that use a specified mesh plus MaterialSwitch combination(s).");
	}

	@Override
	public boolean execute(IScriptEnvironment env) {
		String meshRaw = TaskDialogs.input(env.getParentWindow(), I.tr("Mesh"),
				I.tr("Name of the mesh.\nIt is also possible to enter only a part of a mesh name."), "");
		if (Strings.isNullOrEmpty(meshRaw)) {
			return false;
		}

		String mesh = EntityUtil.cleanAnimatedMeshName(meshRaw.toLowerCase());

		String materialSwitchesRaw = TaskDialogs.input(env.getParentWindow(), I.tr("MaterialSwitches"),
				I.tr("List of MaterialSwitches or MaterialSwitch ranges separated by spaces (e.g. 4-12).\n"
						+ "If the input is empty, all MaterialSwitches are accepted."),
				"");
		if (Objects.isNull(materialSwitchesRaw)) {
			return false;
		}

		Set<Integer> materialSwitches = new HashSet<>();
		if (!materialSwitchesRaw.isEmpty()) {
			for (String entry : materialSwitchesRaw.split(" ")) {
				try {
					if (entry.contains("-")) {
						String[] split = entry.split("-", 2);
						int start = Integer.parseInt(split[0]);
						int end = Integer.parseInt(split[1]);
						IntStream.rangeClosed(start, end).forEach(materialSwitches::add);
					} else {
						materialSwitches.add(Integer.valueOf(entry));
					}
				} catch (NumberFormatException e) {
					env.log(e.getMessage());
					return false;
				}
			}
		}

		env.log(I.tr("Mesh") + ": " + meshRaw);
		env.log(I.tr("MaterialSwitches") + ": " + (materialSwitchesRaw.isEmpty() ? I.tr("Any") : materialSwitchesRaw));

		TemplateFileIterator tpleFilesIterator = env.getFileManager().templateFilesIterator();
		while (tpleFilesIterator.hasNext()) {
			TemplateFile tpleFile = tpleFilesIterator.next();
			TemplateEntity tple = tpleFile.getReferenceHeader();
			if (tple != null) {
				Pair<String, Integer> mam = EntityUtil.getMeshAndMaterialSwitch(tple).orElse(null);
				if (mam != null) {
					String tpleMesh = EntityUtil.cleanAnimatedMeshName(mam.el0());
					if (tpleMesh.toLowerCase().contains(mesh) && (materialSwitches.isEmpty() || materialSwitches.contains(mam.el1()))) {
						env.log(tple.getName() + " (" + tpleMesh + ", " + mam.el1() + ")");
					}
				}
			}
		}

		return true;
	}
}

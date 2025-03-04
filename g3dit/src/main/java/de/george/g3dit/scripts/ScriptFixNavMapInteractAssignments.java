package de.george.g3dit.scripts;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.teamunify.i18n.I;

import de.george.g3dit.util.FileDialogWrapper;
import de.george.g3utils.io.G3FileReaderEx;
import de.george.g3utils.util.FilesEx;
import de.george.g3utils.util.ReflectionUtils;
import de.george.navmap.sections.NavMap;
import de.george.navmap.sections.Section3e;
import de.george.navmap.sections.Section3e.AssignZoneToInteractable;

public class ScriptFixNavMapInteractAssignments implements IScript {

	@Override
	public String getTitle() {
		return I.tr("Fix corrupted NavMap interact object assignments");
	}

	@Override
	public String getDescription() {
		return I.tr(
				"The NavMap distinguishes whether an interact object is outside any nav area or in a nav area but blocked by a NegZone or NegCircle. "
						+ "g3dit before version 1.13.2 did not make this distinction, and instead handled interact object outside any nav area, as if they were not registered in the NavMap."
						+ "That resulted in duplicate entries for interact objects.");
	}

	@Override
	public boolean execute(IScriptEnvironment env) {
		Path loadFile = FileDialogWrapper.openFile(I.tr("Open NavigationMap..."), env.getParentWindow(), FileDialogWrapper.XNAV_FILTER);
		if (loadFile == null)
			return false;

		NavMap navMap;
		try (G3FileReaderEx reader = new G3FileReaderEx(loadFile)) {
			navMap = new NavMap(reader);
		} catch (Exception e) {
			env.log(e.getMessage());
			return false;
		}

		Section3e sec3e = ReflectionUtils.getFieldValue(ReflectionUtils.getField(NavMap.class, "sec3e"), navMap);

		Set<Integer> toRemove = new HashSet<>();
		for (int i = 0; i < sec3e.interactables.size(); i++) {
			AssignZoneToInteractable assign = sec3e.interactables.get(i);
			if (assign.interactable.getGuid() == null || assign.area.getGuid() != null)
				continue;

			for (int j = i + 1; j < sec3e.interactables.size(); j++) {
				AssignZoneToInteractable other = sec3e.interactables.get(j);
				if (other.interactable.getGuid() == null || other.area.getGuid() == assign.area.getGuid())
					continue;
				if (assign.interactable.getGuid().equals(other.interactable.getGuid())) {
					if (other.area.getGuid() == null) {
						env.log("%s: Ignore null nav area duplicate @ %d", assign.interactable.getGuid(), j);
					} else if (other.area.getGuid().equals(NavMap.OUT_OF_NAV_AREA_ID)) {
						toRemove.add(j);
						env.log("%s: Remove out of nav area duplicate @ %d", assign.interactable.getGuid(), j);
					} else {
						toRemove.add(j);
						env.log("%s: Integrate non out of nav area duplicate @ %d", assign.interactable.getGuid(), j);
						// Integrate into the existing entry
						assign.area = other.area;
					}
					break;
				}
			}
		}

		ArrayList<Integer> sorted = new ArrayList<>(toRemove);
		Collections.sort(sorted);
		Collections.reverse(sorted);
		for (int rem : sorted) {
			sec3e.interactables.remove(rem);
		}

		Path saveFile = FileDialogWrapper.saveFile(I.tr("Save fixed NavigationMap"),
				FilesEx.stripExtension(FilesEx.getFileName(loadFile)) + "_fixed.xnav", env.getParentWindow(),
				FileDialogWrapper.XNAV_FILTER);
		if (saveFile == null)
			return false;

		try {
			navMap.save(saveFile);
		} catch (IOException e) {
			env.log(e.getMessage());
			return false;
		}
		return true;
	}
}

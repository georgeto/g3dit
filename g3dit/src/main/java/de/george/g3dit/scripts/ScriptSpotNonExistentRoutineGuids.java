package de.george.g3dit.scripts;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Joiner;
import com.teamunify.i18n.I;

import de.george.lrentnode.archive.ArchiveFile;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.classes.G3Class;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.enums.G3Enums.gESpecies;
import de.george.lrentnode.iterator.ArchiveFileIterator;
import de.george.lrentnode.properties.bCPropertyID;

public class ScriptSpotNonExistentRoutineGuids implements IScript {

	@Override
	public String getTitle() {
		return I.tr("Detect routines with non-existent guids");
	}

	@Override
	public String getDescription() {
		return I.tr("Checks the routines of all entities for non-existent guids.");
	}

	@Override
	public boolean execute(IScriptEnvironment env) {
		ArchiveFileIterator worldFilesIterator = env.getFileManager().worldFilesIterator();
		Set<String> knownGuids = new HashSet<>();
		Map<String, G3Class> entityRoutineMap = new LinkedHashMap<>();

		while (worldFilesIterator.hasNext()) {
			ArchiveFile archiveFile = worldFilesIterator.next();

			for (eCEntity entity : archiveFile.getEntities()) {
				knownGuids.add(entity.getGuid());

				if (entity.hasClass(CD.gCNavigation_PS.class) && entity.hasClass(CD.gCNPC_PS.class)) {
					int species = entity.getClass(CD.gCNPC_PS.class).property(CD.gCNPC_PS.Species).getEnumValue();
					if (species == gESpecies.gESpecies_Human || species == gESpecies.gESpecies_Orc) {
						G3Class nav = entity.getClass(CD.gCNavigation_PS.class);
						String entityIdentifier = worldFilesIterator.nextFile().getFileName() + "#" + entity + " (" + entity.getGuid()
								+ ")";
						entityRoutineMap.put(entityIdentifier, nav);
					}
				}
			}
		}

		entityRoutineMap.forEach((key, navigation) -> {

			List<String> messages = new ArrayList<>();

			// Routinen laden
			List<String> routineNames = new ArrayList<>(navigation.property(CD.gCNavigation_PS.RoutineNames).getNativeEntries());
			List<String> workingPoints = new ArrayList<>(
					navigation.property(CD.gCNavigation_PS.WorkingPoints).getEntries(bCPropertyID::getGuid));
			List<String> relaxingPoints = new ArrayList<>(
					navigation.property(CD.gCNavigation_PS.RelaxingPoints).getEntries(bCPropertyID::getGuid));
			List<String> sleepingPoints = new ArrayList<>(
					navigation.property(CD.gCNavigation_PS.SleepingPoints).getEntries(bCPropertyID::getGuid));

			// Startroutine laden
			String routineName = navigation.property(CD.gCNavigation_PS.Routine).getString();
			String workingPoint = navigation.property(CD.gCNavigation_PS.WorkingPoint).getGuid();
			String relaxingPoint = navigation.property(CD.gCNavigation_PS.RelaxingPoint).getGuid();
			String sleepingPoint = navigation.property(CD.gCNavigation_PS.SleepingPoint).getGuid();

			int index = routineNames.indexOf(routineName);
			if (index != -1) {
				if (!workingPoint.equals(workingPoints.get(index)) || !relaxingPoint.equals(relaxingPoints.get(index))
						|| !sleepingPoint.equals(sleepingPoints.get(index))) {
					messages.add(I.trf("Guids of the start routine ''{0}'' differ from those of the corresponding routine list entry.",
							routineName));
					routineNames.add(0, I.trf("{0} (Start routine)", routineName));
					workingPoints.add(0, workingPoint);
					relaxingPoints.add(0, relaxingPoint);
					sleepingPoints.add(0, sleepingPoint);
				}
			} else {
				messages.add(I.trf("Start routine ''{0}'' is not included in the routine list.", routineName));
				routineNames.add(0, I.trf("{0} (Start routine)", routineName));
				workingPoints.add(0, workingPoint);
				relaxingPoints.add(0, relaxingPoint);
				sleepingPoints.add(0, sleepingPoint);
			}

			for (int i = 0; i < routineNames.size(); i++) {
				boolean validWP = knownGuids.contains(workingPoints.get(i));
				boolean validRP = knownGuids.contains(relaxingPoints.get(i));
				boolean validSP = knownGuids.contains(sleepingPoints.get(i));
				if (!validWP || !validRP || !validSP) {
					String message = routineNames.get(i) + ": ";
					message += Joiner.on(", ").skipNulls().join(validWP ? null : "WorkingPoint", validRP ? null : "RelaxingPoint",
							validSP ? null : "SleepingPoint");
					messages.add(message);
				}
			}

			if (!messages.isEmpty()) {
				env.log(key);
				messages.stream().map(s -> "  " + s).forEach(env::log);
				env.log("");
			}
		});

		return true;
	}
}

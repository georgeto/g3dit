package de.george.g3dit.scripts;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.teamunify.i18n.I;

import de.george.lrentnode.archive.ArchiveFile;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.classes.eCVegetation_PS;
import de.george.lrentnode.classes.eCVegetation_PS.PlantRegionEntry;
import de.george.lrentnode.classes.eCVegetation_PS.eCVegetation_Grid;
import de.george.lrentnode.classes.eCVegetation_PS.eCVegetation_GridNode;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.iterator.ArchiveFileIterator;

public class ScriptCheckVegetationPosition implements IScript {

	@Override
	public String getTitle() {
		return I.tr("Check position of vegetation objects");
	}

	@Override
	public String getDescription() {
		return I.tr("Checks whether all vegetation objects are in the VegetationRoot responsible for their area.");
	}

	@Override
	public boolean execute(IScriptEnvironment env) {
		ArchiveFileIterator worldFilesIterator = env.getFileManager().worldFilesIterator();
		while (worldFilesIterator.hasNext()) {
			ArchiveFile aFile = worldFilesIterator.next();
			for (eCEntity entity : aFile.getEntities()) {
				if (entity.hasClass(CD.eCVegetation_PS.class)) {
					eCVegetation_PS veg = entity.getClass(CD.eCVegetation_PS.class);
					eCVegetation_Grid grid = veg.getGrid();

					String name = worldFilesIterator.nextFile().getName();
					Matcher matcher = Pattern.compile("G3_World_01_x(-?\\d+)y0z(-?\\d+)_CStat").matcher(name);
					if (matcher.find() && matcher.groupCount() == 2) {
						int x = Integer.valueOf(matcher.group(1));
						int z = Integer.valueOf(matcher.group(2));

						for (eCVegetation_GridNode node : grid.getGridNodes()) {
							for (PlantRegionEntry entry : node.getEntries()) {
								if (entry.position.getX() <= x - 5000 || entry.position.getX() >= x + 5000
										|| entry.position.getZ() <= z - 5000 || entry.position.getZ() >= z + 5000) {
									env.log(name + ": " + entry.position + " (" + veg.getMeshClass(entry.meshID).getName() + ")");
								}
							}
						}
					}
				}
			}
		}
		return true;
	}
}

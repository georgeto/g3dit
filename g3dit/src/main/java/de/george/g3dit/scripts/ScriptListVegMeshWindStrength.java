package de.george.g3dit.scripts;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import com.google.common.collect.Multiset;
import com.google.common.collect.TreeMultiset;
import com.teamunify.i18n.I;

import de.george.lrentnode.archive.ArchiveFile;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.classes.eCVegetation_Mesh;
import de.george.lrentnode.classes.eCVegetation_PS;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.iterator.ArchiveFileIterator;

public class ScriptListVegMeshWindStrength implements IScript {

	@Override
	public String getTitle() {
		return I.tr("WindStrength von Vegetations-Meshes auflisten");
	}

	@Override
	public String getDescription() {
		return I.tr("Erstellt einer Liste aller aller vorkommender Vegetations-Meshes, deren WindStrength variiert.");
	}

	@Override
	public boolean execute(IScriptEnvironment env) {
		Map<String, Multiset<Float>> result = new TreeMap<>();
		ArchiveFileIterator worldFilesIterator = env.getFileManager().worldFilesIterator();
		while (worldFilesIterator.hasNext()) {
			ArchiveFile aFile = worldFilesIterator.next();
			for (eCEntity entity : aFile.getEntities()) {
				if (entity.hasClass(CD.eCVegetation_PS.class)) {
					eCVegetation_PS veg = entity.getClass(CD.eCVegetation_PS.class);
					Collection<eCVegetation_Mesh> meshes = veg.getMeshClasses();

					meshes.forEach(m -> {
						String name = m.property(CD.eCVegetation_Mesh.MeshFilePath).getString();
						if (!result.containsKey(name)) {
							result.put(name, TreeMultiset.create());
						}
						result.get(name).add(m.property(CD.eCVegetation_Mesh.WindStrength).getFloat());
					});
				}
			}
		}
		result.keySet().stream().filter(k -> result.get(k).elementSet().size() != 1).forEach(k -> env.log(k + ": " + result.get(k)));
		return true;
	}
}

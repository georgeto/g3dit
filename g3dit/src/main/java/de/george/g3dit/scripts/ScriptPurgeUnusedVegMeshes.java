package de.george.g3dit.scripts;

import java.util.List;
import java.util.stream.Collectors;

import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.classes.eCVegetation_Mesh;
import de.george.lrentnode.classes.eCVegetation_PS;
import de.george.lrentnode.classes.desc.CD;

public class ScriptPurgeUnusedVegMeshes implements IScript {
	@Override
	public String getTitle() {
		return "Nicht verwendete Vegetationsmeshes löschen";
	}

	@Override
	public String getDescription() {
		return "Löscht alle nicht verwendeten Vegetationsmeshes aus den Weltdaten.";
	}

	@Override
	public boolean execute(IScriptEnvironment env) {
		return ScriptUtils.processAndSaveWorldFiles(env, (archive, file) -> {
			int removed = 0;
			for (eCEntity entity : archive.getEntities()) {
				if (entity.hasClass(CD.eCVegetation_PS.class)) {
					eCVegetation_PS veg = entity.getClass(CD.eCVegetation_PS.class);
					List<eCVegetation_Mesh> unusedMeshes = veg.getMeshClasses().stream()
							.filter(m -> veg.getMeshUseCount(m.getMeshID()) == 0).collect(Collectors.toList());
					unusedMeshes.forEach(veg::removeMeshClass);

					if (!unusedMeshes.isEmpty()) {
						env.log("Entferne " + unusedMeshes.size() + " unbenutzte Meshes in " + file.getName() + "#" + entity.getGuid());
						removed += unusedMeshes.size();
					}
				}
			}
			return removed;
		}, "Es wurden insgesamt %d unbenutzte Meshes entfernt.");
	}
}

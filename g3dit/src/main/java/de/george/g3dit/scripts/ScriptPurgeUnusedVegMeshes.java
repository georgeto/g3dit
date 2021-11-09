package de.george.g3dit.scripts;

import java.util.List;
import java.util.stream.Collectors;

import com.teamunify.i18n.I;

import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.classes.eCVegetation_Mesh;
import de.george.lrentnode.classes.eCVegetation_PS;
import de.george.lrentnode.classes.desc.CD;

public class ScriptPurgeUnusedVegMeshes implements IScript {
	@Override
	public String getTitle() {
		return I.tr("Delete unused vegetation meshes");
	}

	@Override
	public String getDescription() {
		return I.tr("Deletes all unused vegetation meshes from the world data.");
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
						env.log(I.trf("Removing {0, number} unused meshes in {1}#{2}", unusedMeshes.size(), file.getName(),
								entity.getGuid()));
						removed += unusedMeshes.size();
					}
				}
			}
			return removed;
		}, I.tr("A total of {0, number} unused meshes were removed."));
	}
}

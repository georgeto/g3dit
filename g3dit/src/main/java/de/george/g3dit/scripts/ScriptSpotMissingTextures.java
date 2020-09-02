package de.george.g3dit.scripts;

import java.io.File;
import java.io.FileInputStream;

import com.teamunify.i18n.I;

import de.george.g3dit.util.AssetResolver;
import de.george.g3dit.util.AssetResolver.MaterialAsset;
import de.george.g3dit.util.AssetResolver.TextureAsset;
import de.george.lrentnode.classes.eCResourceShaderMaterial_PS;
import de.george.lrentnode.util.FileUtil;

public class ScriptSpotMissingTextures implements IScript {

	@Override
	public String getTitle() {
		return I.tr("Fehlende Texturen ermitteln");
	}

	@Override
	public String getDescription() {
		return I.tr("Wenn eine im Material eingetragene Basistextur nicht existiert, wird diese als fehlend klassifiziert.");
	}

	@Override
	public boolean execute(IScriptEnvironment env) {
		AssetResolver assetResolver = AssetResolver.with(env.getEditorContext()).build();

		for (File file : env.getFileManager().listMaterials()) {
			try (FileInputStream is = new FileInputStream(file)) {
				eCResourceShaderMaterial_PS material = FileUtil.openMaterial(is);

				MaterialAsset parsedMaterial = assetResolver.parseMaterial(file.getName(), material, 0);
				if (!parsedMaterial.getTextures().stream().allMatch(TextureAsset::isFound)) {
					env.log(parsedMaterial.print() + "\n\n");
				}
			} catch (Exception e) {
				env.log(I.tr("Material") + ": " + file.getName());
				env.log(I.tr("Exception beim Parsen des Materials") + ": " + e.getMessage() + "\n\n");
			}
		}

		return true;
	}
}

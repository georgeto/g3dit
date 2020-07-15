package de.george.g3dit.scripts;

import java.io.File;
import java.io.FileInputStream;

import de.george.g3dit.util.AssetResolver;
import de.george.g3dit.util.AssetResolver.MaterialAsset;
import de.george.g3dit.util.AssetResolver.TextureAsset;
import de.george.g3dit.util.FileManager;
import de.george.lrentnode.classes.eCResourceShaderMaterial_PS;
import de.george.lrentnode.util.FileUtil;

public class ScriptSpotMissingTextures implements IScript {

	@Override
	public String getTitle() {
		return "Fehlende Texturen ermitteln";
	}

	@Override
	public String getDescription() {
		return "Wenn eine im Material eingetragene Basistextur nicht existiert, wird diese als fehlend klassifiziert.";
	}

	@Override
	public boolean execute(IScriptEnvironment env) {
		AssetResolver assetResolver = new AssetResolver(env.getEditorContext(), true);

		for (File file : env.getFileManager().listFiles(FileManager.RP_COMPILED_MATERIAL, (file) -> file.getName().endsWith(".xshmat"))) {
			try (FileInputStream is = new FileInputStream(file)) {
				eCResourceShaderMaterial_PS material = FileUtil.openMaterial(is);

				MaterialAsset parsedMaterial = assetResolver.parseMaterial(file.getName(), material, 0);
				if (!parsedMaterial.getTextures().stream().allMatch(TextureAsset::isFound)) {
					env.log(parsedMaterial.print() + "\n\n");
				}
			} catch (Exception e) {
				env.log("Material: " + file.getName());
				env.log("Exception beim Parsen des Materials: " + e.getMessage() + "\n\n");
			}
		}

		return true;
	}
}

package de.george.g3dit.scripts;

import java.nio.file.Path;

import com.teamunify.i18n.I;

import de.george.g3dit.util.AssetResolver;
import de.george.g3dit.util.AssetResolver.MaterialAsset;
import de.george.g3dit.util.AssetResolver.TextureAsset;
import de.george.g3utils.util.FilesEx;
import de.george.lrentnode.classes.eCResourceShaderMaterial_PS;
import de.george.lrentnode.util.FileUtil;

public class ScriptSpotMissingTextures implements IScript {

	@Override
	public String getTitle() {
		return I.tr("Detect missing textures");
	}

	@Override
	public String getDescription() {
		return I.tr("If the base texture of a material does not exist, it is classified as missing.");
	}

	@Override
	public boolean execute(IScriptEnvironment env) {
		AssetResolver assetResolver = AssetResolver.with(env.getEditorContext()).build();

		for (Path file : env.getFileManager().listMaterials()) {
			try {
				eCResourceShaderMaterial_PS material = FileUtil.openMaterial(file);

				MaterialAsset parsedMaterial = assetResolver.parseMaterial(FilesEx.getFileName(file), material, 0);
				if (!parsedMaterial.getTextures().stream().allMatch(TextureAsset::isFound)) {
					env.log(parsedMaterial.print() + "\n\n");
				}
			} catch (Exception e) {
				env.log(I.tr("Material") + ": " + file.getFileName());
				env.log(I.tr("Exception while parsing the material") + ": " + e.getMessage() + "\n\n");
			}
		}

		return true;
	}
}

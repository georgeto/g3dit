package de.george.g3dit.scripts;

import java.nio.file.Path;

import com.teamunify.i18n.I;

import de.george.lrentnode.classes.eCResourceShaderMaterial_PS;
import de.george.lrentnode.classes.eCShaderBase;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.enums.G3Enums;
import de.george.lrentnode.enums.G3Enums.eEShaderMaterialBlendMode;
import de.george.lrentnode.util.FileUtil;

public class ScriptListMaterialProperties implements IScript {

	@Override
	public String getTitle() {
		return I.tr("List material properties");
	}

	@Override
	public String getDescription() {
		return I.tr("List material properties");
	}

	@Override
	public boolean execute(IScriptEnvironment env) {
		for (Path file : env.getFileManager().listMaterials()) {
			try {
				eCResourceShaderMaterial_PS material = FileUtil.openMaterial(file);
				eCShaderBase shader = material.getShader();
				int blendMode = shader.property(CD.eCShaderBase.BlendMode).getEnumValue();
				int maskReference = shader.property(CD.eCShaderBase.MaskReference).getChar() & 0xFF;
				String useDethBias = shader.hasProperty(CD.eCShaderBase.UseDepthBias)
						? String.valueOf(shader.property(CD.eCShaderBase.UseDepthBias).isBool())
						: "-";
				env.log(file.getFileName() + ": " + G3Enums.asString(eEShaderMaterialBlendMode.class, blendMode) + ", " + maskReference
						+ ", " + useDethBias);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return true;
	}

}

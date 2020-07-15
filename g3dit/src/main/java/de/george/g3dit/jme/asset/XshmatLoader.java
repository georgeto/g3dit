
package de.george.g3dit.jme.asset;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.logging.Logger;

import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetLoadException;
import com.jme3.asset.AssetLoader;
import com.jme3.asset.AssetManager;
import com.jme3.asset.AssetNotFoundException;
import com.jme3.asset.MaterialKey;
import com.jme3.asset.TextureKey;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendEquation;
import com.jme3.material.RenderState.BlendEquationAlpha;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;

import de.george.g3utils.util.IOUtils;
import de.george.lrentnode.classes.eCColorSrcBase;
import de.george.lrentnode.classes.eCColorSrcCombiner;
import de.george.lrentnode.classes.eCColorSrcSampler;
import de.george.lrentnode.classes.eCResourceShaderMaterial_PS;
import de.george.lrentnode.classes.eCShaderBase;
import de.george.lrentnode.classes.eCShaderDefault;
import de.george.lrentnode.classes.eCShaderEllementBase;
import de.george.lrentnode.classes.eCShaderSkin;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.enums.G3Enums;
import de.george.lrentnode.enums.G3Enums.eEColorSrcSampleTexRepeat;
import de.george.lrentnode.enums.G3Enums.eEColorSrcSwitchRepeat;
import de.george.lrentnode.enums.G3Enums.eEShaderMaterialBlendMode;
import de.george.lrentnode.util.FileUtil;

public class XshmatLoader implements AssetLoader {
	private static final Logger logger = Logger.getLogger(XshmatLoader.class.getName());

	private int materialSwitch;
	private eCResourceShaderMaterial_PS sourceMaterial;

	private Material material;
	private AssetManager assetManager;

	@Override
	public Object load(AssetInfo info) throws IOException {
		if (!(info.getKey() instanceof MaterialKey)) {
			throw new IllegalArgumentException("Material assets must be loaded using a MaterialKey");
		}

		assetManager = info.getManager();
		materialSwitch = 0;
		if (info.getKey() instanceof SwitchedMaterialKey) {
			materialSwitch = ((SwitchedMaterialKey) info.getKey()).getMaterialSwitch();
		}

		logger.info("Loading: " + info.getKey().getName());

		material = new Material(info.getManager(), "CommonG3/MatDefs/Light/Lighting.j3md");
		material.setBoolean("VertexLighting", true);

		try (InputStream is = info.openStream()) {
			sourceMaterial = FileUtil.openMaterial(is);
			eCShaderBase shader = sourceMaterial.getShader();

			// TODO: Optimize Mapping from G3 BlendMode to jMonkeyEngine BlendMode
			// Masked is not handled correctly -> half transparent in screenshots
			// AlphaDiscardThreshold
			int blendMode = shader.property(CD.eCShaderBase.BlendMode).getEnumValue();
			material.setBoolean("UseAlpha", getUseAlpha(blendMode)); // Useless since ever
			material.setTransparent(getUseAlpha(blendMode));
			// Ignore alpha channel of texture, if blendmode != alpha
			material.getAdditionalRenderState().setBlendMode(getBlendMode(blendMode));
			material.getAdditionalRenderState().setBlendEquation(BlendEquation.Add);
			material.getAdditionalRenderState().setBlendEquationAlpha(BlendEquationAlpha.Max);
			if (blendMode == eEShaderMaterialBlendMode.eEShaderMaterialBlendMode_Masked) {
				float discardThreshhold = (shader.property(CD.eCShaderBase.MaskReference).getChar() & 0xFF) / 255f;
				material.setFloat("AlphaDiscardThreshold", discardThreshhold);
				logger.info(
						G3Enums.asString(eEShaderMaterialBlendMode.class, blendMode) + " -> AlphaDiscardThreshold: " + discardThreshhold);
			}

			logger.info(G3Enums.asString(eEShaderMaterialBlendMode.class, blendMode) + " -> UseAlpha: " + getUseAlpha(blendMode));
			logger.info(G3Enums.asString(eEShaderMaterialBlendMode.class, blendMode) + " -> " + getBlendMode(blendMode));

			if (shader instanceof eCShaderSkin) {
				eCShaderSkin shaderSkin = (eCShaderSkin) shader;
				shaderSkin.getElement(shaderSkin.getColorSrcDiffuse().getGuid()).ifPresent(e -> parseDiffuse((eCColorSrcBase) e));
				shaderSkin.getElement(shaderSkin.getColorSrcNormal().getGuid()).ifPresent(e -> parseNormal((eCColorSrcBase) e));
				shaderSkin.getElement(shaderSkin.getColorSrcSpecular().getGuid()).ifPresent(e -> parseSpecular((eCColorSrcBase) e));
			}

			if (shader instanceof eCShaderDefault) {
				eCShaderDefault shaderDefault = (eCShaderDefault) shader;
				shaderDefault.getElement(shaderDefault.getColorSrcDiffuse().getGuid()).ifPresent(e -> parseDiffuse((eCColorSrcBase) e));
				shaderDefault.getElement(shaderDefault.getColorSrcNormal().getGuid()).ifPresent(e -> parseNormal((eCColorSrcBase) e));
				shaderDefault.getElement(shaderDefault.getColorSrcSpecular().getGuid()).ifPresent(e -> parseSpecular((eCColorSrcBase) e));
			}
		}

		return material;
	}

	private boolean getUseAlpha(int blendMode) {
		switch (blendMode) {
			case eEShaderMaterialBlendMode.eEShaderMaterialBlendMode_Normal:
				return false;
			case eEShaderMaterialBlendMode.eEShaderMaterialBlendMode_AlphaBlend:
			case eEShaderMaterialBlendMode.eEShaderMaterialBlendMode_AlphaModulate:
			case eEShaderMaterialBlendMode.eEShaderMaterialBlendMode_Translucent:
			case eEShaderMaterialBlendMode.eEShaderMaterialBlendMode_Masked:
				return true;
			default:
				logger.warning("Unknown BlendMode '" + G3Enums.asString(eEShaderMaterialBlendMode.class, blendMode)
						+ "', setting UseAlpha = false.");
				return false;
		}
	}

	private BlendMode getBlendMode(int blendMode) {
		switch (blendMode) {
			case eEShaderMaterialBlendMode.eEShaderMaterialBlendMode_Normal:
				return BlendMode.Off;
			case eEShaderMaterialBlendMode.eEShaderMaterialBlendMode_AlphaBlend:
			case eEShaderMaterialBlendMode.eEShaderMaterialBlendMode_AlphaModulate:
			case eEShaderMaterialBlendMode.eEShaderMaterialBlendMode_Translucent:
			case eEShaderMaterialBlendMode.eEShaderMaterialBlendMode_Masked:
				return BlendMode.Alpha;
			case eEShaderMaterialBlendMode.eEShaderMaterialBlendMode_Modulate:
				return BlendMode.Modulate;
			default:
				logger.warning("Unknown BlendMode '" + G3Enums.asString(eEShaderMaterialBlendMode.class, blendMode)
						+ "', setting BlendMode = Off.");
				return BlendMode.Off;
		}
	}

	private void parseDiffuse(eCColorSrcBase data) {
		Optional<Texture> texture = parseTexture(data);
		texture.ifPresent(t -> material.setTexture("DiffuseMap", t));
	}

	private void parseNormal(eCColorSrcBase data) {
		Optional<Texture> texture = parseTexture(data);
		texture.ifPresent(t -> material.setTexture("NormalMap", t));
	}

	private void parseSpecular(eCColorSrcBase data) {
		Optional<Texture> texture = parseTexture(data);
		texture.ifPresent(t -> material.setTexture("SpecularMap", t));
	}

	private Optional<Texture> parseTexture(eCColorSrcBase data) {
		if (data instanceof eCColorSrcSampler) {
			return parseColorSampler((eCColorSrcSampler) data);
		} else if (data instanceof eCColorSrcCombiner) {
			return parseColorCombiner((eCColorSrcCombiner) data);
		} else {
			logger.warning("Unknown ColorSrc '" + data.getClass().getSimpleName() + "' has been skipped.");
		}
		return Optional.empty();
	}

	private Optional<Texture> parseColorCombiner(eCColorSrcCombiner data) {
		// TODO: Combine Samplers
		Optional<eCShaderEllementBase> colorSrc1 = sourceMaterial.getShader().getElement(data.getColorSrc1().getGuid());
		if (colorSrc1.isPresent() && colorSrc1.get() instanceof eCColorSrcSampler) {
			return parseColorSampler((eCColorSrcSampler) colorSrc1.get());
		}

		return Optional.empty();
	}

	private Optional<Texture> parseColorSampler(eCColorSrcSampler data) {
		String textureName = data.property(CD.eCColorSrcSampler.ImageFilePath).getString();
		textureName = IOUtils.stripExtension(textureName);

		// strip paths
		textureName = textureName.replaceAll(".*/", "");

		// Switched
		if (textureName.toLowerCase().endsWith("_s1") && materialSwitch != 0) {
			String baseTextureName = textureName.substring(0, textureName.length() - 1);
			int textureCount = 0;
			int switchRepeat = data.property(CD.eCColorSrcSampler.SwitchRepeat).getEnumValue();
			do {
				TextureKey textureKey = new TextureKey(baseTextureName + (textureCount + 1) + ".ximg");
				AssetInfo texture = assetManager.locateAsset(textureKey);
				if (texture == null) {
					break;
				}
				textureCount++;

				// Optimierung
				if (switchRepeat == eEColorSrcSwitchRepeat.eEColorSrcSwitchRepeat_Repeat && materialSwitch > 0
						&& materialSwitch < textureCount) {
					break;
				}
			} while (true);

			if (textureCount == 0) {
				logger.warning("Texture '" + textureName + "' could not be found.");
				return Optional.empty();
			}

			int textureIndex = 0;
			switch (switchRepeat) {
				case eEColorSrcSwitchRepeat.eEColorSrcSwitchRepeat_Repeat:
					textureIndex = materialSwitch % textureCount;
					break;

				case eEColorSrcSwitchRepeat.eEColorSrcSwitchRepeat_Clamp:
					textureIndex = materialSwitch;
					if (textureIndex < 0) {
						textureIndex = 0;
					}
					if (textureIndex > textureCount - 1) {
						textureIndex = textureCount - 1;
					}
					break;

				case eEColorSrcSwitchRepeat.eEColorSrcSwitchRepeat_PingPong:
					textureIndex = materialSwitch % textureCount;
					if ((textureIndex & 1) == 1) {
						textureIndex = textureCount - textureIndex - 1;
					}
					break;
			}

			textureName = baseTextureName + (textureIndex + 1);
			logger.info("Loading switched texture '" + textureName + " with SwitchRepeat of "
					+ G3Enums.asString(eEColorSrcSwitchRepeat.class, switchRepeat) + ".");
		} /*
			 * else if(textureName.toLowerCase().endsWith("_a1")) { // TODO: Animated }
			 */

		Texture texture;
		try {
			texture = assetManager.loadTexture(textureName + ".ximg");
			texture.setWrap(WrapMode.Repeat);
			// Other repeat modes
		} catch (AssetNotFoundException e) {
			logger.warning("Texture '" + textureName + "' could not be found: " + e);
			return Optional.empty();
		} catch (AssetLoadException e) {
			logger.warning("Unable to load the texture '" + textureName + "': " + e);
			return Optional.empty();
		}

		if (!data.hasProperty(CD.eCColorSrcSampler.TexRepeatU) || !data.hasProperty(CD.eCColorSrcSampler.TexRepeatV)) {
			logger.warning(textureName + ".ximg is used without specifying a repeat mode.");
		} else if (data.property(CD.eCColorSrcSampler.TexRepeatU)
				.getEnumValue() != eEColorSrcSampleTexRepeat.eEColorSrcSampleTexRepeat_Wrap
				|| data.property(CD.eCColorSrcSampler.TexRepeatV).getEnumValue() != 0) {
			logger.warning(textureName + ".ximg is used with unkown repeat mode.");
		}

		return Optional.of(texture);
	}
}

package de.george.g3dit.scripts;

import java.io.IOException;
import java.nio.file.Path;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.google.common.base.Strings;
import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;
import com.teamunify.i18n.I;

import de.george.g3dit.settings.BooleanOptionHandler;
import de.george.g3dit.settings.LambdaOption;
import de.george.g3dit.settings.Option;
import de.george.g3dit.settings.OptionPanel;
import de.george.g3dit.util.AssetResolver;
import de.george.g3dit.util.FileDialogWrapper;
import de.george.g3utils.util.Pair;
import de.george.lrentnode.archive.ArchiveFile;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.enums.G3Enums;
import de.george.lrentnode.enums.G3Enums.gEUseType;
import de.george.lrentnode.iterator.ArchiveFileIterator;
import de.george.lrentnode.util.EntityUtil;

public class ScriptListEntities implements IScript {
	private static final Option<Boolean> ONLY_NAMED = new LambdaOption<>(true,
			(parent) -> new BooleanOptionHandler(parent, I.tr("List named entities only")), "ScriptListEntities.ONLY_NAMED",
			I.tr("Export named entities only"));

	private static final Option<Boolean> RESOLVE_ASSETS = new LambdaOption<>(false,
			(parent) -> new BooleanOptionHandler(parent, I.tr("Resolve assets used by entities (slow)")),
			"ScriptListEntities.RESOLVE_ASSETS", I.tr("Resolve assets used by entities"));

	@Override
	public String getTitle() {
		return I.tr("List entities");
	}

	@Override
	public String getDescription() {
		return I.tr("Creates a list of all entities.");
	}

	@Override
	public boolean execute(IScriptEnvironment env) {
		Path saveFile = FileDialogWrapper.saveFile(I.tr("Save listing as..."), env.getParentWindow(), FileDialogWrapper.JSON_FILTER);
		if (saveFile == null) {
			return false;
		}

		Table<String, Integer, Integer> meshLookup = TreeBasedTable.create();

		try {
			JsonFactory factory = new JsonFactory();
			JsonGenerator generator = factory.createGenerator(saveFile.toFile(), JsonEncoding.UTF8);
			generator.useDefaultPrettyPrinter();

			boolean onlyNamed = env.getOption(ONLY_NAMED);
			boolean resolveAssets = (env.getOption(RESOLVE_ASSETS));

			if (resolveAssets) {
				generator.writeStartObject();
				generator.writeArrayFieldStart("Entities");
			} else
				generator.writeStartArray();

			ArchiveFileIterator worldFilesIterator = env.getFileManager().worldFilesIterator();
			while (worldFilesIterator.hasNext()) {
				ArchiveFile aFile = worldFilesIterator.next();
				for (eCEntity entity : aFile.getEntities()) {
					if (onlyNamed && entity.getName().trim().isEmpty() || EntityUtil.isRootLike(entity)) {
						continue;
					}

					generator.writeStartObject();
					generator.writeStringField("Name", entity.toString());
					generator.writeStringField("Guid", entity.getGuid());
					generator.writeStringField("Position", entity.getWorldPosition().toString());
					generator.writeStringField("Rotation", entity.getWorldRotation().toString());
					generator.writeStringField("Scaling", entity.getWorldMatrix().getPureScaling().toString());

					Pair<String, Integer> meshAndMaterialSwitch = EntityUtil.getMeshAndMaterialSwitch(entity).orElse(null);
					if (meshAndMaterialSwitch != null) {
						int materialSwitch = meshAndMaterialSwitch.el1();
						String mesh = EntityUtil.cleanAnimatedMeshName(meshAndMaterialSwitch.el0());
						meshLookup.put(mesh, materialSwitch, 1);

						generator.writeStringField("Mesh", mesh);
						generator.writeNumberField("MaterialSwitch", materialSwitch);
					}

					if (entity.hasClass(CD.gCNavigation_PS.class)) {
						generator.writeArrayFieldStart("Routines");
						for (String routine : entity.getProperty(CD.gCNavigation_PS.RoutineNames).getNativeEntries()) {
							generator.writeString(routine);
						}
						generator.writeEndArray();
					}

					if (entity.hasClass(CD.gCInteraction_PS.class)) {
						generator.writeStringField("UseType",
								G3Enums.asString(gEUseType.class, entity.getProperty(CD.gCInteraction_PS.UseType).getEnumValue()));
						String scriptUseFunc = entity.getProperty(CD.gCInteraction_PS.ScriptUseFunc).getString();
						if (!Strings.isNullOrEmpty(scriptUseFunc)) {
							generator.writeStringField("ScriptUseFunc", scriptUseFunc);
						}
					}

					generator.writeEndObject();
				}
			}
			generator.writeEndArray();

			if (env.getOption(RESOLVE_ASSETS)) {
				resolveAssets(env, meshLookup, generator);
				generator.writeEndObject();
			}

			generator.close();
		} catch (IOException e) {
			env.log(I.trf("Error while writing the file: {0}", e.getMessage()));
			return false;
		}
		return true;
	}

	private static void resolveAssets(IScriptEnvironment env, Table<String, Integer, Integer> meshLookup, JsonGenerator generator)
			throws IOException {
		AssetResolver resolver = AssetResolver.with(env.getEditorContext()).build();
		generator.writeObjectFieldStart("Meshes");
		for (String meshName : meshLookup.rowKeySet()) {
			generator.writeObjectFieldStart(meshName);
			for (int materialSwitch : meshLookup.row(meshName).keySet()) {
				generator.writeObjectFieldStart(Integer.toString(materialSwitch));
				AssetResolver.MeshAsset mesh = resolver.resolveMesh(meshName, materialSwitch);
				generator.writeStringField("Name", mesh.getName());
				generator.writeStringField("Status", mesh.isFound() ? "Ok" : mesh.getError());
				generator.writeArrayFieldStart("Materials");
				for (AssetResolver.MaterialAsset material : mesh.getMaterials()) {
					generator.writeStartObject();
					generator.writeStringField("Name", material.getName());
					generator.writeStringField("Status", material.isFound() ? "Ok" : material.getError());
					generator.writeNumberField("MaterialSwitch", material.getMaterialSwitch());
					generator.writeArrayFieldStart("Textures");
					for (AssetResolver.TextureAsset texture : material.getTextures()) {
						generator.writeStartObject();
						generator.writeStringField("Name", texture.getName());
						generator.writeStringField("Status", texture.isFound() ? "Ok" : texture.getError());
						generator.writeStringField("Usage", texture.getUseType());
						generator.writeBooleanField("Switched", texture.isSwitched());
						if (texture.isSwitched()) {
							generator.writeStringField("BaseName", texture.getBaseName());
							generator.writeStringField("SwitchRepeat",
									G3Enums.asString(G3Enums.eEColorSrcSwitchRepeat.class, texture.getSwitchRepeat()));
						}
						generator.writeEndObject();
					}
					generator.writeEndArray();
					generator.writeEndObject();
				}
				generator.writeEndArray();
				generator.writeEndObject();
			}
			generator.writeEndObject();
		}
		generator.writeEndObject();
	}

	@Override
	public void installOptions(OptionPanel optionPanel) {
		optionPanel.addOption(ONLY_NAMED).addOption(RESOLVE_ASSETS);
	}
}

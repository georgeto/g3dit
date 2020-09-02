package de.george.g3dit.scripts;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.google.common.base.Strings;
import com.teamunify.i18n.I;

import de.george.g3dit.settings.BooleanOptionHandler;
import de.george.g3dit.settings.LambdaOption;
import de.george.g3dit.settings.Option;
import de.george.g3dit.settings.OptionPanel;
import de.george.g3dit.util.FileDialogWrapper;
import de.george.lrentnode.archive.ArchiveFile;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.enums.G3Enums;
import de.george.lrentnode.enums.G3Enums.gEUseType;
import de.george.lrentnode.iterator.ArchiveFileIterator;
import de.george.lrentnode.util.EntityUtil;

public class ScriptListEntities implements IScript {
	private static final Option<Boolean> ONLY_NAMED = new LambdaOption<>(true,
			(parent) -> new BooleanOptionHandler(parent, I.tr("Nur benannte Entities auflisten")), "ScriptListEntities.ONLY_NAMED",
			I.tr("Nur benannte Entities exportieren"));

	@Override
	public String getTitle() {
		return I.tr("Entities auflisten");
	}

	@Override
	public String getDescription() {
		return I.tr("Erstellt eine Liste aller Entities.");
	}

	@Override
	public boolean execute(IScriptEnvironment env) {
		File saveFile = FileDialogWrapper.saveFile(I.tr("Auflistung speichern unter..."), env.getParentWindow(),
				FileDialogWrapper.JSON_FILTER);
		if (saveFile == null) {
			return false;
		}

		try {
			JsonFactory factory = new JsonFactory();
			JsonGenerator generator = factory.createGenerator(saveFile, JsonEncoding.UTF8);
			generator.useDefaultPrettyPrinter();
			generator.writeStartArray();

			boolean onlyNamed = env.getOption(ONLY_NAMED);

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
			generator.close();
		} catch (IOException e) {
			env.log(I.trf("Fehler beim Schreiben der Datei: {0}", e.getMessage()));
			return false;
		}
		return true;
	}

	@Override
	public void installOptions(OptionPanel optionPanel) {
		optionPanel.addOption(ONLY_NAMED);
	}
}

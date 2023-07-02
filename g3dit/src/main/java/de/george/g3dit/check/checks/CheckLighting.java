package de.george.g3dit.check.checks;

import java.nio.file.Path;
import java.util.function.Supplier;

import com.teamunify.i18n.I;

import de.george.g3dit.EditorContext;
import de.george.g3dit.check.EntityDescriptor;
import de.george.lrentnode.archive.ArchiveFile;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.enums.G3Enums.eEStaticIlluminated;
import de.george.lrentnode.enums.G3Enums.eEStaticLighingType;

public class CheckLighting extends AbstractEntityCheck {
	private final EditorContext ctx;

	public CheckLighting(EditorContext ctx) {
		super(I.tr("Find lighting problems"), I.tr("Finds entities and templates with problematic lighting settings."), 0, 1);
		this.ctx = ctx;
	}

	@Override
	protected EntityPassStatus processEntity(ArchiveFile archiveFile, Path dataFile, eCEntity entity, int entityPosition, int pass,
			Supplier<EntityDescriptor> descriptor, StringProblemConsumer problemConsumer) {
		if (entity.hasClass(CD.eCIlluminated_PS.class)) {
			boolean staticIlluminated = entity.getProperty(CD.eCIlluminated_PS.StaticIlluminated)
					.getEnumValue() == eEStaticIlluminated.eEStaticIlluminated_Static;

			if (!staticIlluminated && entity.getPropertyNoThrow(CD.eCVisualMeshBase_PS.StaticLightingType)
					.map(lt -> lt.getEnumValue() == eEStaticLighingType.eEStaticLighingType_Lightmap).orElse(false)) {
				problemConsumer.fatal(I.tr("Dynamically illuminated entity with lightmap"), I.tr(
						"Combination of eEStaticIlluminated_Dynamic and eEStaticLighingType_Lightmap is useless, because with eEStaticIlluminated_Dynamic no lightmaps are used.\nPlease change to eEStaticLighingType_Instance."));
			}
		}
		return EntityPassStatus.Next;
	}
}

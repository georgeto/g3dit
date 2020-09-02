package de.george.g3dit.check.checks;

import java.io.File;
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
		super(I.tr("Beleuchtungsprobleme ermitteln"), I.tr("Ermittelt Entities und Templates mit problematischen Beleuchtungseinstellungen."), 0, 1);
		this.ctx = ctx;
	}

	@Override
	protected EntityPassStatus processEntity(ArchiveFile archiveFile, File dataFile, eCEntity entity, int entityPosition, int pass,
			Supplier<EntityDescriptor> descriptor, StringProblemConsumer problemConsumer) {
		if (entity.hasClass(CD.eCIlluminated_PS.class)) {
			boolean staticIlluminated = entity.getProperty(CD.eCIlluminated_PS.StaticIlluminated)
					.getEnumValue() == eEStaticIlluminated.eEStaticIlluminated_Static;

			if (!staticIlluminated && entity.getPropertyNoThrow(CD.eCVisualMeshBase_PS.StaticLightingType)
					.map(lt -> lt.getEnumValue() == eEStaticLighingType.eEStaticLighingType_Lightmap).orElse(false)) {
				problemConsumer.fatal(I.tr("Dynamisch beleuchtete Entity mit Lightmap"),
						I.tr("Kombination von eEStaticIlluminated_Dynamic und eEStaticLighingType_Lightmap ist sinnlos, da bei eEStaticIlluminated_Dynamic keine Lightmaps genutzt werden.\nBitte auf eEStaticLighingType_Instance umstellen."));
			}
		}
		return EntityPassStatus.Next;
	}
}

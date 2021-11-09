package de.george.g3dit.check.checks;

import java.io.File;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.teamunify.i18n.I;

import de.george.g3dit.check.EntityDescriptor;
import de.george.g3dit.util.HtmlCreator;
import de.george.lrentnode.archive.ArchiveFile;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.classes.desc.CD;

public class CheckNestedItems extends AbstractEntityCheck {
	public CheckNestedItems() {
		super(I.tr("Find items and interaction objects with child entities"),
				I.tr("Interaction objects should usually not have child entities, "
						+ "as these affect the Y position of the focus name if FocusNameType is set to gEFocusNameType_Entity.\n\n"
						+ "Collectable items are removed from the world when picked up, along with their child entities."),
				0, 1);
	}

	@Override
	protected EntityPassStatus processEntity(ArchiveFile archiveFile, File dataFile, eCEntity entity, int entityPosition, int pass,
			Supplier<EntityDescriptor> descriptor, StringProblemConsumer problemConsumer) {

		if (!entity.getChilds().isEmpty() && entity.hasClass(CD.gCInteraction_PS.class) && !entity.hasClass(CD.gCNavigation_PS.class)) {
			String childDetails = entity.getChilds().stream().map(c -> new EntityDescriptor(c, descriptor.get().getFile()))
					.map(HtmlCreator::renderEntityShort).collect(Collectors.joining(", "));

			if (entity.hasClass(CD.gCItem_PS.class)) {
				problemConsumer.warning(I.tr("Item has child entities."), childDetails);
			} else {
				problemConsumer.warning(I.tr("Interaction object has child entities."), childDetails);
			}
		}

		return EntityPassStatus.Next;
	}
}

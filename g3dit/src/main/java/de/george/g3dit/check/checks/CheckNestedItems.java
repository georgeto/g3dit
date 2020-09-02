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
		super(I.tr("Items und Interaktionsobjekte mit Child-Entities ermitteln"),
				I.tr("Interaktionsobjekte sollte überlicherweise keine Child-Entities haben, "
						+ "da diese die Y-Position des Fokusnamens beeinflussen, "
						+ "sofern FocusNameType auf gEFocusNameType_Entity steht.\n\n"
						+ "Aufsammelbare Items werden beim Aufheben aus der Welt entfernt, zusammen mit ihren Child-Entities."),
				0, 1);
	}

	@Override
	protected EntityPassStatus processEntity(ArchiveFile archiveFile, File dataFile, eCEntity entity, int entityPosition, int pass,
			Supplier<EntityDescriptor> descriptor, StringProblemConsumer problemConsumer) {

		if (!entity.getChilds().isEmpty() && entity.hasClass(CD.gCInteraction_PS.class) && !entity.hasClass(CD.gCNavigation_PS.class)) {
			String childDetails = entity.getChilds().stream().map(c -> new EntityDescriptor(c, descriptor.get().getFile()))
					.map(HtmlCreator::renderEntityShort).collect(Collectors.joining(", "));

			if (entity.hasClass(CD.gCItem_PS.class)) {
				problemConsumer.warning(I.tr("Item hat Child-Entities."), childDetails);
			} else {
				problemConsumer.warning(I.tr("Interaktionsobjekt hat Child-Entities."), childDetails);
			}
		}

		return EntityPassStatus.Next;
	}
}

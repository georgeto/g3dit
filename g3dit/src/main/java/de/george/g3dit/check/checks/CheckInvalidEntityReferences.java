package de.george.g3dit.check.checks;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import com.teamunify.i18n.I;

import de.george.g3dit.check.EntityDescriptor;
import de.george.lrentnode.archive.ArchiveFile;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.classes.G3Class;
import de.george.lrentnode.properties.ClassProperty;
import de.george.lrentnode.properties.eCEntityProxy;
import de.george.lrentnode.util.PropertyUtil;

public class CheckInvalidEntityReferences extends AbstractEntityCheck {
	private Set<String> entityGuids = new HashSet<>();

	public CheckInvalidEntityReferences() {
		super(I.tr("Find invalid entity references"), I.tr("Checks all entity references for invalid guids."), 0, 2);
	}

	@Override
	protected EntityPassStatus processEntity(ArchiveFile archiveFile, File dataFile, eCEntity entity, int entityPosition, int pass,
			Supplier<EntityDescriptor> descriptor, StringProblemConsumer problemConsumer) {

		if (pass == 0) {
			entityGuids.add(entity.getGuid());
		} else
			PropertyUtil.visitEntityReferences(entity,
					(value, property, propertySet) -> processEntityProperty(value, property, propertySet, problemConsumer));

		return EntityPassStatus.Next;
	}

	private boolean processEntityProperty(eCEntityProxy value, ClassProperty<?> property, G3Class propertySet,
			StringProblemConsumer problemConsumer) {
		if (value.getGuid() != null && !entityGuids.contains(value.getGuid())) {
			String message = I.trf("Property {0}.{1} contains non-existent Guid", propertySet.getClassName(), property.getName());
			String details = I.trf("Property {0}.{1} contains non-existent Guid\n{2}", propertySet.getClassName(), property.getName(),
					value.getGuid());
			problemConsumer.fatal(message, details);
		}
		return true;
	}

	@Override
	public void reset() {
		entityGuids.clear();
	}
}

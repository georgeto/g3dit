package de.george.g3dit.check.checks;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import com.teamunify.i18n.I;

import de.george.g3dit.check.EntityDescriptor;
import de.george.g3dit.check.FileDescriptor;
import de.george.lrentnode.archive.ArchiveFile;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.classes.G3Class;
import de.george.lrentnode.properties.ClassProperty;
import de.george.lrentnode.properties.eCEntityProxy;
import de.george.lrentnode.template.TemplateFile;
import de.george.lrentnode.util.PropertyUtil;

public class CheckInvalidTemplateReferences extends AbstractEntityCheck {
	private Set<String> refGuids = new HashSet<>();

	public CheckInvalidTemplateReferences() {
		super(I.tr("Find invalid template references"), I.tr("Checks all template references for invalid guids."), 2, 1);
	}

	@Override
	protected EntityPassStatus processEntity(ArchiveFile archiveFile, Path dataFile, eCEntity entity, int entityPosition, int pass,
			Supplier<EntityDescriptor> descriptor, StringProblemConsumer problemConsumer) {

		if (entity.getCreator() != null && !refGuids.contains(entity.getCreator())) {
			problemConsumer.fatal(I.tr("Reference Guid does not exist"), entity.getCreator());
		}

		PropertyUtil.visitTemplateReferences(entity,
				(value, property, propertySet) -> processTemplateProperty(value, property, propertySet, problemConsumer));

		return EntityPassStatus.Next;
	}

	@Override
	public PassStatus processTemplateEntity(TemplateFile tple, Path dataFile, eCEntity entity, int pass, FileDescriptor descriptor,
			StringProblemConsumer problemConsumer) {
		if (pass == 0) {
			refGuids.add(tple.getReferenceHeader().getGuid());
		} else {
			PropertyUtil.visitTemplateReferences(tple.getReferenceHeader(),
					(value, property, propertySet) -> processTemplateProperty(value, property, propertySet, problemConsumer));
		}

		return PassStatus.Next;
	}

	private boolean processTemplateProperty(eCEntityProxy value, ClassProperty<?> property, G3Class propertySet,
			StringProblemConsumer problemConsumer) {
		if (value.getGuid() != null && !refGuids.contains(value.getGuid())) {
			String message = I.trf("Property {0}.{1} contains non-existent Guid", propertySet.getClassName(), property.getName());
			String details = I.trf("Property {0}.{1} contains non-existent Guid\n{2}", propertySet.getClassName(), property.getName(),
					value.getGuid());
			problemConsumer.fatal(message, details);
		}
		return true;
	}

	@Override
	public void reset() {
		refGuids.clear();
	}
}

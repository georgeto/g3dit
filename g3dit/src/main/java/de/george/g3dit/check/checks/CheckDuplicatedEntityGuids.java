package de.george.g3dit.check.checks;

import java.nio.file.Path;
import java.util.Collection;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.teamunify.i18n.I;

import de.george.g3dit.check.EntityDescriptor;
import de.george.g3dit.check.problem.ProblemConsumer;
import de.george.g3dit.gui.components.Severity;
import de.george.g3dit.util.HtmlCreator;
import de.george.g3dit.util.UriUtil;
import de.george.lrentnode.archive.ArchiveFile;
import de.george.lrentnode.archive.eCEntity;

public class CheckDuplicatedEntityGuids extends AbstractEntityCheck {
	private ListMultimap<String, EntityDescriptor> entityGuidMap = ArrayListMultimap.create(300000, 1);

	public CheckDuplicatedEntityGuids() {
		super(I.tr("Find non-unique entity guids"), I.tr("Checks all entities for duplicate guids."), 0, 1);
	}

	@Override
	protected EntityPassStatus processEntity(ArchiveFile archiveFile, Path dataFile, eCEntity entity, int entityPosition, int pass,
			Supplier<EntityDescriptor> descriptor, StringProblemConsumer problemConsumer) {
		entityGuidMap.put(entity.getGuid(), descriptor.get());
		return EntityPassStatus.Next;
	}

	@Override
	public void reportProblems(ProblemConsumer problemConsumer) {
		entityGuidMap.asMap().entrySet().stream().filter(e -> e.getValue().size() > 1)
				.forEach(e -> reportDuplicatedGuid(problemConsumer, e.getKey(), e.getValue()));
	}

	protected void reportDuplicatedGuid(ProblemConsumer problemConsumer, String guid, Collection<EntityDescriptor> entities) {
		String message = I.trf("Duplicate entity guid: {0}", guid);
		String details = entities.stream().map(e -> {
			String entityIdentifier = String.format("%s (%s #%d)", e.getDisplayName(), e.getFile().getPath().getFileName(), e.getIndex());
			return HtmlCreator.renderLink(entityIdentifier, UriUtil.encodeEntity(e));
		}).collect(Collectors.joining(HtmlCreator.LINE_SEPERATOR));

		for (EntityDescriptor entity : entities) {
			postEntityProblem(problemConsumer, entity, Severity.Error, message, details);
		}
	}

	@Override
	public void reset() {
		entityGuidMap.clear();
	}
}

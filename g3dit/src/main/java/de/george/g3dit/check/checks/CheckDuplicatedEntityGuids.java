package de.george.g3dit.check.checks;

import static j2html.TagCreator.a;

import java.io.File;
import java.util.Collection;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import de.george.g3dit.check.EntityDescriptor;
import de.george.g3dit.check.problem.ProblemConsumer;
import de.george.g3dit.check.problem.Severity;
import de.george.g3dit.util.UriUtil;
import de.george.lrentnode.archive.ArchiveFile;
import de.george.lrentnode.archive.eCEntity;

public class CheckDuplicatedEntityGuids extends AbstractEntityCheck {
	private ListMultimap<String, EntityDescriptor> entityGuidMap = ArrayListMultimap.create(300000, 1);

	public CheckDuplicatedEntityGuids() {
		super("Uneindeutige Entity-Guids ermitteln", "Überprüft alle Entities nach mehrfach vorkommenden Guids.", 0, 1);
	}

	@Override
	protected EntityPassStatus processEntity(ArchiveFile archiveFile, File dataFile, eCEntity entity, int entityPosition, int pass,
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
		String message = "Mehrfach vorkommende Entity-Guid: " + guid;
		String details = entities.stream().map(e -> {
			String entityIdentifier = String.format("%s (%s #%d)", e.getDisplayName(), e.getFile().getPath().getName(), e.getIndex());
			return a(entityIdentifier).withHref(UriUtil.encodeEntity(e)).render();
		}).collect(Collectors.joining("<br>"));

		for (EntityDescriptor entity : entities) {
			postEntityProblem(problemConsumer, entity, Severity.Fatal, message, details);
		}
	}

	@Override
	public void reset() {
		entityGuidMap.clear();
	}
}

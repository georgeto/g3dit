package de.george.g3dit.check.checks;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import de.george.g3dit.check.EntityDescriptor;
import de.george.g3dit.check.FileDescriptor;
import de.george.g3dit.check.FileDescriptor.FileType;
import de.george.g3dit.check.problem.ProblemConsumer;
import de.george.g3dit.util.HtmlCreator;
import de.george.g3dit.util.UriUtil;
import de.george.lrentnode.archive.ArchiveFile;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.template.TemplateFile;

public class CheckDifferingChangeTimeValues extends AbstractEntityCheck {
	private Map<String, Long> changeTimeMap = new HashMap<>();
	private Map<String, FileDescriptor> descriptors = new HashMap<>();

	public CheckDifferingChangeTimeValues() {
		super("Abweichend ChangeTime-Werte ermitteln",
				"Überprüft für alle Entities ob ihr ChangeTime-Wert von dem ihrer Template abweicht.", 1, 1);
	}

	@Override
	protected EntityPassStatus processEntity(ArchiveFile archiveFile, File dataFile, eCEntity entity, int entityPosition, int pass,
			Supplier<EntityDescriptor> descriptor, StringProblemConsumer problemConsumer) {

		if (entity.getCreator() != null && changeTimeMap.containsKey(entity.getCreator())) {
			long entityChangeTime = entity.getDataChangedTimeStamp();
			long creatorChangeTime = changeTimeMap.get(entity.getCreator());
			if (entityChangeTime != creatorChangeTime) {
				String creatorLink = HtmlCreator.renderLink("Template", UriUtil.encodeFile(descriptors.get(entity.getCreator())));
				problemConsumer.fatal(
						String.format("ChangeTime-Wert %s als in Template", entityChangeTime < creatorChangeTime ? "kleiner" : "größer"),
						String.format("%d (Entity) != %d (%s)", entityChangeTime, creatorChangeTime, creatorLink));
			}
		}

		return EntityPassStatus.Next;
	}

	@Override
	public PassStatus processTemplate(TemplateFile tple, File dataFile, int pass, ProblemConsumer problemConsumer) {
		changeTimeMap.put(tple.getReferenceHeader().getGuid(), tple.getReferenceHeader().getDataChangedTimeStamp());
		descriptors.put(tple.getReferenceHeader().getGuid(), new FileDescriptor(dataFile, FileType.Template));

		return PassStatus.Next;
	}

	@Override
	public void reset() {
		changeTimeMap.clear();
		descriptors.clear();
	}
}

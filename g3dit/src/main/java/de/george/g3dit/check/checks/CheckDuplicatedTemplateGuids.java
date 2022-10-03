package de.george.g3dit.check.checks;

import java.io.File;
import java.util.Collection;
import java.util.function.Supplier;

import com.google.common.collect.SortedSetMultimap;
import com.google.common.collect.TreeMultimap;
import com.teamunify.i18n.I;

import de.george.g3dit.check.EntityDescriptor;
import de.george.g3dit.check.FileDescriptor;
import de.george.g3dit.check.FileDescriptor.FileType;
import de.george.g3dit.check.problem.ProblemConsumer;
import de.george.g3dit.check.problem.TemplateProblem;
import de.george.g3dit.util.HtmlCreator;
import de.george.g3dit.util.UriUtil;
import de.george.lrentnode.archive.ArchiveFile;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.template.TemplateFile;
import one.util.streamex.StreamEx;

public class CheckDuplicatedTemplateGuids extends AbstractEntityCheck {
	private SortedSetMultimap<String, FileDescriptor> itemGuidMap = TreeMultimap.create();
	private SortedSetMultimap<String, FileDescriptor> refGuidMap = TreeMultimap.create();

	public CheckDuplicatedTemplateGuids() {
		super(I.tr("Find non-unique template guides"), I.tr("Checks all templates for duplicate guids."), 1, 0);
	}

	@Override
	protected EntityPassStatus processEntity(ArchiveFile archiveFile, File dataFile, eCEntity entity, int entityPosition, int pass,
			Supplier<EntityDescriptor> descriptor, StringProblemConsumer problemConsumer) {
		throw new UnsupportedOperationException();
	}

	@Override
	public PassStatus processTemplate(TemplateFile tple, File dataFile, int pass, ProblemConsumer problemConsumer) {
		FileDescriptor descriptor = new FileDescriptor(dataFile, FileType.Template);
		itemGuidMap.put(tple.getItemHeader().getGuid(), descriptor);
		refGuidMap.put(tple.getReferenceHeader().getGuid(), descriptor);
		return PassStatus.Next;
	}

	@Override
	public void reportProblems(ProblemConsumer problemConsumer) {
		itemGuidMap.asMap().entrySet().stream().filter(e -> e.getValue().size() > 1)
				.forEach(e -> reportDuplicatedGuid(problemConsumer, I.tr("Item"), e.getKey(), e.getValue()));

		refGuidMap.asMap().entrySet().stream().filter(e -> e.getValue().size() > 1)
				.forEach(e -> reportDuplicatedGuid(problemConsumer, I.tr("Reference"), e.getKey(), e.getValue()));
	}

	protected void reportDuplicatedGuid(ProblemConsumer problemConsumer, String guidType, String guid, Collection<FileDescriptor> files) {
		String message = I.trf("Duplicate {0} guid: {1}", guidType, guid);
		String details = HtmlCreator
				.renderList(StreamEx.of(files).map(f -> HtmlCreator.renderLink(f.getPath().getName(), UriUtil.encodeFile(f))));

		for (FileDescriptor file : files) {
			TemplateProblem problem = new TemplateProblem(message, details);
			problem.setParent(problemConsumer.getFileHelper(file));
			problemConsumer.fatal(problem);
		}
	}

	@Override
	public void reset() {
		itemGuidMap.clear();
		refGuidMap.clear();
	}
}

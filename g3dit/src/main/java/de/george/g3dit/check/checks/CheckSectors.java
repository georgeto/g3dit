package de.george.g3dit.check.checks;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;
import com.teamunify.i18n.I;

import de.george.g3dit.EditorContext;
import de.george.g3dit.check.FileDescriptor;
import de.george.g3dit.check.FileDescriptor.FileType;
import de.george.g3dit.check.problem.GenericFileProblem;
import de.george.g3dit.check.problem.ProblemConsumer;
import de.george.g3dit.gui.components.Severity;
import de.george.g3dit.util.FileManager;
import de.george.g3dit.util.HtmlCreator;
import de.george.g3utils.util.Converter;
import de.george.g3utils.util.IOUtils;
import de.george.lrentnode.archive.SecDat;
import de.george.lrentnode.util.FileUtil;
import one.util.streamex.StreamEx;

public class CheckSectors extends AbstractEntityCheck {
	private final EditorContext ctx;

	private ProblemConsumer problemConsumer;

	public CheckSectors(EditorContext ctx) {
		super(I.tr("Find sector problems"), I.tr("Check for inconsistencies within sectors and world files."), 0, 0);
		this.ctx = ctx;
	}

	@Override
	public void reportProblems(ProblemConsumer problemConsumer) {
		this.problemConsumer = problemConsumer;

		Optional<File> wrldatasc = ctx.getFileManager().searchFile(FileManager.RP_PROJECTS_COMPILED, "G3_World_01.wrldatasc");
		if (!wrldatasc.isPresent())
			report(Severity.Error, I.tr("Unable to find G3_World_01.wrldatasc."),
					new File(ctx.getFileManager().getPrimaryPath("G3_World_01.wrldatasc")));

		Map<String, Boolean> registeredSectors;
		try {
			registeredSectors = IOUtils.readTextFile(wrldatasc.get(), Converter.WINDOWS_1252).stream().filter(e -> !e.startsWith(";"))
					.map(e -> e.split("=")).filter(e -> e.length == 2).collect(Collectors.toMap(e -> e[0], e -> Boolean.valueOf(e[1])));
		} catch (IOException e) {
			registeredSectors = Collections.emptyMap();
			report(Severity.Error, I.tr("Error while loading G3_World_01.wrldatasc."), e.getMessage(), wrldatasc.get());
		}

		var worldFiles = ctx.getFileManager().listWorldFiles().stream()
				.collect(Multimaps.toMultimap(File::getName, Function.identity(), HashMultimap::create));
		for (var entry : worldFiles.asMap().entrySet()) {
			if (entry.getValue().size() >= 2) {
				report(Severity.Error, I.trf("Multiple world data files with the same name {0}.", entry.getKey()),
						renderFileList(entry.getValue()), entry.getValue());
			}
		}

		Multimap<String, File> knownSectors = HashMultimap.create();
		Multimap<String, File> knownWorldFiles = HashMultimap.create();
		for (File sector : ctx.getFileManager().listFiles(FileManager.RP_PROJECTS_COMPILED, IOUtils.secdatFileFilter)) {
			String sectorName = sector.getName();
			try {
				knownSectors.put(sectorName, sector);
				// Find inactive sectors (not mentioned in .wrldatasc)
				if (registeredSectors.remove(IOUtils.stripExtension(sectorName)) == null)
					report(Severity.Warn, I.trf("Sector {0} is not registered in G3_World_01.wrldatasc.", sectorName), sector);

				// Find sectors referring to non-exisiting files.
				SecDat secDat = FileUtil.openSecdat(sector);
				for (String worldFile : StreamEx.of(secDat.getNodeFiles().stream().map(node -> node + ".node"))
						.append(secDat.getLrentdatFiles().stream().map(lrentdat -> lrentdat + ".lrentdat"))) {
					if (!worldFiles.containsKey(worldFile))
						report(Severity.Error, I.trf("Sector {0} refers to non-existing world file {1}.", sectorName, worldFile), sector);
					knownWorldFiles.put(worldFile, sector);
				}
			} catch (IOException e) {
				report(Severity.Error, I.trf("Error while loading sector {0}.", sectorName), e.getMessage(), sector);
			}
		}

		for (var entry : knownSectors.asMap().entrySet()) {
			if (entry.getValue().size() >= 2) {
				report(Severity.Error, I.trf("Multiple sectors with the same name {0}.", entry.getKey()), renderFileList(entry.getValue()),
						entry.getValue());
			}
		}

		for (var entry : knownWorldFiles.asMap().entrySet()) {
			if (entry.getValue().size() >= 2) {
				var referredFiles = worldFiles.get(entry.getKey());
				if (referredFiles.isEmpty())
					referredFiles = ImmutableSet.of(new File(entry.getKey()));
				report(Severity.Error, I.trf("Multiple sectors refer to world file {0}.", entry.getKey()),
						renderFileList(entry.getValue()), referredFiles);
			}
		}

		// Find files not belonging to any sector.
		for (String unusedWorldFile : Sets.difference(worldFiles.keySet(), knownWorldFiles.keySet()))
			report(Severity.Warn, I.trf("World file {0} is not registered in any sector.", unusedWorldFile),
					worldFiles.get(unusedWorldFile));
	}

	private String renderFileList(Collection<File> files) {
		return HtmlCreator.renderList(files.stream().map(f -> ctx.getFileManager().getRelativePath(f).get()));
	}

	private void report(Severity severity, String message, String details, Iterable<File> files) {
		for (File file : files) {
			var problem = new GenericFileProblem(message, details);
			problem.setSeverity(severity);
			problem.setParent(problemConsumer.getFileHelper(toFileDescriptor(file)));
			problemConsumer.post(problem);
		}
	}

	private void report(Severity severity, String message, String details, File file) {
		report(severity, message, details, StreamEx.of(file));
	}

	private void report(Severity severity, String message, Iterable<File> files) {
		report(severity, message, null, files);
	}

	private void report(Severity severity, String message, File file) {
		report(severity, message, null, file);
	}

	private static FileDescriptor toFileDescriptor(File file) {
		FileType type = FileType.Other;
		if (file.getName().endsWith(".node"))
			type = FileType.Node;
		else if (file.getName().endsWith(".lrentdat"))
			type = FileType.Lrentdat;
		return new FileDescriptor(file, type);
	}
}

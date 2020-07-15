package de.george.g3dit.check.checks;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.george.g3dit.EditorContext;
import de.george.g3dit.check.EntityDescriptor;
import de.george.g3dit.check.FileDescriptor;
import de.george.g3dit.check.FileDescriptor.FileType;
import de.george.g3dit.check.problem.GenericFileProblem;
import de.george.g3dit.check.problem.ProblemConsumer;
import de.george.g3dit.check.problem.Severity;
import de.george.g3dit.util.FileManager;
import de.george.g3utils.structure.GuidUtil;
import de.george.lrentnode.archive.ArchiveFile;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.classes.G3Class;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.util.EntityUtil;

public class CheckLightmaps extends AbstractEntityCheck {
	private static final Pattern PATTERN_LIGHTMAP_NAME = Pattern
			.compile("^(?<mesh>.*)_\\{(?<guid>[0-9A-Fa-f]{8}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{12})\\}\\.xlmp$");

	private EditorContext ctx;
	private Map<String, String> guidMeshMap = new HashMap<>();
	private Set<String> primaryGuids = new HashSet<>();

	public CheckLightmaps(EditorContext ctx) {
		super("Ungültige Lightmaps ermittelt", "Ermittelt Lightmaps deren Guid nicht existiert oder bei denen der Meshname nicht passt.",
				0, 1);
		this.ctx = ctx;
	}

	@Override
	protected EntityPassStatus processEntity(ArchiveFile archiveFile, File dataFile, eCEntity entity, int entityPosition, int pass,
			Supplier<EntityDescriptor> descriptor, StringProblemConsumer problemConsumer) {
		G3Class meshClass = EntityUtil.getStaticMeshClass(entity);
		if (meshClass != null) {
			guidMeshMap.put(entity.getGuid(), meshClass.property(CD.eCVisualMeshBase_PS.ResourceFileName).getString());
		}
		return EntityPassStatus.Next;
	}

	private void checkLightmap(File lightmap, ProblemConsumer problemConsumer, boolean primary) {
		Matcher matcher = PATTERN_LIGHTMAP_NAME.matcher(lightmap.getName());
		if (matcher.matches()) {
			String mesh = matcher.group("mesh");
			String guid = GuidUtil.parseGuid(matcher.group("guid"));

			if (primary) {
				primaryGuids.add(guid);
				// Skip checks for lightmaps in the secondary data folder if a lightmap with the
				// same guid exists in the primary data folder.
			} else if (primaryGuids.contains(guid)) {
				return;
			}

			if (guidMeshMap.containsKey(guid)) {
				String entityMesh = guidMeshMap.get(guid);
				String cleanEntityMesh = entityMesh.replaceFirst("\\.x(c|l)msh$", "");
				if (mesh.equals(cleanEntityMesh)) {
					return;
				}

				if (entityMesh.endsWith(".xlmsh") && mesh.replaceFirst("_LOD1$", "").equals(cleanEntityMesh)) {
					return;
				}

				reportLightmapError(problemConsumer, lightmap, Severity.Fatal,
						"Mesh der Lightmap weicht vom Mesh der zugehörigen Entity ab.",
						String.format("Lightmap: %s\n Entity: %s", mesh, cleanEntityMesh));
			} else {
				// Only report for lightmaps in primary data folder
				if (primary) {
					reportLightmapError(problemConsumer, lightmap, Severity.Warning, "Es existiert keine Entity mit Guid der Lightmap.",
							String.format("Guid: %s", guid));
				}
			}
		} else {
			// Only report for lightmaps in primary data folder
			if (primary) {
				reportLightmapError(problemConsumer, lightmap, Severity.Warning,
						"Name der Lightmap entspricht nicht dem Lightmap-Namensschema.", null);
			}
		}
	}

	@Override
	public void reportProblems(ProblemConsumer problemConsumer) {
		for (File lightmap : ctx.getFileManager().listPrimaryFiles(FileManager.RP_LIGHTMAPS, f -> f.getName().endsWith(".xlmp"))) {
			checkLightmap(lightmap, problemConsumer, true);
		}

		for (File lightmap : ctx.getFileManager().listSecondaryFiles(FileManager.RP_LIGHTMAPS, f -> f.getName().endsWith(".xlmp"))) {
			checkLightmap(lightmap, problemConsumer, false);
		}
	}

	protected void reportLightmapError(ProblemConsumer problemConsumer, File file, Severity severity, String message, String details) {
		GenericFileProblem problem = new GenericFileProblem(message, details);
		problem.setParent(problemConsumer.getFileHelper(new FileDescriptor(file, FileType.Other)));
		problem.setSeverity(severity);
		problemConsumer.post(problem);
	}

	@Override
	public void reset() {
		guidMeshMap.clear();
		primaryGuids.clear();
	}
}

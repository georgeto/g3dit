package de.george.g3dit.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import de.george.g3dit.EditorContext;
import de.george.g3dit.config.ConfigFiles;
import de.george.g3utils.structure.GuidUtil;
import de.george.g3utils.structure.bCBox;
import de.george.g3utils.structure.bCMatrix;
import de.george.g3utils.structure.bCQuaternion;
import de.george.g3utils.util.Converter;
import de.george.g3utils.util.IOUtils;
import de.george.lrentnode.archive.ArchiveFile;
import de.george.lrentnode.archive.SecDat;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.classes.G3Class;
import de.george.lrentnode.classes.eCGeometrySpatialContext;
import de.george.lrentnode.classes.eCResourceMeshComplex_PS;
import de.george.lrentnode.classes.eCVegetation_Mesh;
import de.george.lrentnode.classes.eCVegetation_PS;
import de.george.lrentnode.classes.eCVegetation_PS.PlantRegionEntry;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.util.EntityUtil;
import de.george.lrentnode.util.FileUtil;
import one.util.streamex.StreamEx;

public class LowPolyGenerator {
	private EditorContext ctx;
	private List<String> log;

	private Set<String> lowpolyBlacklist;
	private Map<String, LowpolyMesh> lowpolyMeshes;

	private LowPolyGenerator(EditorContext ctx) {
		this.ctx = ctx;
		log = new ArrayList<>();
	}

	public synchronized void log(String message, Object... arguments) {
		log.add(String.format(message, arguments));
	}

	public static final class LowPolySector {
		private final String sector;
		private final String lowpolyNode;

		public LowPolySector(@JsonProperty("sector") String sector, @JsonProperty("lowpolyNode") String lowpolyNode) {
			this.sector = sector;
			this.lowpolyNode = lowpolyNode;
		}

		public String getSector() {
			return sector;
		}

		public String getLowpolyNode() {
			return lowpolyNode;
		}
	}

	private static class LowpolyMesh {
		public final String name;
		public final bCBox boundingBox;

		public LowpolyMesh(String name, bCBox boundingBox) {
			this.name = name;
			this.boundingBox = boundingBox;
		}
	}

	private static boolean isLowPolyFile(File file) {
		return isLowPolyFile(file.getName());
	}

	private static boolean isLowPolyFile(String fileName) {
		return fileName.toLowerCase().contains("g3_world_lowpoly");
	}

	private boolean isNotBlacklisted(eCEntity entity) {
		return !lowpolyBlacklist.contains(entity.getGuid());
	}

	private eCEntity checkEntityForLowPoly(eCEntity entity) {
		G3Class mesh = EntityUtil.getStaticMeshClass(entity);
		if (mesh == null) {
			return null;
		}

		String meshName = mesh.property(CD.eCVisualMeshBase_PS.ResourceFileName).getString().toLowerCase();
		LowpolyMesh lowpolyMesh = EntityUtil.toLowPolyMesh(meshName).map(lowpolyMeshes::get).orElse(null);
		if (lowpolyMesh != null) {
			eCEntity lowpolyEntity = EntityUtil.newLowPolyEntity(lowpolyMesh.name, 100000);
			// Derive guid by replacing the first four bytes with LOWP
			lowpolyEntity.setGuid(Converter.stringToHex("LOWP") + entity.getGuid().substring(8));
			// Set local node boundary to bounding box of low poly mesh
			lowpolyEntity.updateLocalNodeBoundary(lowpolyMesh.boundingBox);
			// Copy world matrix from entity
			lowpolyEntity.setToWorldMatrix(entity.getWorldMatrix());
			return lowpolyEntity;
		}

		return null;
	}

	private List<eCEntity> checkEntitiesForLowPoly(Stream<eCEntity> entities) {
		return entities.parallel().filter(this::isNotBlacklisted).map(this::checkEntityForLowPoly).filter(Objects::nonNull)
				.collect(Collectors.toList());
	}

	private boolean checkEntityForSpeedTree(eCEntity entity, eCVegetation_PS lowPolyMcpVeg) {
		Optional<String> treeMesh = EntityUtil.getTreeMesh(entity);
		if (!treeMesh.isPresent()) {
			return false;
		}

		Optional<eCVegetation_Mesh> mesh = lowPolyMcpVeg.getMeshClasses().stream().filter(m -> m.getName().equals(treeMesh.get()))
				.findFirst();
		if (!mesh.isPresent()) {
			log("Speedtree mesh %s of entity %s is not available in LowPoly_MCP! Skipping.", treeMesh.get(), entity.getGuid());
			return false;
		}

		float scale = entity.getWorldMatrix().getPureScaling().getX();
		lowPolyMcpVeg.getGrid().insertEntry(new PlantRegionEntry(mesh.get().getMeshID(), entity.getWorldPosition(),
				new bCQuaternion(entity.getWorldMatrix()), scale, scale, 0xFFA0A0A0));
		return true;
	}

	private boolean checkEntitiesForSpeedTree(Stream<eCEntity> entities, eCVegetation_PS lowPolyMcpVeg) {
		return entities.sequential().filter(this::isNotBlacklisted).map(entity -> checkEntityForSpeedTree(entity, lowPolyMcpVeg))
				.reduce(false, Boolean::logicalOr);
	}

	private void createLowpolyNode(String nodeName, List<eCEntity> entities, List<String> contributingSectors, boolean globalNode) {
		log("");
		log("Create low poly node %s with %d entities.", nodeName, entities.size());
		contributingSectors.forEach(f -> log("  - %s", f));

		File sectorDir = new File(ctx.getFileManager().getPrimaryPath(FileManager.RP_PROJECTS_COMPILED), "World\\_Level\\" + nodeName);
		sectorDir.mkdirs();

		try {
			// node
			ArchiveFile lowpolyNode = FileUtil.createEmptyNode();
			lowpolyNode.getGraph().setGuid(GuidUtil.deriveGUID(nodeName));
			lowpolyNode.getGraph().setToWorldMatrix(bCMatrix.getIdentity());
			entities.forEach(lowpolyNode.getGraph()::attachChild);
			lowpolyNode.getGraph().updateParentDependencies();
			String lowpolyNodeName = nodeName + (globalNode ? "_Spat" : "");
			File lowpolyNodeFile = new File(sectorDir, lowpolyNodeName + ".node");
			lowpolyNode.save(lowpolyNodeFile);

			// lrgeodat
			File lowpolyLrgeodatFile = new File(sectorDir, lowpolyNodeName + ".lrgeodat");
			Optional<File> existingLowpolyLrgeodatFile = ctx.getFileManager().searchFile(lowpolyLrgeodatFile);
			eCGeometrySpatialContext lrgeodat = existingLowpolyLrgeodatFile.isPresent()
					? FileUtil.openLrgeodat(existingLowpolyLrgeodatFile.get())
					: FileUtil.createLrgeodat();
			lrgeodat.setPropertyData(CD.eCContextBase.ContextBox, lowpolyNode.getGraph().getWorldTreeBoundary());
			FileUtil.saveLrgeodat(lrgeodat, lowpolyLrgeodatFile);

			if (!globalNode) {
				FileUtil.createLrgeo(lowpolyNodeFile);
			}
		} catch (IOException e) {
			log("Failed to create low poly sector %s: %s", nodeName, e.getMessage());
			return;
		}
	}

	private boolean process() {
		Map<String, String> lowPolySectorMapping;
		Map<String, File> worldFiles;
		try {
			// Map specified sectors to separate low poly node files
			lowPolySectorMapping = ConfigFiles.lowPolySectors(ctx).getContent().stream()
					.collect(ImmutableMap.toImmutableMap(s -> s.getSector().toLowerCase(), LowPolySector::getLowpolyNode));

			lowpolyBlacklist = ConfigFiles.objectsWithoutLowPoly(ctx).getGuids();
			log("%d blacklisted objects.", lowpolyBlacklist.size());

			// Search all available low poly meshes
			lowpolyMeshes = ctx.getFileManager()
					.listFiles(FileManager.RP_COMPILED_MESH, f -> f.getName().toLowerCase().endsWith("_lowpoly.xcmsh")).parallelStream()
					.map(lowpoly -> {
						try {
							eCResourceMeshComplex_PS lowpolyMesh = FileUtil.openMesh(lowpoly);
							return new LowpolyMesh(lowpoly.getName(), lowpolyMesh.getBoundingBox());
						} catch (IOException e) {
							log("");
							log("Failed to open low poly mesh %s.", lowpoly.getName());
							return null;
						}
					}).filter(Objects::nonNull).collect(ImmutableMap.toImmutableMap(m -> m.name.toLowerCase(), Function.identity()));
			log("Found %d low poly meshes.", lowpolyMeshes.size());

			// Search all non low poly lrendat and node files
			worldFiles = ctx.getFileManager().listWorldFiles().stream().filter(f -> !isLowPolyFile(f))
					.collect(ImmutableMap.toImmutableMap(f -> f.getName().toLowerCase(), Function.identity()));
		} catch (IllegalArgumentException e) {
			log(e.getMessage());
			return false;
		}

		// Load speedtree low poly file
		File lowPolyMcpFile = new File(ctx.getFileManager().getPrimaryPath(FileManager.RP_PROJECTS_COMPILED),
				"World\\_Level\\G3_World_Lowpoly_01_Levelmesh_01\\G3_World_Lowpoly_01_Levelmesh_01.lrentdat");
		ArchiveFile lowPolyMcpArchive;
		try {
			lowPolyMcpArchive = FileUtil.openArchive(ctx.getFileManager().searchFile(lowPolyMcpFile).get(), false);
		} catch (IOException e) {
			log(e.getMessage());
			return false;
		}

		eCEntity lowPolyMcp = lowPolyMcpArchive.getEntityByName("LowPoly_MCP").get();
		eCVegetation_PS lowPolyMcpVeg = lowPolyMcp.getClass(CD.eCVegetation_PS.class);
		// Clear speedtree vegetation grid
		lowPolyMcpVeg.getGrid().clear();

		List<eCEntity> globalLowPolyNodeEntities = new ArrayList<>();
		List<String> globalLowPolyNodeSectors = new ArrayList<>();
		for (File secdatFile : ctx.getFileManager().listFiles(FileManager.RP_PROJECTS_COMPILED, IOUtils.secdatFileFilter)) {
			if (isLowPolyFile(secdatFile)) {
				continue;
			}

			try {
				SecDat secdat = FileUtil.openSecdat(secdatFile);
				StreamEx<String> archiveFiles = StreamEx.of(secdat.getLrentdatFiles()).map(f -> f + ".lrentdat")
						.append(StreamEx.of(secdat.getNodeFiles()).map(f -> f + ".node"));

				List<eCEntity> lowPolyEntities = new ArrayList<>();
				for (String worldFileName : archiveFiles) {
					if (isLowPolyFile(worldFileName)) {
						continue;
					}

					File worldFile = worldFiles.get(worldFileName.toLowerCase());
					if (worldFile == null) {
						log("");
						log("File %s referenced by sector %s does not exist.", worldFileName, secdatFile.getName());
						continue;
					}

					try {
						ArchiveFile archiveFile = FileUtil.openArchive(worldFile, false);
						// Low poly meshes
						lowPolyEntities.addAll(checkEntitiesForLowPoly(archiveFile.getEntities().stream()));
						// Low poly tree meshes
						checkEntitiesForSpeedTree(archiveFile.getEntities().stream(), lowPolyMcpVeg);
					} catch (Exception e) {
						log("");
						log("Failed to open archive %s: %s", worldFile.getName(), e.getMessage());
					}
				}

				if (!lowPolyEntities.isEmpty()) {
					String sectorName = IOUtils.stripExtension(secdatFile.getName());
					if (lowPolySectorMapping.containsKey(sectorName.toLowerCase())) {
						// Assign entities and sector to correspondig low poly node
						String lowpolyNodeName = lowPolySectorMapping.get(sectorName.toLowerCase());
						createLowpolyNode(lowpolyNodeName, lowPolyEntities, ImmutableList.of(sectorName), false);
						// Add node to sector
						if (!secdat.getNodeFiles().contains(lowpolyNodeName)) {
							secdat.addNodeFile(lowpolyNodeName);
							secdat.save(secdatFile);
						}
					} else {
						// Put into global low poly node
						globalLowPolyNodeEntities.addAll(lowPolyEntities);
						globalLowPolyNodeSectors.add(sectorName);
					}

				}
			} catch (IOException e) {
				log("");
				log("Failed to open/save secdat %s.", secdatFile.getName());
			}
		}

		createLowpolyNode("G3_World_Lowpoly_01_Levelmesh_01", globalLowPolyNodeEntities, globalLowPolyNodeSectors, true);

		log("");
		log("Create speedtree low poly lrentdat with %d speedtrees.", lowPolyMcpVeg.getGrid().getEntryCount());
		// Save speedtree low poly lrentdat
		lowPolyMcpVeg.getGrid().updateBounds();
		lowPolyMcp.updateLocalNodeBoundary(lowPolyMcpVeg.getBounds());
		try {
			lowPolyMcpArchive.save(lowPolyMcpFile);
		} catch (IOException e) {
			log(e.getMessage());
			return false;
		}

		return true;
	}

	public static List<String> generate(EditorContext ctx) {
		LowPolyGenerator lowPolyGenerator = new LowPolyGenerator(ctx);
		boolean success = lowPolyGenerator.process();
		lowPolyGenerator.log("");
		lowPolyGenerator.log("LowPoly generation %s!", success ? "SUCCESSFUL" : "FAILED");
		return lowPolyGenerator.log;
	}
}

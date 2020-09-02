package de.george.g3dit.scripts;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.teamunify.i18n.I;

import de.george.g3dit.util.FileDialogWrapper;
import de.george.g3dit.util.FileManager;
import de.george.g3utils.structure.bCEulerAngles;
import de.george.g3utils.structure.bCMatrix3;
import de.george.g3utils.structure.bCOrientedBox;
import de.george.g3utils.structure.bCVector;
import de.george.g3utils.util.Misc;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.classes.eCCollisionShape;
import de.george.lrentnode.classes.eCCollisionShape.BoxShape;
import de.george.lrentnode.classes.eCCollisionShape.FileShape;
import de.george.lrentnode.classes.eCCollisionShape.Shape;
import de.george.lrentnode.classes.eCCollisionShape_PS;
import de.george.lrentnode.classes.eCResourceCollisionMesh_PS;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.enums.G3Enums;
import de.george.lrentnode.enums.G3Enums.eECollisionShapeType;
import de.george.lrentnode.enums.G3Enums.eEShapeMaterial;
import de.george.lrentnode.util.FileUtil;

public class ScriptReplaceCollisionShapes implements IScript {
	@Override
	public String getTitle() {
		return I.tr("CollisionShapes ersetzen");
	}

	@Override
	public String getDescription() {
		return I.tr("Ersetzt CollisionShapes gemäß einer Liste von Ersetzungsregeln.");
	}

	private static class ReplaceRule {
		public final String mesh;
		public final String curColShape;
		public final int newShapeType;
		public final int newShapeMaterial;
		public final String rawNewColShape;
		public final Shape newColShape;

		private ReplaceRule(String mesh, String curColShape, String newShapeType, String newShapeMaterial, String newColShape,
				FileManager fileManager) throws IOException {
			this.mesh = mesh;
			this.curColShape = curColShape;
			this.newShapeType = G3Enums.asInt(eECollisionShapeType.class, newShapeType);
			if (this.newShapeType == -1) {
				throw new IllegalArgumentException("Invalid ShapeType: " + newShapeType);
			}
			this.newShapeMaterial = G3Enums.asInt(eEShapeMaterial.class, newShapeMaterial);
			if (this.newShapeMaterial == -1) {
				throw new IllegalArgumentException("Invalid ShapeMaterial: " + newShapeMaterial);
			}
			switch (this.newShapeType) {
				case eECollisionShapeType.eECollisionShapeType_TriMesh, eECollisionShapeType.eECollisionShapeType_ConvexHull -> {
					Optional<File> colFile = fileManager.searchFile(FileManager.RP_COMPILED_PHYSIC, newColShape);
					if (colFile.isPresent()) {
						eCResourceCollisionMesh_PS colMesh = FileUtil.openCollisionMesh(colFile.get());
						if (colMesh.getNxsBoundaries().size() > 1) {
							throw new IllegalArgumentException("CollisionShape has more than one resource: " + newColShape);
						}

						FileShape fileShape = new FileShape(newColShape, 0);
						fileShape.setMeshBoundary(colMesh.getNxsBoundaries().get(0));
						this.newColShape = fileShape;
					} else {
						throw new IllegalArgumentException("Unable to find CollisionShape: " + newColShape);
					}
				}
				case eECollisionShapeType.eECollisionShapeType_Box -> {
					// Center|Extent|Pitch|Yaw|Roll
					String[] splitted = newColShape.split("\\|");
					this.newColShape = new BoxShape(new bCOrientedBox(bCVector.fromString(splitted[0]), bCVector.fromString(splitted[1]),
							new bCMatrix3(bCEulerAngles.fromDegree(Misc.parseFloat(splitted[3]), Misc.parseFloat(splitted[2]),
									Misc.parseFloat(splitted[4])))));
				}
				default -> throw new IllegalArgumentException("Unsupported ShapeType: " + newShapeType);
			}
			rawNewColShape = newColShape;
		}
	}

	@Override
	public boolean execute(IScriptEnvironment env) {
		File ruleFile = FileDialogWrapper.openFile(I.tr("Ersetzungsregeln laden"), env.getParentWindow(), FileDialogWrapper.CSV_FILTER);
		if (ruleFile == null) {
			return false;
		}

		Map<String, ReplaceRule> rules = new HashMap<>();
		try (BufferedReader reader = Files.newBufferedReader(ruleFile.toPath(), StandardCharsets.UTF_8)) {
			CSVParser parser = CSVFormat.DEFAULT.withFirstRecordAsHeader().withTrim().parse(reader);
			for (CSVRecord record : parser) {
				ReplaceRule rule = new ReplaceRule(record.get("Mesh"), record.get("current Collision"), record.get("new ShapeType"),
						record.get("new Material"), record.get("new Collision"), env.getFileManager());
				rules.put(rule.curColShape.toLowerCase(), rule);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return ScriptUtils.processAndSaveWorldFiles(env, (archive, file) -> {
			int replaced = 0;
			for (eCEntity entity : archive.getEntities()) {
				eCCollisionShape_PS colShapePS = entity.getClass(CD.eCCollisionShape_PS.class);
				if (colShapePS == null) {
					continue;
				}

				for (eCCollisionShape colShape : colShapePS.getShapes()) {
					int shapeType = colShape.property(CD.eCCollisionShape.ShapeType).getEnumValue();
					if (shapeType == eECollisionShapeType.eECollisionShapeType_TriMesh
							|| shapeType == eECollisionShapeType.eECollisionShapeType_ConvexHull) {
						FileShape fileShape = (FileShape) colShape.getShape();
						ReplaceRule rule = rules.get(fileShape.getShapeFile().toLowerCase());
						if (rule == null) {
							continue;
						}
						if (fileShape.getResourceIndex() != 0) {
							throw new IllegalArgumentException("To be replaced CollisionShape in entity " + entity.getGuid()
									+ " has non null resource index: " + fileShape.getShapeFile());
						}
						int oldShapeType = colShape.property(CD.eCCollisionShape.ShapeType).getEnumValue();
						int oldShapeMaterial = colShape.property(CD.eCCollisionShape.Material).getEnumValue();
						colShape.property(CD.eCCollisionShape.ShapeType).setEnumValue(rule.newShapeType);
						colShape.property(CD.eCCollisionShape.Material).setEnumValue(rule.newShapeMaterial);
						colShape.setShape(rule.newColShape);
						replaced++;
						env.log("%s;%s;%s;%s;%s;%s;%s;%s", entity.getGuid(), entity.toString(),
								G3Enums.asString(eECollisionShapeType.class, oldShapeType),
								G3Enums.asString(eEShapeMaterial.class, oldShapeMaterial), fileShape.getShapeFile(),
								G3Enums.asString(eECollisionShapeType.class, rule.newShapeType),
								G3Enums.asString(eEShapeMaterial.class, rule.newShapeMaterial), rule.rawNewColShape);
					}
				}
			}
			return replaced;
		}, I.tr("Es wurden insgesamt {0, number} CollisionShapes ersetzt."));
	}
}

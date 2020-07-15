package de.george.navmap.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileReaderVirtual;
import de.george.g3utils.structure.Stringtable;
import de.george.g3utils.structure.bCVector;
import de.george.g3utils.util.Misc;
import de.george.lrentnode.classes.G3Class;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.template.TemplateEntity;
import de.george.lrentnode.template.TemplateFile;
import de.george.lrentnode.util.EntityUtil;
import de.george.lrentnode.util.FileUtil;
import de.george.navmap.data.NegCircle.NegCirclePrototype;

public class NegCircleManager {

	public static Map<String, NegCirclePrototype> createNegCircleBase(List<File> files) {
		Map<String, NegCirclePrototype> mapping = new HashMap<>();
		for (File file : files) {
			if (file.getName().endsWith(".tple") && !file.getName().startsWith("_deleted_")) {
				try {
					TemplateFile tple = FileUtil.openTemplate(file);
					if (tple.getHeaderCount() < 2) {
						continue;
					}
					TemplateEntity refHeader = tple.getReferenceHeader();
					if (refHeader.hasClass(CD.gCCollisionCircle_PS.class)) {
						G3Class colCircle = refHeader.getClass(CD.gCCollisionCircle_PS.class);
						if (colCircle.property(CD.gCCollisionCircle_PS.Type).getEnumValue() != 0) {
							System.out.println("CollisionCircle hat Type != 0: " + file.getName());
						}

						String mesh = EntityUtil.getMesh(refHeader).orElse(null);
						if (mesh == null) {
							mesh = EntityUtil.getTreeMesh(refHeader).orElse(null);
						}
						if (mesh == null) {
							System.out.println("CollisionCircle aber kein Mesh: " + file.getName());
							continue;
						}
						if (mapping.containsKey(mesh)) {
							System.out.println("Duplicate mapping for mesh " + mesh + ": " + tple.getFileName());

							List<bCVector> offsets = mapping.get(mesh).circleOffsets;
							List<Float> radiuss = mapping.get(mesh).circleRadius;

							List<bCVector> curOffsets = colCircle.property(CD.gCCollisionCircle_PS.Offset).getEntries();
							List<Float> curRadiuss = colCircle.property(CD.gCCollisionCircle_PS.Radius).getNativeEntries();

							for (int i = 0; i < offsets.size(); i++) {
								bCVector offset = offsets.get(i);
								bCVector curOffset = curOffsets.get(i);

								if (!offset.simliar(curOffset, 0.1f)) {
									System.out.println(String.format("Position des NegCircle-Offsets %d hat sich verändert: %s -> %s.", i,
											offset, curOffset));
								}

								float radius = radiuss.get(i);
								float curRadius = curRadiuss.get(i);

								if (!Misc.compareFloat(radius, curRadius, 0.1f)) {
									System.out.println(String.format("Radius des NegCircle-Offsets %d hat sich verändert: %f -> %f", i,
											radius, curRadius));
								}
							}
						}
						mapping.put(mesh, new NegCirclePrototype(mesh, colCircle.property(CD.gCCollisionCircle_PS.Offset).getEntries(),
								colCircle.property(CD.gCCollisionCircle_PS.Radius).getNativeEntries()));
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					// e.printStackTrace();
				}
			}
		}
		return mapping;
	}

	public static NegCirclePrototype getMeshCircleMappingFromFile(File file) throws Exception {
		try (G3FileReader reader = new G3FileReaderVirtual(file)) {
			NegCirclePrototype circle = null;

			// TODO: Header größer als 2 und keine Referenced Template...
			if (!reader.read(8).equals("47454E4F4D464C45")) {
				return circle;
			}

			reader.skip(2); // Skip to Offset
			int offset = reader.readInt();

			String typeRaw = reader.read(8);

			if (!typeRaw.equals("47454E4F4D455450")) {
				return circle;
			}

			// read Stringtable
			Stringtable stringtable = new Stringtable();
			reader.seek(offset + 5);
			int stEntryCount = reader.readInt();
			for (int i = 0; i < stEntryCount; i++) {
				stringtable.addEntry(reader.readString(reader.readShort()));
			}

			reader.seek(27);
			reader.skip(2 * reader.readInt() + 4);
			int headerCount = reader.readInt();
			// System.out.println(file.getName());
			// System.out.println(headerCount);
			boolean skip = true;
			for (int i = 0; i < headerCount; i++) {
				// if(!reader.readSilent(2).equals("3E00"))
				// System.out.println("Error: " + reader.getPos());
				reader.skip(32);
				if (reader.readBool()) {
					reader.skip(20);
				}
				reader.skip(230);
				if (skip) {
					reader.skip(4);
					skip = false;
				}
			}

			// System.out.println(reader.getPos());
			int classCount = reader.readInt();

			String mesh = null;
			List<bCVector> circleOffsets = new ArrayList<>();
			List<Float> circleRadius = new ArrayList<>();
			for (int c = 0; c < classCount; c++) {
				reader.skip(2);
				int deadcode = reader.readInt() + reader.getPos() + 6;
				String classST;
				try {
					classST = stringtable.getEntry(reader.readShort());
				} catch (Exception e) {
					System.out.println(file.getName());
					return circle;
				}
				if (classST.equals("eCVisualMeshDynamic_PS") || classST.equals("eCVisualAnimation_PS")) {
					reader.skip(35); // Skip Class Header
					mesh = stringtable.getEntry(reader.readShort());
				} else if (classST.equals("gCCollisionCircle_PS")) {
					reader.skip(64);
					int count = reader.readInt();
					for (int i = 0; i < count; i++) {
						circleOffsets.add(reader.readVector());
					}
					reader.skip(11);
					count = reader.readInt();
					for (int i = 0; i < count; i++) {
						circleRadius.add(reader.readFloat());
					}
				}

				reader.seek(deadcode);
			}
			if (mesh != null && !mesh.equalsIgnoreCase("") && circleOffsets.size() > 0 && circleRadius.size() > 0) {
				circle = new NegCirclePrototype(mesh, circleOffsets, circleRadius);
			}

			return circle;
		}
	}

	public static HashMap<String, String> getGuidMeshMappingFromFile(File file) throws Exception {
		HashMap<String, String> result = new HashMap<>();

		try (G3FileReader reader = new G3FileReaderVirtual(file)) {
			if (!reader.read(8).equals("47454E4F4D464C45") || reader.getSize() < 100) {
				return result;
			}

			reader.skip(2); // Skip to Offset
			int offset = reader.readInt();

			String typeRaw = reader.read(8);
			int type = -1;

			if (typeRaw.equals("47454E4F4D45444C")) {
				type = 0;
			} else {
				// Node
				type = 1;
			}

			// read Stringtable
			Stringtable stringtable = new Stringtable();
			reader.seek(offset + 5);
			int stEntryCount = reader.readInt();
			for (int i = 0; i < stEntryCount; i++) {
				stringtable.addEntry(reader.readString(reader.readShort()));
			}

			if (type == 0) {
				reader.seek(148); // Skip Header
			} else if (type == 1) {
				reader.seek(16); // Skip Header
			}

			int entityCount = reader.readInt(); // Number Of Entities
			for (int i = 0; i < entityCount; i++) {

				if (type == 0) {
					reader.skip(4);
					if (reader.readBool()) {
						reader.skip(20);
					}
					reader.skip(4);
				} else if (type == 1) {
					reader.skip(93);
				}
				String guid = reader.readGUID();
				reader.skip(17);
				int entryST = reader.readShort();
				reader.skip(251);

				int classCount = reader.readInt();

				for (int c = 0; c < classCount; c++) {
					reader.skip(8);
					String classST = stringtable.getEntry(reader.readShort());
					reader.skip(7); // Skip Class Header
					int deadcode = reader.readInt() + reader.getPos() + 4;
					if (classST.equals("eCVisualMeshStatic_PS") || classST.equals("eCVisualMeshDynamic_PS")
							|| classST.equals("eCVisualAnimation_PS") || classST.equals("eCSpeedTree_PS")) {
						reader.skip(16);
						result.put(guid, stringtable.getEntry(reader.readShort()));
					}
					reader.seek(deadcode);
				}
			}

			return result;
		} catch (Exception e) {
			return result;
		}
	}

}

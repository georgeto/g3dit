package de.george.g3dit.scripts;

import de.george.lrentnode.classes.eCCollisionShape.BoxShape;
import de.george.lrentnode.classes.eCCollisionShape.CapsuleShape;
import de.george.lrentnode.classes.eCCollisionShape.Shape;
import de.george.lrentnode.classes.eCCollisionShape_PS;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.enums.G3Enums.gEUseType;
import de.george.lrentnode.iterator.TemplateFileIterator;
import de.george.lrentnode.template.TemplateEntity;
import de.george.lrentnode.template.TemplateFile;
import de.george.lrentnode.util.EntityUtil;

public class ScriptRepairCollisionShapes implements IScript {

	@Override
	public String getTitle() {
		return "CollisionShapes überprüfen";
	}

	@Override
	public String getDescription() {
		return "Überprüft die CollisionShapes von Waffen";
	}

	@Override
	public boolean execute(IScriptEnvironment env) {
		TemplateFileIterator tpleFilesIterator = env.getFileManager().templateFilesIterator();
		while (tpleFilesIterator.hasNext()) {
			TemplateFile aFile = tpleFilesIterator.next();
			if (aFile.getHeaderCount() != 2) {
				continue;
			}

			TemplateEntity tple = aFile.getReferenceHeader();
			switch (EntityUtil.getUseType(tple)) {
				case gEUseType.gEUseType_1H:
				case gEUseType.gEUseType_2H:
				case gEUseType.gEUseType_Axe:
				case gEUseType.gEUseType_Staff:
				case gEUseType.gEUseType_Halberd:
					eCCollisionShape_PS colShapePS = tple.getClass(CD.eCCollisionShape_PS.class);
					if (colShapePS == null || colShapePS.getShapes().size() == 0) {
						env.log(tpleFilesIterator.nextFile().getAbsolutePath() + " hat kein CollisionShape.");
						continue;
					}

					if (colShapePS.getShapes().size() != 1) {
						env.log(tpleFilesIterator.nextFile().getAbsolutePath() + " hat mehr als ein CollisionShape.");
						continue;
					}

					Shape shape = colShapePS.getShapes().get(0).getShape();
					if (shape instanceof BoxShape || shape instanceof CapsuleShape) {
						// Nothing
					} else {
						env.log(tpleFilesIterator.nextFile().getAbsolutePath() + " hat kein Box/Capsule Shape.");
					}

					break;
				default:
					break;
			}
		}

		return true;
	}
}

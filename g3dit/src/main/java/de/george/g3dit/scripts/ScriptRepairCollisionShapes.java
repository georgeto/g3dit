package de.george.g3dit.scripts;

import com.teamunify.i18n.I;

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
		return I.tr("Check collision shapes");
	}

	@Override
	public String getDescription() {
		return I.tr("Checks the collision shapes of weapons");
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
				case gEUseType.gEUseType_1H, gEUseType.gEUseType_2H, gEUseType.gEUseType_Axe, gEUseType.gEUseType_Staff, gEUseType.gEUseType_Halberd -> {
					eCCollisionShape_PS colShapePS = tple.getClass(CD.eCCollisionShape_PS.class);
					if (colShapePS == null || colShapePS.getShapes().size() == 0) {
						env.log(I.trf("{0} has no collision shape.", tpleFilesIterator.nextFile().getAbsolutePath()));
						continue;
					}
					if (colShapePS.getShapes().size() != 1) {
						env.log(I.trf("{0} has more than one collision shape.", tpleFilesIterator.nextFile().getAbsolutePath()));
						continue;
					}
					Shape shape = colShapePS.getShapes().get(0).getShape();
					if (shape instanceof BoxShape || shape instanceof CapsuleShape) {
						// Nothing
					} else {
						env.log(I.trf("{0} has no box/capsule shape.", tpleFilesIterator.nextFile().getAbsolutePath()));
					}
				}
				default -> {
				}
			}
		}

		return true;
	}
}

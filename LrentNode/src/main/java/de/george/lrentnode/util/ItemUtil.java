package de.george.lrentnode.util;

import java.util.Optional;

import de.george.g3utils.structure.bCOrientedBox;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.classes.eCCollisionShape.BoxShape;
import de.george.lrentnode.classes.eCCollisionShape.CapsuleShape;
import de.george.lrentnode.classes.eCCollisionShape.Shape;
import de.george.lrentnode.classes.eCCollisionShape_PS;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.enums.G3Enums.gEUseType;

public class ItemUtil {
	public static Optional<Integer> getWeaponRange(eCEntity weapon) {
		if (!weapon.hasClass(CD.gCDamage_PS.class) || !weapon.hasClass(CD.gCInteraction_PS.class)
				|| !weapon.hasClass(CD.eCCollisionShape_PS.class)) {
			return Optional.empty();
		}

		eCCollisionShape_PS colShape = weapon.getClass(CD.eCCollisionShape_PS.class);
		if (colShape.getShapes().isEmpty()) {
			return Optional.empty();
		}

		float range;
		switch (weapon.getProperty(CD.gCInteraction_PS.UseType).getEnumValue()) {
			case gEUseType.gEUseType_1H:
				range = 7;
				break;
			case gEUseType.gEUseType_2H:
			case gEUseType.gEUseType_Axe:
				range = 13;
				break;
			case gEUseType.gEUseType_Staff:
			case gEUseType.gEUseType_Halberd:
				range = 75;
				break;
			default:
				return Optional.empty();
		}

		Shape shape = colShape.getShapes().get(0).getShape();
		if (shape instanceof BoxShape) {
			bCOrientedBox box = ((BoxShape) shape).getOrientedBox();
			range += Math.abs(box.getExtent().getTransformed(box.getOrientation()).getZ());
			range += box.getCenter().getZ();
		} else if (shape instanceof CapsuleShape) {
			CapsuleShape capsule = (CapsuleShape) shape;
			range += capsule.getHeight() / 2 + capsule.getRadius() + capsule.getCenter().getZ();
		} else {
			return Optional.empty();
		}

		return Optional.of(Math.round(range));
	}
}

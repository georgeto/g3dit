package de.george.g3dit.gui.edit.handler;

import java.util.function.Consumer;

import de.george.g3dit.gui.components.JTemplateGuidField;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.classes.desc.ClassDescriptor;

public abstract class RefFilters {
	@SafeVarargs
	public static Consumer<JTemplateGuidField> tpleHasAnyOf(Class<? extends ClassDescriptor>... propertySets) {
		return tfGuidField -> tfGuidField.setFilter(e -> e.hasAnyClass(propertySets));
	}

	public static void tpleSkillOrMagic(JTemplateGuidField tfGuidField) {
		tpleHasAnyOf(CD.gCSkill_PS.class, CD.gCMagic_PS.class).accept(tfGuidField);
	}
}

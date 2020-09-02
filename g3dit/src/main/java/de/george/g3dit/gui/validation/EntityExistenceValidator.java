package de.george.g3dit.gui.validation;

import org.netbeans.validation.api.AbstractValidator;
import org.netbeans.validation.api.Problems;
import org.netbeans.validation.api.Severity;
import org.netbeans.validation.api.ui.ValidationGroup;

import com.teamunify.i18n.I;

import de.george.g3dit.EditorContext;
import de.george.g3dit.cache.Caches;
import de.george.g3dit.cache.EntityCache;
import de.george.g3utils.structure.GuidUtil;

public class EntityExistenceValidator extends AbstractValidator<String> {
	private EditorContext ctx;

	public EntityExistenceValidator(ValidationGroup group, EditorContext ctx) {
		super(String.class);
		this.ctx = ctx;
		Caches.entity(ctx).addUpdateListener(this, c -> group.performValidation());
	}

	@Override
	public void validate(Problems problems, String compName, String model) {

		EntityCache cache = Caches.entity(ctx);
		if (cache.isValid() && GuidUtil.isValid(model) && !cache.isExisting(GuidUtil.parseGuid(model))) {
			problems.append(I.trf("{0} enthält eine Guid zu der keine Entity existiert.", compName), Severity.WARNING);
		}
	}

}

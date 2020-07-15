package de.george.g3dit.gui.validation;

import org.netbeans.validation.api.AbstractValidator;
import org.netbeans.validation.api.Problems;
import org.netbeans.validation.api.Severity;
import org.netbeans.validation.api.ui.ValidationGroup;

import de.george.g3dit.EditorContext;
import de.george.g3dit.cache.Caches;
import de.george.g3dit.cache.TemplateCache;
import de.george.g3utils.structure.GuidUtil;

public class TemplateExistenceValidator extends AbstractValidator<String> {
	private EditorContext ctx;
	private boolean all;

	public TemplateExistenceValidator(ValidationGroup group, EditorContext ctx) {
		this(group, ctx, false);
	}

	public TemplateExistenceValidator(ValidationGroup group, EditorContext ctx, boolean all) {
		super(String.class);
		this.ctx = ctx;
		this.all = all;
		Caches.template(ctx).addUpdateListener(this, c -> group.performValidation());
	}

	@Override
	public void validate(Problems problems, String compName, String model) {

		TemplateCache cache = Caches.template(ctx);
		if (cache.isValid() && GuidUtil.isValid(model) && !cache.getEntryByGuid(GuidUtil.parseGuid(model), all).isPresent()) {
			problems.append(compName + " enthält eine Guid zu der keine Template existiert.", Severity.WARNING);
		}
	}

}

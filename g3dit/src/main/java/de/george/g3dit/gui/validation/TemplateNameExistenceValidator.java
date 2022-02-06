package de.george.g3dit.gui.validation;

import org.netbeans.validation.api.AbstractValidator;
import org.netbeans.validation.api.Problems;
import org.netbeans.validation.api.Severity;
import org.netbeans.validation.api.ui.ValidationGroup;

import com.teamunify.i18n.I;

import de.george.g3dit.EditorContext;
import de.george.g3dit.cache.Caches;
import de.george.g3dit.cache.TemplateCache;

public class TemplateNameExistenceValidator extends AbstractValidator<String> {
	private EditorContext ctx;
	private boolean all;

	public TemplateNameExistenceValidator(ValidationGroup group, EditorContext ctx) {
		this(group, ctx, false);
	}

	public TemplateNameExistenceValidator(ValidationGroup group, EditorContext ctx, boolean all) {
		super(String.class);
		this.ctx = ctx;
		this.all = all;
		Caches.template(ctx).addUpdateListener(this, c -> group.performValidation());
	}

	@Override
	public void validate(Problems problems, String compName, String model) {
		TemplateCache cache = Caches.template(ctx);
		if (cache.isValid() && !model.isEmpty() && !cache.getEntryByName(model, all).isPresent()) {
			problems.append(I.trf("{0} contains a name for which no template exists.", compName), Severity.WARNING);
		}
	}
}

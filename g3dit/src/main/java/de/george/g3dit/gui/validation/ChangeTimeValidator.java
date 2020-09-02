package de.george.g3dit.gui.validation;

import java.util.Optional;

import org.netbeans.validation.api.AbstractValidator;
import org.netbeans.validation.api.Problems;
import org.netbeans.validation.api.Severity;
import org.netbeans.validation.api.ui.ValidationGroup;

import com.teamunify.i18n.I;

import de.george.g3dit.EditorContext;
import de.george.g3dit.cache.Caches;
import de.george.g3dit.cache.TemplateCache;
import de.george.g3dit.cache.TemplateCache.TemplateCacheEntry;
import de.george.g3dit.gui.components.JTemplateGuidField;
import de.george.g3utils.structure.GuidUtil;

public class ChangeTimeValidator extends AbstractValidator<String> {
	private EditorContext ctx;
	private JTemplateGuidField gfCreator;

	public ChangeTimeValidator(ValidationGroup group, EditorContext ctx, JTemplateGuidField gfCreator) {
		super(String.class);
		this.ctx = ctx;
		this.gfCreator = gfCreator;
		Caches.template(ctx).addUpdateListener(this, c -> group.performValidation());
		gfCreator.addGuidFiedListener(g -> group.performValidation());
	}

	@Override
	public void validate(Problems problems, String compName, String model) {
		TemplateCache cache = Caches.template(ctx);
		String refGuid = gfCreator.getText();
		if (cache.isValid() && GuidUtil.isValid(refGuid)) {
			Optional<TemplateCacheEntry> entry = cache.getEntryByGuid(GuidUtil.parseGuid(refGuid));
			if (entry.isPresent()) {
				try {
					long ourChangeTime = Long.parseLong(model);
					long creatorChangeTime = entry.get().getChangeTime();
					if (ourChangeTime != creatorChangeTime) {
						problems.append(
								ourChangeTime < creatorChangeTime
										? I.trf("ChangeTime-Wert kleiner als in referenzierter Template ({0, number})", creatorChangeTime)
										: I.trf("ChangeTime-Wert größer als in referenzierter Template ({0, number})", creatorChangeTime),
								Severity.WARNING);
					}
				} catch (NumberFormatException e) {
					// Ignore
				}
			}
		}
	}

}

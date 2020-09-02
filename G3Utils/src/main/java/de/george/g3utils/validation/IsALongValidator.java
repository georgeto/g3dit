package de.george.g3utils.validation;

import org.netbeans.validation.api.AbstractValidator;
import org.netbeans.validation.api.Problems;

import com.teamunify.i18n.I;

public class IsALongValidator extends AbstractValidator<String> {
	public static final IsALongValidator INSTANCE = new IsALongValidator();

	private IsALongValidator() {
		super(String.class);
	}

	@Override
	public void validate(Problems problems, String compName, String model) {
		try {
			Long.parseLong(model);
		} catch (NumberFormatException e) {
			problems.append(I.trf("'{0}' in {1} ist keine gültige ganze Zahl.", model, compName));
		}
	}
}

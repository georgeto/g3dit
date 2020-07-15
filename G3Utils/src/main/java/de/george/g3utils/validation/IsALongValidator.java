package de.george.g3utils.validation;

import org.netbeans.validation.api.AbstractValidator;
import org.netbeans.validation.api.Problems;

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
			problems.append("'" + model + "' ist keine gültige ganze Zahl");
		}
	}
}
